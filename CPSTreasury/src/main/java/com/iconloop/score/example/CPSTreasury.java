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

import com.iconloop.score.example.db.ProposalData;
import score.*;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;
import scorex.util.ArrayList;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import com.iconloop.score.example.utils.consts;

public class CPSTreasury {
    private final String name;
    private final String symbol;
    private static final String TAG = "CPS_Treasury";
//    private static final byte[] PROPOSAL_DB_PREFIX = "proposal".getBytes();

    private static final String ID = "id";
    private static final String PROPOSALS_KEYS = "_proposals_keys";
    private static final String PROPOSALS_KEY_LIST_INDEX = "proposals_key_list_index";
    private static final String FUND_RECORD = "fund_record";
    private static final String INSTALLMENT_FUND_RECORD = "installment_fund_record";

    private static final String TOTAL_INSTALLMENT_COUNT = "_total_installment_count";
    private static final String TOTAL_TIMES_INSTALLMENT_PAID = "_total_times_installment_paid";
    private static final String TOTAL_TIMES_REWARD_PAID = "_total_times_reward_paid";
    private static final String TOTAL_INSTALLMENT_PAID = "_total_installment_paid";
    private static final String TOTAL_REWARD_PAID = "_total_reward_paid";
    private static final String INSTALLMENT_AMOUNT = "installment_amount";
    private static final String SPONSOR_BOND_AMOUNT = "sponsor_bond_amount";
    private static final String CPS_SCORE = "_cps_score";
    private static final String CPF_TREASURY_SCORE = "_cpf_treasury_score";
    private static final String BALANCED_DOLLAR = "balanced_dollar";

    private static final String SPONSOR_ADDRESS = "sponsor_address";
    private static final String CONTRIBUTOR_ADDRESS = "contributor_address";
    private static final String STATUS = "status";
    private static final String IPFS_HASH = "ipfs_hash";
    private static final String SPONSOR_REWARD = "sponsor_reward";
    private static final String TOTAL_BUDGET = "total_budget";

    private static final String ACTIVE = "active";
    private static final String DISQUALIFIED = "disqualified";
    private static final String COMPLETED = "completed";


    private static final VarDB<String> id = Context.newVarDB(ID, String.class);
    private static final ArrayDB<String> proposalsKeys = Context.newArrayDB(PROPOSALS_KEYS, String.class);
    private static final DictDB<String, Integer> proposalsKeyListIndex = Context.newDictDB(PROPOSALS_KEY_LIST_INDEX, Integer.class);
    private static final DictDB<String, BigInteger> fundRecord = Context.newDictDB(FUND_RECORD, BigInteger.class);
    private static final BranchDB<String, DictDB<String, BigInteger>> installmentFundRecord = Context.newBranchDB(INSTALLMENT_FUND_RECORD, BigInteger.class);

    private static final VarDB<Address> cpfTreasuryScore = Context.newVarDB(CPF_TREASURY_SCORE, Address.class);
    private static final VarDB<Address> cpsScore = Context.newVarDB(CPS_SCORE, Address.class);
    private static final VarDB<Address> balancedDollar = Context.newVarDB(BALANCED_DOLLAR, Address.class);

    public  CPSTreasury(String name, String symbol){
        this.name=name;
        this.symbol=symbol;
    }

    @External(readonly = true)
    public String name(){
        return this.name;
    }

    @External(readonly = true)
    public String symbol(){
        return this.symbol;
    }

    @Payable
    public void fallback(){
        Context.revert(TAG + ": ICX can only be send by CPF Treasury Score");
    }

    private void set_id(String _val){
        id.set(_val);
    }

    private String get_id(){
        return id.get();
    }

//    private byte proposal_prefix(String _proposal_key){
//        return "m".getBytes();
//    }

    private Boolean _proposal_exists(String _ipfs_key){
        return proposalsKeyListIndex.getOrDefault(_ipfs_key, null) != null;
    }

    private void _validate_admins(){
        Context.require((Boolean) Context.call(cpsScore.get(), "is_admin", Context.getCaller()),
                TAG + ": Only admins can call this method");

    }

    private void _validate_owner(){
        Context.require(Context.getCaller().equals(Context.getOwner()),
                TAG + ": Only owner can call this method");
    }

    private void _validate_owner_score(Address _score){
        _validate_owner();
        Context.require(_score.isContract(), TAG + "Target " + _score + " is not a score.");
    }

    private void _validate_cps_score(){
        Context.require(Context.getCaller().equals(cpsScore.get()),
                TAG + ": Only CPS score " + cpsScore.get() + " can send fund using this method.");
    }

    private void _validate_cpf_treasury_score(){
        Context.require(Context.getCaller().equals(cpfTreasuryScore.get()),
                TAG + ": Only CPF Treasury score " + cpfTreasuryScore.get() + " can send fund using this method.");
    }

    private void _add_record(ProposalData.ProposalAttributes _proposal){
        ProposalData proposalData = new ProposalData();
        String ipfs_hash = _proposal.ipfs_hash;
        Context.require(!_proposal_exists(ipfs_hash), TAG + ": Already have this project");
        proposalsKeys.add(ipfs_hash);
        proposalData.addDataToProposalDB(_proposal, ipfs_hash);
        proposalsKeyListIndex.set(ipfs_hash, proposalsKeys.size() - 1);
    }

    private Map<String, String> _get_projects(String _proposal_key){
        ProposalData proposalData = new ProposalData();
        return proposalData.getDataFromProposalDB(_proposal_key);
    }

    @External
    public void set_cps_score(Address _score){
        _validate_owner_score(_score);
        cpsScore.set(_score);
    }

    @External(readonly = true)
    public Address get_cps_score(){
        return cpsScore.get();
    }

    @External
    public void set_cpf_treasury_score(Address _score){
        _validate_owner_score(_score);
        cpfTreasuryScore.set(_score);
    }

    @External(readonly = true)
    public Address get_cpf_treasury_score(){
        return cpfTreasuryScore.get();
    }

    @External
    public void set_bnUSD_score(Address _score){
        _validate_owner_score(_score);
        balancedDollar.set(_score);
    }

    @External
    public Address get_bnUSD_score(){
        return balancedDollar.get();
    }

    @External(readonly = true)
    public void get_contributor_projected_fund(Address _wallet_address){
        ProposalData proposalData = new ProposalData();
        BigInteger totalAmountToBePaidICX = BigInteger.ZERO;
        BigInteger totalAmountToBePaidbnUSD = BigInteger.ZERO;
        List<Map<String, String>> projectDetails = new ArrayList<>();
        for (int i = 0; i < proposalsKeys.size(); i++){
            String _ipfs_key = proposalsKeys.get(i);
            if (proposalData.getProposalAttributesDetails(_ipfs_key, consts.STATUS).equals(DISQUALIFIED)){
                if (proposalData.getProposalAttributesDetails(_ipfs_key, consts.CONTRIBUTOR_ADDRESS).equals(_wallet_address.toString())){
                    BigInteger totalInstallment = new BigInteger(proposalData.getProposalAttributesDetails(_ipfs_key, consts.PROJECT_DURATION));
                    BigInteger totalPaidCount = new BigInteger(proposalData.getProposalAttributesDetails(_ipfs_key, consts.INSTALLMENT_COUNT));
                    if (totalPaidCount.compareTo(totalInstallment) < 0){
                        String flag = proposalData.getProposalAttributesDetails(_ipfs_key, consts.TOKEN);
                        BigInteger totalBudget = new BigInteger(proposalData.getProposalAttributesDetails(_ipfs_key, consts.TOTAL_BUDGET));
                        BigInteger totalPaidAmount = new BigInteger(proposalData.getProposalAttributesDetails(_ipfs_key, consts.WITHDRAW_AMOUNT));
                        Map<String, String> project_details = Map.of(
                                consts.IPFS_HASH, _ipfs_key,
                                consts.TOTAL_BUDGET, totalBudget.toString(),
                                consts.TOTAL_INSTALLMENT_PAID, totalPaidAmount.toString(),
                                consts.TOTAL_TIMES_INSTALLMENT_PAID, totalPaidCount.toString(),
                                consts.INSTALLMENT_AMOUNT, totalBudget.divide(totalInstallment).toString());
                        projectDetails.add(project_details);
                        if (flag.equals(consts.ICX)){
                            totalAmountToBePaidICX = totalAmountToBePaidICX.add(totalBudget.divide(totalInstallment));
                        }
                        else {
                            totalAmountToBePaidbnUSD = totalAmountToBePaidbnUSD.add(totalBudget);
                        }
                    }
                }
            }
        }
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
