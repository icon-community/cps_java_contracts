package community.icon.cps.score.cpscore;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import community.icon.cps.score.lib.interfaces.CPSCoreInterface.ProposalAttributes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.function.Executable;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import score.Address;
import score.Context;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static community.icon.cps.score.cpscore.utils.Constants.APPROVE;
import static community.icon.cps.score.cpscore.utils.Constants.bnUSD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class AbstractCPSUnitTest extends TestBase {
    public static final ServiceManager sm = getServiceManager();
    public static final Account owner = sm.createAccount();
    public static final Account testingAccount = sm.createAccount();
    public static final Account testingAccount1 = sm.createAccount();
    public static final Account testingAccount2 = sm.createAccount();
    public static final Account testingAccount3 = sm.createAccount();
    public static final Account testingAccount4 = sm.createAccount();
    public static final Account testingAccount5 = sm.createAccount();
    public static final Address SYSTEM_ADDRESS = Address.fromString("cx0000000000000000000000000000000000000000");
    public static final Address cpsTreasury = Address.fromString("cx0000000000000000000000000000000000000002");
    //    public static final Address cpfTreasury = Address.fromString("cx0000000000000000000000000000000000000003");
    public static final Account cpfScore = Account.newScoreAccount(3);
    public static final Address bnUSDScore = Address.fromString("cx0000000000000000000000000000000000000004");

    public Score cpsScore;
    public CPSCore scoreSpy;
    public static MockedStatic<Context> contextMock;

    public static final String TAG = "CPS Score";

    @BeforeEach
    public void setup() throws Exception {

        BigInteger bondPercentage = BigInteger.valueOf(12);

        cpsScore = sm.deploy(owner, CPSCore.class,bondPercentage);
        CPSCore instance = (CPSCore) cpsScore.getInstance();
        scoreSpy = spy(instance);
        cpsScore.setInstance(scoreSpy);
        long currentTime = System.currentTimeMillis() / 1000L;
        sm.getBlock().increase(currentTime / 2);
        contextMock.reset();
    }

    @BeforeAll
    public static void init(){
        contextMock = Mockito.mockStatic(Context.class, CALLS_REAL_METHODS);
    }

    protected void expectErrorMessage(Executable contractCall, String errorMessage) {
        AssertionError e = Assertions.assertThrows(AssertionError.class, contractCall);
        assertEquals(errorMessage, e.getMessage());
    }

    protected void setScoresMethod(){
        cpsScore.invoke(owner, "addAdmin", owner.getAddress());
        cpsScore.invoke(owner, "setCpsTreasuryScore", cpsTreasury);
        cpsScore.invoke(owner, "setCpfTreasuryScore", cpfScore.getAddress());
        cpsScore.invoke(owner, "setBnusdScore", bnUSDScore);
        cpsScore.invoke(cpfScore,"setSponsorBondPercentage", BigInteger.valueOf(12));
    }
    protected void registerPrepsMethod(){
        setScoresMethod();

        List<Map<String, Object>> prepDict =
                List.of(Map.of("name", "owner", "address", owner.getAddress(), "power", BigInteger.valueOf(1000)),
                        Map.of("name", "testingAccount", "address", testingAccount.getAddress(), "power", BigInteger.valueOf(850)),
                        Map.of("name", "testingAccount1", "address", testingAccount1.getAddress(), "power", BigInteger.valueOf(770)),
                        Map.of("name", "testingAccount2" , "address", testingAccount2.getAddress(), "power", BigInteger.valueOf(800)),
                        Map.of("name", "testingAccount3", "address", testingAccount3.getAddress(), "power", BigInteger.valueOf(990)),
                        Map.of("name", "testingAccount4", "address", testingAccount4.getAddress(), "power", BigInteger.valueOf(500)),
                        Map.of("name", "testingAccount5", "address", testingAccount5.getAddress(), "power", BigInteger.valueOf(250))
                );
        Map<String, Object> preps = Map.of("preps", prepDict);

        doReturn(preps).when(scoreSpy).callScore(eq(Map.class), eq(SYSTEM_ADDRESS), eq("getPRepTerm"));
        doReturn(prepDict.get(0)).when(scoreSpy).callScore(eq(Map.class), eq(SYSTEM_ADDRESS), eq("getPRep"), any());
        cpsScore.invoke(owner, "toggleMaintenance");
        cpsScore.invoke(owner, "setInitialBlock");
        cpsScore.invoke(owner, "registerPrep");
        cpsScore.invoke(testingAccount, "registerPrep");
        cpsScore.invoke(testingAccount1, "registerPrep");
        cpsScore.invoke(testingAccount2, "registerPrep");
        cpsScore.invoke(testingAccount3, "registerPrep");
        cpsScore.invoke(testingAccount4, "registerPrep");
        cpsScore.invoke(testingAccount5, "registerPrep");
    }

    void submitProposalMethod(){
        registerPrepsMethod();
        ProposalAttributes proposalAttributes = new ProposalAttributes();
        proposalAttributes.ipfs_hash = "Proposal 1";
        proposalAttributes.project_title = "Title";
        proposalAttributes.project_duration = 2;
        proposalAttributes.total_budget = BigInteger.valueOf(100);
        proposalAttributes.token = bnUSD;
        proposalAttributes.sponsor_address = testingAccount.getAddress();
        proposalAttributes.ipfs_link = "link";
        proposalAttributes.milestoneCount = 5;
        proposalAttributes.isMilestone = true;
        Map<String, BigInteger> remainingSwapAmount = Map.of(
                "remainingSwapAmount", BigInteger.valueOf(1000).multiply(ICX),
                "maxCap", BigInteger.valueOf(1000).multiply(ICX));

        doReturn(remainingSwapAmount).when(scoreSpy).callScore(eq(Map.class), eq(cpfScore.getAddress()), eq("getRemainingSwapAmount"));
        contextMock.when(() -> Context.getValue()).thenReturn(BigInteger.valueOf(50).multiply(ICX));
        byte [] tx_hash = "transaction".getBytes();
        contextMock.when(() -> Context.getTransactionHash()).thenReturn(tx_hash);
        doNothing().when(scoreSpy).callScore(eq(BigInteger.valueOf(25).multiply(ICX)), eq(SYSTEM_ADDRESS), eq("burn"));
        doNothing().when(scoreSpy).callScore(eq(cpfScore.getAddress()), eq("swap_tokens"), eq(0));
        cpsScore.invoke(owner,"submitProposal",proposalAttributes );

    }



    protected void updateNextBlock(){
        cpsScore.invoke(owner, "updateNextBlock", 0);
    }

    public MockedStatic.Verification caller(){
        return () -> score.Context.getCaller();
    }

}
