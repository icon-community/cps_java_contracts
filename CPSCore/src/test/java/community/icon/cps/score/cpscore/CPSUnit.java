package community.icon.cps.score.cpscore;

import com.eclipsesource.json.JsonObject;
import community.icon.cps.score.cpscore.utils.Constants;
import community.icon.cps.score.lib.interfaces.CPSCoreInterface;
import community.icon.cps.score.lib.interfaces.CPSCoreInterface.MilestoneVoteAttributes;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static community.icon.cps.score.cpscore.utils.Constants.ACCEPT;
import static community.icon.cps.score.cpscore.utils.Constants.APPROVE;
import static community.icon.cps.score.cpscore.utils.Constants.COUNT;
import static community.icon.cps.score.cpscore.utils.Constants.DATA;
import static community.icon.cps.score.cpscore.utils.Constants.IPFS_HASH;
import static community.icon.cps.score.cpscore.utils.Constants.REJECT;
import static community.icon.cps.score.cpscore.utils.Constants.SPONSOR_PENDING;
import static community.icon.cps.score.cpscore.utils.Constants.VOTE;
import static community.icon.cps.score.cpscore.utils.Constants.VOTE_REASON;
import static community.icon.cps.score.cpscore.utils.Constants.bnUSD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CPSUnit extends AbstractCPSUnitTest{

    @Test
    void name(){
        assertEquals(cpsScore.call("name"), TAG);
    }

    @Test
    void registerPrep(){
        registerPrepsMethod();

        System.out.println("val  "+ cpsScore.call("getDele", owner.getAddress()));
        assertEquals(7, ((List<?>)(cpsScore.call("getPReps"))).size());
        verify(scoreSpy,times(7)).RegisterPRep(any(),eq("P-Rep Registered."));
    }

    @Test
    void submitProposal(){
        submitProposalMethod();

        List<String> proposalKeys = (List<String>) cpsScore.call("getProposalKeys");
        assertEquals(List.of("Proposal 1"), proposalKeys);

        Map<String, Object> proposalDetails = (Map<String, Object>) cpsScore.call("getProposalDetailsByHash", "" +
                "Proposal 1");
        proposalDetails.remove("sponsor_vote_reason");
        assertEquals("Proposal 1", proposalDetails.get("ipfs_hash"));
        assertEquals("Title", proposalDetails.get("project_title"));
        assertEquals(2, proposalDetails.get("project_duration"));
        assertEquals(testingAccount.getAddress(), proposalDetails.get("sponsor_address"));
        assertEquals(owner.getAddress(), proposalDetails.get("contributor_address"));
        assertEquals("_sponsor_pending", proposalDetails.get("status"));
        assertEquals(BigInteger.valueOf(100).multiply(ICX), proposalDetails.get("total_budget"));

        Map<String, Object> proposalDetailsOfStatus = (Map<String, Object>) cpsScore.call("getProposalDetails",
                SPONSOR_PENDING, owner.getAddress(), 0);
        assertEquals(1, proposalDetailsOfStatus.get(COUNT));

        Map<String,Object> proposalDetailsOf = ((List<Map<String, Object>>)proposalDetailsOfStatus.get(DATA)).get(0);
        assertEquals(proposalDetails.get("approve_voters"),proposalDetailsOf.get("approve_voters"));
        assertEquals(proposalDetails.get("sponsor_vote_reason"),proposalDetailsOf.get("sponsor_vote_reason"));
        assertEquals(proposalDetails.get("sponsor_deposit_amount"),proposalDetailsOf.get("sponsor_deposit_amount"));
        assertEquals(proposalDetails.get("sponsor_address"),proposalDetailsOf.get("sponsor_address"));
        assertEquals(proposalDetails.get("timestamp"),proposalDetailsOf.get("timestamp"));
        assertEquals(proposalDetails.get("contributor_address"),proposalDetailsOf.get("contributor_address"));
        assertEquals(proposalDetails.get("reject_voters"),proposalDetailsOf.get("reject_voters"));
        assertEquals(proposalDetails.get("project_title"),proposalDetailsOf.get("project_title"));
        assertEquals(proposalDetails.get("token"),proposalDetailsOf.get("token"));
        assertEquals(proposalDetails.get("tx_hash"),proposalDetailsOf.get("tx_hash"));
        assertEquals(proposalDetails.get("abstained_votes"),proposalDetailsOf.get("abstained_votes"));
        assertEquals(proposalDetails.get("abstain_voters"),proposalDetailsOf.get("abstain_voters"));
        assertEquals(proposalDetails.get("submit_progress_report"),proposalDetailsOf.get("submit_progress_report"));
        assertEquals(proposalDetails.get("sponsor_deposit_status"),proposalDetailsOf.get("sponsor_deposit_status"));
        assertEquals(proposalDetails.get("budget_adjustment"),proposalDetailsOf.get("budget_adjustment"));
        assertEquals(proposalDetails.get("status"),proposalDetailsOf.get("status"));
        assertEquals(proposalDetails.get("approved_votes"),proposalDetailsOf.get("approved_votes"));
        assertEquals(proposalDetails.get("total_votes"),proposalDetailsOf.get("total_votes"));
        assertEquals(proposalDetails.get("project_duration"),proposalDetailsOf.get("project_duration"));
        assertEquals(proposalDetails.get("total_voters"),proposalDetailsOf.get("total_voters"));
        assertEquals(proposalDetails.get("approved_reports"),proposalDetailsOf.get("approved_reports"));
        assertEquals(proposalDetails.get("rejected_votes"),proposalDetailsOf.get("rejected_votes"));
        assertEquals(proposalDetails.get("sponsored_timestamp"),proposalDetailsOf.get("sponsored_timestamp"));
        assertEquals(proposalDetails.get("percentage_completed"),proposalDetailsOf.get("percentage_completed"));
        assertEquals(proposalDetails.get("ipfs_hash"),proposalDetailsOf.get("ipfs_hash"));
        assertEquals(proposalDetails.get("total_budget"),proposalDetailsOf.get("total_budget"));
        assertEquals(proposalDetails.get("isMilestone"),proposalDetailsOf.get("isMilestone"));

        assertEquals(proposalDetails.size(), ((List<Map<String, Object>>)proposalDetailsOfStatus.get(DATA)).get(0).size());

        Map<String, Object> proposalDetailsOfWallet = (Map<String, Object>) cpsScore.call("getProposalDetailByWallet",
                owner.getAddress(),0);
        assertEquals(proposalDetails.size(), ((List<Map<String, Object>>)proposalDetailsOfWallet.get(DATA)).get(0).size());
        assertEquals(1, proposalDetailsOfWallet.get(COUNT));

        verify(scoreSpy).ProposalSubmitted(owner.getAddress(),"Successfully submitted a Proposal.");

    }

    @Test
    void setSponosrVotePercentage(){
        setScoresMethod();
        cpsScore.invoke(cpfScore,"setSponsorBondPercentage", BigInteger.valueOf(12));
    }

    @Test
    void submitSponsorVote(){
        submitProposalMethod();
        contextMock.when(caller()).thenReturn(bnUSDScore);
        JsonObject sponsorVoteParams = new JsonObject();
        sponsorVoteParams.add("method", "sponsor_vote");
        JsonObject params = new JsonObject();
        params.add(IPFS_HASH, "Proposal 1");
        params.add(VOTE, ACCEPT);
        params.add(VOTE_REASON, "reason");
        sponsorVoteParams.add("params", params);

        cpsScore.invoke(testingAccount, "tokenFallback", testingAccount.getAddress(),
                BigInteger.valueOf(12).multiply(ICX), sponsorVoteParams.toString().getBytes());

    }
    @Test
    public void setApplicationPeriod(){
        setScoresMethod();
        cpsScore.invoke(cpfScore,"setPeriod", BigInteger.valueOf(20));
    }

    @Test
    void voteproposal_Accept(){
        setApplicationPeriod();
        submitSponsorVote();
        contextMock.when(caller()).thenReturn(owner.getAddress());
        updateNextBlock();
        cpsScore.invoke(owner, "updatePeriod");
        System.out.println(cpsScore.call("getPeriodStatus"));
        doNothing().when(scoreSpy).callScore(eq(cpfScore.getAddress()), eq("swap_tokens"), eq(8));
        cpsScore.invoke(owner, "voteProposal", "Proposal 1", APPROVE, "reason", false);
        Map<String, Object> proposalDetails = (Map<String, Object>) cpsScore.call(
                "getProposalDetailsByHash", "Proposal 1");

        assertEquals(BigInteger.valueOf(1000), proposalDetails.get("approved_votes"));
        assertEquals(1, proposalDetails.get("approve_voters"));
        assertEquals(BigInteger.ZERO, proposalDetails.get("rejected_votes"));
        assertEquals(0, proposalDetails.get("reject_voters"));
        assertEquals(BigInteger.valueOf(1000), proposalDetails.get("total_votes"));

        cpsScore.invoke(owner, "voteProposal", "Proposal 1", REJECT, "reason", true);

        proposalDetails = getProposalDetailsByHash("Proposal 1");

        assertEquals(BigInteger.valueOf(0), proposalDetails.get("approved_votes"));
        assertEquals(0, proposalDetails.get("approve_voters"));
        assertEquals(BigInteger.valueOf(1000), proposalDetails.get("rejected_votes"));
        assertEquals(1, proposalDetails.get("reject_voters"));
        assertEquals(BigInteger.valueOf(1000), proposalDetails.get("total_votes"));

        assertEquals(1, cpsScore.call("checkChangeVote", owner.getAddress(), "Proposal 1", "proposal"));

        Map<String, Object> voteResult = (Map<String, Object>) cpsScore.call("getVoteResult", "Proposal 1");
        System.out.println(voteResult);
    }

    void voteProposalMethod(){
        submitSponsorVote();
        contextMock.when(caller()).thenReturn(owner.getAddress());
        updateNextBlock();
        cpsScore.invoke(owner, "updatePeriod");
        doNothing().when(scoreSpy).callScore(eq(cpfScore.getAddress()), eq("swap_tokens"), eq(8));
        String[] proposal = new String[1];
        proposal[0] = "Proposal 1";
        cpsScore.invoke(owner, "voteProposal", "Proposal 1", APPROVE, "reason", false);
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
        cpsScore.invoke(testingAccount4, "voteProposal", "Proposal 1", APPROVE, "reason", false);
        cpsScore.invoke(owner, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(testingAccount5.getAddress());
        cpsScore.invoke(testingAccount5, "voteProposal", "Proposal 1", APPROVE, "reason", false);
        cpsScore.invoke(owner, "votePriority", (Object) proposal);
    }

    @Test
    public void updatePeriod_afte_vote_on_proposal(){
        setApplicationPeriod();
        voteProposalMethod();
        contextMock.when(caller()).thenReturn(owner.getAddress());
        updateNextBlock();
        Map<String, BigInteger> totalFunds = Map.of(
                Constants.ICX, BigInteger.valueOf(1000).multiply(ICX),
                bnUSD, BigInteger.valueOf(1000).multiply(ICX)
        );

        doReturn(totalFunds).when(scoreSpy).callScore(eq(Map.class), eq(cpfScore.getAddress()), eq("getTotalFunds"));
        doNothing().when(scoreSpy).callScore(eq(cpfScore.getAddress()), eq("transfer_proposal_fund_to_cps_treasury"),
                eq("Proposal 1"), eq(2),eq(5), eq(testingAccount.getAddress()), eq(owner.getAddress()),
                eq(bnUSD), eq(BigInteger.valueOf(100).multiply(ICX)));

        doNothing().when(scoreSpy).callScore(eq(cpfScore.getAddress()), eq("reset_swap_state"));
    }

    Map<String, Object> getProposalDetailsByHash(String ipfs_hash){
        return (Map<String, Object>) cpsScore.call("getProposalDetailsByHash", ipfs_hash);
    }

    void updatePeriods(){
        //        1/4
        cpsScore.invoke(owner, "updatePeriod");
        //        2/4
        cpsScore.invoke(owner, "updatePeriod");
        //        3/4
        cpsScore.invoke(owner, "updatePeriod");
        //        4/4
        cpsScore.invoke(owner, "updatePeriod");
    }



    @Test
    public void submitProgressReport_first(){
        updatePeriod_afte_vote_on_proposal();
        CPSCoreInterface.ProgressReportAttributes progressReport = new CPSCoreInterface.ProgressReportAttributes();

        progressReport.ipfs_hash = "Proposal 1";
        progressReport.report_hash = "Report 1";
        progressReport.ipfs_link = "Link";
        progressReport.progress_report_title = "Progress Report Title";
        progressReport.budget_adjustment = true;
        progressReport.additional_budget = BigInteger.valueOf(0);
        progressReport.additional_month = 0;
        progressReport.milestoneCompleted = new int[]{1,2};
        doNothing().when(scoreSpy).callScore(eq(cpfScore.getAddress()), eq("swap_tokens"), any());
        cpsScore.invoke(owner, "toggleBudgetAdjustmentFeature");
        updatePeriods();
        cpsScore.invoke(owner, "submitProgressReport", progressReport);

    }

    @Test
    public void voteProgressReport(){

        List<Map<String, Object>> prepDict =
                List.of(Map.of("name", "owner", "address", owner.getAddress(), "power", BigInteger.valueOf(1000)),
                        Map.of("name", "testingAccount", "address", testingAccount.getAddress(), "power", BigInteger.valueOf(850)),
                        Map.of("name", "testingAccount1", "address", testingAccount1.getAddress(), "power", BigInteger.valueOf(770)),
                        Map.of("name", "testingAccount2" , "address", testingAccount2.getAddress(), "power", BigInteger.valueOf(800)),
                        Map.of("name", "testingAccount3", "address", testingAccount3.getAddress(), "power", BigInteger.valueOf(990)),
                        Map.of("name", "testingAccount4", "address", testingAccount4.getAddress(), "power", BigInteger.valueOf(500)),
                        Map.of("name", "testingAccount5", "address", testingAccount5.getAddress(), "power", BigInteger.valueOf(250))
                );

        submitProgressReport_first();
        updateNextBlock();
        cpsScore.invoke(owner, "updatePeriod");

        MilestoneVoteAttributes milestoneVoteAttributes= new MilestoneVoteAttributes();
        milestoneVoteAttributes.vote = APPROVE;
        milestoneVoteAttributes.id = 1;

        MilestoneVoteAttributes milestoneVoteAttributes2= new MilestoneVoteAttributes();
        milestoneVoteAttributes2.vote = APPROVE;
        milestoneVoteAttributes2.id = 2;


        MilestoneVoteAttributes[] milestoneVoteAttributesList = new MilestoneVoteAttributes[]{
                milestoneVoteAttributes,milestoneVoteAttributes2};
        doNothing().when(scoreSpy).callScore(eq(cpfScore.getAddress()), eq("swap_tokens"), eq(8));

        // VOTE BY OWNER
        doReturn(prepDict.get(0)).when(scoreSpy).callScore(eq(Map.class), eq(SYSTEM_ADDRESS), eq("getPRep"), eq(owner.getAddress()));
        cpsScore.invoke(owner, "voteProgressReport",  "Report 1", "reason", milestoneVoteAttributesList ,"_reject",false);

        // VOTE BY TESTING ACCOUNT
        contextMock.when(caller()).thenReturn(testingAccount.getAddress());
        doReturn(prepDict.get(1)).when(scoreSpy).callScore(eq(Map.class), eq(SYSTEM_ADDRESS), eq("getPRep"), eq(testingAccount.getAddress()));
        cpsScore.invoke(testingAccount, "voteProgressReport",  "Report 1", "reason", (Object)milestoneVoteAttributesList ,"_reject",false);

        // VOTE BY testing account 1
        contextMock.when(caller()).thenReturn(testingAccount1.getAddress());
        doReturn(prepDict.get(2)).when(scoreSpy).callScore(eq(Map.class), eq(SYSTEM_ADDRESS), eq("getPRep"),eq(testingAccount1.getAddress()));
        cpsScore.invoke(testingAccount1, "voteProgressReport",  "Report 1", "reason", milestoneVoteAttributesList ,"_reject",false);

        contextMock.when(caller()).thenReturn(testingAccount2.getAddress());
        cpsScore.invoke(testingAccount2, "voteProgressReport",  "Report 1", "reason", (Object)milestoneVoteAttributesList ,"_reject",false);

        contextMock.when(caller()).thenReturn(testingAccount3.getAddress());
        cpsScore.invoke(testingAccount3, "voteProgressReport",  "Report 1", "reason", (Object)milestoneVoteAttributesList ,"_reject",false);
//
        contextMock.when(caller()).thenReturn(testingAccount4.getAddress());
        cpsScore.invoke(testingAccount4, "voteProgressReport",  "Report 1", "reason", (Object)milestoneVoteAttributesList ,"_reject",false);

        contextMock.when(caller()).thenReturn(testingAccount5.getAddress());
        cpsScore.invoke(testingAccount5, "voteProgressReport",  "Report 1", "reason", (Object)milestoneVoteAttributesList ,"_reject",false);
    }

    @Test
    public void testVoteResult(){
        voteProgressReport();
//        Map<String, Object> progressReportDetails = (Map<String, Object>) cpsScore.call("getProgressReportsByHash", "Report 1");
//        System.out.println("pp" + progressReportDetails);
//
//        List<Integer> mm = (List<Integer> )cpsScore.call("getMilestoneCountOfProgressReport", "Report 1");
//        System.out.println("milestone" + mm);
//
        Map<String, Object> voteResult = (Map<String, Object>) cpsScore.call("getProgressReportVoteDetails", "Report 1");
        System.out.println("pp" + voteResult);
//
        Map<String, Object> milestoneReport = (Map<String, Object>) cpsScore.call("getMilestonesReport", "Proposal 1", 1);
        System.out.println("milestone" + milestoneReport);

        Map<String, Object> milestoneReport2 = (Map<String, Object>) cpsScore.call("getMilestonesReport", "Proposal 1", 2);
        System.out.println("milestone" + milestoneReport2);

        // TODO: check the milestone 3 gives a good message
//        Map<String, Object> milestoneReport3 = (Map<String, Object>) cpsScore.call("getMilestonesReport", "Proposal 1", 3);
//        System.out.println("milestone" + milestoneReport3);
    }


    @Test
    void updatePeriods_after_voting_progress_report(){
        voteProgressReport();
        contextMock.when(caller()).thenReturn(owner.getAddress());
        updateNextBlock();
        doNothing().when(scoreSpy).callScore(eq(BigInteger.ZERO), eq(SYSTEM_ADDRESS), eq("burn"));
        doNothing().when(scoreSpy).callScore(eq(cpsTreasury), eq("send_installment_to_contributor"), eq("Proposal 1"),eq(2));
        doNothing().when(scoreSpy).callScore(eq(cpsTreasury), eq("send_reward_to_sponsor"), eq("Proposal 1"),eq(2));

        updatePeriods();

        Map<String, Object> prr = (Map<String, Object>) cpsScore.call("getProgressReportsByHash", "Report 1");
        System.out.println("pp" + prr);

        Map<String, Object> voteResult = (Map<String, Object>) cpsScore.call("getProgressReportVoteDetails", "Report 1");
        System.out.println("pp" + voteResult);

        Map<String, Object> milestoneReport = (Map<String, Object>) cpsScore.call("getMilestonesReport", "Proposal 1", 2);
        System.out.println("milestone" + milestoneReport);

        Map<String, Object> milestoneReport1 = (Map<String, Object>) cpsScore.call("getMilestonesReport", "Proposal 1", 1);
        System.out.println("milestone" + milestoneReport1);

    }





}
