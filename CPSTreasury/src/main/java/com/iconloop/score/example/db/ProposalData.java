package com.iconloop.score.example.db;

import score.*;
import com.iconloop.score.example.utils.consts;

import java.math.BigInteger;
import java.util.Map;

public class ProposalData {
    public static class ProposalAttributes{
        public String ipfs_hash;
        public int project_duration;
        public BigInteger total_budget;
        public BigInteger sponsor_reward;
        public String token;
        public String contributor_address;
        public String sponsor_address;
    }
    private static final BranchDB<String, VarDB<String>> ipfsHash = Context.newBranchDB(consts.IPFS_HASH, String.class);
    private static final BranchDB<String, VarDB<BigInteger>> totalBudget = Context.newBranchDB(consts.TOTAL_BUDGET, BigInteger.class);
    private static final BranchDB<String, VarDB<BigInteger>> sponsorReward = Context.newBranchDB(consts.SPONSORS_REWARDS, BigInteger.class);
    private static final BranchDB<String, VarDB<Integer>> projectDuration = Context.newBranchDB(consts.PROJECT_DURATION, Integer.class);
    private static final BranchDB<String, VarDB<Address>> sponsorAddress = Context.newBranchDB(consts.SPONSOR_ADDRESS, Address.class);
    private static final BranchDB<String, VarDB<Address>> contributorAddress = Context.newBranchDB(consts.CONTRIBUTOR_ADDRESS, Address.class);
    private static final BranchDB<String, VarDB<String>> token = Context.newBranchDB(consts.TOKEN, String.class);
    private static final BranchDB<String, VarDB<BigInteger>> withdrawAmount = Context.newBranchDB(consts.WITHDRAW_AMOUNT, BigInteger.class);
    private static final BranchDB<String, VarDB<BigInteger>> sponsorWithdrawAmount = Context.newBranchDB(consts.SPONSOR_WITHDRAW_AMOUNT, BigInteger.class);
    private static final BranchDB<String, VarDB<BigInteger>> remainingAmount = Context.newBranchDB(consts.REMAINING_AMOUNT, BigInteger.class);
    private static final BranchDB<String, VarDB<BigInteger>> sponsorRemainingAmount = Context.newBranchDB(consts.SPONSOR_REMAINING_AMOUNT, BigInteger.class);
    private static final BranchDB<String, VarDB<Integer>> installmentCount = Context.newBranchDB(consts.INSTALLMENT_COUNT, Integer.class);
    private static final BranchDB<String, VarDB<Integer>> sponsorRewardCount = Context.newBranchDB(consts.SPONSOR_REWARD_COUNT, Integer.class);
    private static final BranchDB<String, VarDB<String>> status = Context.newBranchDB(consts.STATUS, String.class);




    public void addDataToProposalDB(ProposalAttributes _proposals, String prefix){
        ipfsHash.at(prefix).set(_proposals.ipfs_hash);
        totalBudget.at(prefix).set(_proposals.total_budget);
        sponsorReward.at(prefix).set(_proposals.sponsor_reward);
        projectDuration.at(prefix).set(_proposals.project_duration);
        sponsorAddress.at(prefix).set(Address.fromString(_proposals.sponsor_address));
        contributorAddress.at(prefix).set(Address.fromString(_proposals.contributor_address));
        withdrawAmount.at(prefix).set(BigInteger.ZERO);
        sponsorWithdrawAmount.at(prefix).set(BigInteger.ZERO);
        remainingAmount.at(prefix).set(_proposals.total_budget);
        sponsorRemainingAmount.at(prefix).set(_proposals.sponsor_reward);
        installmentCount.at(prefix).set(_proposals.project_duration);
        sponsorRewardCount.at(prefix).set(_proposals.project_duration);
        status.at(prefix).set(_proposals.token);
    }

    public Map<String, ?> getDataFromProposalDB(String prefix){
        return Map.ofEntries(
                Map.entry(consts.IPFS_HASH, ipfsHash.at(prefix).getOrDefault("")),
                Map.entry(consts.TOTAL_BUDGET, totalBudget.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(consts.SPONSORS_REWARDS, sponsorReward.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(consts.PROJECT_DURATION, projectDuration.at(prefix).getOrDefault(0)),
                Map.entry(consts.SPONSOR_ADDRESS, sponsorAddress.at(prefix).get().toString()),
                Map.entry(consts.CONTRIBUTOR_ADDRESS, contributorAddress.at(prefix).get().toString()),
                Map.entry(consts.WITHDRAW_AMOUNT, withdrawAmount.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(consts.INSTALLMENT_COUNT, installmentCount.at(prefix).getOrDefault(0)),
                Map.entry(consts.SPONSOR_REWARD_COUNT, sponsorRewardCount.at(prefix).getOrDefault(0)),
                Map.entry(consts.SPONSOR_WITHDRAW_AMOUNT, sponsorWithdrawAmount.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(consts.REMAINING_AMOUNT, remainingAmount.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(consts.SPONSOR_REMAINING_AMOUNT, sponsorRemainingAmount.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(consts.TOKEN, token.at(prefix).getOrDefault(""))
        );
    }
}
