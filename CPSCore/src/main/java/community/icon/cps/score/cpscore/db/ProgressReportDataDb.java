package community.icon.cps.score.cpscore.db;

import score.*;
import scorex.util.ArrayList;

import java.math.BigInteger;

import java.util.List;
import java.util.Map;

import static community.icon.cps.score.cpscore.utils.ArrayDBUtils.recordTxHash;
import static community.icon.cps.score.cpscore.utils.Constants.*;
import static community.icon.cps.score.lib.interfaces.CPSCoreInterface.ProgressReportAttributes;

public class ProgressReportDataDb {
    public static final BranchDB<String, VarDB<String>> ipfsHash = Context.newBranchDB(IPFS_HASH, String.class);
    private static final BranchDB<String, VarDB<String>> reportHash = Context.newBranchDB(REPORT_HASH, String.class);
    private static final BranchDB<String, VarDB<String>> progressReportTitle = Context.newBranchDB(PROGRESS_REPORT_TITLE, String.class);
    public static final BranchDB<String, VarDB<BigInteger>> timestamp = Context.newBranchDB(TIMESTAMP, BigInteger.class);
    public static final BranchDB<String, VarDB<String>> status = Context.newBranchDB(STATUS, String.class);
    private static final BranchDB<String, VarDB<String>> txHash = Context.newBranchDB(TX_HASH, String.class);
    private static final BranchDB<String, VarDB<Boolean>> budgetAdjustment = Context.newBranchDB(BUDGET_ADJUSTMENT, Boolean.class);

    private static final BranchDB<String, VarDB<BigInteger>> additionalBudget = Context.newBranchDB(ADDITIONAL_BUDGET, BigInteger.class);
    private static final BranchDB<String, VarDB<Integer>> additionalMonth = Context.newBranchDB(ADDITIONAL_DURATION, Integer.class);

    public static final BranchDB<String, ArrayDB<String>> votersReasons = Context.newBranchDB(VOTERS_REASON, String.class);
    public static final BranchDB<String, VarDB<BigInteger>> totalVotes = Context.newBranchDB(TOTAL_VOTES, BigInteger.class);
    public static final BranchDB<String, VarDB<BigInteger>> approvedVotes = Context.newBranchDB(APPROVED_VOTES, BigInteger.class);
    public static final BranchDB<String, VarDB<BigInteger>> rejectedVotes = Context.newBranchDB(REJECTED_VOTES, BigInteger.class);
    public static final BranchDB<String, ArrayDB<Address>> votersList = Context.newBranchDB(VOTERS_LIST, Address.class);
    public static final BranchDB<String, ArrayDB<Address>> approveVoters = Context.newBranchDB(APPROVE_VOTERS, Address.class);
    public static final BranchDB<String, ArrayDB<Address>> rejectVoters = Context.newBranchDB(REJECT_VOTERS, Address.class);
    public static final BranchDB<String, VarDB<Integer>> totalVoters = Context.newBranchDB(TOTAL_VOTERS, Integer.class);

    public static final BranchDB<String, BranchDB<Address, DictDB<String, Integer>>> votersListIndices = Context.newBranchDB(VOTERS_LIST_INDEXES, Integer.class);
    public static final BranchDB<String, VarDB<BigInteger>> budgetApprovedVotes = Context.newBranchDB(BUDGET_APPROVED_VOTES, BigInteger.class);
    public static final BranchDB<String, VarDB<BigInteger>> budgetRejectedVotes = Context.newBranchDB(BUDGET_REJECTED_VOTES, BigInteger.class);
    public static final BranchDB<String, ArrayDB<Address>> budgetApproveVoters = Context.newBranchDB(BUDGET_APPROVE_VOTERS, Address.class);
    public static final BranchDB<String, ArrayDB<Address>> budgetRejectVoters = Context.newBranchDB(BUDGET_REJECT_VOTERS, Address.class);
    public static final BranchDB<String, VarDB<String>> budgetAdjustmentStatus = Context.newBranchDB(BUDGET_ADJUSTMENT_STATUS, String.class);
    public static final BranchDB<String, VarDB<String>> ipfsLink = Context.newBranchDB(IPFS_LINK, String.class);
    public static final BranchDB<String, BranchDB<Address, DictDB<String, Integer>>> budgetVotersListIndices = Context.newBranchDB(BUDGET_VOTERS_LIST_INDICES, Integer.class);
    public static final BranchDB<String,ArrayDB<Integer>> milestoneSubmitted = Context.newBranchDB(MILESTONE_SUBMITTED_COUNT,Integer.class);

    public static void addDataToProgressReportDB(ProgressReportAttributes progressData, String prefix) {
        ipfsHash.at(prefix).set(progressData.ipfs_hash);
        reportHash.at(prefix).set(progressData.report_hash);
        progressReportTitle.at(prefix).set(progressData.progress_report_title);
        timestamp.at(prefix).set(BigInteger.valueOf(Context.getBlockTimestamp()));
        additionalBudget.at(prefix).set(progressData.additional_budget.multiply(EXA));
        additionalMonth.at(prefix).set(progressData.additional_month);
        status.at(prefix).set(WAITING);
        txHash.at(prefix).set(recordTxHash(Context.getTransactionHash()));
        budgetAdjustment.at(prefix).set(progressData.budget_adjustment);
        budgetAdjustmentStatus.at(prefix).set("N/A");
        totalVotes.at(prefix).set(BigInteger.ZERO);
        totalVoters.at(prefix).set(0);
        approvedVotes.at(prefix).set(BigInteger.ZERO);
        rejectedVotes.at(prefix).set(BigInteger.ZERO);
        budgetApprovedVotes.at(prefix).set(BigInteger.ZERO);
        budgetRejectedVotes.at(prefix).set(BigInteger.ZERO);
        ipfsLink.at(prefix).set(progressData.ipfs_link);
    }

    public static Map<String, Object> getDataFromProgressReportDB(String prefix) {
        String proposalHash = ipfsHash.at(prefix).getOrDefault("");
        return Map.ofEntries(Map.entry(IPFS_HASH, proposalHash),
                Map.entry(REPORT_HASH, reportHash.at(prefix).getOrDefault("")),
                Map.entry(PROGRESS_REPORT_TITLE, progressReportTitle.at(prefix).getOrDefault("")),
                Map.entry(TIMESTAMP, timestamp.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(ADDITIONAL_BUDGET, additionalBudget.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(ADDITIONAL_DURATION, additionalMonth.at(prefix).getOrDefault(0)),
                Map.entry(STATUS, status.at(prefix).getOrDefault("")),
                Map.entry(TX_HASH, txHash.at(prefix).getOrDefault("")),
                Map.entry(IPFS_LINK, ipfsLink.at(prefix).getOrDefault("")),
                Map.entry(BUDGET_ADJUSTMENT, budgetAdjustment.at(prefix).getOrDefault(false)),
                Map.entry(PROJECT_TITLE, ProposalDataDb.projectTitle.at(proposalPrefix(proposalHash)).getOrDefault("")),
                Map.entry(MILESTONE_SUBMITTED_COUNT, milestoneSubmitted.at(prefix).size()),
                Map.entry(CONTRIBUTOR_ADDRESS, ProposalDataDb.contributorAddress.at(proposalPrefix(proposalHash)).get()));
    }
    public static List<Integer> getMilestoneSubmittedFromProgressReportDB(String prefix){
        ArrayDB<Integer> milestoneSize = milestoneSubmitted.at(prefix);
        List<Integer> m = new ArrayList<>();
        for (int i = 0; i < milestoneSize.size(); i++) {
            m.add(milestoneSize.get(i));
        }
        return m;
    }

    public static Map<String, Object> getVoteResultsFromProgressReportDB(String prefix) {
        return Map.ofEntries(Map.entry(TOTAL_VOTES, totalVotes.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(APPROVED_VOTES, approvedVotes.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(REJECTED_VOTES, rejectedVotes.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(TOTAL_VOTERS, totalVoters.at(prefix).getOrDefault(0)),
                Map.entry(APPROVE_VOTERS, approveVoters.at(prefix).size()),
                Map.entry(REJECT_VOTERS, rejectVoters.at(prefix).size()));

    }

    public static Map<String, Object> getBudgetAdjustmentVoteResultsFromProgressReportDB(String prefix) {
        return Map.ofEntries(Map.entry(BUDGET_APPROVED_VOTES, budgetApprovedVotes.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(BUDGET_REJECTED_VOTES, budgetRejectedVotes.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(BUDGET_APPROVE_VOTERS, budgetApproveVoters.at(prefix).size()),
                Map.entry(BUDGET_REJECT_VOTERS, budgetRejectVoters.at(prefix).size()),
                Map.entry(BUDGET_ADJUSTMENT_STATUS, budgetAdjustmentStatus.at(prefix).getOrDefault("")));

    }

    public static String proposalPrefix(String proposalKey) {
        return PROPOSAL_DB_PREFIX + "|" + "|" + proposalKey;
    }
}