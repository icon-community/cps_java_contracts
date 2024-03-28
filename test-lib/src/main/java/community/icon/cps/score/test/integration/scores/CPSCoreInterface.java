package community.icon.cps.score.test.integration.scores;


import foundation.icon.score.client.ScoreClient;
import foundation.icon.score.client.ScoreInterface;
import score.Address;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;
import score.annotation.Payable;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public interface CPSCoreInterface {

    public static class ProposalAttributes {
        public String ipfs_hash;
        public String project_title;
        public int project_duration;
        public BigInteger total_budget;
        public String token;
        public Address sponsor_address;
        public String ipfs_link;
        public int milestoneCount;
        public Boolean isMilestone;

    }

    public static class ProgressReportAttributes {
        public String ipfs_hash;
        public String report_hash;
        public String ipfs_link;
        public String progress_report_title;
        public Boolean budget_adjustment;
        public BigInteger additional_budget;
        public int additional_month;
//        public int[] milestoneCompleted;
    }

    public static class MilestonesAttributes {
        public int id;
        public int completionPeriod;
        public BigInteger budget;
    }

    public static class MilestoneSubmission{
        public int id;
        public boolean status;
    }

    public static class MilestoneVoteAttributes {
        public int id;
        public String vote;
    }

    @External(readonly = true)
    String name();

    String proposalPrefix(String proposalKey);

    String progressReportPrefix(String progressKey);

    @External
    void setCpsTreasuryScore(Address score);

    @External(readonly = true)
    Address getCpsTreasuryScore();

    @External
    void setCpfTreasuryScore(Address score);

    @External(readonly = true)
    Address getCpfTreasuryScore();


    @External
    void setBnusdScore(Address score);


    @External(readonly = true)
    Address getBnusdScore();


    @External(readonly = true)
    boolean isAdmin(Address address);

    @External
    void toggleBudgetAdjustmentFeature();

    @External(readonly = true)
    boolean getBudgetAdjustmentFeature();


    @External
    void toggleMaintenance();

    @External(readonly = true)
    boolean getMaintenanceMode();

    @Payable
    void fallback();


    @External
    void addAdmin(Address address);


    @External
    void removeAdmin(Address address);


    @External
    void unregisterPrep();


    @External
    void registerPrep();

    @External(readonly = true)
    boolean checkPriorityVoting(Address prep);

    @External(readonly = true)
    List<String> sortPriorityProposals();

    @External(readonly = true)
    Map<String, Integer> getPriorityVoteResult();

    @External(readonly = true)
    Map<String, Object> getMilestonesReport(String ipfsKey, int milestoneId);

    @External
    void votePriority(String[] _proposals);

    @External(readonly = true)
    List<Map<String, Object>> getActiveProposals(Address walletAddress);


    @External
    void setPrepPenaltyAmount(BigInteger[] penalty);


    @External
    void setInitialBlock();


    @External(readonly = true)
    Map<String, BigInteger> loginPrep(Address address);


    @External(readonly = true)
    List<Address> getAdmins();

    @SuppressWarnings("unchecked")

    @External(readonly = true)
    Map<String, BigInteger> getRemainingFund();


    @External(readonly = true)
    List<Map<String, Object>> getPReps();


    @External(readonly = true)
    List<Address> getDenylist();


    @External(readonly = true)
    Map<String, ?> getPeriodStatus();


    @External(readonly = true)
    List<Address> getContributors();


    @External(readonly = true)
    Map<String, BigInteger> checkClaimableSponsorBond(Address address);


    @Payable
    @External
    void submitProposal(community.icon.cps.score.lib.interfaces.CPSCoreInterface.ProposalAttributes proposals, community.icon.cps.score.lib.interfaces.CPSCoreInterface.MilestonesAttributes[] milestones);


    @External
    void voteProposal(String ipfsKey, String vote, String voteReason, @Optional boolean voteChange);


    @External
    void submitProgressReport(community.icon.cps.score.lib.interfaces.CPSCoreInterface.ProgressReportAttributes progressReport, community.icon.cps.score.lib.interfaces.CPSCoreInterface.MilestoneSubmission[] milestoneSubmissions);


    @External
    void voteProgressReport(String reportKey, String voteReason, community.icon.cps.score.lib.interfaces.CPSCoreInterface.MilestoneVoteAttributes[] votes, @Optional String budgetAdjustmentVote, @Optional boolean voteChange);


    @External(readonly = true)
    List<String> getProposalsKeysByStatus(String status);

    @External(readonly = true)
    Map<String, Object> getMilestoneVoteResult(String reportKey, int milestoneId);

    @External(readonly = true)
    int getMileststoneStatusOf(String proposalKey, int milestoneId);


    @External(readonly = true)
    int checkChangeVote(Address address, String ipfsHash, String proposalType);


    @External(readonly = true)
    Map<String, ?> getProjectAmounts();


    @External(readonly = true)
    Map<String, Integer> getSponsorsRecord();

    @External
    void updatePeriod();


    @External(readonly = true)
    Map<String, ?> getProposalDetails(String status, @Optional Address walletAddress, @Optional int startIndex);

    @External(readonly = true)
    Map<String, Object> getProposalDetailsByHash(String ipfsKey);


    @External(readonly = true)
    Map<String, ?> getProgressReports(String status, @Optional int startIndex);


    @External(readonly = true)
    Map<String, Object> getProgressReportsByHash(String reportKey);


    @External(readonly = true)
    Map<String, Object> getProgressReportsByProposal(String ipfsKey);


    @External(readonly = true)
    Map<String, Object> getSponsorsRequests(String status, Address sponsorAddress, @Optional int startIndex);

    @External(readonly = true)
    Map<String, Object> getVoteResult(String ipfsKey);

    @External(readonly = true)
    Map<String, Object> getProgressReportResult(String reportKey);

    @External(readonly = true)
    Map<String, Object> getBudgetAdjustmentVoteResult(String reportKey);

    @External
    void tokenFallback(Address from, BigInteger value, byte[] data);

    @External
    void removeDenylistPreps();

    @External
    void claimSponsorBond();

    @External
    void setPeriod(BigInteger applicationPeriod);

    @External
    void setSwapCount(int value);

    @External
    void updateNextBlock(int blockCount);

    @External
    void updateContributor(String _ipfs_key, Address _new_contributor, Address _new_sponsor);


    @External(readonly = true)
    Map<String, Object> getActiveProposalsList(@Optional int startIndex);

    @External(readonly = true)
    Map<String, Object> getProposalDetailByWallet(Address walletAddress, @Optional int startIndex);

    @External(readonly = true)
    Map<String, Object> getProposalsHistory(@Optional int startIndex);

    @External(readonly = true)
    List<Map<String,?>> getRemainingMilestones(String ipfsHash);

    //    EventLogs
    @EventLog(indexed = 1)
    void ProposalSubmitted(Address _sender_address, String note);

    @EventLog(indexed = 1)
    void ProgressReportSubmitted(Address _sender_address, String _project_title);

    @EventLog(indexed = 1)
    void SponsorBondReceived(Address _sender_address, String _notes);

    @EventLog(indexed = 1)
    void SponsorBondRejected(Address _sender_address, String _notes);

    @EventLog(indexed = 1)
    void VotedSuccessfully(Address _sender_address, String _notes);

    @EventLog(indexed = 1)
    void PRepPenalty(Address _prep_address, String _notes);

    @EventLog(indexed = 1)
    void UnRegisterPRep(Address _sender_address, String _notes);

    @EventLog(indexed = 1)
    void RegisterPRep(Address _sender_address, String _notes);

    @EventLog(indexed = 1)
    void SponsorBondReturned(Address _sender_address, String _notes);

    @EventLog(indexed = 1)
    void PeriodUpdate(String _notes);

    @EventLog(indexed = 1)
    void SponsorBondClaimed(Address _receiver_address, BigInteger _fund, String note);

    @EventLog(indexed = 1)
    void PriorityVote(Address _address, String note);

    @EventLog(indexed = 1)
    void UpdateContributorAddress(Address _old, Address _new);

    @EventLog(indexed = 1)
    void UpdateSponsorAddress(Address _old, Address _new);

    @External(readonly = true)
    int getPeriodCount();

    @External
    void setSponsorBondPercentage(BigInteger bondValue);

    @External(readonly = true)
    BigInteger getSponsorBondPercentage();
}