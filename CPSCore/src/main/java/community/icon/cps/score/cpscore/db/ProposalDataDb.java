package community.icon.cps.score.cpscore.db;

import score.*;

import java.math.BigInteger;
import java.util.Map;

import static community.icon.cps.score.cpscore.utils.ArrayDBUtils.recordTxHash;
import static community.icon.cps.score.cpscore.utils.Constants.*;
import static community.icon.cps.score.lib.interfaces.CPSCoreInterface.ProposalAttributes;

public class ProposalDataDb {
    public static final BranchDB<String, VarDB<String>> ipfsHash = Context.newBranchDB(IPFS_HASH, String.class);
    public static final BranchDB<String, VarDB<String>> projectTitle = Context.newBranchDB(PROJECT_TITLE, String.class);
    public static final BranchDB<String, VarDB<BigInteger>> timestamp = Context.newBranchDB(TIMESTAMP, BigInteger.class);
    public static final BranchDB<String, VarDB<BigInteger>> totalBudget = Context.newBranchDB(TOTAL_BUDGET, BigInteger.class);
    public static final BranchDB<String, VarDB<Integer>> projectDuration = Context.newBranchDB(PROJECT_DURATION, Integer.class);
    public static final BranchDB<String, VarDB<Integer>> approvedReports = Context.newBranchDB(APPROVED_REPORTS, Integer.class);
    public static final BranchDB<String, VarDB<Address>> sponsorAddress = Context.newBranchDB(SPONSOR_ADDRESS, Address.class);
    public static final BranchDB<String, VarDB<Address>> contributorAddress = Context.newBranchDB(CONTRIBUTOR_ADDRESS, Address.class);
    public static final BranchDB<String, VarDB<String>> token = Context.newBranchDB(TOKEN, String.class);
    public static final BranchDB<String, VarDB<String>> ipfsLink = Context.newBranchDB(IPFS_LINK, String.class);
    public static final BranchDB<String, VarDB<String>> status = Context.newBranchDB(STATUS, String.class);
    private static final BranchDB<String, VarDB<String>> txHash = Context.newBranchDB(TX_HASH, String.class);
    public static final BranchDB<String, VarDB<Integer>> percentageCompleted = Context.newBranchDB(PERCENTAGE_COMPLETED, Integer.class);
    public static final BranchDB<String, ArrayDB<String>> votersReasons = Context.newBranchDB(VOTERS_REASON, String.class);
    public static final BranchDB<String, VarDB<BigInteger>> totalVotes = Context.newBranchDB(TOTAL_VOTES, BigInteger.class);
    public static final BranchDB<String, VarDB<Integer>> totalVoters = Context.newBranchDB(TOTAL_VOTERS, Integer.class);
    public static final BranchDB<String, VarDB<BigInteger>> approvedVotes = Context.newBranchDB(APPROVED_VOTES, BigInteger.class);
    public static final BranchDB<String, VarDB<BigInteger>> rejectedVotes = Context.newBranchDB(REJECTED_VOTES, BigInteger.class);
    public static final BranchDB<String, VarDB<BigInteger>> abstainedVotes = Context.newBranchDB(ABSTAINED_VOTES, BigInteger.class);
    public static final BranchDB<String, VarDB<BigInteger>> sponsorDepositAmount = Context.newBranchDB(SPONSOR_DEPOSIT, BigInteger.class);
    public static final BranchDB<String, VarDB<BigInteger>> sponsoredTimestamp = Context.newBranchDB(SPONSORED_TIMESTAMP, BigInteger.class);
    public static final BranchDB<String, VarDB<String>> sponsorDepositStatus = Context.newBranchDB(SPONSOR_DEPOSIT_STATUS, String.class);
    public static final BranchDB<String, VarDB<String>> sponsorVoteReason = Context.newBranchDB(SPONSOR_VOTE_REASON, String.class);
    public static final BranchDB<String, ArrayDB<Address>> votersList = Context.newBranchDB(VOTERS_LIST, Address.class);
    public static final BranchDB<String, ArrayDB<Address>> approveVoters = Context.newBranchDB(APPROVE_VOTERS, Address.class);
    public static final BranchDB<String, ArrayDB<Address>> rejectVoters = Context.newBranchDB(REJECT_VOTERS, Address.class);
    public static final BranchDB<String, ArrayDB<Address>> abstainVoters = Context.newBranchDB(ABSTAIN_VOTERS, Address.class);
    public static final BranchDB<String, BranchDB<Address, DictDB<String, Integer>>> votersListIndex = Context.newBranchDB(VOTERS_LIST_INDEXES, Integer.class);
    public static final BranchDB<String, ArrayDB<String>> progressReports = Context.newBranchDB(PROGRESS_REPORTS, String.class);
    public static final BranchDB<String, VarDB<Boolean>> budgetAdjustment = Context.newBranchDB(BUDGET_ADJUSTMENT, Boolean.class);
    public static final BranchDB<String, VarDB<Boolean>> submitProgressReport = Context.newBranchDB(SUBMIT_PROGRESS_REPORT, Boolean.class);

    public static void addDataToProposalDB(ProposalAttributes proposalData, String prefix) {
        ipfsHash.at(prefix).set(proposalData.ipfs_hash);
        projectTitle.at(prefix).set(proposalData.project_title);
        timestamp.at(prefix).set(BigInteger.valueOf(Context.getBlockTimestamp()));
        totalBudget.at(prefix).set(proposalData.total_budget.multiply(EXA));
        projectDuration.at(prefix).set(proposalData.project_duration);
        sponsorAddress.at(prefix).set(proposalData.sponsor_address);
        ipfsLink.at(prefix).set(proposalData.ipfs_link);
        contributorAddress.at(prefix).set(Context.getCaller());
        status.at(prefix).set(SPONSOR_PENDING);
        txHash.at(prefix).set(recordTxHash(Context.getTransactionHash()));
        percentageCompleted.at(prefix).set(0);
        totalVotes.at(prefix).set(BigInteger.ZERO);
        totalVoters.at(prefix).set(0);
        approvedVotes.at(prefix).set(BigInteger.ZERO);
        rejectedVotes.at(prefix).set(BigInteger.ZERO);
        approvedReports.at(prefix).set(0);
        budgetAdjustment.at(prefix).set(false);
        submitProgressReport.at(prefix).set(false);
        token.at(prefix).set(proposalData.token);
    }

    public static Map<String, Object> getDataFromProposalDB(String prefix) {
        String reason = sponsorVoteReason.at(prefix).getOrDefault("");
        if (reason.equalsIgnoreCase("none")) {
            reason = "";
        } else {
            reason = reason.toString();
        }

        Map<String, Object> entryMap = Map.ofEntries(
        Map.entry(IPFS_HASH, ipfsHash.at(prefix).getOrDefault("")),
        Map.entry(PROJECT_TITLE, projectTitle.at(prefix).getOrDefault("")),
        Map.entry(TIMESTAMP, timestamp.at(prefix).getOrDefault(BigInteger.ZERO)),
        Map.entry(TOTAL_BUDGET, totalBudget.at(prefix).getOrDefault(BigInteger.ZERO)),
        Map.entry(PROJECT_DURATION, projectDuration.at(prefix).getOrDefault(0)),
        Map.entry(APPROVED_REPORTS, approvedReports.at(prefix).getOrDefault(0)),
        Map.entry(SPONSOR_ADDRESS, sponsorAddress.at(prefix).get()),
        Map.entry(CONTRIBUTOR_ADDRESS, contributorAddress.at(prefix).get()),
        Map.entry(STATUS, status.at(prefix).getOrDefault("")),
        Map.entry(TX_HASH, txHash.at(prefix).getOrDefault("")),
        Map.entry(PERCENTAGE_COMPLETED, percentageCompleted.at(prefix).getOrDefault(0)),
        Map.entry(TOKEN, token.at(prefix).getOrDefault("")),
        Map.entry(TOTAL_VOTES, totalVotes.at(prefix).getOrDefault(BigInteger.ZERO)),
        Map.entry(TOTAL_VOTERS, totalVoters.at(prefix).getOrDefault(0)),
        Map.entry(APPROVED_VOTES, approvedVotes.at(prefix).getOrDefault(BigInteger.ZERO)),
        Map.entry(REJECTED_VOTES, rejectedVotes.at(prefix).getOrDefault(BigInteger.ZERO)),
        Map.entry(ABSTAINED_VOTES, abstainedVotes.at(prefix).getOrDefault(BigInteger.ZERO)),
        Map.entry(SPONSOR_DEPOSIT_AMOUNT, sponsorDepositAmount.at(prefix).getOrDefault(BigInteger.ZERO)),
        Map.entry(SPONSORED_TIMESTAMP, sponsoredTimestamp.at(prefix).getOrDefault(BigInteger.ZERO)),
        Map.entry(SPONSOR_DEPOSIT_STATUS, sponsorDepositStatus.at(prefix).getOrDefault("")),
        Map.entry(APPROVE_VOTERS, approveVoters.at(prefix).size()),
        Map.entry(REJECT_VOTERS, rejectVoters.at(prefix).size()),
        Map.entry(ABSTAIN_VOTERS, abstainVoters.at(prefix).size()),
        Map.entry(BUDGET_ADJUSTMENT, budgetAdjustment.at(prefix).getOrDefault(false)),
        Map.entry(SUBMIT_PROGRESS_REPORT, submitProgressReport.at(prefix).getOrDefault(false)),
                Map.entry(SPONSOR_VOTE_REASON,reason));
        return entryMap;
    }


}
