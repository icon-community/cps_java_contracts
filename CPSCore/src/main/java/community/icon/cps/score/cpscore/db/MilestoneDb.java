package community.icon.cps.score.cpscore.db;

import community.icon.cps.score.lib.interfaces.CPSCoreInterface;
import score.*;

import java.math.BigInteger;
import java.util.Map;

import static community.icon.cps.score.cpscore.utils.Constants.*;

public class MilestoneDb {
    public static final BranchDB<String, VarDB<Integer>> id = Context.newBranchDB(MILESTONE_ID, Integer.class);
    public static final BranchDB<String, VarDB<String>> progressReportHash = Context.newBranchDB(REPORT_HASH, String.class);
    public static final BranchDB<String, VarDB<Integer>> status = Context.newBranchDB(STATUS, Integer.class);
    public static final BranchDB<String, VarDB<BigInteger>> approvedVotes = Context.newBranchDB(APPROVED_VOTES, BigInteger.class);
    public static final BranchDB<String, VarDB<BigInteger>> rejectedVotes = Context.newBranchDB(REJECTED_VOTES, BigInteger.class);
    public static final BranchDB<String, ArrayDB<Address>> votersList = Context.newBranchDB(VOTERS_LIST, Address.class);
    public static final BranchDB<String, ArrayDB<Address>> approveVoters = Context.newBranchDB(APPROVE_VOTERS, Address.class);
    public static final BranchDB<String, ArrayDB<Address>> rejectVoters = Context.newBranchDB(REJECT_VOTERS, Address.class);
    public static final BranchDB<String, VarDB<Integer>> completionPeriod = Context.newBranchDB(COMPLETION_PERIOD, Integer.class);
    public static final BranchDB<String, VarDB<BigInteger>> budget = Context.newBranchDB(BUDGET, BigInteger.class);
    public static final BranchDB<String, VarDB<Boolean>> extensionFlag = Context.newBranchDB(EXTENSION_FLAG, Boolean.class);

    public static final BranchDB<String, BranchDB<Address, DictDB<String, Integer>>> votersListIndices = Context.newBranchDB(VOTERS_LIST_INDEXES, Integer.class);

    public static void addDataToMilestoneDb(CPSCoreInterface.MilestonesAttributes milestoneData, String prefix) {
        id.at(prefix).set(milestoneData.id);
        approvedVotes.at(prefix).set(BigInteger.ZERO);
        rejectedVotes.at(prefix).set(BigInteger.ZERO);
        completionPeriod.at(prefix).set(milestoneData.completionPeriod);
        budget.at(prefix).set(milestoneData.budget);

    }

    public static Map<String, Object> getDataFromMilestoneDB(String prefix) {
        String reportHash = progressReportHash.at(prefix).getOrDefault("");
        return Map.ofEntries(
                Map.entry(MILESTONE_ID, id.at(prefix).get()),
                Map.entry(STATUS, status.at(prefix).getOrDefault(0)),
                Map.entry(COMPLETION_PERIOD, completionPeriod.at(prefix).getOrDefault(0)),
                Map.entry(BUDGET, budget.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(REPORT_HASH, progressReportHash.at(prefix).getOrDefault("")),
                Map.entry(TOTAL_VOTES, ProgressReportDataDb.totalVotes.at(progressReportPrefix(reportHash)).getOrDefault(BigInteger.ZERO)),
                Map.entry(APPROVED_VOTES, approvedVotes.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(REJECTED_VOTES, rejectedVotes.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(TOTAL_VOTERS, ProgressReportDataDb.totalVoters.at(progressReportPrefix(reportHash)).getOrDefault(0)),
                Map.entry(APPROVE_VOTERS, approveVoters.at(prefix).size()),
                Map.entry(REJECT_VOTERS, rejectVoters.at(prefix).size()),
                Map.entry(EXTENSION_FLAG, extensionFlag.at(prefix).getOrDefault(false)));
    }

    public static String progressReportPrefix(String progressHash) {
        return PROGRESS_REPORT_DB_PREFIX + "|" + "|" + progressHash;
    }
}
