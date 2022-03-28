package com.iconloop.score.example;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import score.*;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;
import score.annotation.Optional;

import scorex.util.ArrayList;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class CPFTreasury {
    private static final BigInteger MULTIPLIER = BigInteger.valueOf(10 ^ 18);
    private static final String TAG = "CPF_TREASURY";
    private static final String ICX = "ICX";
    private static final String bnUSD = "bnUSD";
    private static final String PROPOSAL_BUDGETS = "_proposals_budgets";
    private static final String PROPOSALS_KEYS = "_proposals_keys";
    private static final String CPS_TREASURY_SCORE = "_cps_treasury_score";
    private static final String CPS_SCORE = "_cps_score";
    private static final String TREASURY_FUND = "treasury_fund";
    private static final String TREASURY_FUND_BNUSD = "treasury_fund_bnusd";
    private static final String IPFS_HASH = "_ipfs_hash";
    private static final String TOTAL_BUDGET = "_budget_transfer";
    private static final String BALANCED_DOLLAR = "balanced_dollar";
    private static final String DEX_SCORE = "dex_score";
    private static final String SICX_SCORE = "sicx_score";
    private static final String STAKING_SCORE = "staking_score";
    private static final String ROUTER_SCORE = "router_score";
    private static final String SWAP_STATE = "swap_state";
    private static final String SWAP_COUNT = "swap_count";
    private static final Address SYSTEM_ADDRESS = Address.fromString("cx0000000000000000000000000000000000000000");
    private static final int SICXICXPOOLID = 1;
    private static final int SICXBNUSDPOOLID = 2;


    private static final ArrayDB<String> proposalsKeys = Context.newArrayDB(PROPOSALS_KEYS, String.class);
    private static final DictDB<String, BigInteger> proposalBudgets = Context.newDictDB(PROPOSAL_BUDGETS, BigInteger.class);
    private static final VarDB<BigInteger> treasuryFund = Context.newVarDB(TREASURY_FUND, BigInteger.class);
    private static final VarDB<BigInteger> treasuryFundBnUSd = Context.newVarDB(TREASURY_FUND_BNUSD, BigInteger.class);

    private static final VarDB<Address> cpsTreasuryScore = Context.newVarDB(CPS_TREASURY_SCORE, Address.class);
    private static final VarDB<Address> cpsScore = Context.newVarDB(CPS_SCORE, Address.class);
    private static final VarDB<Address> balancedDollar = Context.newVarDB(BALANCED_DOLLAR, Address.class);
    private static final VarDB<Address> dexScore = Context.newVarDB(DEX_SCORE, Address.class);
    private static final VarDB<Address> stakingScore = Context.newVarDB(STAKING_SCORE, Address.class);
    private static final VarDB<Address> sICXScore = Context.newVarDB(SICX_SCORE, Address.class);
    private static final VarDB<Address> routerScore = Context.newVarDB(ROUTER_SCORE, Address.class);

    private static final VarDB<Integer> swapState = Context.newVarDB(SWAP_STATE, Integer.class);
    private static final VarDB<Integer> swapCount = Context.newVarDB(SWAP_COUNT, Integer.class);

    public CPFTreasury(int amount) {
        treasuryFund.set(BigInteger.valueOf(amount).multiply(MULTIPLIER));
    }

    private boolean proposalExists(String _ipfs_key) {
        return proposalBudgets.getOrDefault(_ipfs_key, null) != null;
    }

    @External(readonly = true)
    public String name() {
        return TAG;
    }

    private void validateAdmins() {
        Context.require((Boolean) Context.call(cpsScore.get(), "is_admin", Context.getCaller()),
                TAG + ": Only Admins can call this method");

    }

    private void validateOwner() {
        Context.require(Context.getCaller().equals(Context.getOwner()),
                TAG + ": Only owner can call this method");
    }

    private void validateOwnerScore(Address _score) {
        validateOwner();
        Context.require(_score.isContract(), TAG + ": Target " + _score + " is not a SCORE");
    }

    private void validateCpsScore(@Optional Address _from) {
        if (_from == null) {
            _from = Context.getCaller();
        }
        Address _cpsScore = cpsScore.get();
        Context.require(_from.equals(_cpsScore),
                TAG + ": Only " + _cpsScore + " SCORE can send fund using this method.");
    }

    private void validateCpsTreasuryScore(@Optional Address _from) {
        if (_from == null) {
            _from = Context.getCaller();
        }
        Address _cpsTreasuryScore = cpsTreasuryScore.get();
        Context.require(_from.equals(_cpsTreasuryScore),
                TAG + ": Only " + _cpsTreasuryScore + " SCORE can send fund using this method.");
    }

    /**
     * Set the maximum Treasury fund. Default 1M in ICX
     *
     * @param _value: value in loop
     */
    @External
    public void set_maximum_treasury_fund_icx(BigInteger _value) {
        validateAdmins();
        treasuryFund.set(_value);
    }

    /**
     * Set the maximum Treasury fund. Default 1M in bnUSD
     *
     * @param _value: value in loop
     */
    @External
    public void set_maximum_treasury_fund_bnusd(BigInteger _value) {
        validateAdmins();
        treasuryFundBnUSd.set(_value);
    }


    /**
     * Sets the cps score address. Only owner can set the method
     *
     * @param _score: Score address of cps score
     */
    @External
    public void set_cps_score(Address _score) {
        validateOwnerScore(_score);
        cpsScore.set(_score);
    }

    /**
     * Retruns the cps score address
     *
     * @return cps score address
     */
    @External(readonly = true)
    public Address get_cps_score() {
        return cpsScore.get();
    }

    /**
     * Sets the cps treasury score address. Only owner can set the method
     *
     * @param _score: Score address of cps treasury score
     */
    @External
    public void set_cps_treasury_score(Address _score) {
        validateOwnerScore(_score);
        cpsTreasuryScore.set(_score);
    }

    /**
     * Retruns the cps treasury score address
     *
     * @return cps treasury score address
     */
    @External(readonly = true)
    public Address get_cps_treasury_score() {
        return cpsTreasuryScore.get();
    }

    /**
     * Sets the bnUSD score address. Only owner can set the method
     *
     * @param _score: Score address of bnUSD score
     */
    @External
    public void set_bnUSD_score(Address _score) {
        validateOwnerScore(_score);
        balancedDollar.set(_score);
    }

    /**
     * Retruns the bnUSD score address
     *
     * @return cps bnUSD address
     */
    @External(readonly = true)
    public Address get_bnUSD_score() {
        return balancedDollar.get();
    }

    /**
     * Sets the sicx score address. Only owner can set the method
     *
     * @param _score: Score address of sicx score
     */
    @External
    public void set_sicx_score(Address _score) {
        validateOwnerScore(_score);
        sICXScore.set(_score);
    }

    /**
     * Retruns the sicx score address
     *
     * @return sicx score address
     */
    @External(readonly = true)
    public Address get_sicx_score() {
        return sICXScore.get();
    }

    /**
     * Sets the dex score address. Only owner can set the method
     *
     * @param _score: Score address of dex score
     */
    @External
    public void set_dex_score(Address _score) {
        validateOwnerScore(_score);
        dexScore.set(_score);
    }

    /**
     * Retruns the dex score address
     *
     * @return dex score address
     */
    @External(readonly = true)
    public Address get_dex_score() {
        return dexScore.get();
    }

    /**
     * Sets the router score address. Only owner can set the method
     *
     * @param _score: Score address of router score
     */
    @External
    public void set_router_score(Address _score) {
        validateOwnerScore(_score);
        routerScore.set(_score);
    }

    /**
     * Retruns the router score address
     *
     * @return router score address
     */
    @External(readonly = true)
    public Address get_router_score() {
        return routerScore.get();
    }

    /**
     * Burns ICX method
     *
     * @param amount: amount to burn in loop
     */
    private void _burn(BigInteger amount) {
        Context.call(amount, SYSTEM_ADDRESS, "burn");
    }

    @External(readonly = true)
    public Map<String, BigInteger> get_total_funds() {
        return Map.of(ICX, Context.getBalance(Context.getAddress()),
                bnUSD, getTotalFundBNUSD());
    }

    private BigInteger getTotalFundBNUSD() {
        return (BigInteger) Context.call(balancedDollar.get(), "balanceOf", Context.getAddress());
    }

    @External(readonly = true)
    public Map<String, BigInteger> get_remaining_swap_amount() {
        BigInteger maxCap = treasuryFundBnUSd.get();
        return Map.of("maxCap", maxCap,
                "remainingToSwap", maxCap.subtract(getTotalFundBNUSD()));
    }

    @External
    @Payable
    public void return_fund_amount(Address _address, @Optional Address _from, @Optional String _flag, @Optional BigInteger _value) {
        if (_from == null) {
            _from = Context.getCaller();
        }
        if (_flag == null) {
            _flag = ICX;
        }
        if (_value == null) {
            _value = BigInteger.ZERO;
        }
        validateCpsScore(_from);
        _burn_extra_fund();
        if (_flag.equals(ICX)) {
            _value = Context.getValue();
        }
        FundReturned(_address, "Sponsor Bond amount " + _value + " " + _flag + " Returned to CPF Treasury.");
    }

    @External
    public void transfer_proposal_fund_to_cps_treasury(String _ipfs_key, int _total_installment_count,
                                                       Address _sponsor_address, Address _contributor_address,
                                                       String token_flag, BigInteger _total_budget) {
        validateCpsScore(Context.getCaller());
        Context.require(!proposalExists(_ipfs_key), TAG + ": Project already exists. Invalid IPFS Hash");
        BigInteger _sponsor_reward = (_total_budget.multiply(BigInteger.TWO)).divide(BigInteger.valueOf(100));
        BigInteger total_transfer = _total_budget.add(_sponsor_reward);

        BigInteger balanceOf = (BigInteger) Context.call(balancedDollar.get(), "balanceOf", Context.getAddress());
        Context.require(balanceOf.compareTo(total_transfer) > 0, TAG + ": Not enough fund " + balanceOf + " token available");
        Context.require(token_flag.equals(bnUSD), TAG + ": " + token_flag + " is not supported. Only " + bnUSD + " token available.");
        proposalsKeys.add(_ipfs_key);
        proposalBudgets.set(_ipfs_key, total_transfer);
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("method", "deposit_proposal_fund");
        JsonObject params = new JsonObject();
        params.add("ipfs_hash", _ipfs_key);
        params.add("project_duration", String.valueOf(_total_installment_count));
        params.add("sponsor_address", _sponsor_address.toString());
        params.add("contributor_address", _contributor_address.toString());
        params.add("totalBudget", _total_budget.toString());
        params.add("sponsor_reward", _sponsor_reward.toString());
        params.add("token", token_flag);
        jsonObject.add("params", params);
        String jsonString = String.valueOf(jsonObject);

        Context.call(balancedDollar.get(), "transfer", cpsTreasuryScore.get(), total_transfer, jsonString.getBytes());
        ProposalFundTransferred(_ipfs_key, "Successfully transferred " + total_transfer + " " + token_flag + " to CPS Treasury " + cpsTreasuryScore.get());
    }

    @External
    public void update_proposal_fund(String _ipfs_key, String _flag, @Optional BigInteger _added_budget, @Optional int _total_installment_count) {
        if (_added_budget == null){
            _added_budget = BigInteger.ZERO;
        }
//        todo: cannot compare int to null. need to find a way to do it.
//        if (_total_installment_count == null){
//            _total_installment_count = 0;
//        }
        validateCpsScore(Context.getCaller());
        Context.require(_flag.equals(bnUSD), TAG + ": Unsupported token. " + _flag);
        BigInteger _sponsor_reward = (_added_budget.multiply(BigInteger.TWO)).divide(BigInteger.valueOf(100));
        BigInteger total_transfer = _added_budget.add(_sponsor_reward);

        Context.require(proposalExists(_ipfs_key), TAG + ": IPFS hash does not exist.");

        BigInteger proposalBudget = proposalBudgets.getOrDefault(_ipfs_key, BigInteger.ZERO);
        proposalBudgets.set(_ipfs_key, proposalBudget.add(total_transfer));
        BigInteger bnUSD_fund = get_total_funds().get(bnUSD);
        Context.require(bnUSD_fund.compareTo(total_transfer) >= 0, TAG + ": Not enough " + total_transfer + " BNUSD on treasury");
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("method", "budget_adjustment");
        JsonObject params = new JsonObject();
        params.add("_ipfs_key", _ipfs_key);
        params.add("_added_budget", _added_budget.toString());
        params.add("_added_sponsor_reward", _sponsor_reward.toString());
        params.add("_added_installment_count", String.valueOf(_total_installment_count));
        jsonObject.add("params", params);
        String jsonString = String.valueOf(jsonObject);

        Context.call(balancedDollar.get(), "transfer", cpsTreasuryScore.get(), total_transfer, jsonString.getBytes());
        ProposalFundTransferred(_ipfs_key, "Successfully transferred " + total_transfer + " " + bnUSD + " to CPS Treasury");
    }

    @External
    @Payable
    public void disqualify_proposal_fund(String _ipfs_key, @Optional BigInteger _value, @Optional String _flag,
                                         @Optional Address _from) {
        if (_value == null) {
            _value = BigInteger.ZERO;
        }
        if (_flag == null) {
            _flag = ICX;
        }
        if (_from == null) {
            _from = Context.getCaller();
        }
        validateCpsTreasuryScore(_from);
        Context.require(proposalExists(_ipfs_key), TAG + ": IPFS key does not exist.");
        BigInteger _budget = proposalBudgets.get(_ipfs_key);
        Context.require(_flag.equals(ICX) || _flag.equals(bnUSD), TAG + ": Not supported token." + _flag);
        if (_flag.equals(ICX)) {
            _value = Context.getValue();
        }
        proposalBudgets.set(_ipfs_key, _budget.subtract(_value));

        _burn_extra_fund();
        ProposalDisqualified(_ipfs_key, "Proposal disqualified. " + _value + " " + _flag + " is returned back to Treasury.");
    }

    @External
    @Payable
    public void add_fund() {
        _burn_extra_fund();
        FundReceived(Context.getCaller(), "Treasury fund " + Context.getValue() + " " + ICX + " received.");
    }

    private void _burn_extra_fund() {
        Map<String, BigInteger> amounts = get_total_funds();
        BigInteger icx_amount = amounts.get(ICX);
        BigInteger bnUSD_amount = amounts.get(bnUSD);
        BigInteger _extra_amount_icx = icx_amount.subtract(treasuryFund.get());
        BigInteger _extra_amount_bnUSD = bnUSD_amount.subtract(treasuryFundBnUSd.get());

        if (_extra_amount_icx.compareTo(BigInteger.ZERO) > 0) {
            _burn(_extra_amount_icx);
        }

        if (_extra_amount_bnUSD.compareTo(BigInteger.ZERO) > 0) {
            _swap_tokens(balancedDollar.get(), sICXScore.get(), _extra_amount_bnUSD);
        }
    }

    private void _swap_tokens(Address _from, Address _to, BigInteger _amount) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("method", "_swap");
        JsonObject params = new JsonObject();
        params.add("toToken", _to.toString());
        jsonObject.add("params", params);
        String jsonString = String.valueOf(jsonObject);
        Context.call(_from, "transfer", dexScore.get(), _amount, jsonString.getBytes());
    }

    @External
    public void swap_icx_bnusd(BigInteger _amount) {
        Address[] path = new Address[]{sICXScore.get(), balancedDollar.get()};
        Context.call(_amount, routerScore.get(), "route", (Object[]) path);
    }

    @External
    public void swap_tokens(int _count) {
        validateCpsScore(Context.getCaller());
        BigInteger sicxICXPrice = (BigInteger) Context.call(dexScore.get(), "getPrice", SICXICXPOOLID);
        BigInteger sicxBnusdPrice = (BigInteger) Context.call(dexScore.get(), "getPrice", SICXBNUSDPOOLID);
        BigInteger icxbnUSDPrice = sicxBnusdPrice.multiply(MULTIPLIER).divide(sicxICXPrice);
        BigInteger bnUSDRemainingToSwap = get_remaining_swap_amount().get("remainingToSwap");

        if (bnUSDRemainingToSwap.compareTo(BigInteger.TEN.multiply(MULTIPLIER)) < 0 || _count == 0) {
            swapState.set(1);
            swapCount.set(0);
        }
        int swap_state = swapState.get();
        if (swap_state == 0) {
            int swap_count = swapCount.get();
            int count = _count - swap_count;
            if (count == 0) {
                swapState.set(1);
                swapCount.set(0);
            } else {
                BigInteger remainingICXToSwap = bnUSDRemainingToSwap.multiply(MULTIPLIER).divide(icxbnUSDPrice.multiply(BigInteger.valueOf(count)));
                BigInteger icxBalance = Context.getBalance(Context.getAddress());
                if (remainingICXToSwap.compareTo(icxBalance) > 0) {
                    remainingICXToSwap = icxBalance;
                }

                if (remainingICXToSwap.compareTo(BigInteger.valueOf(5).multiply(MULTIPLIER)) > 0) {
                    swap_icx_bnusd(remainingICXToSwap);
                }
            }
        }
    }

    @External(readonly = true)
    public Map<String, Integer> get_swap_state_status() {
        return Map.of("state", swapState.get(), "count", swapCount.get());
    }

    @External
    public void reset_swap_state() {
        Address cps_score_address = cpsScore.get();
        Address caller = Context.getCaller();

        boolean checkCaller = caller.equals(cps_score_address) || (Boolean) Context.call(cps_score_address, "is_admin", caller);
        Context.require(checkCaller, TAG + ": Only admin can call this method.");
        swapState.set(0);
    }

    @External(readonly = true)
    public List<Map<String, String>> get_proposal_details(int _start_index, int _end_index) {
        List<Map<String, String>> proposals_list = new ArrayList<>();
        if ((_end_index - _start_index) > 50) {
            Context.revert("Page Length cannot be greater than 50");
        }
        int count = proposalsKeys.size();

        if (_start_index < 0 || _start_index > count) {
            _start_index = 0;
        }

        if (_end_index > count) {
            _end_index = count;
        }

        for (int i = _start_index; i < _end_index; i++) {
            Map<String, String> proposal_details = Map.of(TOTAL_BUDGET, proposalBudgets.getOrDefault(proposalsKeys.get(i), BigInteger.ZERO).toString(),
                    IPFS_HASH, proposalsKeys.get(i));
            proposals_list.add(proposal_details);
        }
        proposals_list.add(Map.of("count", String.valueOf(count)));
        return proposals_list;
    }

    @External
    public void tokenFallback(Address _from, BigInteger _value, byte[] _data) {
        String unpacked_data = new String(_data);
        Address bnUSDScore = balancedDollar.get();
        Address staking = stakingScore.get();

        Address caller = Context.getCaller();
        Context.require(caller.equals(bnUSDScore) || caller.equals(staking), TAG + " Only " + bnUSDScore + " and " + staking + " can send tokens to CPF Treasury.");
        if (caller.equals(staking)) {
            if (_from.equals(dexScore.get())) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.add("method", "_swap_icx");
                String jsonString = String.valueOf(jsonObject);
                Context.call(dexScore.get(), "transfer", _value, jsonString.getBytes());
            }
        } else {
            JsonObject json = Json.parse(unpacked_data).asObject();
            if (_from.equals(cpsScore.get())) {
                if (json.get("method").asString().equals("return_fund_amount")) {
                    Address _sponsor_address = Address.fromString(json.get("params").asObject().get("sponsor_address").asString());
                    return_fund_amount(_sponsor_address, _from, bnUSD, _value);
                } else if (json.get("method").asString().equals("burn_amount")) {
                    _swap_tokens(caller, stakingScore.get(), _value);
                } else {
                    Context.revert(TAG + " Not supported method " + json.get("method"));
                }
            }
            if (_from.equals(cpsTreasuryScore.get())) {
                if (json.get("method").asString().equals("disqualify_project")) {
                    String ipfs_key = json.get("params").asObject().get("ipfs_key").asString();
                    disqualify_proposal_fund(ipfs_key, _value, bnUSD, _from);
                } else {
                    Context.revert(TAG + " Not supported method " + json.get("method"));
                }
            }
        }
    }

    @Payable
    public void fallback() {
        if (Context.getCaller().equals(routerScore.get())) {
            _burn(Context.getValue());
        } else {
            add_fund();
        }
    }

    @EventLog(indexed = 1)
    public void FundReturned(Address _sponsor_address, String note) {
    }

    @EventLog(indexed = 1)
    public void ProposalFundTransferred(String _ipfs_key, String note) {
    }

    @EventLog(indexed = 1)
    public void ProposalDisqualified(String _ipfs_key, String note) {
    }

    @EventLog(indexed = 1)
    public void FundReceived(Address _sponsor_address, String note) {
    }

    @External
    public void is_contract() {
//        Context.revert("contract");
        Address caller = Context.getCaller();
        if (caller.isContract()) {
            Context.revert("is contract");
        } else {
            Context.revert("is not contract");
        }
    }


}
