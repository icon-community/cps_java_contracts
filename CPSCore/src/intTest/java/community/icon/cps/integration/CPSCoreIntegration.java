package community.icon.cps.integration;

import community.icon.cps.score.lib.interfaces.CPSCoreInterface;
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

public class CPSCoreIntegration implements ScoreIntegrationTest {

    private static CPSClient ownerClient;
    private static CPSClient testClient;
    private static CPSClient readerClient;
    static Set<Map.Entry<Address, String>> prepSet = preps.entrySet();

    private BigInteger ICX = BigInteger.valueOf(10).pow(18);


    public static Map<Integer,CPSClient> cpsClients = new HashMap<>();
    @BeforeAll
    public static void setup() throws Exception{
        String contracts = "conf/contracts.json";
        CPS cps = new CPS(contracts);

        cps.setupCPS();
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
        proposalAttributes.total_budget = BigInteger.valueOf(100).multiply(ICX);
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
//        testClient.cpsCore.submitProposal(proposalAttributes,milestonesAttributes);
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
