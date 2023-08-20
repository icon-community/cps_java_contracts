package community.icon.cps.score.cpftreasury;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import community.icon.cps.score.lib.interfaces.CPFTreasuryInterface;
import score.*;
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
        oraclePerDiff.set(3);
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
    public void setMaximumTreasuryFundIcx(BigInteger _value) {
        validateAdmins();
        treasuryFund.set(_value);
    }

    /**
     * Set the maximum Treasury fund. Default 1M in bnUSD
     *
     * @param _value: value in loop
     */
    @Override
    @External
    public void setMaximumTreasuryFundBnusd(BigInteger _value) {
        validateAdmins();
        treasuryFundbnUSD.set(_value);
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
    public Map<String, BigInteger> get_total_funds() {
        return Map.of(ICX, Context.getBalance(Context.getAddress()),
                bnUSD, getTotalFundBNUSD());
    }

    private BigInteger getTotalFundBNUSD() {
        return (BigInteger) Context.call(balancedDollar.get(), "balanceOf", Context.getAddress());
    }

    @Override
    @External(readonly = true)
    public Map<String, BigInteger> get_remaining_swap_amount() {
        BigInteger maxCap = treasuryFundbnUSD.get();
        return Map.of("maxCap", maxCap,
                "remainingToSwap", maxCap.subtract(getTotalFundBNUSD()));
    }

    private void returnFundAmount(Address address, BigInteger value) {
        Context.require(value.compareTo(BigInteger.ZERO) > 0, TAG + ": Sponsor Bond Amount should be greater than 0");
        burnExtraFund();
        FundReturned(address, "Sponsor Bond amount " + value + " " + bnUSD + " Returned to CPF Treasury.");
    }

    @Override
    @External
    public void transfer_proposal_fund_to_cps_treasury(String _ipfs_key, int _total_installment_count,
                                                       Address _sponsor_address, Address _contributor_address,
                                                       String token_flag, BigInteger _total_budget) {
        validateCpsScore();
        Context.require(!proposalExists(_ipfs_key), TAG + ": Project already exists. Invalid IPFS Hash");
        Context.require(token_flag.equals(bnUSD), TAG + ": " + token_flag + " is not supported. Only " + bnUSD + " token available.");
        BigInteger sponsorReward = _total_budget.multiply(BigInteger.TWO).divide(BigInteger.valueOf(100));
        BigInteger totalTransfer = _total_budget.add(sponsorReward);

        Address balancedDollar = CPFTreasury.balancedDollar.get();
        BigInteger bnUSDBalance = Context.call(BigInteger.class, balancedDollar, "balanceOf", Context.getAddress());
        Context.require(totalTransfer.compareTo(bnUSDBalance) < 0, TAG + ": Not enough fund " + bnUSDBalance + " token available");

        proposalsKeys.add(_ipfs_key);
        proposalBudgets.set(_ipfs_key, totalTransfer);

        JsonObject depositProposal = new JsonObject();
        depositProposal.add(METHOD, "deposit_proposal_fund");
        JsonObject params = new JsonObject();
        params.add("ipfs_hash", _ipfs_key);
        params.add("project_duration", _total_installment_count);
        params.add("sponsor_address", _sponsor_address.toString());
        params.add("contributor_address", _contributor_address.toString());
        params.add("total_budget", _total_budget.toString(16));
        params.add("sponsor_reward", sponsorReward.toString(16));
        params.add("token", token_flag);
        depositProposal.add(PARAMS, params);

        Context.call(balancedDollar, TRANSFER, cpsTreasuryScore.get(), totalTransfer, depositProposal.toString().getBytes());
        ProposalFundTransferred(_ipfs_key, "Successfully transferred " + totalTransfer + " " + token_flag + " to CPS Treasury " + cpsTreasuryScore.get());
    }

    @Override
    @External
    public void update_proposal_fund(String _ipfs_key, @Optional String _flag, @Optional BigInteger _added_budget,
                                     @Optional int _total_installment_count) {
        validateCpsScore();
        Context.require(proposalExists(_ipfs_key), TAG + ": IPFS hash does not exist.");
        Context.require(_flag != null && _flag.equals(bnUSD), TAG + ": Unsupported token. " + _flag);

        if (_added_budget == null) {
            _added_budget = BigInteger.ZERO;
        }


        BigInteger sponsorReward = _added_budget.multiply(BigInteger.TWO).divide(BigInteger.valueOf(100));
        BigInteger totalTransfer = _added_budget.add(sponsorReward);

        BigInteger proposalBudget = proposalBudgets.getOrDefault(_ipfs_key, BigInteger.ZERO);
        proposalBudgets.set(_ipfs_key, proposalBudget.add(totalTransfer));
        BigInteger bnUSDFund = get_total_funds().get(bnUSD);
        Context.require(totalTransfer.compareTo(bnUSDFund) <= 0, TAG + ": Not enough " + totalTransfer + " BNUSD on treasury");

        JsonObject budgetAdjustmentData = new JsonObject();
        budgetAdjustmentData.add(METHOD, "budget_adjustment");
        JsonObject params = new JsonObject();
        params.add("_ipfs_key", _ipfs_key);
        params.add("_added_budget", _added_budget.toString(16));
        params.add("_added_sponsor_reward", sponsorReward.toString(16));
        params.add("_added_installment_count", _total_installment_count);
        budgetAdjustmentData.add(PARAMS, params);

        Context.call(balancedDollar.get(), TRANSFER, cpsTreasuryScore.get(), totalTransfer, budgetAdjustmentData.toString().getBytes());
        ProposalFundTransferred(_ipfs_key, "Successfully transferred " + totalTransfer + " " + bnUSD + " to CPS Treasury");
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
    public void add_fund() {
        burnExtraFund();
        FundReceived(Context.getCaller(), "Treasury fund " + Context.getValue() + " " + ICX + " received.");
    }

    private void burnExtraFund() {
        Map<String, BigInteger> amounts = get_total_funds();
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
    public void setSwapLimitAmount(BigInteger _value) {
        validateAdmins();
        Context.require(_value.compareTo(BigInteger.ZERO) > 0, TAG + ": Swap limit amount should be greater than 0");
        swapLimitAmount.set(_value);

    }

    @External(readonly = true)
    public BigInteger getSwapLimitAmount() {
        return swapLimitAmount.getOrDefault(BigInteger.ZERO);

    }


    @Override
    @External
    public void swap_tokens(int _count) {
        validateCpsScore();
        BigInteger sicxICXPrice = (BigInteger) Context.call(dexScore.get(), "getPrice", sICXICXPoolID);
        BigInteger sicxBnusdPrice = (BigInteger) Context.call(dexScore.get(), "getPrice", sICXBNUSDPoolID);
        BigInteger icxbnUSDPrice = sicxBnusdPrice.multiply(EXA).divide(sicxICXPrice);
        BigInteger bnUSDRemainingToSwap = get_remaining_swap_amount().get("remainingToSwap");
        if (bnUSDRemainingToSwap.compareTo(BigInteger.TEN.multiply(EXA)) < 0 || _count == 0) {
            swapState.set(SwapCompleted);
            swapCount.set(SwapReset);
        } else {
            int swapState = this.swapState.getOrDefault(0);
            if (swapState == SwapContinue) {
                int swapCountValue = swapCount.getOrDefault(0);
                int count = _count - swapCountValue;
                if (count == 0) {
                    this.swapState.set(SwapCompleted);
                    swapCount.set(SwapReset);
                } else {
                    BigInteger remainingICXToSwap = bnUSDRemainingToSwap.multiply(EXA).divide(icxbnUSDPrice.multiply(BigInteger.valueOf(count)));
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
    public Map<String, Integer> get_swap_state_status() {
        return Map.of("state", swapState.getOrDefault(0), "count", swapCount.getOrDefault(0));
    }

    @Override
    @External
    public void reset_swap_state() {
        Address cpsScoreAddress = cpsScore.get();
        Address caller = Context.getCaller();

        boolean checkCaller = caller.equals(cpsScoreAddress) || (Boolean) Context.call(cpsScoreAddress, "isAdmin", caller);
        Context.require(checkCaller, TAG + ": Only admin can call this method.");
        swapState.set(SwapContinue);
        swapCount.set(SwapReset);
    }

    @External
    public void setOraclePercentageDifference(int _value) {
        validateAdmins();
        oraclePerDiff.set(_value);
    }

    @Override
    @External(readonly = true)
    public Map<String, Object> get_proposal_details(@Optional int _start_index, @Optional int _end_index) {
        if (_end_index == 0) {
            _end_index = 20;
        }
        List<Map<String, Object>> proposalsList = new ArrayList<>();
        if ((_end_index - _start_index) > 50) {
            Context.revert(TAG + ": Page Length cannot be greater than 50");
        }
        int count = proposalsKeys.size();
        if (_start_index > count) {
            Context.revert(TAG + ": Start index can't be higher than total count.");
        }

        if (_start_index < 0) {
            _start_index = 0;
        }

        if (_end_index > count) {
            _end_index = count;

        }

        for (int i = _start_index; i < _end_index; i++) {
            String proposalHash = proposalsKeys.get(i);
            Map<String, Object> proposalDetails = Map.of(TOTAL_BUDGET, proposalBudgets.getOrDefault(proposalHash, BigInteger.ZERO).toString(), IPFS_HASH, proposalHash);
            proposalsList.add(proposalDetails);
        }
        return Map.of("data", proposalsList, "count", count);
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
                if (transferData.get(METHOD).asString().equals("return_fund_amount")) {
                    Address _sponsor_address = Address.fromString(transferData.get(PARAMS).asObject().get("sponsor_address").asString());
                    returnFundAmount(_sponsor_address, _value);
                } else if (transferData.get(METHOD).asString().equals("burn_amount")) {
                    swapTokens(caller, sICX, _value);
                } else {
                    Context.revert(TAG + ": Not supported method " + transferData.get(METHOD).asString());
                }
            } else if (_from.equals(cpsTreasuryScore.get())) {
                if (transferData.get(METHOD).asString().equals("disqualify_project")) {
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
            Context.revert(TAG + ": Please send fund using add_fund().");
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
}
