package community.icon.cps.integration;

import community.icon.cps.score.test.integration.CPS;
import community.icon.cps.score.test.integration.CPSClient;
import community.icon.cps.score.test.integration.ScoreIntegrationTest;
import community.icon.cps.score.test.integration.config.BaseConfig;
import foundation.icon.score.client.RevertedException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import score.Address;

import static community.icon.cps.score.cpscore.utils.Constants.*;
import static community.icon.cps.score.test.AssertRevertedException.assertReverted;
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
    private static CPSClient readerClient;
    static Set<Map.Entry<Address, String>> prepSet = preps.entrySet();


    public static Map<Integer,CPSClient> cpsClients = new HashMap<>();
    @BeforeAll
    public static void setup() throws Exception{
        String contracts = "conf/contracts.json";
        CPS cps = new CPS(contracts);

        cps.setupCPS();
        ownerClient = cps.defaultClient();
        readerClient = cps.testClient();
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
