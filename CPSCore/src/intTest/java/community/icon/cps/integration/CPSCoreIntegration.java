package community.icon.cps.integration;

import community.icon.cps.score.test.integration.CPS;
import community.icon.cps.score.test.integration.CPSClient;
import community.icon.cps.score.test.integration.ScoreIntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

public class CPSCoreIntegration implements ScoreIntegrationTest {

    private static CPSClient ownerClient;
    @BeforeAll
    public static void setup() throws Exception{
        String contracts = "conf/contracts.json";
        CPS cps = new CPS(contracts);

        cps.setupCPS();
        ownerClient = cps.defaultClient();

    }

    @Test
    @Order(1)
    public void name(){
        ownerClient.cpsCore.name();
    }
}
