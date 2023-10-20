package community.icon.cps.score.cpscore;

import com.eclipsesource.json.JsonObject;
import com.iconloop.score.test.Account;
import community.icon.cps.score.cpscore.utils.Constants;
import community.icon.cps.score.lib.interfaces.CPSCoreInterface;
import org.junit.jupiter.api.Test;
import score.Context;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static community.icon.cps.score.cpscore.utils.Constants.ABSTAIN;
import static community.icon.cps.score.cpscore.utils.Constants.ABSTAINED_VOTES;
import static community.icon.cps.score.cpscore.utils.Constants.ABSTAIN_VOTERS;
import static community.icon.cps.score.cpscore.utils.Constants.ACCEPT;
import static community.icon.cps.score.cpscore.utils.Constants.APPROVE;
import static community.icon.cps.score.cpscore.utils.Constants.APPROVED_REPORTS;
import static community.icon.cps.score.cpscore.utils.Constants.APPROVED_VOTES;
import static community.icon.cps.score.cpscore.utils.Constants.APPROVE_VOTERS;
import static community.icon.cps.score.cpscore.utils.Constants.CONTRIBUTOR_ADDRESS;
import static community.icon.cps.score.cpscore.utils.Constants.IPFS_HASH;
import static community.icon.cps.score.cpscore.utils.Constants.IS_MILESTONE;
import static community.icon.cps.score.cpscore.utils.Constants.MILESTONE_COUNT;
import static community.icon.cps.score.cpscore.utils.Constants.PROJECT_DURATION;
import static community.icon.cps.score.cpscore.utils.Constants.PROJECT_TITLE;
import static community.icon.cps.score.cpscore.utils.Constants.REJECT;
import static community.icon.cps.score.cpscore.utils.Constants.SPONSOR_ADDRESS;
import static community.icon.cps.score.cpscore.utils.Constants.SPONSOR_DEPOSIT_AMOUNT;
import static community.icon.cps.score.cpscore.utils.Constants.STATUS;
import static community.icon.cps.score.cpscore.utils.Constants.TIMESTAMP;
import static community.icon.cps.score.cpscore.utils.Constants.TOTAL_BUDGET;
import static community.icon.cps.score.cpscore.utils.Constants.TOTAL_VOTERS;
import static community.icon.cps.score.cpscore.utils.Constants.TOTAL_VOTES;
import static community.icon.cps.score.cpscore.utils.Constants.VOTE;
import static community.icon.cps.score.cpscore.utils.Constants.VOTE_REASON;
import static community.icon.cps.score.cpscore.utils.Constants.bnUSD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

public class MigrationTest extends CPSScoreTest {
    private static final Account alice = sm.createAccount();

    private void submitOldProposal(){
        registerPrepsMethod();
        CPSCoreInterface.ProposalAttributes proposalAttributes = new CPSCoreInterface.ProposalAttributes();
        proposalAttributes.ipfs_hash = "Proposal 1";
        proposalAttributes.project_title = "Title";
        proposalAttributes.project_duration = 2;
        proposalAttributes.total_budget = BigInteger.valueOf(100);
        proposalAttributes.token = bnUSD;
        proposalAttributes.sponsor_address = testingAccount.getAddress();
        proposalAttributes.ipfs_link = "link";
        proposalAttributes.milestoneCount = 2;

        Map<String, BigInteger> remainingSwapAmount = Map.of(
                "remaining_swap_amount", BigInteger.valueOf(1000).multiply(MULTIPLIER),
                "maxCap", BigInteger.valueOf(1000).multiply(MULTIPLIER));
        doReturn(remainingSwapAmount).when(scoreSpy).callScore(eq(Map.class), eq(cpfTreasury), eq("getRemainingSwapAmount"));
        contextMock.when(() -> Context.getValue()).thenReturn(BigInteger.valueOf(50).multiply(MULTIPLIER));
        byte [] tx_hash = "transaction".getBytes();
        contextMock.when(() -> Context.getTransactionHash()).thenReturn(tx_hash);
        doNothing().when(scoreSpy).callScore(eq(BigInteger.valueOf(25).multiply(MULTIPLIER)), eq(SYSTEM_ADDRESS), eq("burn"));
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), eq(0));

        contextMock.when(caller()).thenReturn(alice.getAddress());
        cpsScore.invoke(alice,"submitProposal",proposalAttributes );
    }

    private void migrateProposal(){
        CPSCoreInterface.ProposalAttributes proposalAttributes = new CPSCoreInterface.ProposalAttributes();
        proposalAttributes.ipfs_hash = "New Proposal 1";
        proposalAttributes.project_title = "Title";
        proposalAttributes.project_duration = 2;
        proposalAttributes.total_budget = BigInteger.valueOf(100);
        proposalAttributes.token = bnUSD;
        proposalAttributes.sponsor_address = testingAccount.getAddress();
        proposalAttributes.ipfs_link = "link";
        proposalAttributes.milestoneCount = 2;
        proposalAttributes.isMilestone = true;

        int[] milestones = new int[]{1,2};

        contextMock.when(caller()).thenReturn(alice.getAddress());
        cpsScore.invoke(alice,"submitProposalMock",proposalAttributes,"Proposal 1", milestones);
    }

    @Test
    void submitedProposalMigration() throws InterruptedException {
        String oldHash = "Proposal 1";
        String newHash = "New Proposal 1";
        submitOldProposal();

        Map<String, Object> proposalDetails = (Map<String, Object>) cpsScore.call("getProposalDetailsByHash",
                oldHash);
        List<String> proposalKeys = (List<String>) cpsScore.call("getProposalKeys");
        assertEquals(proposalKeys.get(0),"Proposal 1");

        Thread.sleep(5);
        migrateProposal();

        Map<String, Object> newProposalDetails = (Map<String, Object>) cpsScore.call("getProposalDetailsByHash",
                newHash);
        // asserting the proposal details
        assertEquals(proposalDetails.get(PROJECT_TITLE),newProposalDetails.get(PROJECT_TITLE));
        assertEquals(proposalDetails.get(TOTAL_BUDGET),newProposalDetails.get(TOTAL_BUDGET));
        assertEquals(proposalDetails.get(PROJECT_DURATION),newProposalDetails.get(PROJECT_DURATION));
        assertEquals(proposalDetails.get(APPROVED_REPORTS),newProposalDetails.get(APPROVED_REPORTS));
        assertEquals(proposalDetails.get(SPONSOR_ADDRESS),newProposalDetails.get(SPONSOR_ADDRESS));
        assertEquals(proposalDetails.get(CONTRIBUTOR_ADDRESS),newProposalDetails.get(CONTRIBUTOR_ADDRESS));
        assertEquals(proposalDetails.get(STATUS),newProposalDetails.get(STATUS));
        assertEquals(proposalDetails.get(TOTAL_VOTES),newProposalDetails.get(TOTAL_VOTES));
        assertEquals(proposalDetails.get(APPROVED_VOTES),newProposalDetails.get(APPROVED_VOTES));
        assertEquals(proposalDetails.get(ABSTAINED_VOTES),newProposalDetails.get(ABSTAINED_VOTES));
        assertEquals(proposalDetails.get(SPONSOR_DEPOSIT_AMOUNT),newProposalDetails.get(SPONSOR_DEPOSIT_AMOUNT));
        assertEquals(proposalDetails.get(TIMESTAMP),newProposalDetails.get(TIMESTAMP));

        // old proposal details
        assertEquals(proposalDetails.get(IPFS_HASH),oldHash);
        assertEquals(proposalDetails.get(IS_MILESTONE),false);

        // new proposal details
        assertEquals(newProposalDetails.get(IPFS_HASH),newHash);
        assertEquals(newProposalDetails.get(MILESTONE_COUNT),2);
        assertEquals(newProposalDetails.get(IS_MILESTONE),true);

        proposalKeys = (List<String>) cpsScore.call("getProposalKeys");
        assertEquals(proposalKeys.get(0),"New Proposal 1");


    }

    private void submitSponsorVote(String ipfsHash){
        contextMock.when(caller()).thenReturn(bnUSDScore);
        JsonObject sponsorVoteParams = new JsonObject();
        sponsorVoteParams.add("method", "sponsorVote");
        JsonObject params = new JsonObject();
        params.add(IPFS_HASH, ipfsHash);
        params.add(VOTE, ACCEPT);
        params.add(VOTE_REASON, "reason");
        sponsorVoteParams.add("params", params);

        cpsScore.invoke(testingAccount, "tokenFallback", testingAccount.getAddress(),
                BigInteger.valueOf(12).multiply(MULTIPLIER), sponsorVoteParams.toString().getBytes());
    }

    private void voteProposal1(){
        contextMock.when(caller()).thenReturn(owner.getAddress());
        updateNextBlock();
        doReturn(BigInteger.valueOf(15)).when(scoreSpy).getApplicationPeriod();
        cpsScore.invoke(owner, "updatePeriod");
        getPeriodStatusMethod();
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), eq(8));
        String[] proposal = new String[]{"Proposal 1"};

        cpsScore.invoke(owner, "voteProposal", "Proposal 1", APPROVE, "voting okay", false);
        cpsScore.invoke(owner, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(testingAccount.getAddress());
        cpsScore.invoke(testingAccount, "voteProposal", "Proposal 1", APPROVE, "reason", false);
        cpsScore.invoke(owner, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(testingAccount1.getAddress());
        cpsScore.invoke(testingAccount1, "voteProposal", "Proposal 1", APPROVE, "reason", false);
        cpsScore.invoke(owner, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(testingAccount2.getAddress());
        cpsScore.invoke(testingAccount2, "voteProposal", "Proposal 1", APPROVE, "reason", false);
        cpsScore.invoke(owner, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(testingAccount3.getAddress());
        cpsScore.invoke(testingAccount3, "voteProposal", "Proposal 1", APPROVE, "reason", false);
        cpsScore.invoke(owner, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(testingAccount4.getAddress());
        cpsScore.invoke(testingAccount4, "voteProposal", "Proposal 1", ABSTAIN, "I am neutral", false);
        cpsScore.invoke(owner, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(testingAccount5.getAddress());
        cpsScore.invoke(testingAccount5, "voteProposal", "Proposal 1", REJECT, "I am rejecting", false);
        cpsScore.invoke(owner, "votePriority", (Object) proposal);
    }

    @Test
    void votedProposalMigration() throws InterruptedException {
        String oldHash = "Proposal 1";
        String newHash = "New Proposal 1";

        submitOldProposal();
        submitSponsorVote(oldHash);
        voteProposal1();

        Map<String, Object> proposalDetails = (Map<String, Object>) cpsScore.call("getProposalDetailsByHash",
                oldHash);

        Map<String, Object> oldVoteResult = (Map<String, Object>) cpsScore.call("getVoteResult",
                oldHash);

        Thread.sleep(5);
        migrateProposal();

        Map<String, Object> newProposalDetails = (Map<String, Object>) cpsScore.call("getProposalDetailsByHash",
                newHash);
        Map<String, Object> migratedVoteResult = (Map<String, Object>) cpsScore.call("getVoteResult",
                oldHash);

        // asserting the proposal details
        assertEquals(proposalDetails.get(APPROVED_REPORTS),newProposalDetails.get(APPROVED_REPORTS));
        assertEquals(proposalDetails.get(STATUS),newProposalDetails.get(STATUS));
        assertEquals(proposalDetails.get(TOTAL_VOTES),newProposalDetails.get(TOTAL_VOTES));
        assertEquals(proposalDetails.get(TOTAL_VOTERS),newProposalDetails.get(TOTAL_VOTERS));
        assertEquals(proposalDetails.get(APPROVED_VOTES),newProposalDetails.get(APPROVED_VOTES));
        assertEquals(proposalDetails.get(APPROVE_VOTERS),newProposalDetails.get(APPROVE_VOTERS));
        assertEquals(proposalDetails.get(ABSTAINED_VOTES),newProposalDetails.get(ABSTAINED_VOTES));
        assertEquals(proposalDetails.get(ABSTAIN_VOTERS),newProposalDetails.get(ABSTAIN_VOTERS));
        assertEquals(proposalDetails.get(TIMESTAMP),newProposalDetails.get(TIMESTAMP));


        // comparing voter's reason
        List<Map<String, Object>> oldVoters = (List<Map<String, Object>>) oldVoteResult.get("data");

        List<Map<String, Object>> migratedVoters = (List<Map<String, Object>>) migratedVoteResult.get("data");
        assertEquals(oldVoters,migratedVoters);
    }

    private void submitProgressReport1(){
        CPSCoreInterface.ProgressReportAttributes progressReport = new CPSCoreInterface.ProgressReportAttributes();
        progressReport.ipfs_hash = "Proposal 1";
        progressReport.report_hash = "Report 1";
        progressReport.ipfs_link = "Link";
        progressReport.progress_report_title = "Progress Report Title";
        progressReport.budget_adjustment = false;
        progressReport.additional_budget = BigInteger.ZERO;
        progressReport.additional_month = 0;
        progressReport.milestoneCompleted = new int[]{1};


        contextMock.when(caller()).thenReturn(alice.getAddress());
        cpsScore.invoke(alice, "submitProgressReport", progressReport);
    }

    private void voteOnProgressReport(){
        CPSCoreInterface.MilestoneVoteAttributes milestoneVoteAttributes= new CPSCoreInterface.MilestoneVoteAttributes();
        milestoneVoteAttributes.vote = APPROVE;
        milestoneVoteAttributes.id = 1;

        CPSCoreInterface.MilestoneVoteAttributes[] milestoneVoteAttributesList = new CPSCoreInterface.MilestoneVoteAttributes[]{
                milestoneVoteAttributes};
//        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swap_tokens"), eq(8));

        cpsScore.invoke(owner, "voteProgressReport",  "Report 1", "reason", milestoneVoteAttributesList ,"_reject",false);

        contextMock.when(caller()).thenReturn(testingAccount.getAddress());
        cpsScore.invoke(testingAccount, "voteProgressReport",  "Report 1", "reason", (Object)milestoneVoteAttributesList ,"_reject",false);

        contextMock.when(caller()).thenReturn(testingAccount1.getAddress());
        cpsScore.invoke(testingAccount1, "voteProgressReport",  "Report 1", "reason", milestoneVoteAttributesList ,"_reject",false);

        contextMock.when(caller()).thenReturn(testingAccount2.getAddress());
        cpsScore.invoke(testingAccount2, "voteProgressReport",  "Report 1", "reason", (Object)milestoneVoteAttributesList ,"_reject",false);

        contextMock.when(caller()).thenReturn(testingAccount3.getAddress());
        cpsScore.invoke(testingAccount3, "voteProgressReport",  "Report 1", "reason", (Object)milestoneVoteAttributesList ,"_reject",false);

        contextMock.when(caller()).thenReturn(testingAccount4.getAddress());
        cpsScore.invoke(testingAccount4, "voteProgressReport",  "Report 1", "reason", (Object)milestoneVoteAttributesList ,"_reject",false);

        contextMock.when(caller()).thenReturn(testingAccount5.getAddress());
        cpsScore.invoke(testingAccount5, "voteProgressReport",  "Report 1", "reason", (Object)milestoneVoteAttributesList ,"_reject",false);

    }



     @Test
    void submitAndVoteProgressReport(){
         String oldHash = "Proposal 1";

         submitOldProposal();
         submitSponsorVote(oldHash);
         voteProposal1();

         contextMock.when(caller()).thenReturn(owner.getAddress());
         updateNextBlock();
         Map<String, BigInteger> totalFunds = Map.of(
                 Constants.ICX, BigInteger.valueOf(1000).multiply(MULTIPLIER),
                 bnUSD, BigInteger.valueOf(1000).multiply(MULTIPLIER)
         );
         doReturn(totalFunds).when(scoreSpy).callScore(eq(Map.class), eq(cpfTreasury), eq("getTotalFunds"));
         doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("transferProposalFundToCpsTreasury"),
                 eq("Proposal 1"), eq(2), eq(testingAccount.getAddress()), eq(alice.getAddress()),
                 eq(bnUSD), eq(BigInteger.valueOf(100).multiply(MULTIPLIER)));

         doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("resetSwapState"));
         doReturn(BigInteger.valueOf(15)).when(scoreSpy).getVotingPeriod();
         updatePeriods();

         contextMock.when(caller()).thenReturn(alice.getAddress());
         submitProgressReport1();

         contextMock.when(caller()).thenReturn(owner.getAddress());
         updateNextBlock();
         cpsScore.invoke(owner, "updatePeriod");
         voteOnProgressReport();
    }





    @Test
    void updatePeriodAfterVotingProgressReport(){
        String newHash = "New Proposal 1";

        submitAndVoteProgressReport();
        contextMock.when(caller()).thenReturn(owner.getAddress());
        updateNextBlock();

        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("resetSwapState"));
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("updateProposalFund"), eq("Proposal 1"), eq(bnUSD), eq(BigInteger.valueOf(10).multiply(MULTIPLIER)), eq(1));
        doNothing().when(scoreSpy).callScore(eq(cpsTreasury), eq("sendInstallmentToContributor"), eq("Proposal 1"),eq(1));
        doNothing().when(scoreSpy).callScore(eq(cpsTreasury), eq("sendRewardToSponsor"), eq("Proposal 1"),eq(1));
        doNothing().when(scoreSpy).callScore(eq(BigInteger.ZERO), eq(SYSTEM_ADDRESS), eq("burn"));

        updatePeriods();


        // proposal with old hash
        Map<String, ?> progressReportDetails = (Map<String, ?>) cpsScore.call("getProgressReports",
                "_approved",0);
        Map<String, Object> oldHashData = (Map<String, Object>)(((List<Object>) progressReportDetails.get("data")).get(0));
        assertEquals(oldHashData.get(IPFS_HASH),"Proposal 1");
        assertEquals(oldHashData.get(STATUS),"_approved");

        migrateProposal();

        // proposal with new hash
        Map<String, ?> newProgressReportDetails = (Map<String, ?>) cpsScore.call("getProgressReports",
                "_approved",0);
        Map<String, Object> newHashData = (Map<String, Object>)(((List<Object>) newProgressReportDetails.get("data")).get(0));
        assertEquals(newHashData.get(IPFS_HASH),"New Proposal 1");
        assertEquals(newHashData.get(STATUS),"_approved");

    }


}
