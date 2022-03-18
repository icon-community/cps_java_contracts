package com.iconloop.score.example.db;

import score.BranchDB;
import score.Context;
import score.DictDB;
import score.VarDB;
import com.iconloop.score.example.utils.consts;

import java.math.BigInteger;
import java.util.Locale;
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
    private static final BranchDB<String, DictDB<String, String>> proposalsData = Context.newBranchDB("proposals_data", String.class);

    public void addDataToProposalDB(ProposalAttributes _proposals, String prefix){
        proposalsData.at(prefix).set(consts.IPFS_HASH, _proposals.ipfs_hash);
        proposalsData.at(prefix).set(consts.TOTAL_BUDGET, _proposals.total_budget.toString());
        proposalsData.at(prefix).set(consts.SPONSORS_REWARDS, _proposals.sponsor_reward.toString());
        proposalsData.at(prefix).set(consts.PROJECT_DURATION, String.valueOf(_proposals.project_duration));
        proposalsData.at(prefix).set(consts.SPONSOR_ADDRESS, _proposals.sponsor_address);
        proposalsData.at(prefix).set(consts.CONTRIBUTOR_ADDRESS, _proposals.contributor_address);
        proposalsData.at(prefix).set(consts.WITHDRAW_AMOUNT, "0");
        proposalsData.at(prefix).set(consts.SPONSOR_WITHDRAW_AMOUNT, "0");
        proposalsData.at(prefix).set(consts.REMAINING_AMOUNT, _proposals.total_budget.toString());
        proposalsData.at(prefix).set(consts.SPONSOR_REMAINING_AMOUNT, _proposals.sponsor_reward.toString());
        proposalsData.at(prefix).set(consts.INSTALLMENT_COUNT, String.valueOf(_proposals.project_duration));
        proposalsData.at(prefix).set(consts.SPONSOR_REWARD_COUNT, String.valueOf(_proposals.project_duration));
        proposalsData.at(prefix).set(consts.TOKEN, _proposals.token);
    }

    public Map<String, String> getDataFromProposalDB(String prefix){
        return Map.ofEntries(
                Map.entry(consts.IPFS_HASH, proposalsData.at(prefix).getOrDefault(consts.IPFS_HASH, "")),
                Map.entry(consts.TOTAL_BUDGET, proposalsData.at(prefix).getOrDefault(consts.TOTAL_BUDGET, "")),
                Map.entry(consts.SPONSORS_REWARDS, proposalsData.at(prefix).getOrDefault(consts.SPONSORS_REWARDS, "")),
                Map.entry(consts.PROJECT_DURATION, proposalsData.at(prefix).getOrDefault(consts.PROJECT_DURATION, "")),
                Map.entry(consts.SPONSOR_ADDRESS, proposalsData.at(prefix).getOrDefault(consts.SPONSOR_ADDRESS, "")),
                Map.entry(consts.CONTRIBUTOR_ADDRESS, proposalsData.at(prefix).getOrDefault(consts.CONTRIBUTOR_ADDRESS, "")),
                Map.entry(consts.WITHDRAW_AMOUNT, proposalsData.at(prefix).getOrDefault(consts.WITHDRAW_AMOUNT, "")),
                Map.entry(consts.INSTALLMENT_COUNT, proposalsData.at(prefix).getOrDefault(consts.INSTALLMENT_COUNT, "")),
                Map.entry(consts.SPONSOR_REWARD_COUNT, proposalsData.at(prefix).getOrDefault(consts.SPONSOR_REWARD_COUNT, "")),
                Map.entry(consts.SPONSOR_WITHDRAW_AMOUNT, proposalsData.at(prefix).getOrDefault(consts.SPONSOR_WITHDRAW_AMOUNT, "")),
                Map.entry(consts.REMAINING_AMOUNT, proposalsData.at(prefix).getOrDefault(consts.REMAINING_AMOUNT, "")),
                Map.entry(consts.SPONSOR_REMAINING_AMOUNT, proposalsData.at(prefix).getOrDefault(consts.SPONSOR_REMAINING_AMOUNT, "")),
                Map.entry(consts.TOKEN, proposalsData.at(prefix).getOrDefault(consts.TOKEN, ""))
        );
    }

    public String getProposalAttributesDetails(String prefix, String attribute){
        return proposalsData.at(prefix).getOrDefault(attribute, "");
    }

}
