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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.iconloop.score.example.utils.consts;

public class CPSTreasury extends ProposalData{
    private final String name;
    private final String symbol;
    private static final String TAG = "CPS_Treasury";
    private static final String PROPOSAL_DB_PREFIX = "proposal";

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

    private String proposal_prefix(String _proposal_key){
        return PROPOSAL_DB_PREFIX + "|" + id.get() + "|" + _proposal_key;
    }

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

    private Map<String, ?> _get_projects(String _proposal_key){
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
    public Map<String, ?> get_contributor_projected_fund(Address _wallet_address){
        ProposalData proposalData = new ProposalData();
        BigInteger totalAmountToBePaidICX = BigInteger.ZERO;
        BigInteger totalAmountToBePaidbnUSD = BigInteger.ZERO;
        List<Map<String, String>> projectDetails = new ArrayList<>();
        for (int i = 0; i < proposalsKeys.size(); i++){
            String _ipfs_key = proposalsKeys.get(i);
            Map<String, ?> proposal_details = proposalData.getDataFromProposalDB(_ipfs_key);
            if (proposal_details.get(consts.STATUS).equals(DISQUALIFIED)){
                if (proposal_details.get(consts.SPONSOR_ADDRESS).equals(_wallet_address.toString())){
                    int totalInstallment = (int) proposal_details.get(consts.PROJECT_DURATION);
                    int totalPaidCount = (int) proposal_details.get(consts.INSTALLMENT_COUNT);
                    if (totalPaidCount < totalInstallment){
                        String flag = (String) proposal_details.get(consts.TOKEN);
                        BigInteger totalBudget = (BigInteger) proposal_details.get(consts.TOTAL_BUDGET);
                        BigInteger totalPaidAmount = (BigInteger) proposal_details.get(consts.WITHDRAW_AMOUNT);

                        Map<String, String> project_details = Map.of(
                                consts.IPFS_HASH, _ipfs_key,
                                consts.TOKEN, flag,
                                consts.TOTAL_BUDGET, totalBudget.toString(),
                                consts.TOTAL_INSTALLMENT_PAID, totalPaidAmount.toString(),
                                consts.TOTAL_INSTALLMENT_COUNT, String.valueOf(totalInstallment),
                                consts.TOTAL_TIMES_INSTALLMENT_PAID, String.valueOf(totalPaidCount),
                                consts.INSTALLMENT_AMOUNT, totalBudget.divide(BigInteger.valueOf(totalInstallment)).toString());

                        projectDetails.add(project_details);
                        if (flag.equals(consts.ICX)){
                            totalAmountToBePaidICX = totalAmountToBePaidICX.add(totalBudget.divide(BigInteger.valueOf(totalInstallment)));
                        }
                        else {
                            totalAmountToBePaidbnUSD = totalAmountToBePaidbnUSD.add(totalBudget.divide(BigInteger.valueOf(totalInstallment)));
                        }
                    }
                }
            }
        }
        return Map.of(
                "data", projectDetails,
                "project_count", projectDetails.size(),
                "total_amount", Map.of("ICX", totalAmountToBePaidICX, "bnUSD", totalAmountToBePaidbnUSD),
                "withdraw_amount_icx", installmentFundRecord.at(_wallet_address.toString()).getOrDefault(consts.ICX, BigInteger.ZERO),
                "withdraw_amount_bnUSD", installmentFundRecord.at(_wallet_address.toString()).getOrDefault(consts.bnUSD, BigInteger.ZERO));
    }


    @External(readonly = true)
    public Map<String, ?> get_sponsor_projected_fund(Address _wallet_address){
        ProposalData proposalData = new ProposalData();
        BigInteger totalAmountToBePaidICX = BigInteger.ZERO;
        BigInteger totalAmountToBePaidbnUSD = BigInteger.ZERO;
        BigInteger totalSponsorBondICX = BigInteger.ZERO;
        BigInteger totalSponsorBondbnUSD = BigInteger.ZERO;
        List<Map<String, String>> projectDetails = new ArrayList<>();
        for (int i = 0; i < proposalsKeys.size(); i++){
            String _ipfs_key = proposalsKeys.get(i);
            Map<String, ?> proposal_details = proposalData.getDataFromProposalDB(_ipfs_key);
            if (proposal_details.get(consts.STATUS).equals(DISQUALIFIED)){
                if (proposal_details.get(consts.SPONSOR_ADDRESS).equals(_wallet_address.toString())){
                    int totalInstallment = (int) proposal_details.get(consts.PROJECT_DURATION);
                    int totalPaidCount = (int) proposal_details.get(consts.INSTALLMENT_COUNT);
                    if (totalPaidCount < totalInstallment){
                        String flag = (String) proposal_details.get(consts.TOKEN);
                        BigInteger totalBudget = (BigInteger) proposal_details.get(consts.TOTAL_BUDGET);
                        BigInteger totalPaidAmount = (BigInteger) proposal_details.get(consts.WITHDRAW_AMOUNT);
                        BigInteger depositedSponsorBond = (BigInteger) proposal_details.get(consts.TOTAL_BUDGET);

                        Map<String, String> project_details = Map.of(
                                consts.IPFS_HASH, _ipfs_key,
                                consts.TOKEN, flag,
                                consts.TOTAL_BUDGET, totalBudget.toString(),
                                consts.TOTAL_INSTALLMENT_PAID, totalPaidAmount.toString(),
                                consts.TOTAL_INSTALLMENT_COUNT, String.valueOf(totalInstallment),
                                consts.TOTAL_TIMES_INSTALLMENT_PAID, String.valueOf(totalPaidCount),
                                consts.INSTALLMENT_AMOUNT, totalBudget.divide(BigInteger.valueOf(totalInstallment)).toString(),
                                consts.SPONSOR_BOND_AMOUNT, depositedSponsorBond.toString());

                        projectDetails.add(project_details);
                        if (flag.equals(consts.ICX)){
                            totalAmountToBePaidICX = totalAmountToBePaidICX.add(totalBudget.divide(BigInteger.valueOf(totalInstallment)));
                            totalSponsorBondICX = totalSponsorBondICX.add(depositedSponsorBond);
                        }
                        else {
                            totalAmountToBePaidbnUSD = totalAmountToBePaidbnUSD.add(totalBudget.divide(BigInteger.valueOf(totalInstallment)));
                            totalSponsorBondbnUSD = totalSponsorBondbnUSD.add(depositedSponsorBond);
                        }
                    }
                }
            }
        }
        return Map.of(
                "data", projectDetails,
                "project_count", projectDetails.size(),
                "total_amount", Map.of("ICX", totalAmountToBePaidICX, "bnUSD", totalAmountToBePaidbnUSD),
                "withdraw_amount_icx", installmentFundRecord.at(_wallet_address.toString()).getOrDefault(consts.ICX, BigInteger.ZERO),
                "withdraw_amount_bnUSD", installmentFundRecord.at(_wallet_address.toString()).getOrDefault(consts.bnUSD, BigInteger.ZERO),
                "total_sponsor_bond", Map.of("ICX", totalSponsorBondICX, "bnUSD", totalSponsorBondbnUSD)
        );
    }

    private void _deposit_proposal_fund(ProposalData.ProposalAttributes _proposals, BigInteger _value){
        _add_record(_proposals);
        ProposalFundDeposited(_proposals.ipfs_hash, "Received " + _proposals.ipfs_hash + " " + _value + " " +
                consts.bnUSD + " fund from CPF");
    }

    @External
    @Payable
    public void update_proposal_fund(String _ipfs_key, BigInteger _added_budget, BigInteger _added_sponsor_reward,
                                     int _added_installment_count){
        ProposalData proposalData = new ProposalData();
        Context.require(_proposal_exists(_ipfs_key), TAG + ": Invalid IPFS hash.");
        String proposalPrefix = proposal_prefix(_ipfs_key);
        Map<String, ?> proposalDetails = proposalData.getDataFromProposalDB(proposalPrefix);
        BigInteger totalBudget = (BigInteger) proposalDetails.get(consts.TOTAL_BUDGET);
        BigInteger sponsorReward = (BigInteger) proposalDetails.get(consts.SPONSORS_REWARDS);
        int totalDuration = (int) proposalDetails.get(consts.PROJECT_DURATION);
        BigInteger remainingAmount = (BigInteger) proposalDetails.get(consts.REMAINING_AMOUNT);
        BigInteger sponsorRemainingAmount = (BigInteger) proposalDetails.get(consts.SPONSOR_REMAINING_AMOUNT);
        int installmentCount = (int) proposalDetails.get(consts.INSTALLMENT_COUNT);
        int sponsorRewardCount = (int) proposalDetails.get(consts.SPONSOR_REWARD_COUNT);
        String flag = (String) proposalDetails.get(consts.TOKEN);

        ProposalData.totalBudget.at(proposalPrefix).set(totalBudget.add(_added_budget));
        ProposalData.sponsorReward.at(proposalPrefix).set(sponsorReward.add(_added_sponsor_reward));
        ProposalData.projectDuration.at(proposalPrefix).set(totalDuration + _added_installment_count);
        ProposalData.remainingAmount.at(proposalPrefix).set(remainingAmount.add(_added_budget));
        ProposalData.sponsorRemainingAmount.at(proposalPrefix).set(sponsorRemainingAmount.add(_added_sponsor_reward));
        ProposalData.installmentCount.at(proposalPrefix).set(installmentCount + _added_installment_count);
        ProposalData.sponsorRewardCount.at(proposalPrefix).set(sponsorRewardCount + _added_installment_count);

        ProposalFundDeposited(_ipfs_key, _ipfs_key + ": Added Budget: " + _added_budget + " " +
                flag + "and Added time: " + _added_installment_count + " Successfully");
    }

    @External
    public void send_installment_to_contributor(String _ipfs_key){
        _validate_cps_score();
        Context.require(_proposal_exists(_ipfs_key), TAG + ": Invalid IPFS Hash.");
        BigInteger installmentAmount = BigInteger.ZERO;
        ProposalData proposalData = new ProposalData();
        String prefix = proposal_prefix(_ipfs_key);

        int installmentCount = ProposalData.installmentCount.at(prefix).getOrDefault(0);
        BigInteger withdrawAmount = ProposalData.withdrawAmount.at(prefix).getOrDefault(BigInteger.ZERO);
        BigInteger remainingAmount = ProposalData.remainingAmount.at(prefix).getOrDefault(BigInteger.ZERO);
        Address contributorAddress = ProposalData.contributorAddress.at(prefix).get();
        String flag = ProposalData.token.at(prefix).get();

        try {
            if (installmentCount == 1) {
                installmentAmount = remainingAmount;
            } else {
                installmentAmount = remainingAmount.divide(BigInteger.valueOf(installmentCount));
            }
            int newInstallmentCount = installmentCount - 1;
            ProposalData.installmentCount.at(prefix).set(newInstallmentCount);
            ProposalData.remainingAmount.at(prefix).set(remainingAmount.subtract(installmentAmount));
            ProposalData.withdrawAmount.at(prefix).set(withdrawAmount.add(installmentAmount));
            installmentFundRecord.at(contributorAddress.toString()).set(flag,
                    installmentFundRecord.at(contributorAddress.toString()).get(flag).add(installmentAmount));
            ProposalFundSent(contributorAddress, "new installment " + installmentAmount + " " + flag + " sent to contributors address.");

            if (newInstallmentCount == 0){
                ProposalData.status.at(prefix).set(COMPLETED);
            }
        }
        catch (Exception e){
            Context.revert(TAG + ": Network problem. Sending project funds to contributor. " + e);
        }
    }

    @External
    public void send_reward_to_sponsor(String _ipfs_key){
        _validate_cps_score();

        Context.require(_proposal_exists(_ipfs_key), TAG + ": Invalid IPFS Hash.");
        BigInteger installmentAmount = BigInteger.ZERO;
        String prefix = proposal_prefix(_ipfs_key);

        int sponsorRewardCount = ProposalData.sponsorRewardCount.at(prefix).getOrDefault(0);
        BigInteger sponsorWithdrawAmount = ProposalData.sponsorWithdrawAmount.at(prefix).getOrDefault(BigInteger.ZERO);
        BigInteger sponsorRemainingAmount = ProposalData.sponsorRemainingAmount.at(prefix).getOrDefault(BigInteger.ZERO);
        Address sponsorAddress = ProposalData.sponsorAddress.at(prefix).get();
        String flag = ProposalData.token.at(prefix).get();

        try {
            if (sponsorRewardCount == 1) {
                installmentAmount = sponsorRemainingAmount;
            } else {
                installmentAmount = sponsorRemainingAmount.divide(BigInteger.valueOf(sponsorRewardCount));
            }
            int newSponsorRewardCount = sponsorRewardCount - 1;
            ProposalData.sponsorRewardCount.at(prefix).set(newSponsorRewardCount);
            ProposalData.sponsorWithdrawAmount.at(prefix).set(sponsorWithdrawAmount.add(installmentAmount));
            ProposalData.sponsorRemainingAmount.at(prefix).set(sponsorRemainingAmount.subtract(installmentAmount));
            installmentFundRecord.at(sponsorAddress.toString()).set(flag, installmentFundRecord.at(sponsorAddress.toString()).get(flag).add(installmentAmount));
            ProposalFundSent(sponsorAddress, "New installment " + installmentAmount + " " +
                    flag + " sent to sponsor address.");
        }
        catch (Exception e){
            Context.revert(TAG + ": Network problem. Sending project funds to sponsor.");
        }
    }

    @External
    public void disqualify_project(String _ipfs_key){

    }

    @EventLog(indexed = 1)
    public void FundReturned(Address _sponsor_address, String note){}

    @EventLog(indexed = 1)
    public void ProposalFundTransferred(String _ipfs_key, String note){}

    @EventLog(indexed = 1)
    public void ProposalDisqualified(String _ipfs_key, String note){}

    @EventLog(indexed = 1)
    public void FundReceived(Address _sponsor_address, String note){}

    @EventLog(indexed = 1)
    public void ProposalFundDeposited(String _ipfs_key, String note){}

    @EventLog(indexed = 1)
    public void ProposalFundSent(Address _receiver_address, String note){}


}
