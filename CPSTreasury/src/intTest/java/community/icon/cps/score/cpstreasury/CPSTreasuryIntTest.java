package community.icon.cps.score.cpstreasury;

import community.icon.cps.score.lib.interfaces.CPFTreasuryInterfaceScoreClient;
import community.icon.cps.score.lib.interfaces.CPSTreasuryInterfaceScoreClient;
import community.icon.cps.score.test.integration.CPS;
import community.icon.cps.score.test.integration.ScoreIntegrationTest;
import foundation.icon.icx.Wallet;

import org.junit.jupiter.api.*;

import java.math.BigInteger;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CPSTreasuryIntTest {
    private static CPS cps;
    private static Wallet tester;
    private static Wallet owner;

    private static CPSTreasuryInterfaceScoreClient cpsTreasury;
    private static CPFTreasuryInterfaceScoreClient cpfTreasury;

    @BeforeAll
    static void setup() throws Exception {
        tester = ScoreIntegrationTest.createWalletWithBalance(BigInteger.TEN.pow(24));
        cps = new CPS();
        cps.setupCPS();
        owner = cps.owner;
        cpsTreasury = new CPSTreasuryInterfaceScoreClient(cps.cpsTreasury);
        cpfTreasury = new CPFTreasuryInterfaceScoreClient(cps.cpfTreasury);
    }

    @Test
    void name(){
        System.out.println(cpsTreasury.name());
        System.out.println(cpfTreasury.name());
        System.out.println(cpsTreasury._address());
        System.out.println(cpfTreasury._address());
    }

}
