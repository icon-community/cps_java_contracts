package community.icon.cps.score.cpftreasury;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import community.icon.cps.score.lib.interfaces.CPFTreasuryInterface;
import score.Address;
import score.ArrayDB;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;
import score.annotation.Payable;
import scorex.util.ArrayList;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static community.icon.cps.score.cpftreasury.Constants.*;
import static community.icon.cps.score.cpftreasury.Validations.validateAdmins;
import static community.icon.cps.score.cpftreasury.Validations.validateCpsScore;

public class CPFTreasury extends SetterGetter implements CPFTreasuryInterface {
    public static final VarDB<Address> cpsTreasuryScore = Context.newVarDB(CPS_TREASURY_SCORE, Address.class);
    public static final VarDB<Address> cpsScore = Context.newVarDB(CPS_SCORE, Address.class);
    public static final VarDB<Address> balancedDollar = Context.newVarDB(BALANCED_DOLLAR, Address.class);
    public static final VarDB<Address> dexScore = Context.newVarDB(DEX_SCORE, Address.class);
    public static final VarDB<Address> sICXScore = Context.newVarDB(SICX_SCORE, Address.class);
    public static final VarDB<Address> routerScore = Context.newVarDB(ROUTER_SCORE, Address.class);
    public static final VarDB<Address> oracleAddress = Context.newVarDB(ORACLE_ADDRESS, Address.class);
    private final ArrayDB<String> proposalsKeys = Context.newArrayDB(PROPOSALS_KEYS, String.class);
    private final DictDB<String, BigInteger> proposalBudgets = Context.newDictDB(PROPOSAL_BUDGETS, BigInteger.class);
    private final VarDB<BigInteger> treasuryFund = Context.newVarDB(TREASURY_FUND, BigInteger.class);
    private final VarDB<BigInteger> emergencyFund = Context.newVarDB(EMERGENCY_FUND, BigInteger.class);
    private final VarDB<BigInteger> treasuryFundbnUSD = Context.newVarDB(TREASURY_FUND_BNUSD, BigInteger.class);

    private final VarDB<Integer> swapState = Context.newVarDB(SWAP_STATE, Integer.class);
    private final VarDB<Integer> swapCount = Context.newVarDB(SWAP_COUNT, Integer.class);
    private final VarDB<Integer> oraclePerDiff = Context.newVarDB(ORACLE_PERCENTAGE_DIFF, Integer.class);

    private final VarDB<Boolean> swapFlag = Context.newVarDB(SWAP_FLAG, Boolean.class);
    private final VarDB<BigInteger> swapLimitAmount = Context.newVarDB(SWAP_LIMIT_AMOUNT, BigInteger.class);

    public CPFTreasury() {
        if (treasuryFund.get() == null) {
            treasuryFund.set(BigInteger.valueOf(1000000).multiply(EXA));
            swapCount.set(SwapReset);
            swapState.set(SwapReset);
            swapFlag.set(false);
        }
    }

    private boolean proposalExists(String ipfsKey) {
        return proposalBudgets.get(ipfsKey) != null;
    }

    @Override
    @External(readonly = true)
    public String name() {
        return TAG;
    }


    @Override
    @External
    public void setMaximumTreasuryFundIcx(BigInteger value) {
        validateAdmins();
        treasuryFund.set(value);
    }

    /**
     * Set the maximum Treasury fund. Default 1M in bnUSD
     *
     * @param value: value in loop
     */
    @Override
    @External
    public void setMaximumTreasuryFundBnusd(BigInteger value) {
        validateAdmins();
        treasuryFundbnUSD.set(value);
    }

    @External
    public void toggleSwapFlag() {
        validateAdmins();
        swapFlag.set(!swapFlag.getOrDefault(false));
    }

    @External(readonly = true)
    public boolean getSwapFlag() {
        return swapFlag.getOrDefault(false);
    }


    private void burn(BigInteger amount) {
        Context.call(amount, SYSTEM_ADDRESS, "burn");
    }

    /**
     * Get total amount of fund on the SCORE
     *
     * @return map of ICX and bnUSD amount
     */
    @Override
    @External(readonly = true)
    public Map<String, BigInteger> getTotalFunds() {
        return Map.of(ICX, Context.getBalance(Context.getAddress()),
                bnUSD, getBNUSDAvailableBalance());
    }

    @External(readonly = true)
    public BigInteger getBNUSDAvailableBalance() {
        return getTotalFundBNUSD().get(AVAILABLE_BALANCE);
    }

    @External(readonly = true)
    public BigInteger getEmergencyFund() {
        return getTotalFundBNUSD().get(EMERGENCY_FUND);
    }


    private Map<String, BigInteger> getTotalFundBNUSD() {
        BigInteger bnusdBalance = (BigInteger) Context.call(balancedDollar.get(), "balanceOf", Context.getAddress());
        BigInteger emergencyFund = this.emergencyFund.getOrDefault(BigInteger.ZERO);
        BigInteger availableBalance = bnusdBalance.subtract(emergencyFund);
        return Map.of(BNUSD_BALANCE, bnusdBalance,
                EMERGENCY_FUND, emergencyFund,
                AVAILABLE_BALANCE, availableBalance);
    }

    @Override
    @External(readonly = true)
    public Map<String, BigInteger> getRemainingSwapAmount() {
        BigInteger maxCap = treasuryFundbnUSD.get();
        return Map.of(MAX_CAP, maxCap,
                REMAINING_TO_SWAP, maxCap.subtract(getBNUSDAvailableBalance()));
    }

    private void returnFundAmount(Address address, BigInteger value) {
        Context.require(value.compareTo(BigInteger.ZERO) > 0, TAG + ": Sponsor Bond Amount should be greater than 0");
        burnExtraFund();
        FundReturned(address, "Sponsor Bond amount " + value + " " + bnUSD + " Returned to CPF Treasury.");
    }

    @Override
    @External
    public void transferProposalFundToCpsTreasury(String ipfsKey, int projectDuration,
                                                  Address sponsorAddress, Address contributorAddress,
                                                  String tokenFlag, BigInteger totalBudget) {
        validateCpsScore();
        Context.require(!proposalExists(ipfsKey), TAG + ": Project already exists. Invalid IPFS Hash");
        Context.require(tokenFlag.equals(bnUSD), TAG + ": " + tokenFlag + " is not supported. Only " + bnUSD + " token available.");
        BigInteger sponsorReward = totalBudget.multiply(BigInteger.TWO).divide(BigInteger.valueOf(100));
        BigInteger totalTransfer = totalBudget.add(sponsorReward);

        Address balancedDollar = CPFTreasury.balancedDollar.get();
        BigInteger bnUSDBalance = Context.call(BigInteger.class, balancedDollar, "balanceOf", Context.getAddress());
        Context.require(totalTransfer.compareTo(bnUSDBalance) < 0, TAG + ": Not enough fund " + bnUSDBalance + " token available");

        proposalsKeys.add(ipfsKey);
        proposalBudgets.set(ipfsKey, totalTransfer);

        JsonObject depositProposal = new JsonObject();
        depositProposal.add(METHOD, "depositProposalFund");
        JsonObject params = new JsonObject();
        params.add(PROJECT_IPFS_HASH, ipfsKey);
        params.add(PROJECT_DURATION, projectDuration);
        params.add(SPONSOR_ADDRESS, sponsorAddress.toString());
        params.add(CONTRIBUTOR_ADDRESS, contributorAddress.toString());
        params.add(PROJECT_TOTAL_BUDGET, totalBudget.toString(16));
        params.add(SPONSOR_REWARD, sponsorReward.toString(16));
        params.add(TOKEN, tokenFlag);
        depositProposal.add(PARAMS, params);

        Context.call(balancedDollar, TRANSFER, cpsTreasuryScore.get(), totalTransfer, depositProposal.toString().getBytes());
        ProposalFundTransferred(ipfsKey, "Successfully transferred " + totalTransfer + " " + tokenFlag + " to CPS Treasury " + cpsTreasuryScore.get());
    }

    @Override
    @External
    public void updateProposalFund(String ipfsKey, @Optional String flag, @Optional BigInteger addedBudget,
                                   @Optional int totalInstallmentCount) {
        validateCpsScore();
        Context.require(proposalExists(ipfsKey), TAG + ": IPFS hash does not exist.");
        Context.require(flag != null && flag.equals(bnUSD), TAG + ": Unsupported token. " + flag);

        if (addedBudget == null) {
            addedBudget = BigInteger.ZERO;
        }


        BigInteger sponsorReward = addedBudget.multiply(BigInteger.TWO).divide(BigInteger.valueOf(100));
        BigInteger totalTransfer = addedBudget.add(sponsorReward);

        BigInteger proposalBudget = proposalBudgets.getOrDefault(ipfsKey, BigInteger.ZERO);
        proposalBudgets.set(ipfsKey, proposalBudget.add(totalTransfer));
        BigInteger bnUSDFund = getTotalFunds().get(bnUSD);
        Context.require(totalTransfer.compareTo(bnUSDFund) <= 0, TAG + ": Not enough " + totalTransfer + " BNUSD on treasury");

        JsonObject budgetAdjustmentData = new JsonObject();
        budgetAdjustmentData.add(METHOD, "budgetAdjustment");
        JsonObject params = new JsonObject();
        params.add("_ipfs_key", ipfsKey);
        params.add("_added_budget", addedBudget.toString(16));
        params.add("_added_sponsor_reward", sponsorReward.toString(16));
        params.add("_added_installment_count", totalInstallmentCount);
        budgetAdjustmentData.add(PARAMS, params);

        Context.call(balancedDollar.get(), TRANSFER, cpsTreasuryScore.get(), totalTransfer, budgetAdjustmentData.toString().getBytes());
        ProposalFundTransferred(ipfsKey, "Successfully transferred " + totalTransfer + " " + bnUSD + " to CPS Treasury");
    }

    private void disqualifyProposalFund(String ipfsKey, BigInteger value) {
        Context.require(proposalExists(ipfsKey), TAG + ": IPFS key does not exist.");

        BigInteger budget = proposalBudgets.get(ipfsKey);
        proposalBudgets.set(ipfsKey, budget.subtract(value));

        burnExtraFund();
        ProposalDisqualified(ipfsKey, "Proposal disqualified. " + value + " " + bnUSD + " is returned back to Treasury.");
    }

    @Override
    @External
    @Payable
    public void addFund() {
        burnExtraFund();
        FundReceived(Context.getCaller(), "Treasury fund " + Context.getValue() + " " + ICX + " received.");
    }

    private void burnExtraFund() {
        Map<String, BigInteger> amounts = getTotalFunds();
        BigInteger icxAmount = amounts.get(ICX);
        BigInteger bnUSDAmount = amounts.get(bnUSD);
        BigInteger extraAmountIcx = icxAmount.subtract(treasuryFund.get());
        BigInteger extraAmountBnUSD = bnUSDAmount.subtract(treasuryFundbnUSD.get());

        if (extraAmountIcx.compareTo(BigInteger.ZERO) > 0) {
            burn(extraAmountIcx);
        }

        if (extraAmountBnUSD.compareTo(BigInteger.ZERO) > 0) {
            swapTokens(balancedDollar.get(), sICXScore.get(), extraAmountBnUSD);
        }
    }

    @External(readonly = true)
    public int getSlippagePercentage() {
        return oraclePerDiff.getOrDefault(0);
    }

    private void swapTokens(Address _from, Address _to, BigInteger _amount) {
        JsonObject swapData = new JsonObject();
        swapData.add(METHOD, "_swap");
        JsonObject params = new JsonObject();
        params.add("toToken", _to.toString());
        swapData.add(PARAMS, params);
        Context.call(_from, TRANSFER, dexScore.get(), _amount, swapData.toString().getBytes());
    }

    private void swapIcxBnusd(BigInteger amount, BigInteger _minReceive) {
        if (getSwapFlag()) {
            Address[] path = new Address[]{sICXScore.get(), balancedDollar.get()};
            BigInteger icxPrice = getOraclePrice();
            int diffValue = 100 - oraclePerDiff.getOrDefault(2);
            BigInteger minReceive = icxPrice.multiply(BigInteger.valueOf(diffValue)).divide(BigInteger.valueOf(100));
            if (_minReceive.equals(BigInteger.ZERO) || _minReceive.compareTo(minReceive) < 0) {
                _minReceive = minReceive;
            }

            try {
                BigInteger swapLimitAmount = getSwapLimitAmount();
                if (swapLimitAmount.compareTo(BigInteger.ZERO) > 0 && amount.compareTo(swapLimitAmount) > 0) {
                    amount = swapLimitAmount;
                }
                Context.call(amount, routerScore.get(), "route", path, _minReceive);
            } catch (Exception e) {
                Context.println("Ignoring Errors from Router. Error Message: " +  e.getMessage());
            }
        }
    }

    @Override
    @External
    public void swapICXToBnUSD(BigInteger amount, @Optional BigInteger _minReceive) {
        if (!getSwapFlag()) {
            Context.revert(TAG + "SwapTurnedOff.");
        }
        if (_minReceive == null) {
            _minReceive = BigInteger.ZERO;
        }
        swapIcxBnusd(amount, _minReceive);
    }

    @External(readonly = true)
    public BigInteger getOraclePrice() {
        String quote = "USD";

        Map<String, BigInteger> priceData = (Map<String, BigInteger>) Context.call(oracleAddress.get(), "get_reference_data", ICX, quote);
        return priceData.get("rate");

    }

    @External
    public void setSwapLimitAmount(BigInteger value) {
        validateAdmins();
        Context.require(value.compareTo(BigInteger.ZERO) > 0, TAG + ": Swap limit amount should be greater than 0");
        swapLimitAmount.set(value);

    }

    @External(readonly = true)
    public BigInteger getSwapLimitAmount() {
        return swapLimitAmount.getOrDefault(BigInteger.ZERO);

    }

    @External
    public void allocateEmergencyFund(BigInteger value) {
        validateAdmins();
        Context.require(value.compareTo(BigInteger.ZERO) > 0, TAG + ": Emergency Fund amount should be greater than 0");
        emergencyFund.set(value);

    }

    @External
    public void withdrawFromEmergencyFund(BigInteger value, Address address, String purpose) {
        validateAdmins();
        Context.require(value.compareTo(BigInteger.ZERO) > 0, TAG + ": Emergency Fund amount should be greater than 0");
        BigInteger emergencyFund = this.emergencyFund.getOrDefault(BigInteger.ZERO);
        Context.require(emergencyFund.compareTo(value) >= 0, TAG + ": Request amount is greater than Available Emergency Fund");
        this.emergencyFund.set(emergencyFund.subtract(value));
        Address balancedDollar = CPFTreasury.balancedDollar.get();

        Context.call(balancedDollar, TRANSFER, address, value, "".getBytes());
        EmergencyFundTranserred(address, value,purpose);
    }


    @Override
    @External
    public void swapTokens(int count) {
        validateCpsScore();
        BigInteger sicxICXPrice = (BigInteger) Context.call(dexScore.get(), "getPrice", sICXICXPoolID);
        BigInteger sicxBnusdPrice = (BigInteger) Context.call(dexScore.get(), "getPrice", sICXBNUSDPoolID);
        BigInteger icxbnUSDPrice = sicxBnusdPrice.multiply(EXA).divide(sicxICXPrice);
        BigInteger bnUSDRemainingToSwap = getRemainingSwapAmount().get(REMAINING_TO_SWAP);
        if (bnUSDRemainingToSwap.compareTo(BigInteger.TEN.multiply(EXA)) < 0 || count == 0) {
            swapState.set(SwapCompleted);
            swapCount.set(SwapReset);
        } else {
            int swapState = this.swapState.getOrDefault(0);
            if (swapState == SwapContinue) {
                int swapCountValue = swapCount.getOrDefault(0);
                int _count = count - swapCountValue;
                if (_count == 0) {
                    this.swapState.set(SwapCompleted);
                    swapCount.set(SwapReset);
                } else {
                    BigInteger remainingICXToSwap = bnUSDRemainingToSwap.multiply(EXA).divide(icxbnUSDPrice.multiply(BigInteger.valueOf(_count)));
                    BigInteger icxBalance = Context.getBalance(Context.getAddress());
                    swapCount.set(swapCountValue + 1);
                    if (remainingICXToSwap.compareTo(icxBalance) > 0) {
                        remainingICXToSwap = icxBalance;
                    }

                    if (remainingICXToSwap.compareTo(BigInteger.valueOf(5).multiply(EXA)) > 0) {
                        swapIcxBnusd(remainingICXToSwap, BigInteger.ZERO);
                    }
                }
            }
        }
    }

    @Override
    @External(readonly = true)
    public Map<String, Integer> getSwapStateStatus() {
        return Map.of(STATE, swapState.getOrDefault(0), COUNT, swapCount.getOrDefault(0));
    }

    @Override
    @External
    public void resetSwapState() {
        Address cpsScoreAddress = cpsScore.get();
        Address caller = Context.getCaller();

        boolean checkCaller = caller.equals(cpsScoreAddress) || (Boolean) Context.call(cpsScoreAddress, "isAdmin", caller);
        Context.require(checkCaller, TAG + ": Only admin can call this method.");
        swapState.set(SwapContinue);
        swapCount.set(SwapReset);
    }

    @External
    public void setOraclePercentageDifference(int value) {
        validateAdmins();
        oraclePerDiff.set(value);
    }

    @Override
    @External(readonly = true)
    public Map<String, Object> getProposalDetails(@Optional int startIndex, @Optional int endIndex) {
        if (endIndex == 0) {
            endIndex = 20;
        }
        List<Map<String, Object>> proposalsList = new ArrayList<>();
        if ((endIndex - startIndex) > 50) {
            Context.revert(TAG + ": Page Length cannot be greater than 50");
        }
        int count = proposalsKeys.size();
        if (startIndex > count) {
            Context.revert(TAG + ": Start index can't be higher than total count.");
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        if (endIndex > count) {
            endIndex = count;

        }

        for (int i = startIndex; i < endIndex; i++) {
            String proposalHash = proposalsKeys.get(i);
            Map<String, Object> proposalDetails = Map.of(TOTAL_BUDGET, proposalBudgets.getOrDefault(proposalHash, BigInteger.ZERO).toString(), IPFS_HASH, proposalHash);
            proposalsList.add(proposalDetails);
        }
        return Map.of(DATA, proposalsList, COUNT, count);
    }

    @Override
    @External
    public void tokenFallback(Address _from, BigInteger _value, byte[] _data) {
        Address bnUSDScore = balancedDollar.get();
        Address sICX = sICXScore.get();
        Address caller = Context.getCaller();

        Context.require(caller.equals(bnUSDScore) || caller.equals(sICX), TAG +
                " Only " + bnUSDScore + " and " + sICX + " can send tokens to CPF Treasury.");
        if (caller.equals(sICX)) {
            if (_from.equals(dexScore.get())) {
                JsonObject swapICX = new JsonObject();
                swapICX.add(METHOD, "_swap_icx");
                Context.call(caller, TRANSFER, dexScore.get(), _value, swapICX.toString().getBytes());
            } else {
                Context.revert(TAG + ": sICX can be approved only from Balanced DEX.");
            }
        } else {

            if (_data == null || new String(_data).equalsIgnoreCase("none")) {
                _data = "{}".getBytes();
            }
            String unpacked_data = new String(_data);
            JsonObject transferData = Json.parse(unpacked_data).asObject();

            if (_from.equals(cpsScore.get())) {
                if (transferData.get(METHOD).asString().equals("returnFundAmount")) {
                    Address _sponsor_address = Address.fromString(transferData.get(PARAMS).asObject().get(SPONSOR_ADDRESS).asString());
                    returnFundAmount(_sponsor_address, _value);
                } else if (transferData.get(METHOD).asString().equals("burnAmount")) {
                    swapTokens(caller, sICX, _value);
                } else {
                    Context.revert(TAG + ": Not supported method " + transferData.get(METHOD).asString());
                }
            } else if (_from.equals(cpsTreasuryScore.get())) {
                if (transferData.get(METHOD).asString().equals("disqualifyProject")) {
                    String ipfs_key = transferData.get(PARAMS).asObject().get("ipfs_key").asString();
                    disqualifyProposalFund(ipfs_key, _value);
                } else {
                    Context.revert(TAG + ": Not supported method " + transferData.get(METHOD).asString());
                }
            } else {
                burnExtraFund();
            }
        }
    }

    @Override
    @Payable
    public void fallback() {
        if (Context.getCaller().equals(dexScore.get())) {
            burn(Context.getValue());
        } else {
            Context.revert(TAG + ": Please send fund using addFund().");
        }
    }


    //EventLogs
    @Override
    @EventLog(indexed = 1)
    public void FundReturned(Address _sponsor_address, String note) {
    }

    @Override
    @EventLog(indexed = 1)
    public void ProposalFundTransferred(String _ipfs_key, String note) {
    }

    @Override
    @EventLog(indexed = 1)
    public void ProposalDisqualified(String _ipfs_key, String note) {
    }

    @Override
    @EventLog(indexed = 1)
    public void FundReceived(Address _sponsor_address, String note) {
    }

    @EventLog(indexed = 1)
    public void EmergencyFundTranserred(Address _address, BigInteger _value, String _purpose) {
    }
}
