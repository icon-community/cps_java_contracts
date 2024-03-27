package community.icon.cps.integration;

import com.eclipsesource.json.JsonObject;
import community.icon.cps.score.lib.interfaces.CPSCoreInterface;
import community.icon.cps.score.lib.interfaces.CPSCoreInterface.MilestonesAttributes;
import community.icon.cps.score.lib.interfaces.CPSCoreInterface.MilestoneVoteAttributes;
import community.icon.cps.score.lib.interfaces.CPSCoreInterface.ProposalAttributes;
import community.icon.cps.score.lib.interfaces.CPSCoreInterface.ProgressReportAttributes;
import community.icon.cps.score.test.integration.scores.CPSCoreInterfaceScoreClient;
import community.icon.cps.score.test.integration.CPS;
import community.icon.cps.score.test.integration.CPSClient;
import community.icon.cps.score.test.integration.ScoreIntegrationTest;
import community.icon.cps.score.test.integration.config.BaseConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import score.Address;

import static community.icon.cps.score.cpscore.utils.Constants.*;
import static community.icon.cps.score.test.AssertRevertedException.assertUserRevert;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static community.icon.cps.score.test.integration.Environment.preps;
import static org.junit.jupiter.api.Assertions.assertTrue;

import community.icon.cps.score.test.integration.scores.SystemInterfaceScoreClient;
import score.UserRevertException;
import score.annotation.Optional;

public class CPSCoreIntegration implements ScoreIntegrationTest {

    private static CPSClient ownerClient;
    private static CPSClient testClient;
    private static CPSClient readerClient;
    static Set<Map.Entry<Address, String>> prepSet = preps.entrySet();

    private final BigInteger ICX = BigInteger.valueOf(10).pow(18);

    private static Map<String, foundation.icon.jsonrpc.Address> addressMap;
    public static Map<Integer,CPSClient> cpsClients = new HashMap<>();
    @BeforeAll
    public static void setup() throws Exception{
        String contracts = "conf/contracts.json";
        CPS cps = new CPS(contracts);

        cps.setupCPS();
        addressMap = cps.getAddresses();
        ownerClient = cps.defaultClient();
        readerClient = cps.newClient(BigInteger.TEN.pow(24));
        testClient = cps.testClient();
        BaseConfig config = new BaseConfig(ownerClient);
        config.call();

        for (int i = 0; i < 7; i++) {
            String privKey = prepSet.toArray()[i].toString().substring(43);
            cpsClients.put(i,cps.customClient(privKey));

        }

    }

    @Test
    @Order(1)
    public void name(){
        ownerClient.cpsCore.name();
    }



    @DisplayName("register all preps")
    @Test
    @Order(1)
    public void registerPrep(){

        // expected Prep address
        List<String> expectedPrepAddress = new ArrayList<>(cpsClients.size());
        for (CPSClient prepClient:cpsClients.values()){
            prepClient.cpsCore.registerPrep();
            expectedPrepAddress.add(prepClient.getAddress().toString());

        }

        // actual prep address
        List<Map<String, Object>> preps = ownerClient.cpsCore.getPReps();
        List<String> actualPrepAddress = new ArrayList<>(preps.size());
        for (Map<String, Object> prep : preps) {
            actualPrepAddress.add((String) prep.get("address"));
        }

        assertEquals(expectedPrepAddress,actualPrepAddress);
//        SYSTEM_INTERFACE = new SystemInterfaceScoreClient(godClient);
//        System.out.println(SYSTEM_INTERFACE.getPRepTerm());
    }

    @Test
    @Order(2)
    public void registerPrepExceptions(){

        // readerClient tries to be registered as prep
        assertUserRevert(new UserRevertException(TAG+": Not a P-Rep"),
                () -> readerClient.cpsCore.registerPrep(),null);

        // registered prep registers again
        CPSClient registeredPrep = cpsClients.get(0);

        assertUserRevert(new UserRevertException(TAG+": P-Rep is already registered."),
                () -> registeredPrep.cpsCore.registerPrep(),null);

        // TODO: handle deny list
    }

    @DisplayName("unregister prep")
    @Test
    @Order(2)
    public void unregisterPrep(){
        CPSClient prep1 = cpsClients.get(0);
        prep1.cpsCore.unregisterPrep();

        // expected unregistered prep status
        Map<String, BigInteger> expectedPrepData = unregisteredPrepData();

        Map<String, BigInteger> actualPrepData = loginPrep(prep1.getAddress());
        assertEquals(expectedPrepData,actualPrepData);

        prep1.cpsCore.registerPrep();
    }


    @DisplayName("check prep data")
    @Test
    @Order(3)
    public void loginAsPrep(){
        // query period status
        Map<String,? > periodStatus = getPeriodStatus();
        assertEquals(periodStatus.get(PERIOD_NAME),"Application Period");

        CPSClient prep1 = cpsClients.get(0);
        // expected prep1 status
        Map<String, BigInteger> expectedPrepData = votingPrepData();

        Map<String, BigInteger> actualPrepData = loginPrep(prep1.getAddress());
        assertEquals(expectedPrepData,actualPrepData);
    }

    @DisplayName("submit proposal 1")
    @Test
    @Order(4)
    public void submitProposal(){
        CPSClient prep1 = cpsClients.get(0);
        ProposalAttributes proposalAttributes = new ProposalAttributes();

        proposalAttributes.ipfs_hash = "Test_Proposal_1";
        proposalAttributes.project_title = "Proposal_1";
        proposalAttributes.project_duration = 3;
        proposalAttributes.total_budget = BigInteger.valueOf(100);
        proposalAttributes.token = bnUSD;
        proposalAttributes.sponsor_address = prep1.getAddress();
        proposalAttributes.ipfs_link ="https://proposal_1";
        proposalAttributes.milestoneCount = 3;
        proposalAttributes.isMilestone = true;

        CPSCoreInterface.MilestonesAttributes milestonesAttributes1 = new CPSCoreInterface.MilestonesAttributes();
        milestonesAttributes1.completionPeriod = 1;
        milestonesAttributes1.budget = BigInteger.valueOf(30).multiply(ICX);
        milestonesAttributes1.id = 1;

        CPSCoreInterface.MilestonesAttributes milestonesAttributes2 = new CPSCoreInterface.MilestonesAttributes();
        milestonesAttributes2.completionPeriod = 2;
        milestonesAttributes2.budget = BigInteger.valueOf(40).multiply(ICX);
        milestonesAttributes2.id = 2;

        CPSCoreInterface.MilestonesAttributes milestonesAttributes3 = new CPSCoreInterface.MilestonesAttributes();
        milestonesAttributes3.completionPeriod = 3;
        milestonesAttributes3.budget = BigInteger.valueOf(20).multiply(ICX);
        milestonesAttributes3.id = 3; // TODO: check if same id is sent

        CPSCoreInterface.MilestonesAttributes[] milestonesAttributes =new CPSCoreInterface.MilestonesAttributes[]
                {milestonesAttributes1, milestonesAttributes2,milestonesAttributes3};
        ((CPSCoreInterfaceScoreClient) testClient.cpsCore).submitProposal(BigInteger.valueOf(50).multiply(ICX),proposalAttributes,milestonesAttributes);

        List<String> proposalsIpfs = readerClient.cpsCore.getProposalsKeysByStatus(SPONSOR_PENDING);
        assertEquals(proposalsIpfs.size(),1);
        assertEquals(proposalsIpfs.get(0),proposalAttributes.ipfs_hash);

        verifyProposalDetails(proposalAttributes);
    }

    private void verifyProposalDetails(ProposalAttributes expectedDetails){
        Map<String,Object> actualDetails = getProposalDetails(expectedDetails.ipfs_hash);
        assertEquals(actualDetails.get("project_title"), expectedDetails.project_title);
        assertEquals(actualDetails.get("sponsor_address"), expectedDetails.sponsor_address.toString());
        assertEquals(actualDetails.get("ipfs_hash"), expectedDetails.ipfs_hash);
        assertEquals(toInt((String)actualDetails.get("project_duration")), expectedDetails.project_duration);
        assertEquals(toInt((String)actualDetails.get("milestoneCount")), expectedDetails.milestoneCount);
        assertEquals(toBigInt((String)actualDetails.get("total_budget")), expectedDetails.total_budget.multiply(ICX));

    }


    @DisplayName("submit sponsor vote on first proposal")
    @Test
    @Order(5)
    public void submitSponsorVote(){
        CPSClient sponsorPrep = cpsClients.get(0);

        bnUSDMint(sponsorPrep.getAddress(),BigInteger.valueOf(50).multiply(ICX));

        byte[] data = createByteArray("sponsorVote",  "Test_Proposal_1",ACCEPT,
                "Proposal looks good");
        BigInteger sponsorBond = BigInteger.valueOf(15).multiply(ICX);
        sponsorPrep.bnUSD.transfer(addressMap.get("cpsCore"),sponsorBond,data);

        List<String> proposalsIpfs = readerClient.cpsCore.getProposalsKeysByStatus(PENDING);
        assertEquals(proposalsIpfs.size(),1);
        assertEquals(proposalsIpfs.get(0),"Test_Proposal_1");


        Map<String,Object> proposalDetails = getProposalDetails("Test_Proposal_1");
        assertEquals(sponsorBond, toBigInt((String) proposalDetails.get(SPONSOR_DEPOSIT_AMOUNT)));
        assertEquals(BOND_RECEIVED, proposalDetails.get(SPONSOR_DEPOSIT_STATUS));

        assertEquals(BigInteger.valueOf(35).multiply(ICX),readerClient.bnUSD.balanceOf(sponsorPrep.getAddress()));
    }

    @DisplayName("vote on first proposal")
    @Test()
    @Order(6)
    public void voteProposal() throws InterruptedException {
        // update Period
        updateNextBlock();
        ownerClient.cpsCore.updatePeriod();

        Map<String,? > periodStatus = getPeriodStatus();
        assertEquals(periodStatus.get(PERIOD_NAME),"Voting Period");

        Map<String,Object> proposalVote = getProposalVote("Test_Proposal_1");
        assertEquals(toInt((String)proposalVote.get("total_voters")),0);
        assertEquals(toInt((String)proposalVote.get("approve_voters")),0);
        assertEquals(toInt((String)proposalVote.get("reject_voters")),0);

        for (CPSClient prepClient:cpsClients.values()){
            voteByPrep(prepClient,"Test_Proposal_1",APPROVE,"Seems fruitful",false);
            prepClient.cpsCore.votePriority(new String[]{"Test_Proposal_1"});

        }
        proposalVote = getProposalVote("Test_Proposal_1");
        assertEquals(toInt((String)proposalVote.get("total_voters")),7);
        assertEquals(toInt((String)proposalVote.get("approve_voters")),7);
        assertEquals(toInt((String)proposalVote.get("reject_voters")),0);
        // TODO: approved votes does not match up
//        assertEquals(toBigInt((String)proposalVote.get("approved_votes")),BigInteger.valueOf(9540000).multiply(ICX));
    }

    @DisplayName("pass proposal 1 and send the initial payement")
    @Test
    @Order(7)
    public void transferFundsToProposal() throws InterruptedException {
        updateToApplicationPeriod();
        Map<String,? > periodStatus = getPeriodStatus();
        assertEquals(periodStatus.get(PERIOD_NAME),"Application Period");

        Map<String,?> contributorProject = getContributorFunds(testClient.getAddress());
        List<Map<String, ?>> contributorData = (List<Map<String, ?>> )contributorProject.get("data");

        assertEquals(toBigInt((String) contributorData.get(0).get("installment_amount")),BigInteger.valueOf(30).multiply(ICX));
        assertEquals(toBigInt((String) contributorData.get(0).get("total_budget")),BigInteger.valueOf(100).multiply(ICX));
        assertEquals( contributorData.get(0).get("total_installment_count"),"0x3");
        assertEquals(toBigInt((String) contributorData.get(0).get("total_installment_paid")),BigInteger.valueOf(10).multiply(ICX));
        assertEquals(contributorProject.get("project_count"),"0x1");


        Map<String,?> sponsorProject = getSponsorFunds(cpsClients.get(0).getAddress());

        List<Map<String,?>> sponsorData = (List<Map<String, ?>> )sponsorProject.get("data");
        assertEquals(toBigInt((String) sponsorData.get(0).get("installment_amount")),BigInteger.valueOf(6).multiply(ICX).divide(BigInteger.TEN));
        assertEquals(toBigInt((String) sponsorData.get(0).get("sponsor_bond_amount")),BigInteger.valueOf(15).multiply(ICX));
        assertEquals( sponsorData.get(0).get("total_installment_count"),"0x3");
        assertEquals(toBigInt((String) sponsorData.get(0).get("total_installment_paid")),BigInteger.valueOf(2).multiply(ICX).divide(BigInteger.TEN));
        assertEquals(toBigInt((String) sponsorProject.get("withdraw_amount_bnUSD")),BigInteger.valueOf(2).multiply(ICX).divide(BigInteger.TEN));
    }



    private Map<String,?> getContributorFunds(Address contributorAddr){
        return readerClient.cpsTreasury.getContributorProjectedFund(contributorAddr);
    }

    private Map<String,?> getSponsorFunds(Address sponsorAddr){
        return readerClient.cpsTreasury.getSponsorProjectedFund(sponsorAddr);
    }
    @DisplayName("submit first milestone")
    @Test
    @Order(8)
    public void submitMilestoneReport(){

        List<String> proposalsIpfs = readerClient.cpsCore.getProposalsKeysByStatus(ACTIVE);
        assertEquals(proposalsIpfs.size(),1);
        assertEquals(proposalsIpfs.get(0),"Test_Proposal_1");

        List<Map<String, Object>> activeProposal = readerClient.cpsCore.getActiveProposals(testClient.getAddress());
        assertEquals( "0x0", activeProposal.get(0).get("new_progress_report"));

        CPSCoreInterface.ProgressReportAttributes progressReportAttributes = createProgressReport("1");

        CPSCoreInterface.MilestoneSubmission submission = new CPSCoreInterface.MilestoneSubmission();
        submission.id = 1;
        submission.status = true;
        CPSCoreInterface.MilestoneSubmission[] milestoneSubmissions = new CPSCoreInterface.MilestoneSubmission[]{submission};

        testClient.cpsCore.submitProgressReport(progressReportAttributes,milestoneSubmissions);

        activeProposal = readerClient.cpsCore.getActiveProposals(testClient.getAddress());
        assertEquals( "0x1", activeProposal.get(0).get("new_progress_report"));

        Map<String,Object> progressReportDetails = getProgressReportDetails("Report_Proposal_1");
        assertEquals(1,toInt((String) progressReportDetails.get(MILESTONE_SUBMITTED_COUNT)));
        assertEquals(WAITING,progressReportDetails.get(STATUS));
        assertEquals("Report_1",progressReportDetails.get(PROGRESS_REPORT_TITLE));
        assertEquals("Report_Proposal_1",progressReportDetails.get(REPORT_HASH));

        int milestoneStatus = getMilestoneStatus("Test_Proposal_1",1);
        assertEquals(MILESTONE_REPORT_COMPLETED,milestoneStatus);
    }

    private ProgressReportAttributes createProgressReport(String no){
        ProgressReportAttributes progressReportAttributes = new ProgressReportAttributes();
        progressReportAttributes.ipfs_hash = "Test_Proposal_1";
        progressReportAttributes.progress_report_title = "Report_"+no;
        progressReportAttributes.report_hash ="Report_Proposal_"+no;
        progressReportAttributes.ipfs_link = "https://proposal_"+ no;
        progressReportAttributes.budget_adjustment = false;
        progressReportAttributes.additional_budget = BigInteger.ZERO;
        progressReportAttributes.additional_month = 0;

        return progressReportAttributes;

    }


    @DisplayName("vote on first progress report")
    @Test
    @Order(9)
    public void voteOnMilestone() throws InterruptedException {
        updateNextBlock();
        ownerClient.cpsCore.updatePeriod();

        Thread.sleep(2000);
        Map<String,? > periodStatus = getPeriodStatus();
        assertEquals(periodStatus.get(PERIOD_NAME),"Voting Period");

        MilestoneVoteAttributes[] voteAttributes = new MilestoneVoteAttributes[]{vote(1,APPROVE)};

        List<Map<String,Object>> expectedMilestoneData = new ArrayList<>(7);
        for (int i = 0; i < cpsClients.size(); i++) {
            cpsClients.get(i).cpsCore.voteProgressReport("Report_Proposal_1","Working well " + i,
                    voteAttributes,null,false);
            expectedMilestoneData.add(Map.of(
                    ADDRESS,cpsClients.get(i).getAddress(),
                    PREP_NAME,cpsClients.get(i).getAddress(),
                    VOTE_REASON,"Working well "+ i,
                    VOTE,APPROVE));
        }

        Map<String,Object> progressReportVote = getProgressReportVote("Report_Proposal_1");
        assertEquals(toInt((String) progressReportVote.get("total_voters")),7);

        Map<String, Object> milestoneVoteResult = getMilestoneReport("Report_Proposal_1", 1);
        assertEquals(toInt((String) milestoneVoteResult.get(APPROVE_VOTERS)),7);
        assertEquals(toInt((String) milestoneVoteResult.get(REJECT_VOTERS)),0);

        List<Map<String,Object>> actualMilestoneData = (List<Map<String, Object>>) milestoneVoteResult.get(DATA);


        for (int i = 0; i < 7; i++) {
            assertEquals(expectedMilestoneData.get(0).get(ADDRESS).toString(),actualMilestoneData.get(0).get(ADDRESS));
        }

        int milestoneStatus = getMilestoneStatus("Test_Proposal_1",1);
        assertEquals(MILESTONE_REPORT_COMPLETED,milestoneStatus);


    }

    private int getMilestoneStatus(String ipfsHash, int milestoneId){
        return readerClient.cpsCore.getMileststoneStatusOf(ipfsHash,milestoneId);
    }

    private Map<String,Object> getMilestoneReport(String reportHash, int milestoneId){
        return readerClient.cpsCore.getMilestoneVoteResult(reportHash,milestoneId);
    }

    private Map<String,Object> getProgressReportDetails(String reportHash){
        return readerClient.cpsCore.getProgressReportsByHash(reportHash);
    }

    private Map<String,Object> getProgressReportVote(String reportHash){
        return readerClient.cpsCore.getProgressReportResult(reportHash);
    }

    @DisplayName("submitting all remaining milestones")
    @Test
    @Order(10)
    public void submitAllMilestoneReport() throws InterruptedException {
        updateToApplicationPeriod();
        Map<String,? > periodStatus = getPeriodStatus();
        assertEquals(periodStatus.get(PERIOD_NAME),"Application Period");

        int milestoneStatus = getMilestoneStatus("Test_Proposal_1",1);
        assertEquals(MILESTONE_REPORT_APPROVED,milestoneStatus);


        // verifying installment count and funds record
        Map<String,?> contributorProject = getContributorFunds(testClient.getAddress());
        List<Map<String, ?>> contributorData = (List<Map<String, ?>> )contributorProject.get("data");
        assertEquals(toBigInt((String) contributorData.get(0).get("installment_amount")),BigInteger.valueOf(40).multiply(ICX));
        assertEquals(toBigInt((String) contributorData.get(0).get("total_installment_paid")),BigInteger.valueOf(40).multiply(ICX));
        assertEquals( contributorData.get(0).get("total_times_installment_paid"),"0x1");
        assertEquals(contributorProject.get("project_count"),"0x1");
        assertEquals(toBigInt((String) contributorProject.get("withdraw_amount_bnUSD")),BigInteger.valueOf(40).multiply(ICX));



        Map<String,?> sponsorProject = getSponsorFunds(cpsClients.get(0).getAddress());
        List<Map<String,?>> sponsorData = (List<Map<String, ?>> )sponsorProject.get("data");
        assertEquals(toBigInt((String) sponsorData.get(0).get("installment_amount")),BigInteger.valueOf(6).multiply(ICX).divide(BigInteger.TEN));
        assertEquals(toBigInt((String) sponsorData.get(0).get("total_installment_paid")),BigInteger.valueOf(8).multiply(ICX).divide(BigInteger.TEN));
        assertEquals( sponsorData.get(0).get("total_times_installment_paid"),"0x1");
        assertEquals(toBigInt((String) sponsorProject.get("withdraw_amount_bnUSD")),BigInteger.valueOf(8).multiply(ICX).divide(BigInteger.TEN));


        CPSCoreInterface.ProgressReportAttributes progressReportAttributes = createProgressReport("2");

        CPSCoreInterface.MilestoneSubmission submissionNo2 = new CPSCoreInterface.MilestoneSubmission();
        submissionNo2.id = 2;
        submissionNo2.status = true;

        CPSCoreInterface.MilestoneSubmission submissionNo3 = new CPSCoreInterface.MilestoneSubmission();
        submissionNo3.id = 3;
        submissionNo3.status = true;

        CPSCoreInterface.MilestoneSubmission[] milestoneSubmissions = new CPSCoreInterface.MilestoneSubmission[]{
                submissionNo2,submissionNo3};

        testClient.cpsCore.submitProgressReport(progressReportAttributes,milestoneSubmissions);


        Map<String,Object> progressReportDetails = getProgressReportDetails("Report_Proposal_2");
        assertEquals(2,toInt((String) progressReportDetails.get(MILESTONE_SUBMITTED_COUNT)));
//        assertEquals(new String[]{"0x2","0x3"},progressReportDetails.get("milestoneId"));
        assertEquals(WAITING,progressReportDetails.get(STATUS));
        assertEquals("Report_2",progressReportDetails.get(PROGRESS_REPORT_TITLE));
        assertEquals("Report_Proposal_2",progressReportDetails.get(REPORT_HASH));

    }


    @DisplayName("vote on progress report 2")
    @Test
    @Order(11)
    public void voteOnAllMilestone() throws InterruptedException {

        updateNextBlock();
        ownerClient.cpsCore.updatePeriod();

        Thread.sleep(2000);
        Map<String,? > periodStatus = getPeriodStatus();
        assertEquals(periodStatus.get(PERIOD_NAME),"Voting Period");

        MilestoneVoteAttributes[] voteAttributes = new MilestoneVoteAttributes[]{vote(2,APPROVE), vote(3,APPROVE)};

        List<Map<String,Object>> expectedMilestoneData = new ArrayList<>(7);
        for (int i = 0; i < cpsClients.size(); i++) {
            cpsClients.get(i).cpsCore.voteProgressReport("Report_Proposal_2","Project is completed " + i,
                    voteAttributes,null,false);
            expectedMilestoneData.add(Map.of(
                    ADDRESS,cpsClients.get(i).getAddress(),
                    PREP_NAME,cpsClients.get(i).getAddress(),
                    VOTE_REASON,"Project is completed "+ i,
                    VOTE,APPROVE));
        }

        Map<String,Object> progressReportVote = getProgressReportVote("Report_Proposal_2");
        assertEquals(toInt((String) progressReportVote.get("total_voters")),7);

        Map<String, Object> milestoneVoteResult = getMilestoneReport("Report_Proposal_2", 2);
        assertEquals(toInt((String) milestoneVoteResult.get(APPROVE_VOTERS)),7);
        assertEquals(toInt((String) milestoneVoteResult.get(REJECT_VOTERS)),0);

//        assertEquals(milestoneVoteResult.get(DATA),expectedMilestoneData);
        List<Map<String,Object>> actualMilestoneData = (List<Map<String, Object>>) milestoneVoteResult.get(DATA);


        for (int i = 0; i < 7; i++) {
            assertEquals(expectedMilestoneData.get(0).get(ADDRESS).toString(),actualMilestoneData.get(0).get(ADDRESS));
            assertEquals(expectedMilestoneData.get(0).get(PREP_NAME).toString(),actualMilestoneData.get(0).get(PREP_NAME));
            assertEquals(expectedMilestoneData.get(0).get(VOTE_REASON),actualMilestoneData.get(0).get(VOTE_REASON));
            assertEquals(expectedMilestoneData.get(0).get(VOTE),actualMilestoneData.get(0).get(VOTE));

        }

        int milestoneStatus = getMilestoneStatus("Test_Proposal_1",2);
        assertEquals(MILESTONE_REPORT_COMPLETED,milestoneStatus);

    }

    @DisplayName("proposal 1 is completed and user claims reward")
    @Test
    @Order(12)
    public void completeProposal1() throws InterruptedException {
        updateToApplicationPeriod();

        int milestoneStatus = getMilestoneStatus("Test_Proposal_1",3);
        assertEquals(MILESTONE_REPORT_APPROVED,milestoneStatus);

        milestoneStatus = getMilestoneStatus("Test_Proposal_1",2);
        assertEquals(MILESTONE_REPORT_APPROVED,milestoneStatus);

        List<String> proposalsIpfs = readerClient.cpsCore.getProposalsKeysByStatus(COMPLETED);
        assertEquals(proposalsIpfs.size(),1);
        assertEquals(proposalsIpfs.get(0),"Test_Proposal_1");


        assertEquals(BigInteger.ZERO,readerClient.bnUSD.balanceOf(testClient.getAddress()));
        testClient.cpsTreasury.claimReward();
        assertEquals(BigInteger.valueOf(100).multiply(ICX),readerClient.bnUSD.balanceOf(testClient.getAddress()));

        CPSClient sponsorClient = cpsClients.get(0);
        assertEquals(BigInteger.valueOf(35).multiply(ICX),readerClient.bnUSD.balanceOf(sponsorClient.getAddress()));
        sponsorClient.cpsTreasury.claimReward();
        assertEquals(BigInteger.valueOf(37).multiply(ICX),readerClient.bnUSD.balanceOf(sponsorClient.getAddress()));

        // sponsor bond is returned after completion of project
        sponsorClient.cpsCore.claimSponsorBond();
        assertEquals(BigInteger.valueOf(52).multiply(ICX), readerClient.bnUSD.balanceOf(sponsorClient.getAddress()));


    }

    //    @DisplayName("proposal with greater milestone than project duration")
    @DisplayName("submit proposal in different conditions")
    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class submitProposal {
        CPSClient sponsorPrep = cpsClients.get(0);

        @DisplayName("proposal with zero milestone")
        @Test
        @Order(1)
        public void zeroMilestoneProposal() {
            CPSClient sponsorPrep = cpsClients.get(0);

            ProposalAttributes proposalAttributes = new ProposalAttributes();

            proposalAttributes.ipfs_hash = "Test_Proposal_2";
            proposalAttributes.project_title = "Proposal_2";
            proposalAttributes.project_duration = 3;
            proposalAttributes.total_budget = BigInteger.valueOf(100);
            proposalAttributes.token = bnUSD;
            proposalAttributes.sponsor_address = sponsorPrep.getAddress();
            proposalAttributes.ipfs_link = "https://proposal_1";
            proposalAttributes.milestoneCount = 3;
            proposalAttributes.isMilestone = true;
            ;

            CPSCoreInterface.MilestonesAttributes[] milestonesAttributes = new MilestonesAttributes[]
                    {};

            assertUserRevert(new UserRevertException(TAG + ": Milestone count mismatch"),
                    () -> ((CPSCoreInterfaceScoreClient) testClient.cpsCore).submitProposal(BigInteger.valueOf(50).multiply(ICX), proposalAttributes, milestonesAttributes),
                    null);
        }

        @DisplayName("submit proposal when maintenance is on")
        @Test
        @Order(1)
        public void maintenanceModeOff() {

            ownerClient.cpsCore.toggleMaintenance();

            CPSClient sponsorPrep = cpsClients.get(0);

            ProposalAttributes proposalAttributes = new ProposalAttributes();

            proposalAttributes.ipfs_hash = "Test_Proposal_2";
            proposalAttributes.project_title = "Proposal_2";
            proposalAttributes.project_duration = 1;
            proposalAttributes.total_budget = BigInteger.valueOf(50);
            proposalAttributes.token = bnUSD;
            proposalAttributes.sponsor_address = sponsorPrep.getAddress();
            proposalAttributes.ipfs_link = "https://proposal_1";
            proposalAttributes.milestoneCount = 1;
            proposalAttributes.isMilestone = true;

            CPSCoreInterface.MilestonesAttributes milestonesAttributes1 = new CPSCoreInterface.MilestonesAttributes();
            milestonesAttributes1.completionPeriod = 1;
            milestonesAttributes1.budget = BigInteger.valueOf(50).multiply(ICX);
            milestonesAttributes1.id = 1;

            CPSCoreInterface.MilestonesAttributes[] milestonesAttributes = new MilestonesAttributes[]
                    {milestonesAttributes1};

            assertUserRevert(new UserRevertException(TAG + ": Maintenance mode is on. Will resume soon."),
                    () -> ((CPSCoreInterfaceScoreClient) testClient.cpsCore).submitProposal(BigInteger.valueOf(50).multiply(ICX), proposalAttributes, milestonesAttributes),
                    null);

        }

        @DisplayName("submit proposal when same milestone id")
        @Test
        @Order(1)
        public void milestoneWithSameId() {

            ownerClient.cpsCore.toggleMaintenance();

            CPSClient sponsorPrep = cpsClients.get(0);

            ProposalAttributes proposalAttributes = new ProposalAttributes();

            proposalAttributes.ipfs_hash = "Test_Proposal_2";
            proposalAttributes.project_title = "Proposal_2";
            proposalAttributes.project_duration = 2;
            proposalAttributes.total_budget = BigInteger.valueOf(50);
            proposalAttributes.token = bnUSD;
            proposalAttributes.sponsor_address = sponsorPrep.getAddress();
            proposalAttributes.ipfs_link = "https://proposal_1";
            proposalAttributes.milestoneCount = 2;
            proposalAttributes.isMilestone = true;

            CPSCoreInterface.MilestonesAttributes milestonesAttributes1 = new CPSCoreInterface.MilestonesAttributes();
            milestonesAttributes1.completionPeriod = 1;
            milestonesAttributes1.budget = BigInteger.valueOf(50).multiply(ICX);
            milestonesAttributes1.id = 1;

            CPSCoreInterface.MilestonesAttributes milestonesAttributes2 = new CPSCoreInterface.MilestonesAttributes();
            milestonesAttributes2.completionPeriod = 2;
            milestonesAttributes2.budget = BigInteger.valueOf(40).multiply(ICX);
            milestonesAttributes2.id = 1;

            CPSCoreInterface.MilestonesAttributes[] milestonesAttributes = new MilestonesAttributes[]
                    {milestonesAttributes1, milestonesAttributes2};

            assertUserRevert(new UserRevertException(TAG + ":: Total milestone budget and project budget is not equal"),
                    () -> ((CPSCoreInterfaceScoreClient) testClient.cpsCore).submitProposal(BigInteger.valueOf(50).multiply(ICX), proposalAttributes, milestonesAttributes),
                    null);

        }

        @DisplayName("submit proposal when with higher milestone count than project duration")
        @Test
        @Order(2)
        public void proposalWithMoreMilestone() {

//            ownerClient.cpsCore.toggleMaintenance();

            CPSClient sponsorPrep = cpsClients.get(0);

            ProposalAttributes proposalAttributes = new ProposalAttributes();

            proposalAttributes.ipfs_hash = "Test_Proposal_2";
            proposalAttributes.project_title = "Proposal_2";
            proposalAttributes.project_duration = 2;
            proposalAttributes.total_budget = BigInteger.valueOf(100);
            proposalAttributes.token = bnUSD;
            proposalAttributes.sponsor_address = sponsorPrep.getAddress();
            proposalAttributes.ipfs_link = "https://proposal_1";
            proposalAttributes.milestoneCount = 3;
            proposalAttributes.isMilestone = true;

            CPSCoreInterface.MilestonesAttributes milestonesAttributes1 = new CPSCoreInterface.MilestonesAttributes();
            milestonesAttributes1.completionPeriod = 1;
            milestonesAttributes1.budget = BigInteger.valueOf(30).multiply(ICX);
            milestonesAttributes1.id = 1;

            CPSCoreInterface.MilestonesAttributes milestonesAttributes2 = new CPSCoreInterface.MilestonesAttributes();
            milestonesAttributes2.completionPeriod = 2;
            milestonesAttributes2.budget = BigInteger.valueOf(40).multiply(ICX);
            milestonesAttributes2.id = 2;

            CPSCoreInterface.MilestonesAttributes milestonesAttributes3 = new CPSCoreInterface.MilestonesAttributes();
            milestonesAttributes3.completionPeriod = 3;
            milestonesAttributes3.budget = BigInteger.valueOf(20).multiply(ICX);
            milestonesAttributes3.id = 3;


            CPSCoreInterface.MilestonesAttributes[] milestonesAttributes = new MilestonesAttributes[]
                    {milestonesAttributes1, milestonesAttributes2, milestonesAttributes3};

            ((CPSCoreInterfaceScoreClient) testClient.cpsCore).submitProposal(BigInteger.valueOf(50).multiply(ICX),
                    proposalAttributes, milestonesAttributes);
        }

        @DisplayName("proposal with higher fund than in treasury")
        @Test
        @Order(2)
        public void proposalWithHighBudget() {


            BigInteger treasuryBalance = readerClient.bnUSD.balanceOf(addressMap.get("cpfTreasury"));
            BigInteger totalBudget = BigInteger.valueOf(6000);

            assertTrue(treasuryBalance.compareTo(totalBudget.multiply(ICX)) < 0);

            ProposalAttributes proposalAttributes = new ProposalAttributes();

            proposalAttributes.ipfs_hash = "Test_Proposal_3";
            proposalAttributes.project_title = "Proposal_3";
            proposalAttributes.project_duration = 3;
            proposalAttributes.total_budget = totalBudget;
            proposalAttributes.token = bnUSD;
            proposalAttributes.sponsor_address = sponsorPrep.getAddress();
            proposalAttributes.ipfs_link = "https://proposal_1";
            proposalAttributes.milestoneCount = 3;
            proposalAttributes.isMilestone = true;

            CPSCoreInterface.MilestonesAttributes milestonesAttributes1 = new CPSCoreInterface.MilestonesAttributes();
            milestonesAttributes1.completionPeriod = 1;
            milestonesAttributes1.budget = BigInteger.valueOf(3000).multiply(ICX);
            milestonesAttributes1.id = 1;

            CPSCoreInterface.MilestonesAttributes milestonesAttributes2 = new CPSCoreInterface.MilestonesAttributes();
            milestonesAttributes2.completionPeriod = 2;
            milestonesAttributes2.budget = BigInteger.valueOf(2000).multiply(ICX);
            milestonesAttributes2.id = 2;

            CPSCoreInterface.MilestonesAttributes milestonesAttributes3 = new CPSCoreInterface.MilestonesAttributes();
            milestonesAttributes3.completionPeriod = 3;
            milestonesAttributes3.budget = BigInteger.valueOf(400).multiply(ICX);
            milestonesAttributes3.id = 3;


            CPSCoreInterface.MilestonesAttributes[] milestonesAttributes = new MilestonesAttributes[]
                    {milestonesAttributes1, milestonesAttributes2, milestonesAttributes3};

            ((CPSCoreInterfaceScoreClient) testClient.cpsCore).submitProposal(BigInteger.valueOf(50).multiply(ICX),
                    proposalAttributes, milestonesAttributes);
        }

        @DisplayName("proposal rejected by sponsor")
        @Test
        @Order(2)
        public void proposalRejectedBySponsor() {


            ProposalAttributes proposalAttributes = new ProposalAttributes();

            proposalAttributes.ipfs_hash = "Test_Proposal_4";
            proposalAttributes.project_title = "Proposal_4";
            proposalAttributes.project_duration = 1;
            proposalAttributes.total_budget = BigInteger.valueOf(100);
            proposalAttributes.token = bnUSD;
            proposalAttributes.sponsor_address = sponsorPrep.getAddress();
            proposalAttributes.ipfs_link = "https://proposal_1";
            proposalAttributes.milestoneCount = 1;
            proposalAttributes.isMilestone = true;

            CPSCoreInterface.MilestonesAttributes milestonesAttributes1 = new CPSCoreInterface.MilestonesAttributes();
            milestonesAttributes1.completionPeriod = 1;
            milestonesAttributes1.budget = BigInteger.valueOf(90).multiply(ICX);
            milestonesAttributes1.id = 1;


            CPSCoreInterface.MilestonesAttributes[] milestonesAttributes = new MilestonesAttributes[]
                    {milestonesAttributes1};

            ((CPSCoreInterfaceScoreClient) testClient.cpsCore).submitProposal(BigInteger.valueOf(50).multiply(ICX),
                    proposalAttributes, milestonesAttributes);
        }

        @DisplayName("proposal which is not sincere")
        @Test
        @Order(2)
        public void proposalWillBeRejected() {
            ProposalAttributes proposalAttributes = new ProposalAttributes();

            proposalAttributes.ipfs_hash = "Test_Proposal_5";
            proposalAttributes.project_title = "Proposal_5";
            proposalAttributes.project_duration = 1;
            proposalAttributes.total_budget = BigInteger.valueOf(100);
            proposalAttributes.token = bnUSD;
            proposalAttributes.sponsor_address = sponsorPrep.getAddress();
            proposalAttributes.ipfs_link = "https://proposal_1";
            proposalAttributes.milestoneCount = 1;
            proposalAttributes.isMilestone = true;

            CPSCoreInterface.MilestonesAttributes milestonesAttributes1 = new CPSCoreInterface.MilestonesAttributes();
            milestonesAttributes1.completionPeriod = 1;
            milestonesAttributes1.budget = BigInteger.valueOf(90).multiply(ICX);
            milestonesAttributes1.id = 1;


            CPSCoreInterface.MilestonesAttributes[] milestonesAttributes = new MilestonesAttributes[]
                    {milestonesAttributes1};

            ((CPSCoreInterfaceScoreClient) testClient.cpsCore).submitProposal(BigInteger.valueOf(50).multiply(ICX),
                    proposalAttributes, milestonesAttributes);
        }


        @DisplayName("sponsor vote on the proposal")
        @Test
        @Order(3)
        public void sponsorBondsWithProposals() {

            bnUSDMint(sponsorPrep.getAddress(), BigInteger.valueOf(1000).multiply(ICX));

            BigInteger beforeBalance = readerClient.bnUSD.balanceOf(addressMap.get("cpsCore"));

            byte[] data = createByteArray("sponsorVote", "Test_Proposal_2", ACCEPT,
                    "Bonding with proposal 2");
            BigInteger sponsorBond = BigInteger.valueOf(15).multiply(ICX);
            sponsorPrep.bnUSD.transfer(addressMap.get("cpsCore"), sponsorBond, data);

            BigInteger afterBalance = readerClient.bnUSD.balanceOf(addressMap.get("cpsCore"));
            assertEquals(beforeBalance.add(sponsorBond), afterBalance);

            beforeBalance = afterBalance;
            data = createByteArray("sponsorVote", "Test_Proposal_3", ACCEPT,
                    "Bonding with proposal 3");
            sponsorBond = BigInteger.valueOf(900).multiply(ICX);
            sponsorPrep.bnUSD.transfer(addressMap.get("cpsCore"), sponsorBond, data);

            afterBalance = readerClient.bnUSD.balanceOf(addressMap.get("cpsCore"));
            assertEquals(beforeBalance.add(sponsorBond), afterBalance);


            beforeBalance = afterBalance;
            data = createByteArray("sponsorVote", "Test_Proposal_4", REJECT,
                    "Rejecting proposal 4");
            sponsorBond = BigInteger.ZERO;
            sponsorPrep.bnUSD.transfer(addressMap.get("cpsCore"), sponsorBond, data);

            afterBalance = readerClient.bnUSD.balanceOf(addressMap.get("cpsCore"));
            assertEquals(beforeBalance.add(sponsorBond), afterBalance);

            beforeBalance = afterBalance;
            data = createByteArray("sponsorVote", "Test_Proposal_5", ACCEPT,
                    "Bonding with proposal 5");
            sponsorBond = BigInteger.valueOf(15).multiply(ICX);
            sponsorPrep.bnUSD.transfer(addressMap.get("cpsCore"), sponsorBond, data);
            afterBalance = readerClient.bnUSD.balanceOf(addressMap.get("cpsCore"));
            assertEquals(beforeBalance.add(sponsorBond), afterBalance);

        }


        @DisplayName("voting on proposal conditions")
        @Nested
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class voteProposal {

            String[] votingProposal = new String[]{"Test_Proposal_2", "Test_Proposal_5", "Test_Proposal_3"};

            @DisplayName("verify proposal which are active")
            @Test
            @Order(3)
            public void verifyPendingProposal() {

                List<String> proposalKey = getProposalByStatus(PENDING);

                for (String key : votingProposal) {
                    assertTrue(proposalKey.contains(key));
                }

            }

            @DisplayName("vote on proposal with higher milestone count than project duration")
            @Test
            @Order(3)
            public void voteOnProposalWithMoreMilestone() throws InterruptedException {
                updateNextBlock();
                ownerClient.cpsCore.updatePeriod();

                Thread.sleep(2000);
                verifyPeriod("Voting Period");

                for (CPSClient prepClient : cpsClients.values()) {
                    voteByPrep(prepClient, "Test_Proposal_2", APPROVE, "Passing Proposal_2", false);
                }
            }

            @DisplayName("vote on proposal with higher fund than in treasury")
            @Test
            @Order(4)
            public void voteOnProposalWithHighBudget() {

                for (CPSClient prepClient : cpsClients.values()) {
                    voteByPrep(prepClient, "Test_Proposal_3", APPROVE, "Passing Proposal_3", false);
                }
            }

            @DisplayName("reject proposal")
            @Test
            @Order(4)
            public void rejectProposal() {
                for (CPSClient prepClient : cpsClients.values()) {

                    if (prepClient.equals(cpsClients.get(0))) {
                        voteByPrep(prepClient, "Test_Proposal_5", APPROVE, "Looks good", false);
                        continue;
                    }
                    voteByPrep(prepClient, "Test_Proposal_5", REJECT, "Rejecting Proposal_5", false);

                }

                Map<String, Object> proposalVote = getProposalVote("Test_Proposal_5");
                assertEquals(toInt((String) proposalVote.get(APPROVE_VOTERS)), 1);
                assertEquals(toInt((String) proposalVote.get(REJECT_VOTERS)), 6);
                assertEquals(toInt((String) proposalVote.get(TOTAL_VOTERS)), 7);

                // one of the prep change their vote to abstain
                voteByPrep(cpsClients.get(4), "Test_Proposal_5", ABSTAIN, "Changing vote", true);

                proposalVote = getProposalVote("Test_Proposal_5");
                assertEquals(toInt((String) proposalVote.get(APPROVE_VOTERS)), 1);
                assertEquals(toInt((String) proposalVote.get(REJECT_VOTERS)), 5);
                assertEquals(toInt((String) proposalVote.get(ABSTAIN_VOTERS)), 1);
                assertEquals(toInt((String) proposalVote.get(TOTAL_VOTERS)), 7);

            }

            @DisplayName("vote priority txn except one ")
            @Test
            @Order(4)
            public void votePriority() {
                for (CPSClient prepClient : cpsClients.values()) {
                    if (prepClient.equals(cpsClients.get(2))) {
                        continue;
                    }
                    prepClient.cpsCore.votePriority(votingProposal);

                }

                // trying to Register by deny prep TODO
            }


            @DisplayName("updating to application period")
            @Nested
            @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
            class applicationChecks {

                /* Test_Proposal_2 is approved, Test_Proposal_5 is rejected, Test_Proposal_3 is still in pending
                 * */

                @DisplayName("update to application period")
                @Test
                @Order(5)
                public void checkAfterUpdatePeriod() throws InterruptedException {

                    Map<String, BigInteger> sponsorBalance = claimableSponsorBond(cpsClients.get(0).getAddress());
                    assertEquals(BigInteger.ZERO, sponsorBalance.get(bnUSD));


                    updateToApplicationPeriod();

                    Map<String, Object> activeProposal = readerClient.cpsCore.getActiveProposalsList(0);

                    List<Map<String, Object>> proposalData = (List<Map<String, Object>>) activeProposal.get(DATA);
                    assertEquals(proposalData.get(0).get(IPFS_HASH), "Test_Proposal_2");

                    List<String> pendingProposal = getProposalByStatus(PENDING);
                    assertTrue(pendingProposal.contains("Test_Proposal_3"));

                    List<String> activeProposals = getProposalByStatus(ACTIVE);
                    assertTrue(activeProposals.contains("Test_Proposal_2"));

                    List<String> rejectedProposals = getProposalByStatus(REJECTED);
                    assertTrue(rejectedProposals.contains("Test_Proposal_4"));
                    assertTrue(rejectedProposals.contains("Test_Proposal_5"));

                    BigInteger sponsorBondOnProposal5 = BigInteger.valueOf(15).multiply(ICX);
                    sponsorBalance = claimableSponsorBond(cpsClients.get(0).getAddress());
                    assertEquals(sponsorBondOnProposal5, sponsorBalance.get(bnUSD));


                    List<Address> denyList = readerClient.cpsCore.getDenylist();
                    assertTrue(denyList.contains(cpsClients.get(2).getAddress()));

                    Address prep = cpsClients.get(2).getAddress();
                    Map<String, BigInteger> prepData = loginPrep(prep);
                    assertEquals(prepData, denyPrepData());
                }

                @DisplayName("prep pays penalty and registers")
                @Test
                @Order(6)
                public void prepPayPenalty() {
                    CPSClient prep = cpsClients.get(2);
                    BigInteger penaltyAmount = (BigInteger.TWO.multiply(ICX)).divide(BigInteger.valueOf(100));

                    bnUSDMint(prep.getAddress(), BigInteger.TWO.multiply(ICX));

                    byte[] data = penaltyByteArray("payPrepPenalty");
                    // TODO: pay the penalty
                    ownerClient.cpsCore.removeDenylistPreps();
//                    prep.bnUSD.transfer(addressMap.get("cpsCore"),penaltyAmount,data);


                    Map<String, BigInteger> prepData = loginPrep(prep.getAddress());
                    assertEquals(prepData, unregisteredPrepData());


                    prep.cpsCore.registerPrep();
                    Map<String, BigInteger> prepData2 = loginPrep(prep.getAddress());

                    assertEquals(prepData2, votingPrepData());
                }


                @DisplayName("submit progress reports")
                @Nested
                @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
                class submitProgressReport {

                    @DisplayName("submit progress report with not completed status")
                    @Order(7)
                    @Test
                    public void progressReportNotCompleted() {

                        CPSCoreInterface.ProgressReportAttributes progressReportAttributes = createProgressReport("1",
                                "Test_Proposal_2");

                        assertEquals(getMilestoneStatus("Test_Proposal_2", 1), 0);


                        CPSCoreInterface.MilestoneSubmission submission = new CPSCoreInterface.MilestoneSubmission();
                        submission.id = 1;
                        submission.status = false;
                        CPSCoreInterface.MilestoneSubmission[] milestoneSubmissions = new CPSCoreInterface.MilestoneSubmission[]{submission};

                        testClient.cpsCore.submitProgressReport(progressReportAttributes, milestoneSubmissions);

                        assertEquals(getMilestoneStatus("Test_Proposal_2", 1), MILESTONE_REPORT_NOT_COMPLETED);

                        System.out.println("-----boom----" + readerClient.cpsCore.getMilestonesReport("Test_Proposal_2", 1));

                    }

                    @DisplayName("vote progress reports and proposal")
                    @Nested
                    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
                    class voteOnProposalAndPR {

                        @Test
                        @Order(8)
                        public void toVotingPeriod() throws InterruptedException {
                            // minting bnUSD on cpfTreasury to pass proposal 3
                            bnUSDMint(addressMap.get("cpfTreasury"), BigInteger.valueOf(5000).multiply(ICX));

                            updateNextBlock();
                            ownerClient.cpsCore.updatePeriod();
                        }

                        @DisplayName("vote on PR and proposal 3")
                        @Test
                        @Order(9)
                        public void votePRandProposal() {
                            for (CPSClient prepClient : cpsClients.values()) {
                                // proposal voting
                                voteByPrep(prepClient, "Test_Proposal_3", APPROVE, "Voting Again", false);
                                prepClient.cpsCore.votePriority(new String[]{"Test_Proposal_3"});


                                MilestoneVoteAttributes[] voteAttributes = new MilestoneVoteAttributes[]{vote(1, APPROVE)};
                                prepClient.cpsCore.voteProgressReport("Test_Proposal_2_Report_Proposal_1",
                                        "Seems right ",
                                        voteAttributes, null, false);

                            }

                            Map<String, Object> proposalVote = getProposalVote("Test_Proposal_3");
                            assertEquals(toInt((String) proposalVote.get(APPROVE_VOTERS)), 7);
                            assertEquals(toInt((String) proposalVote.get(REJECT_VOTERS)), 0);
                            assertEquals(toInt((String) proposalVote.get(TOTAL_VOTERS)), 7);

                            Map<String, Object> milestoneVoteResult = getMilestoneReport("Test_Proposal_2_Report_Proposal_1", 1);
                            assertEquals(toInt((String) milestoneVoteResult.get(APPROVE_VOTERS)), 7);
                            assertEquals(toInt((String) milestoneVoteResult.get(REJECT_VOTERS)), 0);

//                            MilestoneVoteAttributes[] voteAttributes = new MilestoneVoteAttributes[]{vote(1,REJECT)};

                            // one of the prep changes their vote on the progress report
//                            cpsClients.get(0).cpsCore.voteProgressReport("Test_Proposal_2_Report_Proposal_1",
//                                    "Changing vote ",
//                                    voteAttributes,null,true);
//
//                            milestoneVoteResult = getMilestoneReport("Test_Proposal_2_Report_Proposal_1", 1);
//                            assertEquals(toInt((String) milestoneVoteResult.get(APPROVE_VOTERS)),6);
//                            assertEquals(toInt((String) milestoneVoteResult.get(REJECT_VOTERS)),1);
                        }


                        @DisplayName("submit progress report for proposal 2 and 3 ")
                        @Nested
                        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
                        class SecondProgressReport {

                            @DisplayName("update to application period and assert values")
                            @Order(10)
                            @Test
                            public void checksInApplicationPeriod() throws InterruptedException {

                                String proposal2 = "Test_Proposal_2";
                                Map<String, Object> proposalDetails = getProposalDetails(proposal2);
                                assertEquals(toInt((String) proposalDetails.get(PROJECT_DURATION)), 2);

                                Map<String, Object> milestoneDetails = getMilestonesReport(proposal2, 1);
                                assertEquals(milestoneDetails.get(EXTENSION_FLAG), "0x0");
                                assertEquals(toInt((String) milestoneDetails.get(COMPLETION_PERIOD)), 1);


                                updateToApplicationPeriod();
                                verifyPeriod(APPLICATION_PERIOD);

                                // milestone completion period and project duration increases
                                milestoneDetails = getMilestonesReport(proposal2, 1);
                                assertEquals(milestoneDetails.get(EXTENSION_FLAG), "0x1");
                                assertEquals(toInt((String) milestoneDetails.get(COMPLETION_PERIOD)), 2);

                                proposalDetails = getProposalDetails(proposal2);
                                assertEquals(toInt((String) proposalDetails.get(PROJECT_DURATION)), 3);

                                List<String> activeProposals = getProposalByStatus(ACTIVE);

                                assertTrue(activeProposals.contains("Test_Proposal_2"));
                                assertTrue(activeProposals.contains("Test_Proposal_3"));

                            }

                            @DisplayName("submit progress report with completed status ")
                            @Order(11)
                            @Test
                            public void progressReportCompleted() {
                                CPSCoreInterface.MilestoneSubmission submission = new CPSCoreInterface.MilestoneSubmission();
                                submission.id = 1;
                                submission.status = true;
                                CPSCoreInterface.MilestoneSubmission[] milestoneSubmissions = new CPSCoreInterface.MilestoneSubmission[]{submission};

                                // for proposal 2
                                String proposal2 = "Test_Proposal_2";
                                assertEquals(getMilestoneStatus("Test_Proposal_2", 1), MILESTONE_REPORT_NOT_COMPLETED);

                                CPSCoreInterface.ProgressReportAttributes progressReportAttributes = createProgressReport("2", proposal2);

                                testClient.cpsCore.submitProgressReport(progressReportAttributes, milestoneSubmissions);

                                assertEquals(getMilestoneStatus("Test_Proposal_2", 1), MILESTONE_REPORT_COMPLETED);

                                // for proposal 3
                                String proposal3 = "Test_Proposal_3";
                                assertEquals(getMilestoneStatus("Test_Proposal_3", 1), 0);

                                progressReportAttributes = createProgressReport("1", proposal3);

                                testClient.cpsCore.submitProgressReport(progressReportAttributes, milestoneSubmissions);

                                assertEquals(getMilestoneStatus("Test_Proposal_3", 1), MILESTONE_REPORT_COMPLETED);

                            }

                            @DisplayName("vote progress report for proposal 2 and 3. ")
                            @Nested
                            @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
                            class voteSecondReport {
                                /* Progress report for proposal 2 will be accepted and proposal 3 will be rejected */
                                @DisplayName("to voting period")
                                @Order(12)
                                @Test
                                public void updateToVotingPeriod() throws InterruptedException {
                                    updateNextBlock();
                                    ownerClient.cpsCore.updatePeriod();
                                    verifyPeriod(VOTING_PERIOD);
                                    // vote on progress report 2 with acceptance
                                }

                                @DisplayName("vote on progress reports")
                                @Order(12)
                                @Test
                                public void voteProgressReport() {
                                    String reportForPropsoal2 = "Test_Proposal_2_Report_Proposal_2";
                                    String reportForPropsoal3 = "Test_Proposal_3_Report_Proposal_1";


                                    MilestoneVoteAttributes[] voteAttributes = new MilestoneVoteAttributes[]{vote(1, APPROVE)};
                                    for (CPSClient prepClient : cpsClients.values()) {
                                        prepClient.cpsCore.voteProgressReport(reportForPropsoal2,
                                                "Seems right ",
                                                voteAttributes, null, false);

                                        prepClient.cpsCore.voteProgressReport(reportForPropsoal3,
                                                "Looks legit ",
                                                voteAttributes, null, false);
                                    }

                                    MilestoneVoteAttributes[] rejectVoteAttributes = new MilestoneVoteAttributes[]{vote(1, REJECT)};
                                    cpsClients.get(0).cpsCore.voteProgressReport(reportForPropsoal3, "I reject",
                                            rejectVoteAttributes, null, true);

                                    // verify the votes
                                    Map<String, Object> milestoneVoteResult = getMilestoneReport(reportForPropsoal2, 1);
                                    assertEquals(toInt((String) milestoneVoteResult.get(APPROVE_VOTERS)), 7);
                                    assertEquals(toInt((String) milestoneVoteResult.get(REJECT_VOTERS)), 0);

                                    milestoneVoteResult = getMilestoneReport(reportForPropsoal3, 1);
                                    assertEquals(toInt((String) milestoneVoteResult.get(APPROVE_VOTERS)), 6);
                                    assertEquals(toInt((String) milestoneVoteResult.get(REJECT_VOTERS)), 1);

                                    // vote on progress report 2 with acceptance
                                }

                                @DisplayName("submit third progress report ")
                                @Nested
                                @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
                                class thirdReport {

                                    @DisplayName("update to application period and assert values")
                                    @Order(13)
                                    @Test
                                    public void checks() throws InterruptedException {

                                        Map<String, ?> contributorProject2 = getContributorFunds(testClient.getAddress());
                                        List<Map<String, ?>> contributorData2 = (List<Map<String, ?>>) contributorProject2.get("data");

                                        BigInteger milestoneBudget = toBigInt((String) contributorData2.get(0).get("installment_amount"));
                                        BigInteger installPaidTillDate = toBigInt((String) contributorData2.get(0).get("total_installment_paid"));


                                        updateToApplicationPeriod();
                                        verifyPeriod(APPLICATION_PERIOD);

                                        String proposal2 = "Test_Proposal_2";
                                        int milestoneStatus = getMilestoneStatus(proposal2, 1);
                                        assertEquals(milestoneStatus, MILESTONE_REPORT_APPROVED);

                                        Map<String, Object> milestoneDetails = getMilestonesReport(proposal2, 1);
                                        System.out.println("milestone details " + milestoneDetails);


                                        Map<String, Object> proposalDetails = getProposalDetails(proposal2);
                                        assertEquals(toInt((String) proposalDetails.get(APPROVED_REPORTS)), 1);

                                        List<String> activeProposals = getProposalByStatus(ACTIVE);
                                        assertTrue(activeProposals.contains(proposal2));

                                        // check contributor's fund
                                        Map<String, ?> contributorProject = getContributorFunds(testClient.getAddress());
                                        List<Map<String, ?>> contributorData = (List<Map<String, ?>>) contributorProject.get("data");
                                        BigInteger installmentPaidNow = toBigInt((String) contributorData.get(0).get("total_installment_paid"));
                                        assertEquals(installmentPaidNow, installPaidTillDate.add(milestoneBudget));


                                        String proposal3 = "Test_Proposal_3";
                                        milestoneStatus = getMilestoneStatus(proposal3, 1);
                                        assertEquals(milestoneStatus, MILESTONE_REPORT_REJECTED);


                                        proposalDetails = getProposalDetails(proposal3);
                                        assertEquals(toInt((String) proposalDetails.get(APPROVED_REPORTS)), 0);

                                        List<String> pausedProposal = getProposalByStatus(PAUSED);

                                        assertTrue(pausedProposal.contains("Test_Proposal_3"));

                                    }

                                    @DisplayName("one of the milestone is rejected on this progress report")
                                    @Order(14)
                                    @Test
                                    public void submitProgressReport() {

                                        List<Map<String, Object>> activeProposal = readerClient.cpsCore.getActiveProposals(testClient.getAddress());
                                        System.out.println("active proposal " + activeProposal);
                                        assertEquals("Test_Proposal_2", activeProposal.get(1).get("ipfs_hash"));
                                        assertEquals("0x0", activeProposal.get(1).get("new_progress_report"));
                                        assertEquals("0x1", activeProposal.get(1).get("last_progress_report"));

                                        // submit progress report for proposal 2
                                        CPSCoreInterface.ProgressReportAttributes progressReportAttributes = createProgressReport("3",
                                                "Test_Proposal_2");

                                        assertEquals(getMilestoneStatus("Test_Proposal_2", 2), 0);
                                        assertEquals(getMilestoneStatus("Test_Proposal_2", 3), 0);

                                        CPSCoreInterface.MilestoneSubmission submission2 = new CPSCoreInterface.MilestoneSubmission();
                                        submission2.id = 2;
                                        submission2.status = true;

                                        CPSCoreInterface.MilestoneSubmission submission3 = new CPSCoreInterface.MilestoneSubmission();
                                        submission3.id = 3;
                                        submission3.status = true;
                                        CPSCoreInterface.MilestoneSubmission[] milestoneSubmissions = new CPSCoreInterface.MilestoneSubmission[]{
                                                submission2, submission3
                                        };

                                        testClient.cpsCore.submitProgressReport(progressReportAttributes, milestoneSubmissions);


                                        Map<String, Object> progressReport = getProgressReportDetails("Test_Proposal_2_Report_Proposal_3");
                                        assertEquals(progressReport.get("milestone_submitted_count"), "0x2");

                                        assertEquals(getMilestoneStatus("Test_Proposal_2", 2), MILESTONE_REPORT_COMPLETED);
                                        assertEquals(getMilestoneStatus("Test_Proposal_2", 3), MILESTONE_REPORT_COMPLETED);

                                    }

                                    @DisplayName("submit progress report with all milestones of a project")
                                    @Order(14)
                                    @Test
                                    public void withAllMilestones() {

                                        CPSCoreInterface.ProgressReportAttributes progressReportAttributes = createProgressReport("2",
                                                "Test_Proposal_3");

                                        assertEquals(getMilestoneStatus("Test_Proposal_3", 2), 0);

                                        CPSCoreInterface.MilestoneSubmission submission2 = new CPSCoreInterface.MilestoneSubmission();
                                        submission2.id = 2;
                                        submission2.status = true;

                                        CPSCoreInterface.MilestoneSubmission submission3 = new CPSCoreInterface.MilestoneSubmission();
                                        submission3.id = 3;
                                        submission3.status = true;
                                        CPSCoreInterface.MilestoneSubmission[] milestoneSubmissions = new CPSCoreInterface.MilestoneSubmission[]{
                                                submission2, submission3
                                        };

                                        testClient.cpsCore.submitProgressReport(progressReportAttributes, milestoneSubmissions);


                                        assertEquals(getMilestoneStatus("Test_Proposal_3", 2), MILESTONE_REPORT_COMPLETED);

                                    }

                                    @DisplayName("vote on third progress report. ")
                                    @Nested
                                    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
                                    class voteReport {
                                        @DisplayName("to voting period")
                                        @Order(15)
                                        @Test
                                        public void updateToVotingPeriod() throws InterruptedException {
                                            updateNextBlock();
                                            ownerClient.cpsCore.updatePeriod();
                                            verifyPeriod(VOTING_PERIOD);
                                            // vote on progress report 2 with acceptance
                                        }

                                        @DisplayName("vote on progress reports")
                                        @Order(16)
                                        @Test
                                        public void voteProgressReport() {

                                            String reportForPropsoal2 = "Test_Proposal_2_Report_Proposal_3";
                                            String reportForPropsoal3 = "Test_Proposal_3_Report_Proposal_2";


                                            MilestoneVoteAttributes[] voteAttributes = new MilestoneVoteAttributes[]{
                                                    vote(2, APPROVE), vote(3, REJECT)};
                                            for (CPSClient prepClient : cpsClients.values()) {
                                                prepClient.cpsCore.voteProgressReport(reportForPropsoal2,
                                                        "Progress not satisfactory ",
                                                        voteAttributes, null, false);

                                                prepClient.cpsCore.voteProgressReport(reportForPropsoal3,
                                                        "no ",
                                                        voteAttributes, null, false);
                                            }

                                            // verify the votes
                                            Map<String, Object> milestoneVoteResult = getMilestoneReport(reportForPropsoal2, 2);
                                            assertEquals(toInt((String) milestoneVoteResult.get(APPROVE_VOTERS)), 7);
                                            assertEquals(toInt((String) milestoneVoteResult.get(REJECT_VOTERS)), 0);

                                            milestoneVoteResult = getMilestoneReport(reportForPropsoal2, 3);
                                            assertEquals(toInt((String) milestoneVoteResult.get(APPROVE_VOTERS)), 0);
                                            assertEquals(toInt((String) milestoneVoteResult.get(REJECT_VOTERS)), 7);

                                            milestoneVoteResult = getMilestoneReport(reportForPropsoal3, 2);
                                            assertEquals(toInt((String) milestoneVoteResult.get(APPROVE_VOTERS)), 7);
                                            assertEquals(toInt((String) milestoneVoteResult.get(REJECT_VOTERS)), 0);

                                        }


                                        @DisplayName("submit final progress report of both proposals. ")
                                        @Nested
                                        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
                                        class finalReports {

                                            @DisplayName("update to application period and assert values")
                                            @Order(17)
                                            @Test
                                            public void checksInApplicationPeriod() throws InterruptedException {

                                                Map<String, ?> contributorProject2 = getContributorFunds(testClient.getAddress());
                                                List<Map<String, ?>> contributorData2 = (List<Map<String, ?>>) contributorProject2.get("data");

                                                BigInteger installPaidTillDate = toBigInt((String) contributorData2.get(0).get("total_installment_paid"));


                                                updateToApplicationPeriod();
                                                verifyPeriod(APPLICATION_PERIOD);

                                                String proposal2 = "Test_Proposal_2";
                                                int milestoneStatus = getMilestoneStatus(proposal2, 2);
                                                assertEquals(milestoneStatus, MILESTONE_REPORT_APPROVED);

                                                milestoneStatus = getMilestoneStatus(proposal2, 3);
                                                assertEquals(milestoneStatus, MILESTONE_REPORT_REJECTED);

                                                Map<String, Object> milestoneDetails = getMilestonesReport(proposal2, 2);
                                                BigInteger milestoneBudget = toBigInt((String) milestoneDetails.get(BUDGET));


                                                Map<String, Object> proposalDetails = getProposalDetails(proposal2);
                                                assertEquals(toInt((String) proposalDetails.get(APPROVED_REPORTS)), 2);

                                                // proposal 2 submitted the last progress report and the milestone got rejected
                                                // so proposal 2 will be in paused state
                                                List<String> pausedProposal = getProposalByStatus(PAUSED);
                                                assertTrue(pausedProposal.contains(proposal2));

                                                // check contributor's fund
                                                Map<String, ?> contributorProject = getContributorFunds(testClient.getAddress());
                                                List<Map<String, ?>> contributorData = (List<Map<String, ?>>) contributorProject.get("data");
                                                BigInteger installmentPaidNow = toBigInt((String) contributorData.get(0).get("total_installment_paid"));
                                                assertEquals(installmentPaidNow, installPaidTillDate.add(milestoneBudget));


                                                String proposal3 = "Test_Proposal_3";

                                                milestoneStatus = getMilestoneStatus(proposal3, 2);
                                                assertEquals(milestoneStatus, MILESTONE_REPORT_APPROVED);

                                                milestoneStatus = getMilestoneStatus(proposal3, 3);
                                                assertEquals(milestoneStatus, MILESTONE_REPORT_REJECTED);


                                                proposalDetails = getProposalDetails(proposal3);
                                                assertEquals(toInt((String) proposalDetails.get(APPROVED_REPORTS)), 1);


                                                List<String> activeProposals = getProposalByStatus(ACTIVE);
                                                assertTrue(activeProposals.contains("Test_Proposal_3"));

                                            }

                                            @DisplayName("report for proposal 2 ")
                                            @Order(18)
                                            @Test
                                            public void finalReport() {
                                                // proposal 2 ko milestone 2
                                                // proposal 3 ko milestone 1 and 3

                                                CPSCoreInterface.ProgressReportAttributes progressReportAttributes = createProgressReport("4",
                                                        "Test_Proposal_2");

                                                assertEquals(getMilestoneStatus("Test_Proposal_2", 3), MILESTONE_REPORT_REJECTED);

                                                CPSCoreInterface.MilestoneSubmission submission3 = new CPSCoreInterface.MilestoneSubmission();
                                                submission3.id = 3;
                                                submission3.status = true;
                                                CPSCoreInterface.MilestoneSubmission[] milestoneSubmissions = new CPSCoreInterface.MilestoneSubmission[]{
                                                        submission3
                                                };

                                                testClient.cpsCore.submitProgressReport(progressReportAttributes, milestoneSubmissions);


                                                Map<String, Object> progressReport = getProgressReportDetails("Test_Proposal_2_Report_Proposal_4");


                                            }

                                            @DisplayName("report for proposal 3 ")
                                            @Order(18)
                                            @Test
                                            public void finalReport2() {

                                                List<Map<String, Object>> activeProposal = readerClient.cpsCore.getActiveProposals(testClient.getAddress());
                                                System.out.println("the milestone deadlines are " + activeProposal.get(1).get("milestoneDeadlines"));
                                                CPSCoreInterface.ProgressReportAttributes progressReportAttributes = createProgressReport("3",
                                                        "Test_Proposal_3");

                                                assertEquals(getMilestoneStatus("Test_Proposal_3", 1), MILESTONE_REPORT_REJECTED);
                                                assertEquals(getMilestoneStatus("Test_Proposal_3", 3), MILESTONE_REPORT_REJECTED);

                                                CPSCoreInterface.MilestoneSubmission submission1 = new CPSCoreInterface.MilestoneSubmission();
                                                submission1.id = 1;
                                                submission1.status = true;

                                                CPSCoreInterface.MilestoneSubmission submission3 = new CPSCoreInterface.MilestoneSubmission();
                                                submission3.id = 3;
                                                submission3.status = true;
                                                CPSCoreInterface.MilestoneSubmission[] milestoneSubmissions = new CPSCoreInterface.MilestoneSubmission[]{
                                                        submission1, submission3
                                                };

                                                testClient.cpsCore.submitProgressReport(progressReportAttributes, milestoneSubmissions);


                                                Map<String, Object> progressReport = getProgressReportDetails("Test_Proposal_3_Report_Proposal_3");

                                                assertEquals(getMilestoneStatus("Test_Proposal_3", 1), MILESTONE_REPORT_COMPLETED);


                                            }

                                            @DisplayName("vote on reports of proposals ")
                                            @Nested
                                            @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
                                            class voteFinalReport {
                                                @DisplayName("update to voting period")
                                                @Order(19)
                                                @Test
                                                public void updateToVotingPeriod() throws InterruptedException {
                                                    updateNextBlock();
                                                    ownerClient.cpsCore.updatePeriod();
                                                    verifyPeriod(VOTING_PERIOD);
                                                }

                                                @DisplayName("vote on final report")
                                                @Order(20)
                                                @Test
                                                public void voteFinalReports() {
                                                    // proposal 3 reject

                                                    String reportForPropsoal2 = "Test_Proposal_2_Report_Proposal_4";
                                                    String reportForPropsoal3 = "Test_Proposal_3_Report_Proposal_3";


                                                    MilestoneVoteAttributes[] voteForProposal2 = new MilestoneVoteAttributes[]{
                                                            vote(3, APPROVE)};

                                                    MilestoneVoteAttributes[] voteForProposal3 = new MilestoneVoteAttributes[]{
                                                            vote(1, REJECT), vote(3, APPROVE)};
                                                    for (CPSClient prepClient : cpsClients.values()) {
                                                        prepClient.cpsCore.voteProgressReport(reportForPropsoal2,
                                                                "Looks good ",
                                                                voteForProposal2, null, false);

                                                        prepClient.cpsCore.voteProgressReport(reportForPropsoal3,
                                                                "can only approve one of them ",
                                                                voteForProposal3, null, false);
                                                    }

                                                    // verify the votes
                                                    Map<String, Object> milestoneVoteResult = getMilestoneReport(reportForPropsoal2, 3);
                                                    assertEquals(toInt((String) milestoneVoteResult.get(APPROVE_VOTERS)), 7);
                                                    assertEquals(toInt((String) milestoneVoteResult.get(REJECT_VOTERS)), 0);

                                                    milestoneVoteResult = getMilestoneReport(reportForPropsoal3, 1);
                                                    assertEquals(toInt((String) milestoneVoteResult.get(APPROVE_VOTERS)), 0);
                                                    assertEquals(toInt((String) milestoneVoteResult.get(REJECT_VOTERS)), 7);

                                                    milestoneVoteResult = getMilestoneReport(reportForPropsoal3, 3);
                                                    assertEquals(toInt((String) milestoneVoteResult.get(APPROVE_VOTERS)), 7);
                                                    assertEquals(toInt((String) milestoneVoteResult.get(REJECT_VOTERS)), 0);

                                                }

                                                @DisplayName("completeAllProject")
                                                @Order(21)
                                                @Test
                                                public void completionOfProjects() throws InterruptedException {

                                                    Map<String, BigInteger> claimableBond = claimableSponsorBond(
                                                            cpsClients.get(0).getAddress());
                                                    BigInteger sponsorBondBefore = claimableBond.get(bnUSD);
                                                    System.out.println("sponsorBo" + sponsorBondBefore);

                                                    updateToApplicationPeriod();

                                                    // check the contributors and sponsor's balance

                                                    Map<String, Object> milestoneReport = getMilestonesReport("Test_Proposal_3", 1);
                                                    assertEquals(milestoneReport.get("extensionFlag"), "0x0");
                                                    assertEquals(toInt((String) milestoneReport.get("status")), MILESTONE_REPORT_REJECTED);

                                                    milestoneReport = getMilestonesReport("Test_Proposal_3", 3);
                                                    assertEquals(milestoneReport.get("extensionFlag"), "0x0");
                                                    assertEquals(toInt((String) milestoneReport.get("status")), MILESTONE_REPORT_APPROVED);

                                                    List<String> activeProposals = getProposalByStatus(ACTIVE);
                                                    assertTrue(activeProposals.contains("Test_Proposal_3"));

                                                    List<String> completedProposals = getProposalByStatus(COMPLETED);
                                                    assertTrue(completedProposals.contains("Test_Proposal_2"));

                                                    // check the sponsor bond is return
                                                    Map<String, BigInteger> sponsorBondAfter = claimableSponsorBond(
                                                            cpsClients.get(0).getAddress());
                                                    Map<String, Object> proposalDetail = getProposalDetails("Test_Proposal_2");
                                                    BigInteger sponsorBondAmount = toBigInt((String) proposalDetail.get(SPONSOR_DEPOSIT_AMOUNT));
                                                    assertEquals(sponsorBondAmount.add(sponsorBondBefore), sponsorBondAfter.get(bnUSD));
                                                }
                                            }


                                        }
                                    }


                                }
                            }


                        }

                    }
                }
            }
        }


    }

    private Map<String, Object> getMilestonesReport(String ipfsHash, int id) {
        return readerClient.cpsCore.getMilestonesReport(ipfsHash, id);
    }

    private Map<String, BigInteger> claimableSponsorBond(Address prepAddress) {
        return readerClient.cpsCore.checkClaimableSponsorBond(prepAddress);
    }

    private List<String> getProposalByStatus(String status) {
        return readerClient.cpsCore.getProposalsKeysByStatus(status);
    }


    private void verifyPeriod(String period) {
        Map<String, ?> periodStatus = getPeriodStatus();
        assertEquals(periodStatus.get(PERIOD_NAME), period);
    }


    private MilestoneVoteAttributes vote(int id, String vote){

        MilestoneVoteAttributes milestoneVoteAttributes = new MilestoneVoteAttributes();
        milestoneVoteAttributes.id = id;
        milestoneVoteAttributes.vote = vote;
        return milestoneVoteAttributes;
    }



    private void updateToApplicationPeriod() throws InterruptedException {
        updateNextBlock();
        ownerClient.cpsCore.updatePeriod();
        ownerClient.cpsCore.updatePeriod();
        ownerClient.cpsCore.updatePeriod();
        ownerClient.cpsCore.updatePeriod();
    }


    private BigInteger toBigInt(String inputString) {
        return new BigInteger(inputString.substring(2), 16);
    }

    private Integer toInt(String inputString) {
        return Integer.parseInt(inputString.substring(2));
    }



    private void voteByPrep(CPSClient caller, String ipfsKey, String vote, String voteReason,
                            @Optional boolean voteChange){

        caller.cpsCore.voteProposal(ipfsKey,vote,voteReason,voteChange);
    }

    private Map<String,Object> getProposalDetails(String ipfsHash){
        return readerClient.cpsCore.getProposalDetailsByHash(ipfsHash);
    }

    private Map<String,Object> getProposalVote(String ipfsHash){
        return readerClient.cpsCore.getVoteResult(ipfsHash);
    }

    private void updateNextBlock() throws InterruptedException {
        ownerClient.cpsCore.updateNextBlock(1);
        Thread.sleep(2000);
    }

    private byte[] createByteArray(String methodName, String ipfsHash, String vote, String voteReason) {

        JsonObject internalParameters = new JsonObject()
                .add("ipfs_hash", String.valueOf(ipfsHash))
                .add("vote", String.valueOf(vote))
                .add("vote_reason", String.valueOf(voteReason));


        JsonObject jsonData = new JsonObject()
                .add("method", methodName)
                .add("params", internalParameters);

        return jsonData.toString().getBytes();
    }

    private byte[] penaltyByteArray(String methodName) {

        JsonObject jsonData = new JsonObject()
                .add("method", methodName)
                .add("params", new JsonObject());


        return jsonData.toString().getBytes();
    }

    private void bnUSDMint(Address to, BigInteger amount) {
        ownerClient.bnUSD.mintTo(to, amount);
    }


    private Map<String,?> getPeriodStatus(){
        return readerClient.cpsCore.getPeriodStatus();
    }

    private Map<String,BigInteger> loginPrep(Address prepAddress){
        return readerClient.cpsCore.loginPrep(prepAddress);
    }

    private Map<String,BigInteger> unregisteredPrepData(){
        return Map.of(IS_PREP, BigInteger.ONE,
                IS_REGISTERED, BigInteger.ZERO,
                PAY_PENALTY, BigInteger.ZERO,
                VOTING_PREP, BigInteger.ZERO);
    }

    private Map<String,BigInteger> votingPrepData(){
        return Map.of(IS_PREP, BigInteger.ONE,
                IS_REGISTERED, BigInteger.ONE,
                PAY_PENALTY, BigInteger.ZERO,
                VOTING_PREP, BigInteger.ONE);
    }

    private Map<String,BigInteger> registerPrepData(){
        return Map.of(IS_PREP, BigInteger.ONE,
                IS_REGISTERED, BigInteger.ONE,
                PAY_PENALTY, BigInteger.ZERO,
                VOTING_PREP, BigInteger.ZERO);
    }


}
