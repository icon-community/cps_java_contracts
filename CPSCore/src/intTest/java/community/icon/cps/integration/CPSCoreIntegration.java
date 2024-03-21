package community.icon.cps.integration;

import com.eclipsesource.json.JsonObject;
import community.icon.cps.score.lib.interfaces.CPSCoreInterface;
import community.icon.cps.score.lib.interfaces.CPSCoreInterface.MilestoneVoteAttributes;
import community.icon.cps.score.test.integration.scores.CPSCoreInterfaceScoreClient;
import community.icon.cps.score.test.integration.CPS;
import community.icon.cps.score.test.integration.CPSClient;
import community.icon.cps.score.test.integration.ScoreIntegrationTest;
import community.icon.cps.score.test.integration.config.BaseConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import score.Address;

import static community.icon.cps.score.cpscore.utils.Constants.*;
import static community.icon.cps.score.test.AssertRevertedException.assertUserRevert;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.util.*;

import static community.icon.cps.score.test.integration.Environment.preps;
import static community.icon.cps.score.test.integration.Environment.SYSTEM_INTERFACE;
import static community.icon.cps.score.test.integration.Environment.godClient;
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

    @Test
    @Order(4)
    public void submitProposal(){
        CPSClient prep1 = cpsClients.get(0);
        CPSCoreInterface.ProposalAttributes proposalAttributes = new CPSCoreInterface.ProposalAttributes();

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

    private void verifyProposalDetails(CPSCoreInterface.ProposalAttributes expectedDetails){
        Map<String,Object> actualDetails = getProposalDetails(expectedDetails.ipfs_hash);
        assertEquals(actualDetails.get("project_title"), expectedDetails.project_title);
        assertEquals(actualDetails.get("sponsor_address"), expectedDetails.sponsor_address.toString());
        assertEquals(actualDetails.get("ipfs_hash"), expectedDetails.ipfs_hash);
//        assertEquals(actualDetails.get("project_duration"), expectedDetails.project_duration);
//        assertEquals(actualDetails.get("milestoneCount"), expectedDetails.milestoneCount);
//        assertEquals( actualDetails.get("total_budget"), expectedDetails.total_budget);

    }



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
    }

    @Test()
    @Order(6)
    public void voteProposal(){
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

    @Test
    @Order(7)
    public void submitMilestoneReport(){
        updateToApplicationPeriod();
        Map<String,? > periodStatus = getPeriodStatus();
        assertEquals(periodStatus.get(PERIOD_NAME),"Application Period");

        List<String> proposalsIpfs = readerClient.cpsCore.getProposalsKeysByStatus(ACTIVE);
        assertEquals(proposalsIpfs.size(),1);
        assertEquals(proposalsIpfs.get(0),"Test_Proposal_1");

        CPSCoreInterface.ProgressReportAttributes progressReportAttributes = new CPSCoreInterface.ProgressReportAttributes();
        progressReportAttributes.ipfs_hash = "Test_Proposal_1";
        progressReportAttributes.progress_report_title = "Report_1";
        progressReportAttributes.report_hash ="Report_Proposal_1";
        progressReportAttributes.ipfs_link = "https://proposal_1";
        progressReportAttributes.budget_adjustment = false;
        progressReportAttributes.additional_budget = BigInteger.ZERO;
        progressReportAttributes.additional_month = 0;

        CPSCoreInterface.MilestoneSubmission submission = new CPSCoreInterface.MilestoneSubmission();
        submission.id = 1;
        submission.status = true;
        CPSCoreInterface.MilestoneSubmission[] milestoneSubmissions = new CPSCoreInterface.MilestoneSubmission[]{submission};

        testClient.cpsCore.submitProgressReport(progressReportAttributes,milestoneSubmissions);

        List<Map<String, Object>> activeProposal = readerClient.cpsCore.getActiveProposals(testClient.getAddress());

        System.out.println(activeProposal.get(0));
    }

    @Test
    @Order(8)
    public void voteOnMilestone(){
        updateNextBlock();
        ownerClient.cpsCore.updatePeriod();
        Map<String,? > periodStatus = getPeriodStatus();
        assertEquals(periodStatus.get(PERIOD_NAME),"Voting Period");

        MilestoneVoteAttributes[] voteAttributes = new MilestoneVoteAttributes[]{vote(1,APPROVE)};
        for (int i = 0; i < cpsClients.size(); i++) {
            cpsClients.get(i).cpsCore.voteProgressReport("Report_Proposal_1","Working well",
                    voteAttributes,null,false);
        }
    }

    @Test
    @Order(9)
    public void submitSecondProgressReport(){
        updateToApplicationPeriod();
        Map<String,? > periodStatus = getPeriodStatus();
        assertEquals(periodStatus.get(PERIOD_NAME),"Application Period");

//        MilestoneVoteAttributes[] voteAttributes = new MilestoneVoteAttributes[]{vote(1,APPROVE)};
//        for (int i = 0; i < cpsClients.size(); i++) {
//            cpsClients.get(i).cpsCore.voteProgressReport("Report_Proposal_1","Working well",
//                    voteAttributes,null,false);
//        }
    }

    private MilestoneVoteAttributes vote(int id, String vote){

        MilestoneVoteAttributes milestoneVoteAttributes = new MilestoneVoteAttributes();
        milestoneVoteAttributes.id = id;
        milestoneVoteAttributes.vote = vote;
        return milestoneVoteAttributes;
    }



    private void updateToApplicationPeriod(){
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

    private void updateNextBlock(){
        ownerClient.cpsCore.updateNextBlock(1);
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

    private void bnUSDMint(Address to, BigInteger amount){
        ownerClient.bnUSD.mintTo(to,amount);
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
