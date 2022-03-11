/*
 * Copyright 2020 ICONLOOP Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iconloop.score.example;

import score.Address;
import score.VarDB;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import score.BranchDB;
import score.ArrayDB;
import score.DictDB;
import score.Context;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;
import scorex.util.ArrayList;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.iconloop.score.example.utils.ArrayDBUtils;
import com.iconloop.score.example.utils.consts;
import com.iconloop.score.example.utils.Eventlogs;

public class CPFTreasury {
    private final String name;
    private final String symbol;
    private static final BigInteger decimal = BigInteger.valueOf(10^18);
    private static final String TAG = "CPF_TREASURY";
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

    public CPFTreasury(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
        swapState.set(0);
        swapCount.set(0);
        treasuryFund.set(BigInteger.valueOf(1000000));
    }

    private void _validate_admins(){
        Context.require((Boolean) Context.call(cpsScore.get(), "is_admin", Context.getCaller()),
                TAG + ": Only Admins can call this method");

    }

    private void _validate_owner(){
        Context.require(Context.getCaller().equals(Context.getOwner()),
                TAG + ": Only owner can call this method");
    }

    private void _validate_owner_score(Address _score){
        _validate_owner();
        Context.require(_score.isContract(), TAG + ": Target " + _score + " is not a SCORE");
    }

    private void _validate_cps_score(Address _from){
        Address cps_score = cpsScore.get();
        Context.require(Context.getCaller().equals(cpsScore.get()),
                TAG + ": Only " + cps_score + " SCORE can send fund using this method.");
    }

    private void _validate_cps_treasury_score(Address _from){
        Address cps_score = cpsScore.get();
        Context.require(Context.getCaller().equals(cpsScore.get()),
                TAG + ": Only " + cps_score + " SCORE can send fund using this method.");
    }

    /**
     * Set the maximum Treasury fund. Default 1M in ICX
     * @param _value: value in loop
     */
    @External
    public void set_maximum_treasury_fund_icx(BigInteger _value){
        _validate_owner();
        treasuryFund.set(_value);
    }

    /**
     * Set the maximum Treasury fund. Default 1M in bnUSD
     * @param _value: value in loop
     */
    @External
    public void set_maximum_treasury_fund_bnusd(BigInteger _value){
        _validate_owner();
        treasuryFundBnUSd.set(_value);
    }

    /**
     * Sets the cps score address. Only owner can set the method
     * @param _score: Score address of cps score
     */
    @External
    public void set_cps_score(Address _score){
        _validate_owner_score(_score);
        cpsScore.set(_score);
    }

    /**
     * Retruns the cps score address
     * @return cps score address
     */
    @External(readonly = true)
    public Address get_cps_score(){
        return cpsScore.get();
    }

    /**
     * Sets the cps treasury score address. Only owner can set the method
     * @param _score: Score address of cps treasury score
     */
    @External
    public void set_cps_treasury_score(Address _score){
        _validate_owner_score(_score);
        cpsTreasuryScore.set(_score);
    }

    /**
     * Retruns the cps treasury score address
     * @return cps treasury score address
     */
    @External(readonly = true)
    public Address get_cps_treasury_score(){
        return cpsTreasuryScore.get();
    }

    /**
     * Sets the bnUSD score address. Only owner can set the method
     * @param _score: Score address of bnUSD score
     */
    @External
    public void set_bnUSD_score(Address _score){
        _validate_owner_score(_score);
        balancedDollar.set(_score);
    }

    /**
     * Retruns the bnUSD score address
     * @return cps bnUSD address
     */
    @External(readonly = true)
    public Address get_bnUSD_score(){
        return balancedDollar.get();
    }

    /**
     * Sets the staking score address. Only owner can set the method
     * @param _score: Score address of staking score
     */
    @External
    public void set_staking_score(Address _score){
        _validate_owner_score(_score);
        stakingScore.set(_score);
    }

    /**
     * Retruns the staking score address
     * @return staking score address
     */
    @External(readonly = true)
    public Address get_staking_score(){
        return stakingScore.get();
    }

    /**
     * Sets the sicx score address. Only owner can set the method
     * @param _score: Score address of sicx score
     */
    @External
    public void set_sicx_score(Address _score){
        _validate_owner_score(_score);
        sICXScore.set(_score);
    }

    /**
     * Retruns the sicx score address
     * @return sicx score address
     */
    @External(readonly = true)
    public Address get_sicx_score(){
        return sICXScore.get();
    }

    /**
     * Sets the dex score address. Only owner can set the method
     * @param _score: Score address of dex score
     */
    @External
    public void set_dex_score(Address _score){
        _validate_owner_score(_score);
        dexScore.set(_score);
    }

    /**
     * Retruns the dex score address
     * @return dex score address
     */
    @External(readonly = true)
    public Address get_dex_score(){
        return dexScore.get();
    }

    /**
     * Sets the router score address. Only owner can set the method
     * @param _score: Score address of router score
     */
    @External
    public void set_router_score(Address _score){
        _validate_owner_score(_score);
        routerScore.set(_score);
    }

    /**
     * Retruns the router score address
     * @return router score address
     */
    @External(readonly = true)
    public Address get_router_score(){
        return routerScore.get();
    }

    @External(readonly = true)
    public String name(){
        return this.name;
    }

    @External(readonly = true)
    public String symbol(){
        return this.symbol;
    }

    /**
     * Burns ICX method
     * @param amount: amount to burn in loop
     */
    private void _burn(BigInteger amount){
        try {
            Context.call(amount, SYSTEM_ADDRESS, "burn");
        }
        catch (Exception e){
            Context.revert(TAG + ": Network problem while burning " + amount + " ICX. Exception " + e);
        }
    }

    @External(readonly = true)
    public Map<String, BigInteger> get_total_funds(){
        return Map.of("ICX", Context.getBalance(Context.getAddress()),
                "bnUSD", _get_total_fund_bnusd());
    }

    private BigInteger _get_total_fund_bnusd(){
        return (BigInteger) Context.call(balancedDollar.get(), "balanceOf", Context.getAddress());
    }

    @External(readonly = true)
    public Map<String, BigInteger> get_remaining_swap_amount(){
        BigInteger maxCap = treasuryFundBnUSd.get();
        return Map.of("maxCap", maxCap,
                "remainingToSwap", maxCap.subtract(_get_total_fund_bnusd()));
    }

    @External
    @Payable
    public void return_fund_amount(Address _address, Address _from, String _flag, BigInteger _value){
        _validate_cps_score(_from);
        _burn_extra_fund();
        if (_flag.equals(consts.ICX)){
            _value = Context.getValue();
        }
        FundReturned(_address, "Sponsor Bond amount " + _value + " " + _flag + " Returned to CPF Treasury.");
    }

    @External
    public void transfer_proposal_fund_to_cps_treasury(String _ipfs_key, int _total_installment_count,
                                                       Address _sponsor_address, Address _contributor_address,
                                                       String token_flag, BigInteger _total_budget){
        _validate_cps_score(Context.getCaller());
        Context.require(!_proposal_exists(_ipfs_key), TAG + ": Project already exists. Invalid IPFS Hash");
        BigInteger _sponsor_reward = (_total_budget.multiply(BigInteger.valueOf(2))).divide(BigInteger.valueOf(100));
        BigInteger total_transfer = _total_budget.add(_sponsor_reward);

        BigInteger balanceOf = (BigInteger) Context.call(balancedDollar.get(), "balanceOf", Context.getAddress());
        Context.require(balanceOf.compareTo(total_transfer) > 0, TAG + ": Not enough fund " + balanceOf + " token available");
        Context.require(token_flag.equals(consts.bnUSD), TAG + ": " + token_flag + " is not supported. Only " + consts.bnUSD + " token available.");
        proposalsKeys.add(_ipfs_key);
        proposalBudgets.set(_ipfs_key, total_transfer);

        String params = "" + "{\"method\":\"deposit_proposal_fund\",\"params\":{\"ipfs_hash\":" + _ipfs_key + ",\"project_duration\":" + _total_installment_count + ",\"sponsor_address\":" + _sponsor_address.toString() + ",\"contributor_address\":" + _contributor_address.toString() + ",\"total_budget\":" + _total_budget + ",\"sponsor_reward\":" + _sponsor_reward + ",\"token\":" + token_flag + "}}";

        try{
            Context.call(balancedDollar.get(), "transfer", total_transfer, params.getBytes());
            ProposalFundTransferred(_ipfs_key, "Successfully transferred " + total_transfer +  " " +  token_flag + " to CPS Treasury");
        }
        catch (Exception e){
            Context.revert(TAG + " Network problem. Sending proposal funds. " + e);
        }
    }

    @External
    public void update_proposal_fund(String _ipfs_key, String _flag, BigInteger _added_budget, int _total_installment_count){
        _validate_cps_score(Context.getCaller());
        BigInteger _sponsor_reward = (_added_budget.multiply(BigInteger.TWO)).divide(BigInteger.valueOf(100));
        BigInteger total_transfer = _added_budget.add(_sponsor_reward);

        Context.require(_proposal_exists(_ipfs_key), TAG + ": IPFS hash does not exist.");

        proposalBudgets.set(_ipfs_key, proposalBudgets.getOrDefault(_ipfs_key, BigInteger.ZERO).add(total_transfer));
        BigInteger bnUSD_fund = get_total_funds().get(consts.bnUSD);
        try{
            Context.require(bnUSD_fund.compareTo(total_transfer) >= 0, TAG + ": Not enough " + total_transfer + " BNUSD on treasury");
            String params = "" + "{\"method\":\"budget_adjustment\",\"params\":{\"_ipfs_key\":" + _ipfs_key + ",\"_added_budget\":" + _added_budget + ",\"_added_sponsor_reward\":" + _sponsor_reward + ",\"_added_installment_count\":" + _total_installment_count + "}}";
            Context.call(balancedDollar.get(), "transfer", total_transfer, params.getBytes());
            ProposalFundTransferred(_ipfs_key, "Successfully transferred " + total_transfer +  " " + consts.bnUSD + " to CPS Treasury");
        }
        catch (Exception e){
            Context.revert(TAG + ": Network problem. Sending proposal funds. " + e);
        }

    }

    @External
    @Payable
    public void disqualify_proposal_fund(String _ipfs_key, BigInteger _value, String _flag,
                                         Address _from){
        _validate_cps_score(_from);
        Context.require(_proposal_exists(_ipfs_key), TAG + ": IPFS key does not exist.");
        BigInteger _budget = proposalBudgets.get(_ipfs_key);
        if (_flag.equals(consts.ICX)){
            _value = Context.getValue();
            proposalBudgets.set(_ipfs_key, _budget.subtract(_value));
        }
        else if (_flag.equals(consts.bnUSD)){
            proposalBudgets.set(_ipfs_key, _budget.subtract(_value));
        }
        _burn_extra_fund();
        ProposalDisqualified(_ipfs_key, "Proposal disqualified. " + _value + " " + _flag + " is returned back to Treasury.");
    }

    @External
    @Payable
    public void add_fund(){
        _burn_extra_fund();
        FundReceived(Context.getCaller(), "Treasury fund " + Context.getValue() + " " + consts.ICX + " received.");
    }




    private boolean _proposal_exists(String _ipfs_key){
        return proposalBudgets.getOrDefault(_ipfs_key, null) != null;
    }

    private void _burn_extra_fund(){
        Map<String, BigInteger> amounts = get_total_funds();
        BigInteger icx_amount = amounts.get(consts.ICX);
        BigInteger bnUSD_amount = amounts.get(consts.bnUSD);
        BigInteger _extra_amount_icx = icx_amount.subtract(treasuryFund.get());
        BigInteger _extra_amount_bnUSD = bnUSD_amount.subtract(treasuryFundBnUSd.get());

        if (_extra_amount_icx.compareTo(BigInteger.ZERO) > 0){
            _burn(_extra_amount_icx);
        }

        if (_extra_amount_bnUSD.compareTo(BigInteger.ZERO) > 0){
            _swap_tokens(balancedDollar.get(), sICXScore.get(), _extra_amount_bnUSD);
        }
    }

    private void _swap_tokens(Address _from, Address _to, BigInteger _amount){
        // TODO: figure out how to send json string as data
    }

    @External
    public void swap_icx_bnusd(BigInteger _amount, int _sicx){
        Address sicxContract = sICXScore.get();
        if (_sicx == 1){
            _amount = (BigInteger) Context.call(sicxContract, "balanceOf", Context.getAddress());
            _swap_tokens(sicxContract, balancedDollar.get(), _amount);
        }
        else{
            Address[] path = new Address[2];
            path[0] = sicxContract;
            path[1] = balancedDollar.get();
            Context.call(_amount.multiply(decimal), routerScore.get(),"route", (Object[]) path);
        }
    }

    @External
    public void swap_tokens(int _count){
        _validate_cps_score(Context.getCaller());
        BigInteger sicxICXPrice = (BigInteger) Context.call(dexScore.get(), "getPrice", 1);
        BigInteger sicxBnusdPrice = (BigInteger) Context.call(dexScore.get(), "getPrice", 2);
        BigInteger icxbnUSDPrice = (sicxBnusdPrice.multiply(decimal)).divide(sicxICXPrice);
        BigInteger bnUSDRemainingToSwap = get_remaining_swap_amount().get("remainingToSwap");

        if (bnUSDRemainingToSwap.compareTo(BigInteger.valueOf(10).multiply(decimal)) < 0 || _count == 0){
            swapState.set(1);
        }
        try{
            int swap_state = swapState.get();
            if (swap_state == 0){
                int swap_count = swapCount.get();
                BigInteger remainingICXToSwap = (bnUSDRemainingToSwap.divide(icxbnUSDPrice.multiply(BigInteger.valueOf(_count - swap_count)))).multiply(decimal);
                BigInteger icxBalance = Context.getBalance(Context.getAddress());
                if (remainingICXToSwap.compareTo(icxBalance) > 0){
                    remainingICXToSwap = icxBalance;
                }

                if (remainingICXToSwap.compareTo(BigInteger.valueOf(5).multiply(decimal)) > 0){
                    Address[] path = new Address[2];
                    path[0] = sICXScore.get();
                    path[1] = balancedDollar.get();
                    Context.call(remainingICXToSwap, routerScore.get(), "route", (Object[]) path);
                    swapCount.set(swap_count + 1);
                }
            }
        }
        catch (Exception e){
            Context.revert(TAG + ": Error swapping tokens. " + e);
        }
    }

    @External(readonly = true)
    public Map<String, Integer> get_swap_state_status(){
        return Map.of("state", swapState.get(), "count", swapCount.get());
    }

    @External
    public void reset_swap_state(){
        Address cps_score_address = cpsScore.get();
        Context.require((Boolean) Context.call(cps_score_address, "is_admin", Context.getCaller()) || Context.getCaller().equals(cps_score_address), TAG + ": Only admin can call this method.");
        swapState.set(0);
    }

    @External
    public void tokenFallback(Address _from, BigInteger _value, byte[] _data){
        String unpacked_data = new String(_data);
        Address bnUSD = balancedDollar.get();
        Address staking = stakingScore.get();

        Address caller = Context.getCaller();
        Context.require(caller.equals(bnUSD) || caller.equals(staking), TAG + " Only " + bnUSD + " and " + staking + " can send tokens to CPF Treasury.");
        if (caller.equals(staking)){
            if (_from.equals(dexScore.get())){
                String string_data = "" + "{\"method\":\"_swap_icx\"}";
                _data = string_data.getBytes();
                Context.call(dexScore.get(), "transfer", _value, _data);
            }
        }
        else{
            JsonObject json = Json.parse(unpacked_data).asObject();
            if (_from.equals(cpsScore.get())){
                if (json.get("method").asString().equals("return_fund_amount")){
                    Address _sponsor_address = Address.fromString(json.get("params").asObject().get("sponsor_address").asString());
                    return_fund_amount(_sponsor_address, _from, consts.bnUSD, _value);
                }
                else if (json.get("method").asString().equals("burn_amount")){
                    _swap_tokens(caller, staking, _value);
                }
                else{
                    Context.revert(TAG + " Not supported method " + json.get("method"));
                }
            }
            if (_from.equals(cpsTreasuryScore.get())){
                if (json.get("method").asString().equals("disqualify_project")){
                    String ipfs_key = json.get("params").asObject().get("ipfs_key").asString();
                    disqualify_proposal_fund(ipfs_key, _value, consts.bnUSD, _from);
                }
                else{
                    Context.revert(TAG + " Not supported method " + json.get("method"));
                }
            }
        }
    }

    @Payable
    public void fallback(){
        Context.require(Context.getCaller().equals(dexScore.get()), TAG + ": Please send fund using add_fund method.");
        _burn(Context.getValue());
    }

    @EventLog(indexed = 1)
    public void FundReturned(Address _sponsor_address, String note){}

    @EventLog(indexed = 1)
    public void ProposalFundTransferred(String _ipfs_key, String note){}

    @EventLog(indexed = 1)
    public void ProposalDisqualified(String _ipfs_key, String note){}

    @EventLog(indexed = 1)
    public void FundReceived(Address _sponsor_address, String note){}


    }
