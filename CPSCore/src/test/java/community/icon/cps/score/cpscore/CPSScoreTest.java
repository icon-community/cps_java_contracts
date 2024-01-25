package community.icon.cps.score.cpscore;

import com.eclipsesource.json.JsonObject;
import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import community.icon.cps.score.cpscore.utils.Constants;
import community.icon.cps.score.lib.interfaces.CPSCoreInterface;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import score.Address;
import score.ArrayDB;
import score.Context;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static community.icon.cps.score.cpscore.utils.Constants.*;
import static community.icon.cps.score.lib.interfaces.CPSCoreInterface.ProgressReportAttributes;
import static community.icon.cps.score.lib.interfaces.CPSCoreInterface.ProposalAttributes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class CPSScoreTest extends TestBase {
    public static final Address SYSTEM_ADDRESS = Address.fromString("cx0000000000000000000000000000000000000000");
    public static final Address cpsTreasury = Address.fromString("cx0000000000000000000000000000000000000002");
    public static final Address cpfTreasury = Address.fromString("cx0000000000000000000000000000000000000003");
    public static final Address bnUSDScore = Address.fromString("cx0000000000000000000000000000000000000004");
    public static final String TAG = "CPS Score";
    public static final BigInteger MULTIPLIER = new BigInteger("1000000000000000000");

    public static final ServiceManager sm = getServiceManager();
    public static final Account owner = sm.createAccount(1000);
    public static final Account testingAccount = sm.createAccount();
    public static final Account testingAccount1 = sm.createAccount();
    public static final Account testingAccount2 = sm.createAccount();
    public static final Account testingAccount3 = sm.createAccount();
    public static final Account testingAccount4 = sm.createAccount();
    public static final Account testingAccount5 = sm.createAccount();
    public static final Account testingAccount6 = sm.createAccount();
    public static final Account testingAccount7 = sm.createAccount();
    public static final Account testingAccount8 = sm.createAccount();
    public static final Account testingAccount9 = sm.createAccount();
    public static final Account cpfTreasuryScore = Account.newScoreAccount(1);
    public Score cpsScore;
    public static MockedStatic<Context> contextMock;

    CPSCore scoreSpy;

    @BeforeEach
    public void setup() throws Exception {
        cpsScore = sm.deploy(owner, CPSCore.class, BigInteger.valueOf(12), BigInteger.valueOf(20));
        CPSCore instance = (CPSCore) cpsScore.getInstance();
        scoreSpy = spy(instance);
        cpsScore.setInstance(scoreSpy);
        long currentTime = System.currentTimeMillis() / 1000L;
        sm.getBlock().increase(currentTime / 2);
        contextMock.reset();
    }

    @BeforeAll
    public static void init() {
        contextMock = Mockito.mockStatic(Context.class, CALLS_REAL_METHODS);
    }

    public void expectErrorMessage(Executable contractCall, String errorMessage) {
        AssertionError e = Assertions.assertThrows(AssertionError.class, contractCall);
        assertEquals(errorMessage, e.getMessage());
    }

    @Test
    void name() {
        assertEquals(cpsScore.call("name"), TAG);
    }

    @Test
    void addAdmin() {
        cpsScore.invoke(owner, "addAdmin", testingAccount.getAddress());
        assertEquals(List.of(testingAccount.getAddress()), cpsScore.call("getAdmins"));
    }

    @Test
    void addAdminNotOwner() {
        Executable addAdminNotOwner = () -> cpsScore.invoke(testingAccount, "addAdmin", testingAccount.getAddress());
        expectErrorMessage(addAdminNotOwner, "Reverted(0): SenderNotScoreOwner: Sender=" + testingAccount.getAddress() + " Owner=" + owner.getAddress());
    }

    @Test
    void removeAdmin() {
        cpsScore.invoke(owner, "addAdmin", testingAccount.getAddress());
        assertEquals(List.of(testingAccount.getAddress()), cpsScore.call("getAdmins"));
        cpsScore.invoke(owner, "removeAdmin", testingAccount.getAddress());
        assertEquals(0, ((List) cpsScore.call("getAdmins")).size());
    }

    @Test
    void removeAdminNotOwner() {
        cpsScore.invoke(owner, "addAdmin", testingAccount.getAddress());
        assertEquals(List.of(testingAccount.getAddress()), cpsScore.call("getAdmins"));
        Executable removeAdminNotOwner = () -> cpsScore.invoke(testingAccount, "removeAdmin", testingAccount.getAddress());
        expectErrorMessage(removeAdminNotOwner, "Reverted(0): SenderNotScoreOwner: Sender=" + testingAccount.getAddress() + " Owner=" + owner.getAddress());
    }

    @Test
    void removeAdminAddressNotAdmin() {
        cpsScore.invoke(owner, "addAdmin", testingAccount.getAddress());
        assertEquals(List.of(testingAccount.getAddress()), cpsScore.call("getAdmins"));
        Executable removeAdminNotOwner = () -> cpsScore.invoke(owner, "removeAdmin", testingAccount1.getAddress());
        expectErrorMessage(removeAdminNotOwner, "Reverted(0): CPS Score: Address not registered as admin.");
    }

    void addAdminMethod() {
        cpsScore.invoke(owner, "addAdmin", owner.getAddress());
    }

    @Test
    void registerPRep() {
        registerPrepsMethod();
        assertEquals(10, ((List<?>) (cpsScore.call("getPReps"))).size());
    }

    @Test
    void registerPrepAlreadyRegistered() {
        List<Map<String, Object>> prepDict =
                List.of(Map.of("name", "owner", "address", owner.getAddress(), "power", BigInteger.valueOf(1000)),
                        Map.of("name", "testingAccount", "address", testingAccount.getAddress(), "power", BigInteger.valueOf(850)),
                        Map.of("name", "testingAccount1", "address", testingAccount1.getAddress(), "power", BigInteger.valueOf(770)),
                        Map.of("name", "testingAccount2", "address", testingAccount2.getAddress(), "power", BigInteger.valueOf(800)),
                        Map.of("name", "testingAccount3", "address", testingAccount3.getAddress(), "power", BigInteger.valueOf(990)),
                        Map.of("name", "testingAccount4", "address", testingAccount4.getAddress(), "power", BigInteger.valueOf(500)),
                        Map.of("name", "testingAccount5", "address", testingAccount5.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount6", "address", testingAccount6.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount7", "address", testingAccount7.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount8", "address", testingAccount8.getAddress(), "power", BigInteger.valueOf(250))
                );
        Map<String, Object> preps = Map.of("preps", prepDict);

        addAdminMethod();
        doReturn(preps).when(scoreSpy).callScore(eq(Map.class), eq(SYSTEM_ADDRESS), eq("getPRepTerm"));
        doReturn(prepDict.get(0)).when(scoreSpy).callScore(eq(Map.class), eq(SYSTEM_ADDRESS), eq("getPRep"), any());
        cpsScore.invoke(owner, "toggleMaintenance");
        cpsScore.invoke(owner, "setInitialBlock");
        cpsScore.invoke(owner, "registerPrep");
        Executable register = () -> cpsScore.invoke(owner, "registerPrep");
        expectErrorMessage(register, "Reverted(0): CPS Score: P-Rep is already registered.");
    }

    @Test
    void registerPrepNotAPrep() {
        List<Map<String, Object>> prepDict =
                List.of(Map.of("name", "owner", "address", owner.getAddress(), "power", BigInteger.valueOf(1000)),
                        Map.of("name", "testingAccount", "address", testingAccount.getAddress(), "power", BigInteger.valueOf(850)),
                        Map.of("name", "testingAccount1", "address", testingAccount1.getAddress(), "power", BigInteger.valueOf(770)),
                        Map.of("name", "testingAccount2", "address", testingAccount2.getAddress(), "power", BigInteger.valueOf(800)),
                        Map.of("name", "testingAccount3", "address", testingAccount3.getAddress(), "power", BigInteger.valueOf(990)),
                        Map.of("name", "testingAccount4", "address", testingAccount4.getAddress(), "power", BigInteger.valueOf(500)),
                        Map.of("name", "testingAccount5", "address", testingAccount5.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount6", "address", testingAccount6.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount7", "address", testingAccount7.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount8", "address", testingAccount8.getAddress(), "power", BigInteger.valueOf(250))
                );
        Map<String, Object> preps = Map.of("preps", prepDict);

        addAdminMethod();
        doReturn(preps).when(scoreSpy).callScore(eq(Map.class), eq(SYSTEM_ADDRESS), eq("getPRepTerm"));
        doReturn(prepDict.get(0)).when(scoreSpy).callScore(eq(Map.class), eq(SYSTEM_ADDRESS), eq("getPRep"), any());
        cpsScore.invoke(owner, "toggleMaintenance");
        cpsScore.invoke(owner, "setInitialBlock");
        Executable register = () -> cpsScore.invoke(testingAccount9, "registerPrep");
        expectErrorMessage(register, "Reverted(0): CPS Score: Not a P-Rep.");
    }

    @Test
    void registerPrepPrepInDenyList() {
        ArrayDB<Address> denylist = mock(ArrayDB.class);
        denylist.add(owner.getAddress());
        List<Map<String, Object>> prepDict =
                List.of(Map.of("name", "owner", "address", owner.getAddress(), "power", BigInteger.valueOf(1000)),
                        Map.of("name", "testingAccount", "address", testingAccount.getAddress(), "power", BigInteger.valueOf(850)),
                        Map.of("name", "testingAccount1", "address", testingAccount1.getAddress(), "power", BigInteger.valueOf(770)),
                        Map.of("name", "testingAccount2", "address", testingAccount2.getAddress(), "power", BigInteger.valueOf(800)),
                        Map.of("name", "testingAccount3", "address", testingAccount3.getAddress(), "power", BigInteger.valueOf(990)),
                        Map.of("name", "testingAccount4", "address", testingAccount4.getAddress(), "power", BigInteger.valueOf(500)),
                        Map.of("name", "testingAccount5", "address", testingAccount5.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount6", "address", testingAccount6.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount7", "address", testingAccount7.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount8", "address", testingAccount8.getAddress(), "power", BigInteger.valueOf(250))
                );
        Map<String, Object> preps = Map.of("preps", prepDict);

        addAdminMethod();
        doReturn(preps).when(scoreSpy).callScore(eq(Map.class), eq(SYSTEM_ADDRESS), eq("getPRepTerm"));
        doReturn(prepDict.get(0)).when(scoreSpy).callScore(eq(Map.class), eq(SYSTEM_ADDRESS), eq("getPRep"), any());
        cpsScore.invoke(owner, "toggleMaintenance");
        cpsScore.invoke(owner, "setInitialBlock");
//        contextMock.when()
        cpsScore.invoke(owner, "registerPrep");
        // todo check when voiting logic is achecked
    }

    @Test
    void unregisterPrep() {
        List<Map<String, Object>> prepDict =
                List.of(Map.of("name", "owner", "address", owner.getAddress(), "power", BigInteger.valueOf(1000)),
                        Map.of("name", "testingAccount", "address", testingAccount.getAddress(), "power", BigInteger.valueOf(850)),
                        Map.of("name", "testingAccount1", "address", testingAccount1.getAddress(), "power", BigInteger.valueOf(770)),
                        Map.of("name", "testingAccount2", "address", testingAccount2.getAddress(), "power", BigInteger.valueOf(800)),
                        Map.of("name", "testingAccount3", "address", testingAccount3.getAddress(), "power", BigInteger.valueOf(990)),
                        Map.of("name", "testingAccount4", "address", testingAccount4.getAddress(), "power", BigInteger.valueOf(500)),
                        Map.of("name", "testingAccount5", "address", testingAccount5.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount6", "address", testingAccount6.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount7", "address", testingAccount7.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount8", "address", testingAccount8.getAddress(), "power", BigInteger.valueOf(250))
                );
        Map<String, Object> preps = Map.of("preps", prepDict);

        addAdminMethod();
        doReturn(preps).when(scoreSpy).callScore(eq(Map.class), eq(SYSTEM_ADDRESS), eq("getPRepTerm"));
        doReturn(prepDict.get(0)).when(scoreSpy).callScore(eq(Map.class), eq(SYSTEM_ADDRESS), eq("getPRep"), any());
        cpsScore.invoke(owner, "toggleMaintenance");
        cpsScore.invoke(owner, "setInitialBlock");

        cpsScore.invoke(owner, "registerPrep");
        assertEquals(1, ((List<?>) (cpsScore.call("getPReps"))).size());

        Map<String, BigInteger> loginPrep = (Map<String, BigInteger>) cpsScore.call("loginPrep", owner.getAddress());
        System.out.println(loginPrep);
        assertEquals(BigInteger.ONE, loginPrep.get("isRegistered"));

        cpsScore.invoke(owner, "unregisterPrep");
        assertEquals(0, ((List<?>) (cpsScore.call("getPReps"))).size());

        loginPrep = (Map<String, BigInteger>) cpsScore.call("loginPrep", owner.getAddress());
        System.out.println(loginPrep);
        assertEquals(BigInteger.ZERO, loginPrep.get("isRegistered"));
    }

    @Test
    void unregisterPrepNotInValidPrep() {
        List<Map<String, Object>> prepDict =
                List.of(Map.of("name", "owner", "address", owner.getAddress(), "power", BigInteger.valueOf(1000)),
                        Map.of("name", "testingAccount", "address", testingAccount.getAddress(), "power", BigInteger.valueOf(850)),
                        Map.of("name", "testingAccount1", "address", testingAccount1.getAddress(), "power", BigInteger.valueOf(770)),
                        Map.of("name", "testingAccount2", "address", testingAccount2.getAddress(), "power", BigInteger.valueOf(800)),
                        Map.of("name", "testingAccount3", "address", testingAccount3.getAddress(), "power", BigInteger.valueOf(990)),
                        Map.of("name", "testingAccount4", "address", testingAccount4.getAddress(), "power", BigInteger.valueOf(500)),
                        Map.of("name", "testingAccount5", "address", testingAccount5.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount6", "address", testingAccount6.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount7", "address", testingAccount7.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount8", "address", testingAccount8.getAddress(), "power", BigInteger.valueOf(250))
                );
        Map<String, Object> preps = Map.of("preps", prepDict);

        addAdminMethod();
        doReturn(preps).when(scoreSpy).callScore(eq(Map.class), eq(SYSTEM_ADDRESS), eq("getPRepTerm"));
        doReturn(prepDict.get(0)).when(scoreSpy).callScore(eq(Map.class), eq(SYSTEM_ADDRESS), eq("getPRep"), any());
        cpsScore.invoke(owner, "toggleMaintenance");
        cpsScore.invoke(owner, "setInitialBlock");
        cpsScore.invoke(owner, "registerPrep");

        Executable unregister = () -> cpsScore.invoke(testingAccount1, "unregisterPrep");
        expectErrorMessage(unregister, "Reverted(0): P-Rep is not registered yet.");
    }

    @Test
    void loginPrep() {
        List<Map<String, Object>> prepDict =
                List.of(Map.of("name", "owner", "address", owner.getAddress(), "power", BigInteger.valueOf(1000)),
                        Map.of("name", "testingAccount", "address", testingAccount.getAddress(), "power", BigInteger.valueOf(850)),
                        Map.of("name", "testingAccount1", "address", testingAccount1.getAddress(), "power", BigInteger.valueOf(770)),
                        Map.of("name", "testingAccount2", "address", testingAccount2.getAddress(), "power", BigInteger.valueOf(800)),
                        Map.of("name", "testingAccount3", "address", testingAccount3.getAddress(), "power", BigInteger.valueOf(990)),
                        Map.of("name", "testingAccount4", "address", testingAccount4.getAddress(), "power", BigInteger.valueOf(500)),
                        Map.of("name", "testingAccount5", "address", testingAccount5.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount6", "address", testingAccount6.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount7", "address", testingAccount7.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount8", "address", testingAccount8.getAddress(), "power", BigInteger.valueOf(250))
                );
        Map<String, Object> preps = Map.of("preps", prepDict);

        addAdminMethod();
        doReturn(preps).when(scoreSpy).callScore(eq(Map.class), eq(SYSTEM_ADDRESS), eq("getPRepTerm"));
        doReturn(prepDict.get(0)).when(scoreSpy).callScore(eq(Map.class), eq(SYSTEM_ADDRESS), eq("getPRep"), any());
        cpsScore.invoke(owner, "toggleMaintenance");
        cpsScore.invoke(owner, "setInitialBlock");
        cpsScore.invoke(owner, "registerPrep");

        Map<String, BigInteger> loginPrep = (Map<String, BigInteger>) cpsScore.call("loginPrep", owner.getAddress());
        assertEquals(BigInteger.ONE, loginPrep.get("isPRep"));
        assertEquals(BigInteger.ONE, loginPrep.get("isRegistered"));
        assertEquals(BigInteger.ZERO, loginPrep.get("payPenalty"));
        assertEquals(BigInteger.ONE, loginPrep.get("votingPRep"));

        loginPrep = (Map<String, BigInteger>) cpsScore.call("loginPrep", testingAccount.getAddress());
        assertEquals(BigInteger.ONE, loginPrep.get("isPRep"));
        assertEquals(BigInteger.ZERO, loginPrep.get("isRegistered"));
        assertEquals(BigInteger.ZERO, loginPrep.get("payPenalty"));
        assertEquals(BigInteger.ZERO, loginPrep.get("votingPRep"));

        loginPrep = (Map<String, BigInteger>) cpsScore.call("loginPrep", testingAccount9.getAddress());
        assertEquals(BigInteger.ZERO, loginPrep.get("isPRep"));
        assertEquals(BigInteger.ZERO, loginPrep.get("isRegistered"));
        assertEquals(BigInteger.ZERO, loginPrep.get("payPenalty"));
        assertEquals(BigInteger.ZERO, loginPrep.get("votingPRep"));

    }

    @Test
    void getPeriodStatus() {
        Map<String, ?> periodStatus = (Map<String, ?>) cpsScore.call("getPeriodStatus");
        assertEquals("None", periodStatus.get("previous_period_name"));
        assertEquals("None", periodStatus.get("period_name"));
        assertEquals(BigInteger.ZERO, periodStatus.get("next_block"));
        assertEquals(BigInteger.ZERO, periodStatus.get("remaining_time"));
        assertEquals(BigInteger.valueOf(1293600), periodStatus.get("period_span"));

        List<Map<String, Object>> prepDict =
                List.of(Map.of("name", "owner", "address", owner.getAddress(), "power", BigInteger.valueOf(1000)),
                        Map.of("name", "testingAccount", "address", testingAccount.getAddress(), "power", BigInteger.valueOf(850)),
                        Map.of("name", "testingAccount1", "address", testingAccount1.getAddress(), "power", BigInteger.valueOf(770)),
                        Map.of("name", "testingAccount2", "address", testingAccount2.getAddress(), "power", BigInteger.valueOf(800)),
                        Map.of("name", "testingAccount3", "address", testingAccount3.getAddress(), "power", BigInteger.valueOf(990)),
                        Map.of("name", "testingAccount4", "address", testingAccount4.getAddress(), "power", BigInteger.valueOf(500)),
                        Map.of("name", "testingAccount5", "address", testingAccount5.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount6", "address", testingAccount6.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount7", "address", testingAccount7.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount8", "address", testingAccount8.getAddress(), "power", BigInteger.valueOf(250))
                );
        Map<String, Object> preps = Map.of("preps", prepDict);

        addAdminMethod();
        doReturn(preps).when(scoreSpy).callScore(eq(Map.class), eq(SYSTEM_ADDRESS), eq("getPRepTerm"));
        doReturn(prepDict.get(0)).when(scoreSpy).callScore(eq(Map.class), eq(SYSTEM_ADDRESS), eq("getPRep"), any());
        cpsScore.invoke(owner, "toggleMaintenance");
        cpsScore.invoke(owner, "setInitialBlock");

        periodStatus = (Map<String, ?>) cpsScore.call("getPeriodStatus");
        assertEquals("None", periodStatus.get("previous_period_name"));
        assertEquals(APPLICATION_PERIOD, periodStatus.get("period_name"));
        assertEquals(BigInteger.valueOf(1293600), periodStatus.get("period_span"));
        System.out.println(periodStatus);
        assertEquals(BigInteger.valueOf(Context.getBlockHeight()).add(BLOCKS_DAY_COUNT.multiply(DAY_COUNT)), periodStatus.get("next_block"));
    }

    void setScoresMethod() {
        cpsScore.invoke(owner, "addAdmin", owner.getAddress());
        cpsScore.invoke(owner, "setCpsTreasuryScore", cpsTreasury);
        cpsScore.invoke(owner, "setCpfTreasuryScore", cpfTreasury);
        cpsScore.invoke(owner, "setBnusdScore", bnUSDScore);

    }

    public void registerPrepsMethod() {
        setScoresMethod();
        List<Map<String, Object>> prepDict =
                List.of(Map.of("name", "owner", "address", owner.getAddress(), "power", BigInteger.valueOf(1000)),
                        Map.of("name", "testingAccount", "address", testingAccount.getAddress(), "power", BigInteger.valueOf(850)),
                        Map.of("name", "testingAccount1", "address", testingAccount1.getAddress(), "power", BigInteger.valueOf(770)),
                        Map.of("name", "testingAccount2", "address", testingAccount2.getAddress(), "power", BigInteger.valueOf(800)),
                        Map.of("name", "testingAccount3", "address", testingAccount3.getAddress(), "power", BigInteger.valueOf(990)),
                        Map.of("name", "testingAccount4", "address", testingAccount4.getAddress(), "power", BigInteger.valueOf(500)),
                        Map.of("name", "testingAccount5", "address", testingAccount5.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount6", "address", testingAccount6.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount7", "address", testingAccount7.getAddress(), "power", BigInteger.valueOf(250)),
                        Map.of("name", "testingAccount8", "address", testingAccount8.getAddress(), "power", BigInteger.valueOf(250))
                );
        Map<String, Object> preps = Map.of("preps", prepDict);

        addAdminMethod();
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
        cpsScore.invoke(testingAccount6, "registerPrep");
        cpsScore.invoke(testingAccount7, "registerPrep");
        cpsScore.invoke(testingAccount8, "registerPrep");
    }


    @Test
    void submitProposal() {
        submitProposalMethod();
        Map<String, Object> proposalDetails = (Map<String, Object>) cpsScore.call("getProposalDetailsByHash", "Proposal 1");
        System.out.println(proposalDetails);
        proposalDetails.remove("sponsor_vote_reason");
        System.out.println(proposalDetails);
        assertEquals("Proposal 1", proposalDetails.get("ipfs_hash"));
        assertEquals("Title", proposalDetails.get("project_title"));
        assertEquals(2, proposalDetails.get("project_duration"));
        assertEquals(testingAccount.getAddress(), proposalDetails.get("sponsor_address"));
        assertEquals(owner.getAddress(), proposalDetails.get("contributor_address"));
        assertEquals("_sponsor_pending", proposalDetails.get("status"));
        assertEquals(BigInteger.valueOf(100).multiply(MULTIPLIER), proposalDetails.get("total_budget"));
        Map<String, Object> proposalDetailsOfStatus = (Map<String, Object>) cpsScore.call("getProposalDetails", SPONSOR_PENDING, owner.getAddress(), 0, 10);
        assertEquals(1, proposalDetailsOfStatus.get(COUNT));


        Map<String, Object> proposalVoteDetails = (Map<String, Object>) cpsScore.call("getVoteResult", "Proposal 1");


        Map<String, Object> proposalDetailsOf = ((List<Map<String, Object>>) proposalDetailsOfStatus.get(DATA)).get(0);
        assertEquals(proposalVoteDetails.get("approve_voters"), proposalDetailsOf.get("approve_voters"));
        assertEquals(proposalDetails.get("sponsor_vote_reason"), proposalDetailsOf.get("sponsor_vote_reason"));
        assertEquals(proposalDetails.get("sponsor_deposit_amount"), BigInteger.ZERO);
        assertEquals(proposalVoteDetails.get("reject_voters"), proposalDetailsOf.get("reject_voters"));
        assertEquals(proposalDetails.get("project_title"), proposalDetailsOf.get("project_title"));
        assertEquals(proposalDetails.get("token"), proposalDetailsOf.get("token"));
        assertEquals(proposalVoteDetails.get("abstained_votes"), proposalDetailsOf.get("abstained_votes"));
        assertEquals(proposalVoteDetails.get("abstain_voters"), proposalDetailsOf.get("abstain_voters"));
        assertEquals(proposalVoteDetails.get("approved_votes"), proposalDetailsOf.get("approved_votes"));
        assertEquals(proposalVoteDetails.get("total_votes"), proposalDetailsOf.get("total_votes"));
        assertEquals(proposalVoteDetails.get("total_voters"), proposalDetailsOf.get("total_voters"));
        assertEquals(proposalVoteDetails.get("approved_reports"), proposalDetailsOf.get("approved_reports"));
        assertEquals(proposalVoteDetails.get("rejected_votes"), proposalDetailsOf.get("rejected_votes"));
        assertEquals(proposalDetails.get("ipfs_hash"), proposalDetailsOf.get("ipfs_hash"));
        assertEquals(proposalDetails.get("total_budget"), proposalDetailsOf.get("total_budget"));

        Map<String, Object> proposalDetailsOfWallet = (Map<String, Object>) cpsScore.call("getProposalDetailByWallet", owner.getAddress(), 0);
        assertEquals(proposalDetails.size(), ((List<Map<String, Object>>) proposalDetailsOfWallet.get(DATA)).get(0).size());
        assertEquals(1, proposalDetailsOfWallet.get(COUNT));

        List<String> proposalKeys = (List<String>) cpsScore.call("getProposalKeys");
        assertEquals(List.of("Proposal 1"), proposalKeys);
    }

    void submitProposalMethod() {
        registerPrepsMethod();
        ProposalAttributes proposalAttributes = new ProposalAttributes();
        proposalAttributes.ipfs_hash = "Proposal 1";
        proposalAttributes.project_title = "Title";
        proposalAttributes.project_duration = 2;
        proposalAttributes.total_budget = BigInteger.valueOf(100);
        proposalAttributes.token = bnUSD;
        proposalAttributes.sponsor_address = testingAccount.getAddress();
        proposalAttributes.ipfs_link = "link";
        proposalAttributes.milestoneCount = 2;
        proposalAttributes.isMilestone = true;


        CPSCoreInterface.MilestonesAttributes milestonesAttributes1 = new CPSCoreInterface.MilestonesAttributes();
        milestonesAttributes1.completionPeriod = 1;
        milestonesAttributes1.budget = BigInteger.valueOf(30).multiply(ICX);
        milestonesAttributes1.id = 1;

        CPSCoreInterface.MilestonesAttributes milestonesAttributes2 = new CPSCoreInterface.MilestonesAttributes();
        milestonesAttributes2.completionPeriod = 2;
        milestonesAttributes2.budget = BigInteger.valueOf(60).multiply(ICX);
        milestonesAttributes2.id = 2;

        CPSCoreInterface.MilestonesAttributes[] milestonesAttributes = new CPSCoreInterface.MilestonesAttributes[]
                {milestonesAttributes1, milestonesAttributes2};


        Map<String, BigInteger> remainingSwapAmount = Map.of(
                "remaining_swap_amount", BigInteger.valueOf(1000).multiply(MULTIPLIER),
                "maxCap", BigInteger.valueOf(1000).multiply(MULTIPLIER));
        doReturn(remainingSwapAmount).when(scoreSpy).callScore(eq(Map.class), eq(cpfTreasury), eq("getRemainingSwapAmount"));
        doReturn(BigInteger.TEN).when(scoreSpy).callScore(eq(BigInteger.class), any(Address.class), eq("getOnsetPayment"));
        contextMock.when(() -> Context.getValue()).thenReturn(BigInteger.valueOf(50).multiply(MULTIPLIER));
        byte[] tx_hash = "transaction".getBytes();
        contextMock.when(() -> Context.getTransactionHash()).thenReturn(tx_hash);
        doNothing().when(scoreSpy).callScore(eq(BigInteger.valueOf(25).multiply(MULTIPLIER)), eq(SYSTEM_ADDRESS), eq("burn"));
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), eq(0));
        cpsScore.invoke(owner, "submitProposal", proposalAttributes, milestonesAttributes);

    }

    @Test
    void sponsorVote() {
        submitAndSponsorVote();
        Map<String, Object> proposalDetails = (Map<String, Object>) cpsScore.call("getProposalDetailsByHash", "Proposal 1");
        System.out.println(proposalDetails);
        assertEquals(PENDING, proposalDetails.get("status"));
        assertEquals(BOND_RECEIVED, proposalDetails.get("sponsor_deposit_status"));
        Map<String, Map<String, Map<String, BigInteger>>> projectAmounts = (Map<String, Map<String, Map<String, BigInteger>>>) cpsScore.call("getProjectAmounts");
        Map<String, Object> amount = Map.of(AMOUNT, Map.of(Constants.ICX, BigInteger.ZERO,
                        bnUSD, BigInteger.valueOf(100).multiply(MULTIPLIER)),
                "count", 1);
        assertEquals(amount, (projectAmounts.get(PENDING)));

        Map<String, Object> sponosrsRequest = (Map<String, Object>) cpsScore.call("getSponsorsRequests", APPROVED, testingAccount.getAddress(), 0, 10);
        System.out.println("Sponsors request" + sponosrsRequest);
    }

    void submitAndSponsorVote() {
        submitProposalMethod();
        contextMock.when(caller()).thenReturn(bnUSDScore);
        JsonObject sponsorVoteParams = new JsonObject();
        sponsorVoteParams.add("method", "sponsorVote");
        JsonObject params = new JsonObject();
        params.add(IPFS_HASH, "Proposal 1");
        params.add(VOTE, ACCEPT);
        params.add(VOTE_REASON, "reason");
        sponsorVoteParams.add("params", params);

        cpsScore.invoke(testingAccount, "tokenFallback", testingAccount.getAddress(), BigInteger.valueOf(12).multiply(MULTIPLIER), sponsorVoteParams.toString().getBytes());
    }

    void updateNextBlock() {
        cpsScore.invoke(owner, "updateNextBlock", 0);
    }

    @Test
    @DisplayName("vote approve then change it to reject")
    void voteProposal() {
        submitAndSponsorVote();
        contextMock.when(caller()).thenReturn(owner.getAddress());
        updateNextBlock();
        doReturn(BigInteger.valueOf(15)).when(scoreSpy).getApplicationPeriod();
        cpsScore.invoke(owner, "updatePeriod");
        getPeriodStatusMethod();
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), any());
        cpsScore.invoke(owner, "voteProposal", "Proposal 1", APPROVE, "reason", false);
        Map<String, Object> proposalDetails = getVoteResult("Proposal 1");

        assertEquals(BigInteger.valueOf(1000), proposalDetails.get("approved_votes"));
        assertEquals(1, proposalDetails.get("approve_voters"));
        assertEquals(BigInteger.ZERO, proposalDetails.get("rejected_votes"));
        assertEquals(0, proposalDetails.get("reject_voters"));
        assertEquals(BigInteger.valueOf(10000), proposalDetails.get("total_votes"));
        assertEquals(10, proposalDetails.get("total_voters"));


        // changing vote with same vote
//        Executable alreadyVoted = () -> cpsScore.invoke(owner, "voteProposal", "Proposal 1", APPROVE, "reason", true);
//        expectErrorMessage(alreadyVoted,"Reverted(0): CPS Score:: Cannot cast same vote. Change your vote");


        cpsScore.invoke(owner, "voteProposal", "Proposal 1", REJECT, "reason", true);

        proposalDetails = getVoteResult("Proposal 1");

        assertEquals(BigInteger.valueOf(0), proposalDetails.get("approved_votes"));
        assertEquals(0, proposalDetails.get("approve_voters"));
        assertEquals(BigInteger.valueOf(1000), proposalDetails.get("rejected_votes"));
        assertEquals(1, proposalDetails.get("reject_voters"));
        assertEquals(BigInteger.valueOf(10000), proposalDetails.get("total_votes"));
        assertEquals(10, proposalDetails.get("total_voters"));


        assertEquals(1, cpsScore.call("checkChangeVote", owner.getAddress(), "Proposal 1", "proposal"));

        Map<String, Object> voteResult = (Map<String, Object>) cpsScore.call("getVoteResult", "Proposal 1");
        System.out.println(voteResult);
    }

    @Test
    @DisplayName("trying to vote on already vote proposal")
    void voteOnProposalException() {
        voteProposal();
        contextMock.when(caller()).thenReturn(owner.getAddress());
        Executable alreadyVoted = () -> cpsScore.invoke(owner, "voteProposal", "Proposal 1", REJECT, "reason", false);
        expectErrorMessage(alreadyVoted, "Reverted(0): CPS Score:: Already Voted");
    }

    @Test
    @DisplayName("vote reject then change it to approve")
    void voteProposal2() {
        submitAndSponsorVote();
        contextMock.when(caller()).thenReturn(owner.getAddress());
        updateNextBlock();
        doReturn(BigInteger.valueOf(15)).when(scoreSpy).getApplicationPeriod();
        cpsScore.invoke(owner, "updatePeriod");
        getPeriodStatusMethod();
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), eq(8));
        cpsScore.invoke(owner, "voteProposal", "Proposal 1", REJECT, "reason", false);
        Map<String, Object> proposalDetails = getVoteResult("Proposal 1");

        assertEquals(BigInteger.valueOf(0), proposalDetails.get("approved_votes"));
        assertEquals(0, proposalDetails.get("approve_voters"));
        assertEquals(BigInteger.valueOf(1000), proposalDetails.get("rejected_votes"));
        assertEquals(1, proposalDetails.get("reject_voters"));
        assertEquals(BigInteger.valueOf(10000), proposalDetails.get("total_votes"));
        assertEquals(10, proposalDetails.get("total_voters"));


        cpsScore.invoke(owner, "voteProposal", "Proposal 1", ABSTAIN, "reason", true);
        proposalDetails = getVoteResult("Proposal 1");

        assertEquals(BigInteger.valueOf(1000), proposalDetails.get("abstained_votes"));
        assertEquals(1, proposalDetails.get("abstain_voters"));
        assertEquals(BigInteger.ZERO, proposalDetails.get("rejected_votes"));
        assertEquals(0, proposalDetails.get("reject_voters"));
        assertEquals(BigInteger.valueOf(10000), proposalDetails.get("total_votes"));
        assertEquals(10, proposalDetails.get("total_voters"));


        assertEquals(1, cpsScore.call("checkChangeVote", owner.getAddress(), "Proposal 1", "proposal"));
    }

    void voteProposalMethod() {
        submitAndSponsorVote();
        contextMock.when(caller()).thenReturn(owner.getAddress());
        updateNextBlock();
        doReturn(BigInteger.valueOf(15)).when(scoreSpy).getApplicationPeriod();
        cpsScore.invoke(owner, "updatePeriod");
        getPeriodStatusMethod();
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), any());
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

        contextMock.when(caller()).thenReturn(testingAccount6.getAddress());
        cpsScore.invoke(testingAccount6, "voteProposal", "Proposal 1", APPROVE, "reason", false);
        cpsScore.invoke(testingAccount6, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(testingAccount7.getAddress());
        cpsScore.invoke(testingAccount7, "voteProposal", "Proposal 1", APPROVE, "reason", false);
        cpsScore.invoke(testingAccount7, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(testingAccount8.getAddress());
        cpsScore.invoke(testingAccount8, "voteProposal", "Proposal 1", APPROVE, "reason", false);
        cpsScore.invoke(testingAccount8, "votePriority", (Object) proposal);
    }

    @Test
    void submitMultipleProposals() {
        registerPrepsMethod();
        Map<String, BigInteger> remainingSwapAmount = Map.of(
                "remaining_swap_amount", BigInteger.valueOf(1000).multiply(MULTIPLIER),
                "maxCap", BigInteger.valueOf(1000).multiply(MULTIPLIER));
        doReturn(BigInteger.TEN).when(scoreSpy).callScore(eq(BigInteger.class), any(Address.class), eq("getOnsetPayment"));
        doReturn(remainingSwapAmount).when(scoreSpy).callScore(eq(Map.class), eq(cpfTreasury), eq("getRemainingSwapAmount"));
        contextMock.when(() -> Context.getValue()).thenReturn(BigInteger.valueOf(50).multiply(MULTIPLIER));
        byte[] tx_hash = "transaction".getBytes();
        contextMock.when(() -> Context.getTransactionHash()).thenReturn(tx_hash);
        doNothing().when(scoreSpy).callScore(eq(BigInteger.valueOf(25).multiply(MULTIPLIER)), eq(SYSTEM_ADDRESS), eq("burn"));
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), eq(0));
        for (int i = 0; i < 10; i++) {
            ProposalAttributes proposalAttributes = new ProposalAttributes();
            proposalAttributes.ipfs_hash = "Proposal " + i;
            proposalAttributes.project_title = "Title";
            proposalAttributes.project_duration = 2;
            proposalAttributes.total_budget = BigInteger.valueOf(100);
            proposalAttributes.token = bnUSD;
            proposalAttributes.sponsor_address = testingAccount.getAddress();
            proposalAttributes.ipfs_link = "link";
            proposalAttributes.milestoneCount = 2;
            proposalAttributes.isMilestone = true;

            CPSCoreInterface.MilestonesAttributes milestonesAttributes1 = new CPSCoreInterface.MilestonesAttributes();
            milestonesAttributes1.completionPeriod = 1;
            milestonesAttributes1.budget = BigInteger.valueOf(30).multiply(ICX);
            milestonesAttributes1.id = 1;

            CPSCoreInterface.MilestonesAttributes milestonesAttributes2 = new CPSCoreInterface.MilestonesAttributes();
            milestonesAttributes2.completionPeriod = 2;
            milestonesAttributes2.budget = BigInteger.valueOf(60).multiply(ICX);
            milestonesAttributes2.id = 2;

            CPSCoreInterface.MilestonesAttributes[] milestonesAttributes = new CPSCoreInterface.MilestonesAttributes[]
                    {milestonesAttributes1, milestonesAttributes2};


            cpsScore.invoke(owner, "submitProposal", proposalAttributes, milestonesAttributes);
        }
        contextMock.when(caller()).thenReturn(bnUSDScore);
        JsonObject sponsorVoteParams = new JsonObject();
        sponsorVoteParams.add("method", "sponsorVote");
        JsonObject params = new JsonObject();
        for (int i = 0; i < 10; i++) {
            params.add(IPFS_HASH, "Proposal " + i);
            params.add(VOTE, ACCEPT);
            params.add(VOTE_REASON, "reason");
            sponsorVoteParams.add("params", params);

            cpsScore.invoke(testingAccount, "tokenFallback", testingAccount.getAddress(), BigInteger.valueOf(12).multiply(MULTIPLIER), sponsorVoteParams.toString().getBytes());
        }
    }

    @Test
    void voteMultipleProposals() {
        submitMultipleProposals();
        contextMock.when(caller()).thenReturn(owner.getAddress());
        updateNextBlock();
        doReturn(BigInteger.valueOf(15)).when(scoreSpy).getApplicationPeriod();
        cpsScore.invoke(owner, "updatePeriod");
        getPeriodStatusMethod();
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), any());
        String[] proposal = new String[]{"Proposal 0", "Proposal 1", "Proposal 2", "Proposal 3", "Proposal 4", "Proposal 5",
                "Proposal 6", "Proposal 7", "Proposal 8", "Proposal 9"};

        for (int i = 0; i < 10; i++) {
            contextMock.when(caller()).thenReturn(owner.getAddress());
            cpsScore.invoke(owner, "voteProposal", "Proposal " + i, APPROVE, "reason", false);

            contextMock.when(caller()).thenReturn(testingAccount.getAddress());
            cpsScore.invoke(testingAccount, "voteProposal", "Proposal " + i, APPROVE, "reason", false);

            contextMock.when(caller()).thenReturn(testingAccount1.getAddress());
            cpsScore.invoke(testingAccount1, "voteProposal", "Proposal " + i, APPROVE, "reason", false);

            contextMock.when(caller()).thenReturn(testingAccount2.getAddress());
            cpsScore.invoke(testingAccount2, "voteProposal", "Proposal " + i, APPROVE, "reason", false);

            contextMock.when(caller()).thenReturn(testingAccount3.getAddress());
            cpsScore.invoke(testingAccount3, "voteProposal", "Proposal " + i, APPROVE, "reason", false);

            contextMock.when(caller()).thenReturn(testingAccount4.getAddress());
            cpsScore.invoke(testingAccount4, "voteProposal", "Proposal " + i, APPROVE, "reason", false);

            contextMock.when(caller()).thenReturn(testingAccount5.getAddress());
            cpsScore.invoke(testingAccount5, "voteProposal", "Proposal " + i, APPROVE, "reason", false);
        }

        contextMock.when(caller()).thenReturn(owner.getAddress());
        cpsScore.invoke(owner, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(testingAccount.getAddress());
        cpsScore.invoke(owner, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(testingAccount1.getAddress());
        cpsScore.invoke(owner, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(testingAccount2.getAddress());
        cpsScore.invoke(owner, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(testingAccount3.getAddress());
        cpsScore.invoke(owner, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(testingAccount4.getAddress());
        cpsScore.invoke(owner, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(testingAccount5.getAddress());
        cpsScore.invoke(owner, "votePriority", (Object) proposal);

    }

    void voteProposalMethodReject() {
        submitAndSponsorVote();
        contextMock.when(caller()).thenReturn(owner.getAddress());
        updateNextBlock();
        doReturn(BigInteger.valueOf(15)).when(scoreSpy).getApplicationPeriod();
        doReturn(BigInteger.valueOf(15)).when(scoreSpy).getVotingPeriod();
        cpsScore.invoke(owner, "updatePeriod");
        getPeriodStatusMethod();
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), any());
        String[] proposal = new String[1];
        proposal[0] = "Proposal 1";
        cpsScore.invoke(owner, "voteProposal", "Proposal 1", REJECT, "reason", false);
        cpsScore.invoke(owner, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(testingAccount.getAddress());
        cpsScore.invoke(testingAccount, "voteProposal", "Proposal 1", REJECT, "reason", false);
        cpsScore.invoke(owner, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(testingAccount1.getAddress());
        cpsScore.invoke(testingAccount1, "voteProposal", "Proposal 1", REJECT, "reason", false);
        cpsScore.invoke(owner, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(testingAccount2.getAddress());
        cpsScore.invoke(testingAccount2, "voteProposal", "Proposal 1", REJECT, "reason", false);
        cpsScore.invoke(owner, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(testingAccount3.getAddress());
        cpsScore.invoke(testingAccount3, "voteProposal", "Proposal 1", REJECT, "reason", false);
        cpsScore.invoke(owner, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(testingAccount4.getAddress());
        cpsScore.invoke(testingAccount4, "voteProposal", "Proposal 1", REJECT, "reason", false);
        cpsScore.invoke(owner, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(testingAccount5.getAddress());
        cpsScore.invoke(testingAccount5, "voteProposal", "Proposal 1", REJECT, "reason", false);
        cpsScore.invoke(owner, "votePriority", (Object) proposal);
    }

    void updatePeriods() {
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
    void updatePeriodAfterProposalVoting() {
        voteProposalMethod();
        contextMock.when(caller()).thenReturn(owner.getAddress());
        updateNextBlock();

        Map<String, BigInteger> totalFunds = Map.of(
                Constants.ICX, BigInteger.valueOf(1000).multiply(MULTIPLIER),
                bnUSD, BigInteger.valueOf(1000).multiply(MULTIPLIER)
        );

        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), any());

                doReturn(totalFunds).when(scoreSpy).callScore(eq(Map.class), eq(cpfTreasury), eq("getTotalFunds"));
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("transferProposalFundToCpsTreasury"),
                eq("Proposal 1"), eq(2), eq(testingAccount.getAddress()), eq(owner.getAddress()),
                eq(bnUSD), eq(BigInteger.valueOf(100).multiply(MULTIPLIER)));

        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("resetSwapState"));
        doReturn(BigInteger.valueOf(15)).when(scoreSpy).getVotingPeriod();
        updatePeriods();

        Map<String, Object> proposalDetails = getProposalDetailsByHash("Proposal 1");
        Map<String, Object> activeProposals = (Map<String, Object>) cpsScore.call("getActiveProposalsList", 0);
        assertEquals(ACTIVE, proposalDetails.get("status"));
        List<Map<String, Object>> activeProposalList = (List<Map<String, Object>>) activeProposals.get(DATA);
        assertEquals(List.of(proposalDetails).size(), activeProposalList.size());

        Map<String, Object> sponosrsRequest = (Map<String, Object>) cpsScore.call("getSponsorsRequests", APPROVED, testingAccount.getAddress(), 0, 10);
        System.out.println("Sponsors request" + sponosrsRequest);

        Map<String, Integer> sponsorsRecord = (Map<String, Integer>) cpsScore.call("getSponsorsRecord");
        assertEquals(1, sponsorsRecord.get(testingAccount.getAddress().toString()));

        Map<String, Map<String, Map<String, BigInteger>>> projectAmounts = (Map<String, Map<String, Map<String, BigInteger>>>) cpsScore.call("getProjectAmounts");
        Map<String, Object> amount = Map.of(AMOUNT, Map.of(Constants.ICX, BigInteger.ZERO,
                        bnUSD, BigInteger.valueOf(100).multiply(MULTIPLIER)),
                "count", 1);
        assertEquals(amount, (projectAmounts.get(ACTIVE)));

        Map<String, Object> voteResult = (Map<String, Object>) cpsScore.call("getVoteResult", "Proposal 1");
        System.out.println("voteResult: " + voteResult);
    }

    @Test
    void rejectProposal() {
        voteProposalMethodReject();
        contextMock.when(caller()).thenReturn(owner.getAddress());
        updateNextBlock();

        Map<String, BigInteger> totalFunds = Map.of(
                Constants.ICX, BigInteger.valueOf(1000).multiply(MULTIPLIER),
                bnUSD, BigInteger.valueOf(1000).multiply(MULTIPLIER)
        );
        contextMock.when(() -> Context.transfer(any(), any())).thenAnswer((Answer<Void>) invocation -> null);
        doReturn(totalFunds).when(scoreSpy).callScore(eq(Map.class), eq(cpfTreasury), eq("getTotalFunds"));


        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("resetSwapState"));
        doNothing().when(scoreSpy).callScore(eq(BigInteger.ZERO), eq(SYSTEM_ADDRESS), eq("burn"));
        updatePeriods();

        Map<String, Object> proposalDetails = getProposalDetailsByHash("Proposal 1");
        proposalDetails.remove("sponsor_vote_reason");
        assertEquals(REJECTED, proposalDetails.get("status"));

        Map<String, BigInteger> claimableSponsorBond = (Map<String, BigInteger>) cpsScore.call("checkClaimableSponsorBond", testingAccount.getAddress());
        assertEquals(BigInteger.valueOf(12).multiply(MULTIPLIER), claimableSponsorBond.get(bnUSD));

        Map<String, Object> voteResult = (Map<String, Object>) cpsScore.call("getVoteResult", "Proposal 1");
        System.out.println("voteResult: " + voteResult);

        Map<String, Object> proposalsHistory = (Map<String, Object>) cpsScore.call("getProposalsHistory", 0);
        List<Map<String, Object>> proposalHistory = (List<Map<String, Object>>) proposalsHistory.get(DATA);

        assertEquals(proposalDetails.size(), proposalHistory.get(0).size());
    }

    @Test
    void submitProgressReport() {
        ProgressReportAttributes progressReport = new ProgressReportAttributes();
        progressReport.ipfs_hash = "Proposal 1";
        progressReport.report_hash = "Report 1";
        progressReport.ipfs_link = "Link";
        progressReport.progress_report_title = "Progress Report Title";
        progressReport.budget_adjustment = true;
        progressReport.additional_budget = BigInteger.valueOf(10);
        progressReport.additional_month = 1;

        CPSCoreInterface.MilestoneSubmission milestoneSubmission1 = new CPSCoreInterface.MilestoneSubmission();
        milestoneSubmission1.id = 1;
        milestoneSubmission1.status = true;

        CPSCoreInterface.MilestoneSubmission milestoneSubmission2 = new CPSCoreInterface.MilestoneSubmission();
        milestoneSubmission2.id = 2;
        milestoneSubmission2.status = true;
        CPSCoreInterface.MilestoneSubmission[] milestoneSubmission = new CPSCoreInterface.MilestoneSubmission[]{
                milestoneSubmission1, milestoneSubmission2};

        updatePeriodAfterProposalVoting();

        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), any());
        cpsScore.invoke(owner, "toggleBudgetAdjustmentFeature");
        cpsScore.invoke(owner, "submitProgressReport", progressReport, milestoneSubmission);

        @SuppressWarnings("unchecked")
        Map<String, Object> progressReportDetails = (Map<String, Object>) cpsScore.call("getProgressReportsByHash", "Report 1");

        assertEquals(progressReport.ipfs_hash, progressReportDetails.get("ipfs_hash"));
        assertEquals(progressReport.report_hash, progressReportDetails.get("report_hash"));
        assertEquals(progressReport.ipfs_link, progressReportDetails.get("ipfs_link"));
        assertEquals(progressReport.progress_report_title, progressReportDetails.get("progress_report_title"));
        assertEquals(progressReport.budget_adjustment, progressReportDetails.get("budget_adjustment"));
        assertEquals(progressReport.additional_budget.multiply(MULTIPLIER), progressReportDetails.get("additional_budget"));
        assertEquals(progressReport.additional_month, progressReportDetails.get("additional_month"));
//        assertEquals(progressReport.percentage_completed, progressReportDetails.get("percentage_completed"));

        List<String> progressKeys = (List<String>) cpsScore.call("getProgressKeys");
        assertEquals(List.of("Report 1"), progressKeys);

        Map<String, Object> progressReports = (Map<String, Object>) cpsScore.call("getProgressReports", WAITING, 0, 10);
        System.out.println("Progerss reports: " + progressReports);
//        assertEquals(List.of(progressReportDetails), progressReports.get(DATA));
        assertEquals(1, progressReports.get(COUNT));

        Map<String, Object> progressReportsByProposal = (Map<String, Object>) cpsScore.call("getProgressReportsByProposal", "Proposal 1");
//        assertEquals(List.of(progressReportDetails), progressReportsByProposal.get(DATA));
        assertEquals(1, progressReportsByProposal.get(COUNT));
    }

    @Test
    void voteProgressReportExceptions() {
        submitProgressReport();
        updateNextBlock();
        cpsScore.invoke(owner, "updatePeriod");
        getPeriodStatusMethod();
        CPSCoreInterface.MilestoneVoteAttributes milestoneVoteAttributes = new CPSCoreInterface.MilestoneVoteAttributes();
        milestoneVoteAttributes.vote = APPROVE;
        milestoneVoteAttributes.id = 1;

        CPSCoreInterface.MilestoneVoteAttributes milestoneVoteAttributes2 = new CPSCoreInterface.MilestoneVoteAttributes();
        milestoneVoteAttributes2.vote = APPROVE;
        milestoneVoteAttributes2.id = 3;

        CPSCoreInterface.MilestoneVoteAttributes[] milestoneVoteAttributesList = new CPSCoreInterface.MilestoneVoteAttributes[]{
                milestoneVoteAttributes, milestoneVoteAttributes2};
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), eq(8));
        Executable call = () -> cpsScore.invoke(owner, "voteProgressReport", "Report 1", "reason",
                milestoneVoteAttributesList, "_reject", false);
        expectErrorMessage(call, "Reverted(0): CPS Score: Voting can only be done for milestone " +
                "submitted in this progress report");

    }

    @Test
    void voteProgressReportException() {
        submitProgressReport();
        updateNextBlock();
        cpsScore.invoke(owner, "updatePeriod");
        getPeriodStatusMethod();
        CPSCoreInterface.MilestoneVoteAttributes milestoneVoteAttributes = new CPSCoreInterface.MilestoneVoteAttributes();
        milestoneVoteAttributes.vote = APPROVE;
        milestoneVoteAttributes.id = 1;

        CPSCoreInterface.MilestoneVoteAttributes[] milestoneVoteAttributesList = new CPSCoreInterface.MilestoneVoteAttributes[]{
                milestoneVoteAttributes};
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), eq(8));
        Executable call = () -> cpsScore.invoke(owner, "voteProgressReport", "Report 1", "reason",
                milestoneVoteAttributesList, "_reject", false);
        expectErrorMessage(call, "Reverted(0): CPS Score:: You should submit votes for all milestones of the progress report");

        milestoneVoteAttributes = new CPSCoreInterface.MilestoneVoteAttributes();
        milestoneVoteAttributes.vote = APPROVE;
        milestoneVoteAttributes.id = 1;

        CPSCoreInterface.MilestoneVoteAttributes milestoneVoteAttributes2 = new CPSCoreInterface.MilestoneVoteAttributes();
        milestoneVoteAttributes2.vote = APPROVE;
        milestoneVoteAttributes2.id = 1;

        CPSCoreInterface.MilestoneVoteAttributes[] milestoneVoteAttributesList2 = new CPSCoreInterface.MilestoneVoteAttributes[]{
                milestoneVoteAttributes, milestoneVoteAttributes2};
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), eq(8));
        Executable call2 = () -> cpsScore.invoke(owner, "voteProgressReport", "Report 1", "reason",
                milestoneVoteAttributesList2, "_reject", false);
        expectErrorMessage(call2, "Reverted(0): You should submit votes for all milestones of the progress report");

    }

    @Test
    void voteProgressReport() {
        submitProgressReport();
        updateNextBlock();
        cpsScore.invoke(owner, "updatePeriod");
        getPeriodStatusMethod();
        CPSCoreInterface.MilestoneVoteAttributes milestoneVoteAttributes = new CPSCoreInterface.MilestoneVoteAttributes();
        milestoneVoteAttributes.vote = APPROVE;
        milestoneVoteAttributes.id = 1;

        CPSCoreInterface.MilestoneVoteAttributes milestoneVoteAttributes2 = new CPSCoreInterface.MilestoneVoteAttributes();
        milestoneVoteAttributes2.vote = APPROVE;
        milestoneVoteAttributes2.id = 2;

        CPSCoreInterface.MilestoneVoteAttributes[] milestoneVoteAttributesList = new CPSCoreInterface.MilestoneVoteAttributes[]{
                milestoneVoteAttributes, milestoneVoteAttributes2};
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), eq(8));
        cpsScore.invoke(owner, "voteProgressReport", "Report 1", "reason", milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount.getAddress());
        cpsScore.invoke(testingAccount, "voteProgressReport", "Report 1", "reason", (Object) milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount1.getAddress());
        cpsScore.invoke(testingAccount1, "voteProgressReport", "Report 1", "reason", milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount2.getAddress());
        cpsScore.invoke(testingAccount2, "voteProgressReport", "Report 1", "reason", (Object) milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount3.getAddress());
        cpsScore.invoke(testingAccount3, "voteProgressReport", "Report 1", "reason", (Object) milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount4.getAddress());
        cpsScore.invoke(testingAccount4, "voteProgressReport", "Report 1", "reason", (Object) milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount5.getAddress());
        cpsScore.invoke(testingAccount5, "voteProgressReport", "Report 1", "reason", (Object) milestoneVoteAttributesList, "_reject", false);

        @SuppressWarnings("unchecked")
        Map<String, Object> progressReportVoteDetails = (Map<String, Object>) cpsScore.call("getProgressReportVoteDetails", "Report 1");
        Map<String, Object> budgetAdjustmentDetails = (Map<String, Object>) cpsScore.call("getBudgetAdjustmentDetails", "Report 1");


        assertEquals(10, progressReportVoteDetails.get(TOTAL_VOTERS));
        assertEquals(0, budgetAdjustmentDetails.get(BUDGET_APPROVE_VOTERS));

        Map<String, Object> milestoneReport = (Map<String, Object>) cpsScore.call("getMilestonesReport", "Proposal 1", 1);
        assertEquals(7, milestoneReport.get(APPROVE_VOTERS));

        Map<String, Object> voteResult = (Map<String, Object>) cpsScore.call("getProgressReportResult", "Report 1");
        System.out.println("progress report vote Result: " + voteResult);
    }

    @Test
    void voteProgressReportAgain() {
        voteProgressReport();
        getPeriodStatusMethod();
        CPSCoreInterface.MilestoneVoteAttributes milestoneVoteAttributes = new CPSCoreInterface.MilestoneVoteAttributes();
        milestoneVoteAttributes.vote = APPROVE;
        milestoneVoteAttributes.id = 1;

        CPSCoreInterface.MilestoneVoteAttributes milestoneVoteAttributes2 = new CPSCoreInterface.MilestoneVoteAttributes();
        milestoneVoteAttributes2.vote = APPROVE;
        milestoneVoteAttributes2.id = 2;

        CPSCoreInterface.MilestoneVoteAttributes[] milestoneVoteAttributesList = new CPSCoreInterface.MilestoneVoteAttributes[]{
                milestoneVoteAttributes, milestoneVoteAttributes2};
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), eq(8));
        Executable call = () -> cpsScore.invoke(owner, "voteProgressReport", "Report 1", "reason",
                milestoneVoteAttributesList, "_reject", false);
        expectErrorMessage(call, "Reverted(0): CPS Score:: Already Voted");

    }

    @Test
    void voteProgressReportVoteChangeFromApproveToReject() {
        submitProgressReport();
        updateNextBlock();
        cpsScore.invoke(owner, "updatePeriod");
        getPeriodStatusMethod();
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), eq(8));
        // vote change only on milestone 1
        CPSCoreInterface.MilestoneVoteAttributes milestoneVoteAttributes = new CPSCoreInterface.MilestoneVoteAttributes();
        milestoneVoteAttributes.vote = APPROVE;
        milestoneVoteAttributes.id = 1;

        CPSCoreInterface.MilestoneVoteAttributes milestoneVoteAttributes2 = new CPSCoreInterface.MilestoneVoteAttributes();
        milestoneVoteAttributes2.vote = APPROVE;
        milestoneVoteAttributes2.id = 2;

        CPSCoreInterface.MilestoneVoteAttributes[] milestoneVoteAttributesList = new CPSCoreInterface.MilestoneVoteAttributes[]{
                milestoneVoteAttributes, milestoneVoteAttributes2};

        cpsScore.invoke(owner, "voteProgressReport", "Report 1", "reason", milestoneVoteAttributesList, "_reject", false);

        contextMock.when(caller()).thenReturn(testingAccount.getAddress());
        cpsScore.invoke(testingAccount, "voteProgressReport", "Report 1", "I accept", (Object) milestoneVoteAttributesList, "_reject", false);

        assertEquals(0, cpsScore.call("checkChangeVote", owner.getAddress(), "Report 1", "progress_reports"));
        assertEquals(0, cpsScore.call("checkChangeVote", testingAccount.getAddress(), "Report 1", "progress_reports"));


        Map<String, Object> progressReportDetails = (Map<String, Object>) cpsScore.call("getProgressReportsByHash", "Report 1");
        Map<String, Object> milestoneReport = (Map<String, Object>) cpsScore.call("getMilestonesReport", "Proposal 1", 1);

        assertEquals(2, milestoneReport.get(APPROVE_VOTERS));
        assertEquals(BigInteger.valueOf(2000), milestoneReport.get(APPROVED_VOTES));
        assertEquals(BigInteger.valueOf(0), milestoneReport.get(REJECTED_VOTES));
        assertEquals(2, milestoneReport.get(APPROVE_VOTERS));
        assertEquals(0, milestoneReport.get(REJECT_VOTERS));

        milestoneVoteAttributes = new CPSCoreInterface.MilestoneVoteAttributes();
        milestoneVoteAttributes.vote = REJECT;
        milestoneVoteAttributes.id = 1;

        milestoneVoteAttributesList = new CPSCoreInterface.MilestoneVoteAttributes[]{
                milestoneVoteAttributes};

        System.out.println("reason " + cpsScore.call("getProgressReportVoters", "Report 1"));

        contextMock.when(caller()).thenReturn(owner.getAddress());
        cpsScore.invoke(owner, "voteProgressReport", "Report 1", "I am rejecting", milestoneVoteAttributesList, "_reject", true);

        assertEquals(1, cpsScore.call("checkChangeVote", owner.getAddress(), "Report 1", "progress_reports"));
        assertEquals(0, cpsScore.call("checkChangeVote", testingAccount.getAddress(), "Report 1", "progress_reports"));


        contextMock.when(caller()).thenReturn(testingAccount.getAddress());
        cpsScore.invoke(testingAccount, "voteProgressReport", "Report 1", "Lets rejects ", milestoneVoteAttributesList, "_reject", true);


        milestoneReport = (Map<String, Object>) cpsScore.call("getMilestonesReport", "Proposal 1", 1);
        assertEquals(BigInteger.valueOf(0), milestoneReport.get(APPROVED_VOTES));
        assertEquals(BigInteger.valueOf(2000), milestoneReport.get(REJECTED_VOTES));
//        assertEquals(BigInteger.valueOf(0), progressReportDetails.get(BUDGET_APPROVED_VOTES));
//        assertEquals(BigInteger.valueOf(1000), progressReportDetails.get(BUDGET_REJECTED_VOTES));
        assertEquals(0, milestoneReport.get(APPROVE_VOTERS));
        assertEquals(2, milestoneReport.get(REJECT_VOTERS));
//        assertEquals(0, progressReportDetails.get(BUDGET_APPROVE_VOTERS));
//        assertEquals(1, progressReportDetails.get(BUDGET_REJECT_VOTERS));

        System.out.println("reason 1 " + cpsScore.call("getProgressReportVoters", "Report 1"));


        assertEquals(1, cpsScore.call("checkChangeVote", owner.getAddress(), "Report 1", "progress_reports"));
    }

    @Test
    void updatePeriodAfterProgressReportSubmission() {
        voteProgressReport();
        contextMock.when(caller()).thenReturn(owner.getAddress());
        updateNextBlock();

        Map<String, BigInteger> totalFunds = Map.of(
                Constants.ICX, BigInteger.valueOf(1000).multiply(MULTIPLIER),
                bnUSD, BigInteger.valueOf(1000).multiply(MULTIPLIER)
        );

        doReturn(totalFunds).when(scoreSpy).callScore(eq(Map.class), eq(cpfTreasury), eq("getTotalFunds"));
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("transferProposalFundToCpsTreasury"),
                eq("Proposal 1"), eq(2), eq(testingAccount.getAddress()), eq(owner.getAddress()),
                eq(bnUSD), eq(BigInteger.valueOf(100).multiply(MULTIPLIER)));

        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("resetSwapState"));
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("updateProposalFund"), eq("Proposal 1"), eq(bnUSD), eq(BigInteger.valueOf(10).multiply(MULTIPLIER)), eq(1));
        doNothing().when(scoreSpy).callScore(eq(cpsTreasury), eq("sendInstallmentToContributor"), eq("Proposal 1"), any(BigInteger.class));
        doNothing().when(scoreSpy).callScore(eq(cpsTreasury), eq("sendRewardToSponsor"), eq("Proposal 1"), eq(2));
        doNothing().when(scoreSpy).callScore(eq(BigInteger.ZERO), eq(SYSTEM_ADDRESS), eq("burn"));
        updatePeriods();

        @SuppressWarnings("unchecked")
        Map<String, Object> progressReportDetails = (Map<String, Object>) cpsScore.call("getProgressReportsByHash", "Report 1");
        System.out.println(progressReportDetails);

        // TODO: update progress report also -> assertEquals(APPROVED, progressReportDetails.get(STATUS));
        Map<String, Object> milestoneReport = (Map<String, Object>) cpsScore.call("getMilestonesReport", "Proposal 1", 1);
        assertEquals(MILESTONE_REPORT_APPROVED, milestoneReport.get(STATUS));
//        assertEquals(APPROVED, progressReportDetails.get(BUDGET_ADJUSTMENT_STATUS));
    }

    @Test
    void submitProgressReportWithoutBudgetAdjustment() {
        ProgressReportAttributes progressReport = new ProgressReportAttributes();
        progressReport.ipfs_hash = "Proposal 1";
        progressReport.report_hash = "Report 1";
        progressReport.ipfs_link = "Link";
        progressReport.progress_report_title = "Progress Report Title";
        progressReport.budget_adjustment = false;
        progressReport.additional_budget = BigInteger.valueOf(0);
        progressReport.additional_month = 0;
//        progressReport.milestoneCompleted = new int[]{1};
//        progressReport.percentage_completed = 50;


        CPSCoreInterface.MilestoneSubmission milestoneSubmission1 = new CPSCoreInterface.MilestoneSubmission();
        milestoneSubmission1.id = 1;
        milestoneSubmission1.status = true;

//        CPSCoreInterface.MilestoneSubmission milestoneSubmission2 = new CPSCoreInterface.MilestoneSubmission();
//        milestoneSubmission2.id = 2;
//        milestoneSubmission2.status = true;
        CPSCoreInterface.MilestoneSubmission[] milestoneSubmission = new CPSCoreInterface.MilestoneSubmission[]{
                milestoneSubmission1};

        updatePeriodAfterProposalVoting();

        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), any());
        cpsScore.invoke(owner, "toggleBudgetAdjustmentFeature");
        cpsScore.invoke(owner, "submitProgressReport", progressReport, milestoneSubmission);
    }

    @Test
    void voteProgressReportAfterSubmittingProposalWithoutBudgetAdjustment() {
        submitProgressReportWithoutBudgetAdjustment();
        updateNextBlock();
        cpsScore.invoke(owner, "updatePeriod");
        getPeriodStatusMethod();
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), eq(8));


        CPSCoreInterface.MilestoneVoteAttributes milestoneVoteAttributes = new CPSCoreInterface.MilestoneVoteAttributes();
        milestoneVoteAttributes.vote = APPROVE;
        milestoneVoteAttributes.id = 1;


        CPSCoreInterface.MilestoneVoteAttributes[] milestoneVoteAttributesList = new CPSCoreInterface.MilestoneVoteAttributes[]{
                milestoneVoteAttributes};

        cpsScore.invoke(owner, "voteProgressReport", "Report 1", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount.getAddress());
        cpsScore.invoke(testingAccount, "voteProgressReport", "Report 1", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount1.getAddress());
        cpsScore.invoke(testingAccount1, "voteProgressReport", "Report 1", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount2.getAddress());
        cpsScore.invoke(testingAccount2, "voteProgressReport", "Report 1", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount3.getAddress());
        cpsScore.invoke(testingAccount3, "voteProgressReport", "Report 1", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount4.getAddress());
        cpsScore.invoke(testingAccount4, "voteProgressReport", "Report 1", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount5.getAddress());
        cpsScore.invoke(testingAccount5, "voteProgressReport", "Report 1", "reason", milestoneVoteAttributesList, APPROVE, false);

        @SuppressWarnings("unchecked")
        Map<String, Object> progressReportDetails = (Map<String, Object>) cpsScore.call("getProgressReportVoteDetails", "Report 1");

        assertEquals(10, progressReportDetails.get(TOTAL_VOTERS));

        Map<String, Object> milestonesReport = (Map<String, Object>) cpsScore.call("getMilestonesReport", "Proposal 1", 1);
        assertEquals(7, milestonesReport.get(APPROVE_VOTERS));

        contextMock.when(caller()).thenReturn(owner.getAddress());
        updateNextBlock();

        Map<String, BigInteger> totalFunds = Map.of(
                Constants.ICX, BigInteger.valueOf(1000).multiply(MULTIPLIER),
                bnUSD, BigInteger.valueOf(1000).multiply(MULTIPLIER)
        );

        doReturn(totalFunds).when(scoreSpy).callScore(eq(Map.class), eq(cpfTreasury), eq("get_total_funds"));
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("transfer_proposal_fund_to_cps_treasury"),
                eq("Proposal 1"), eq(2), eq(2), eq(testingAccount.getAddress()), eq(owner.getAddress()),
                eq(bnUSD), eq(BigInteger.valueOf(100).multiply(MULTIPLIER)));

        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("resetSwapState"));
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("updateProposalFund"), eq("Proposal 1"), eq(bnUSD), eq(BigInteger.valueOf(10).multiply(MULTIPLIER)), eq(1));
        doNothing().when(scoreSpy).callScore(eq(cpsTreasury), eq("sendInstallmentToContributor"), eq("Proposal 1"), any(BigInteger.class));
        doNothing().when(scoreSpy).callScore(eq(cpsTreasury), eq("sendRewardToSponsor"), eq("Proposal 1"), eq(1));
        doNothing().when(scoreSpy).callScore(eq(BigInteger.ZERO), eq(SYSTEM_ADDRESS), eq("burn"));
        updatePeriods();

        progressReportDetails = (Map<String, Object>) cpsScore.call("getProgressReportsByHash", "Report 1");
        System.out.println(progressReportDetails);

        ProgressReportAttributes progressReport = new ProgressReportAttributes();
        progressReport.ipfs_hash = "Proposal 1";
        progressReport.report_hash = "Report 2";
        progressReport.ipfs_link = "Link";
        progressReport.progress_report_title = "Progress Report Title";
        progressReport.budget_adjustment = false;
        progressReport.additional_budget = BigInteger.valueOf(0);
        progressReport.additional_month = 0;
//        progressReport.milestoneCompleted = new int[]{2};
//        progressReport.percentage_completed = 50;


        CPSCoreInterface.MilestoneSubmission milestoneSubmission1 = new CPSCoreInterface.MilestoneSubmission();
        milestoneSubmission1.id = 2;
        milestoneSubmission1.status = true;

//        CPSCoreInterface.MilestoneSubmission milestoneSubmission2 = new CPSCoreInterface.MilestoneSubmission();
//        milestoneSubmission2.id = 2;
//        milestoneSubmission2.status = true;
        CPSCoreInterface.MilestoneSubmission[] milestoneSubmission = new CPSCoreInterface.MilestoneSubmission[]{
                milestoneSubmission1};


        cpsScore.invoke(owner, "submitProgressReport", progressReport, milestoneSubmission);

        updateNextBlock();
        cpsScore.invoke(owner, "updatePeriod");
        getPeriodStatusMethod();
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), eq(8));

        milestoneVoteAttributes = new CPSCoreInterface.MilestoneVoteAttributes();
        milestoneVoteAttributes.vote = APPROVE;
        milestoneVoteAttributes.id = 2;

        milestoneVoteAttributesList = new CPSCoreInterface.MilestoneVoteAttributes[]{
                milestoneVoteAttributes};

        cpsScore.invoke(owner, "voteProgressReport", "Report 2", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount.getAddress());
        cpsScore.invoke(testingAccount, "voteProgressReport", "Report 2", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount1.getAddress());
        cpsScore.invoke(testingAccount1, "voteProgressReport", "Report 2", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount2.getAddress());
        cpsScore.invoke(testingAccount2, "voteProgressReport", "Report 2", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount3.getAddress());
        cpsScore.invoke(testingAccount3, "voteProgressReport", "Report 2", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount4.getAddress());
        cpsScore.invoke(testingAccount4, "voteProgressReport", "Report 2", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount5.getAddress());
        cpsScore.invoke(testingAccount5, "voteProgressReport", "Report 2", "reason", milestoneVoteAttributesList, APPROVE, false);

        progressReportDetails = (Map<String, Object>) cpsScore.call("getProgressReportVoteDetails", "Report 2");

        assertEquals(7, progressReportDetails.get(TOTAL_VOTERS));

        milestonesReport = (Map<String, Object>) cpsScore.call("getMilestonesReport", "Proposal 1", 2);
        assertEquals(7, milestonesReport.get(APPROVE_VOTERS));

        contextMock.when(caller()).thenReturn(owner.getAddress());
        updateNextBlock();

        totalFunds = Map.of(
                Constants.ICX, BigInteger.valueOf(1000).multiply(MULTIPLIER),
                bnUSD, BigInteger.valueOf(1000).multiply(MULTIPLIER)
        );

        doReturn(totalFunds).when(scoreSpy).callScore(eq(Map.class), eq(cpfTreasury), eq("get_total_funds"));
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("transfer_proposal_fund_to_cps_treasury"),
                eq("Proposal 1"), eq(2), eq(2), eq(testingAccount.getAddress()), eq(owner.getAddress()),
                eq(bnUSD), eq(BigInteger.valueOf(100).multiply(MULTIPLIER)));

        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("resetSwapState"));
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("updateProposalFund"), eq("Proposal 1"), eq(bnUSD), eq(BigInteger.valueOf(10).multiply(MULTIPLIER)), eq(1));
        doNothing().when(scoreSpy).callScore(eq(cpsTreasury), eq("sendInstallmentToContributor"), eq("Proposal 1"), eq(1));
        doNothing().when(scoreSpy).callScore(eq(cpsTreasury), eq("sendRewardToSponsor"), eq("Proposal 1"), eq(1));
        doNothing().when(scoreSpy).callScore(eq(BigInteger.ZERO), eq(SYSTEM_ADDRESS), eq("burn"));
        updatePeriods();

        Map<String, Object> proposalDetails = (Map<String, Object>) cpsScore.call("getProposalDetailsByHash", "Proposal 1");
        proposalDetails.remove("sponsor_vote_reason");
        assertEquals(COMPLETED, proposalDetails.get("status"));

        Map<String, Integer> sponsorsRecord = (Map<String, Integer>) cpsScore.call("getSponsorsRecord");
        assertEquals(1, sponsorsRecord.get(testingAccount.getAddress().toString()));

        Map<String, Map<String, Map<String, BigInteger>>> projectAmounts = (Map<String, Map<String, Map<String, BigInteger>>>) cpsScore.call("getProjectAmounts");
        System.out.println("Project Amount: " + projectAmounts);
        Map<String, Object> amount = Map.of(AMOUNT, Map.of(Constants.ICX, BigInteger.ZERO,
                        bnUSD, BigInteger.valueOf(100).multiply(MULTIPLIER)),
                "count", 1);
        assertEquals(amount, (projectAmounts.get(COMPLETED)));

        Map<String, Object> progressReportsByProposal = (Map<String, Object>) cpsScore.call("getProgressReportsByProposal", "Proposal 1");
        assertEquals(2, progressReportsByProposal.get(COUNT));

        Map<String, BigInteger> claimableSponsorBond = (Map<String, BigInteger>) cpsScore.call("checkClaimableSponsorBond", testingAccount.getAddress());
        assertEquals(BigInteger.valueOf(12).multiply(MULTIPLIER), claimableSponsorBond.get(bnUSD));

        Map<String, Object> proposalsHistory = (Map<String, Object>) cpsScore.call("getProposalsHistory", 0);
        List<Map<String, Object>> proposalHistory = (List<Map<String, Object>>) proposalsHistory.get(DATA);
        assertEquals(proposalDetails.size(), proposalHistory.get(0).size());
    }

    @Test
    void claimSponsorBond() {
        voteProgressReportAfterSubmittingProposalWithoutBudgetAdjustment();
        doNothing().when(scoreSpy).callScore(eq(bnUSDScore), eq("transfer"), eq(testingAccount.getAddress()), eq(BigInteger.valueOf(12).multiply(MULTIPLIER)));
        contextMock.when(caller()).thenReturn(testingAccount.getAddress());
        cpsScore.invoke(testingAccount, "claimSponsorBond");
        Map<String, BigInteger> claimableSponsorBond = (Map<String, BigInteger>) cpsScore.call("checkClaimableSponsorBond", testingAccount.getAddress());
        assertEquals(BigInteger.valueOf(0), claimableSponsorBond.get(bnUSD));

    }

    @Test
    void sortPriorityProposals() {
        voteMultipleProposals();
        List<String> proposal = List.of("Proposal 0", "Proposal 1", "Proposal 2", "Proposal 3", "Proposal 4", "Proposal 5",
                "Proposal 6", "Proposal 7", "Proposal 8", "Proposal 9");
//        @SuppressWarnings("unchecked")
        List<String> proposalList = (List<String>) cpsScore.call("sortPriorityProposals");
        assertEquals(proposal, proposalList);

        @SuppressWarnings("unchecked")
        Map<String, Integer> priorityVoteResult = (Map<String, Integer>) cpsScore.call("getPriorityVoteResult");
        System.out.println(priorityVoteResult);
    }


    @Test
    void setCpsTreasury() {
        contextMock.when(caller()).thenReturn(owner.getAddress());
        addAdminMethod();
        cpsScore.invoke(owner, "setCpsTreasuryScore", cpsTreasury);
        assertEquals(cpsTreasury, cpsScore.call("getCpsTreasuryScore"));
    }

    @Test
    void setCpfTreasury() {
        addAdminMethod();
        contextMock.when(caller()).thenReturn(owner.getAddress());
        cpsScore.invoke(owner, "setCpfTreasuryScore", cpfTreasury);
        assertEquals(cpfTreasury, cpsScore.call("getCpfTreasuryScore"));
    }

    @Test
    void setbnUSDScore() {
        addAdminMethod();
        contextMock.when(caller()).thenReturn(owner.getAddress());
        cpsScore.invoke(owner, "setBnusdScore", bnUSDScore);
        assertEquals(bnUSDScore, cpsScore.call("getBnusdScore"));
    }

    @Test
    void toggleMaintenanceMode() {
        addAdminMethod();
        cpsScore.invoke(owner, "toggleMaintenance");
        assertEquals(Boolean.FALSE, cpsScore.call("getMaintenanceMode"));
    }

    @Test
    void payPrepPenalty() {
        submitAndSponsorVote();
        BigInteger[] penaltyAmount = new BigInteger[3];
        penaltyAmount[0] = BigInteger.valueOf(5);
        penaltyAmount[1] = BigInteger.valueOf(10);
        penaltyAmount[2] = BigInteger.valueOf(15);
        contextMock.when(caller()).thenReturn(owner.getAddress());
        cpsScore.invoke(owner, "setPrepPenaltyAmount", (Object) penaltyAmount);
        updateNextBlock();
        doReturn(BigInteger.TEN).when(scoreSpy).getApplicationPeriod();
        cpsScore.invoke(owner, "updatePeriod");
        getPeriodStatusMethod();
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), any());
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

        contextMock.when(caller()).thenReturn(testingAccount6.getAddress());
        cpsScore.invoke(testingAccount6, "voteProposal", "Proposal 1", APPROVE, "reason", false);
        cpsScore.invoke(owner, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(testingAccount7.getAddress());
        cpsScore.invoke(testingAccount7, "voteProposal", "Proposal 1", APPROVE, "reason", false);
        cpsScore.invoke(owner, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(testingAccount8.getAddress());
        cpsScore.invoke(testingAccount8, "voteProposal", "Proposal 1", APPROVE, "reason", false);
        cpsScore.invoke(owner, "votePriority", (Object) proposal);

        contextMock.when(caller()).thenReturn(owner.getAddress());
        updateNextBlock();

        Map<String, BigInteger> totalFunds = Map.of(
                Constants.ICX, BigInteger.valueOf(1000).multiply(MULTIPLIER),
                bnUSD, BigInteger.valueOf(1000).multiply(MULTIPLIER)
        );

        doReturn(totalFunds).when(scoreSpy).callScore(eq(Map.class), eq(cpfTreasury), eq("getTotalFunds"));
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("transferProposalFundToCpsTreasury"),
                eq("Proposal 1"), eq(2), eq(testingAccount.getAddress()), eq(owner.getAddress()),
                eq(bnUSD), eq(BigInteger.valueOf(100).multiply(MULTIPLIER)));

        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("resetSwapState"));
        doReturn(BigInteger.valueOf(15)).when(scoreSpy).getVotingPeriod();

        contextMock.when(() -> Context.transfer(any(), any())).thenAnswer((Answer<Void>) invocation -> null);
        doNothing().when(scoreSpy).callScore(eq(BigInteger.ZERO), eq(SYSTEM_ADDRESS), eq("burn"));

        updatePeriods();

        @SuppressWarnings("unchecked")
        List<Address> denyList = (List<Address>) cpsScore.call("getDenylist");
        assertEquals(List.of(testingAccount5.getAddress()), denyList);

        @SuppressWarnings("unchecked")
        Map<String, BigInteger> loginPrep = (Map<String, BigInteger>) cpsScore.call("loginPrep", testingAccount5.getAddress());
        System.out.println(loginPrep);

        JsonObject payPenalty = new JsonObject();
        payPenalty.add("method", "payPrepPenalty");
        JsonObject params = new JsonObject();
        payPenalty.add("params", params);

        JsonObject burnTokens = new JsonObject();
        burnTokens.add("method", "burnAmount");
        doNothing().when(scoreSpy).callScore(eq(bnUSDScore), eq("transfer"), eq(cpfTreasury), eq(new BigInteger("5000000000000000000")), eq(burnTokens.toString().getBytes()));
        contextMock.when(caller()).thenReturn(bnUSDScore);
        cpsScore.invoke(owner, "tokenFallback", testingAccount5.getAddress(), new BigInteger("5000000000000000000"), payPenalty.toString().getBytes());
    }

    @Test
    void disqualifyProposal() {
        updatePeriodAfterProposalVoting();
        updateNextBlock();
        doNothing().when(scoreSpy).callScore(eq(cpsTreasury), eq("disqualifyProject"), eq("Proposal 1"));

        JsonObject disqualifyProject = new JsonObject();
        disqualifyProject.add("method", "returnFundAmount");
        JsonObject params = new JsonObject();
        params.add(SPONSOR_ADDRESS, testingAccount.getAddress().toString());
        disqualifyProject.add("params", params);

        doNothing().when(scoreSpy).callScore(eq(bnUSDScore), eq("transfer"), eq(cpfTreasury), eq(BigInteger.valueOf(12).multiply(MULTIPLIER)), eq(disqualifyProject.toString().getBytes()));
        cpsScore.invoke(owner, "updatePeriod");
        Map<String, Object> proposalDetails = getProposalDetailsByHash("Proposal 1");
        assertEquals(PAUSED, proposalDetails.get(STATUS));

        Map<String, Object> sponosrsRequest = (Map<String, Object>) cpsScore.call("getSponsorsRequests", APPROVED, testingAccount.getAddress(), 0, 10);
        System.out.println("Sponsors request::" + sponosrsRequest);

        Map<String, Integer> sponsorsRecord = (Map<String, Integer>) cpsScore.call("getSponsorsRecord");
        assertEquals(1, sponsorsRecord.get(testingAccount.getAddress().toString()));

        Map<String, Map<String, Map<String, BigInteger>>> projectAmounts = (Map<String, Map<String, Map<String, BigInteger>>>) cpsScore.call("getProjectAmounts");
        Map<String, Object> amount = Map.of(AMOUNT, Map.of(Constants.ICX, BigInteger.ZERO,
                        bnUSD, BigInteger.valueOf(100).multiply(MULTIPLIER)),
                "count", 1);
        assertEquals(amount, (projectAmounts.get(PAUSED)));

        updateNextBlock();
        cpsScore.invoke(owner, "updatePeriod");
        proposalDetails = getProposalDetailsByHash("Proposal 1");
        proposalDetails.remove("sponsor_vote_reason");
        assertEquals(DISQUALIFIED, proposalDetails.get(STATUS));
        assertEquals(BOND_CANCELLED, proposalDetails.get(SPONSOR_DEPOSIT_STATUS));

        projectAmounts = (Map<String, Map<String, Map<String, BigInteger>>>) cpsScore.call("getProjectAmounts");
        amount = Map.of(AMOUNT, Map.of(Constants.ICX, BigInteger.ZERO,
                        bnUSD, BigInteger.valueOf(100).multiply(MULTIPLIER)),
                "count", 1);
        assertEquals(amount, (projectAmounts.get(DISQUALIFIED)));

        Map<String, Object> proposalsHistory = (Map<String, Object>) cpsScore.call("getProposalsHistory", 0);
        List<Map<String, Object>> proposalHistory = (List<Map<String, Object>>) proposalsHistory.get(DATA);
        assertEquals(proposalDetails.size(), proposalHistory.get(0).size());
    }

    @Test
    void getContributors() {
        submitAndSponsorVote();
        @SuppressWarnings("unchecked")
        List<Address> contributors = (List<Address>) cpsScore.call("getContributors");
        assertEquals(List.of(owner.getAddress()), contributors);
    }

    @Test
    void rejectSponsorVote() {
        submitProposalMethod();
        contextMock.when(caller()).thenReturn(bnUSDScore);
        contextMock.when(() -> Context.transfer(any(), any())).thenAnswer((Answer<Void>) invocation -> null);
        JsonObject sponsorVoteParams = new JsonObject();
        sponsorVoteParams.add("method", "sponsorVote");
        JsonObject params = new JsonObject();
        params.add(IPFS_HASH, "Proposal 1");
        params.add(VOTE, REJECT);
        params.add(VOTE_REASON, "reason");
        sponsorVoteParams.add("params", params);

        cpsScore.invoke(testingAccount, "tokenFallback", testingAccount.getAddress(), BigInteger.valueOf(0), sponsorVoteParams.toString().getBytes());
    }

    @Test
    void disqualifyProjectByRejectingProgressReport() {
        submitProgressReportWithoutBudgetAdjustment();
        updateNextBlock();
        cpsScore.invoke(owner, "updatePeriod");
        getPeriodStatusMethod();
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), eq(8));

        JsonObject disqualifyProject = new JsonObject();
        disqualifyProject.add("method", "returnFundAmount");
        JsonObject params = new JsonObject();
        params.add(SPONSOR_ADDRESS, testingAccount.getAddress().toString());
        disqualifyProject.add("params", params);

        doNothing().when(scoreSpy).callScore(eq(bnUSDScore), eq("transfer"), eq(cpfTreasury), eq(BigInteger.valueOf(12).multiply(MULTIPLIER)), eq(disqualifyProject.toString().getBytes()));

        CPSCoreInterface.MilestoneVoteAttributes milestoneVoteAttributes = new CPSCoreInterface.MilestoneVoteAttributes();
        milestoneVoteAttributes.id = 1;
        milestoneVoteAttributes.vote = REJECT;

        CPSCoreInterface.MilestoneVoteAttributes[] milestoneVoteAttributesList = new CPSCoreInterface.MilestoneVoteAttributes[]{
                milestoneVoteAttributes};
        contextMock.when(caller()).thenReturn(owner.getAddress());
        cpsScore.invoke(owner, "voteProgressReport", "Report 1", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount.getAddress());
        cpsScore.invoke(testingAccount, "voteProgressReport", "Report 1", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount1.getAddress());
        cpsScore.invoke(testingAccount1, "voteProgressReport", "Report 1", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount2.getAddress());
        cpsScore.invoke(testingAccount2, "voteProgressReport", "Report 1", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount3.getAddress());
        cpsScore.invoke(testingAccount3, "voteProgressReport", "Report 1", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount4.getAddress());
        cpsScore.invoke(testingAccount4, "voteProgressReport", "Report 1", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount5.getAddress());
        cpsScore.invoke(testingAccount5, "voteProgressReport", "Report 1", "reason", milestoneVoteAttributesList, APPROVE, false);

        @SuppressWarnings("unchecked")
        Map<String, Object> progressReportDetails = (Map<String, Object>) cpsScore.call("getProgressReportVoteDetails", "Report 1");

        assertEquals(10, progressReportDetails.get(TOTAL_VOTERS));

        Map<String, Object> milestoneReportDetailes = (Map<String, Object>) cpsScore.call("getMilestonesReport", "Proposal 1", 1);

        assertEquals(7, milestoneReportDetailes.get(REJECT_VOTERS));

        contextMock.when(caller()).thenReturn(owner.getAddress());
        updateNextBlock();

        Map<String, BigInteger> totalFunds = Map.of(
                Constants.ICX, BigInteger.valueOf(1000).multiply(MULTIPLIER),
                bnUSD, BigInteger.valueOf(1000).multiply(MULTIPLIER)
        );

        doReturn(totalFunds).when(scoreSpy).callScore(eq(Map.class), eq(cpfTreasury), eq("getTotalFunds"));
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("transferProposalFundToCpsTreasury"),
                eq("Proposal 1"), eq(2), eq(testingAccount.getAddress()), eq(owner.getAddress()),
                eq(bnUSD), eq(BigInteger.valueOf(100).multiply(MULTIPLIER)));

        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("resetSwapState"));
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("updateProposalFund"), eq("Proposal 1"), eq(bnUSD), eq(BigInteger.valueOf(10).multiply(MULTIPLIER)), eq(1));
        doNothing().when(scoreSpy).callScore(eq(cpsTreasury), eq("sendInstallmentToContributor"), eq("Proposal 1"));
        doNothing().when(scoreSpy).callScore(eq(cpsTreasury), eq("sendRewardToSponsor"), eq("Proposal 1"));
        doNothing().when(scoreSpy).callScore(eq(BigInteger.ZERO), eq(SYSTEM_ADDRESS), eq("burn"));
        updatePeriods();

        progressReportDetails = (Map<String, Object>) cpsScore.call("getProgressReportsByHash", "Report 1");
        System.out.println(progressReportDetails);

        ProgressReportAttributes progressReport = new ProgressReportAttributes();
        progressReport.ipfs_hash = "Proposal 1";
        progressReport.report_hash = "Report 2";
        progressReport.ipfs_link = "Link";
        progressReport.progress_report_title = "Progress Report Title";
        progressReport.budget_adjustment = false;
        progressReport.additional_budget = BigInteger.valueOf(0);
        progressReport.additional_month = 0;
//        progressReport.milestoneCompleted = new int[]{2};
//        progressReport.percentage_completed = 50;


        CPSCoreInterface.MilestoneSubmission milestoneSubmission1 = new CPSCoreInterface.MilestoneSubmission();
        milestoneSubmission1.id = 2;
        milestoneSubmission1.status = true;

//        CPSCoreInterface.MilestoneSubmission milestoneSubmission2 = new CPSCoreInterface.MilestoneSubmission();
//        milestoneSubmission2.id = 2;
//        milestoneSubmission2.status = true;
        CPSCoreInterface.MilestoneSubmission[] milestoneSubmission = new CPSCoreInterface.MilestoneSubmission[]{
                milestoneSubmission1};


        cpsScore.invoke(owner, "submitProgressReport", progressReport, milestoneSubmission);

        milestoneVoteAttributes = new CPSCoreInterface.MilestoneVoteAttributes();
        milestoneVoteAttributes.id = 2;
        milestoneVoteAttributes.vote = REJECT;

        milestoneVoteAttributesList = new CPSCoreInterface.MilestoneVoteAttributes[]{
                milestoneVoteAttributes};

        updateNextBlock();
        cpsScore.invoke(owner, "updatePeriod");
        getPeriodStatusMethod();
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), eq(8));
        doNothing().when(scoreSpy).callScore(eq(cpsTreasury), eq("disqualifyProject"), eq("Proposal 1"));
        contextMock.when(caller()).thenReturn(owner.getAddress());
        cpsScore.invoke(owner, "voteProgressReport", "Report 2", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount.getAddress());
        cpsScore.invoke(testingAccount, "voteProgressReport", "Report 2", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount1.getAddress());
        cpsScore.invoke(testingAccount1, "voteProgressReport", "Report 2", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount2.getAddress());
        cpsScore.invoke(testingAccount2, "voteProgressReport", "Report 2", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount3.getAddress());
        cpsScore.invoke(testingAccount3, "voteProgressReport", "Report 2", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount4.getAddress());
        cpsScore.invoke(testingAccount4, "voteProgressReport", "Report 2", "reason", milestoneVoteAttributesList, APPROVE, false);
        contextMock.when(caller()).thenReturn(testingAccount5.getAddress());
        cpsScore.invoke(testingAccount5, "voteProgressReport", "Report 2", "reason", milestoneVoteAttributesList, APPROVE, false);

        progressReportDetails = (Map<String, Object>) cpsScore.call("getProgressReportVoteDetails", "Report 1");

        assertEquals(10, progressReportDetails.get(TOTAL_VOTERS));

        milestoneReportDetailes = (Map<String, Object>) cpsScore.call("getMilestonesReport", "Proposal 1", 2);

        assertEquals(7, milestoneReportDetailes.get(REJECT_VOTERS));

        contextMock.when(caller()).thenReturn(owner.getAddress());
        updateNextBlock();

        totalFunds = Map.of(
                Constants.ICX, BigInteger.valueOf(1000).multiply(MULTIPLIER),
                bnUSD, BigInteger.valueOf(1000).multiply(MULTIPLIER)
        );

        doReturn(totalFunds).when(scoreSpy).callScore(eq(Map.class), eq(cpfTreasury), eq("getTotalFunds"));
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("transferProposalFundToCpsTreasury"),
                eq("Proposal 1"), eq(2), eq(testingAccount.getAddress()), eq(owner.getAddress()),
                eq(bnUSD), eq(BigInteger.valueOf(100).multiply(MULTIPLIER)));

        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("resetSwapState"));
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("updateProposalFund"), eq("Proposal 1"), eq(bnUSD), eq(BigInteger.valueOf(10).multiply(MULTIPLIER)), eq(1));
        doNothing().when(scoreSpy).callScore(eq(cpsTreasury), eq("sendInstallmentToContributor"), eq("Proposal 1"), any(BigInteger.class));
        doNothing().when(scoreSpy).callScore(eq(cpsTreasury), eq("sendRewardToSponsor"), eq("Proposal 1"), eq(1));
        doNothing().when(scoreSpy).callScore(eq(BigInteger.ZERO), eq(SYSTEM_ADDRESS), eq("burn"));
        doNothing().when(scoreSpy).callScore(eq(cpsTreasury), eq("disqualifyProject"), eq("Proposal 1"));


        doNothing().when(scoreSpy).callScore(eq(bnUSDScore), eq("transfer"), eq(cpfTreasury), eq(BigInteger.valueOf(10).multiply(MULTIPLIER)), eq(disqualifyProject.toString().getBytes()));


        updatePeriods();

        Map<String, Object> proposalDetails = (Map<String, Object>) cpsScore.call("getProposalDetailsByHash", "Proposal 1");
        assertEquals(DISQUALIFIED, proposalDetails.get("status"));

        Map<String, Map<String, Map<String, BigInteger>>> projectAmounts = (Map<String, Map<String, Map<String, BigInteger>>>) cpsScore.call("getProjectAmounts");
        System.out.println("Project Amount: " + projectAmounts);
        Map<String, Object> amount = Map.of(AMOUNT, Map.of(Constants.ICX, BigInteger.ZERO,
                        bnUSD, BigInteger.valueOf(100).multiply(MULTIPLIER)),
                "count", 1);
        assertEquals(amount, (projectAmounts.get(DISQUALIFIED)));

        Map<String, Object> voteResult = (Map<String, Object>) cpsScore.call("getProgressReportResult", "Report 1");
        System.out.println("progress report vote Result of Report 1: " + voteResult);

        voteResult = (Map<String, Object>) cpsScore.call("getProgressReportResult", "Report 2");
        System.out.println("progress report vote Result of Report 2: " + voteResult);
    }

    @Test
    void fallback() {
        Executable fallback = () -> cpsScore.invoke(owner, "fallback");
        expectErrorMessage(fallback, "Reverted(0): " + TAG + ": ICX can only be sent while submitting a proposal or paying the penalty.");
    }

    @Test
    void getPeriodCount() {
        assertEquals(0, cpsScore.call("getPeriodCount"));
    }

    @Test
    void sponsorDepositsLessThanBondPercentage() {
        submitProposalMethod();
        contextMock.when(caller()).thenReturn(bnUSDScore);

        doReturn(BigInteger.valueOf(15)).when(scoreSpy).getSponsorBondPercentage();
        JsonObject sponsorVoteParams = new JsonObject();
        sponsorVoteParams.add("method", "sponsorVote");
        JsonObject params = new JsonObject();
        params.add(IPFS_HASH, "Proposal 1");
        params.add(VOTE, ACCEPT);
        params.add(VOTE_REASON, "reason");
        sponsorVoteParams.add("params", params);

        Executable bondPercentageRevert = () -> cpsScore.invoke(testingAccount, "tokenFallback", testingAccount.getAddress(), BigInteger.valueOf(10).multiply(MULTIPLIER), sponsorVoteParams.toString().getBytes());
        expectErrorMessage(bondPercentageRevert, "Reverted(0): CPS Score: Deposit 15% of the total budget of the project.");
    }


    @DisplayName("submit_progress_report_with_not_completed_status")
    @Test
    void progressReportWithNotCompletedStatus() {
//        voteProposal();
//        cpsScore.invoke(owner, "toggleMaintenance");
        updatePeriodAfterProposalVoting();


        ProgressReportAttributes progressReport = new ProgressReportAttributes();
        progressReport.ipfs_hash = "Proposal 1";
        progressReport.report_hash = "Report 1";
        progressReport.ipfs_link = "Link";
        progressReport.progress_report_title = "Progress Report Title";
        progressReport.budget_adjustment = true;
        progressReport.additional_budget = BigInteger.valueOf(10);
        progressReport.additional_month = 1;

        CPSCoreInterface.MilestoneSubmission milestoneSubmission1 = new CPSCoreInterface.MilestoneSubmission();
        milestoneSubmission1.id = 1;
        milestoneSubmission1.status = true; // completed milestone

        CPSCoreInterface.MilestoneSubmission milestoneSubmission2 = new CPSCoreInterface.MilestoneSubmission();
        milestoneSubmission2.id = 2;
        milestoneSubmission2.status = false; // not completed milestone
        CPSCoreInterface.MilestoneSubmission[] milestoneSubmission = new CPSCoreInterface.MilestoneSubmission[]{
                milestoneSubmission1, milestoneSubmission2};


        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), any());
        cpsScore.invoke(owner, "toggleBudgetAdjustmentFeature");
        cpsScore.invoke(owner, "submitProgressReport", progressReport, milestoneSubmission);

        @SuppressWarnings("unchecked")
        Map<String, Object> progressReportDetails = (Map<String, Object>) cpsScore.call("getProgressReportsByHash", "Report 1");

        assertEquals(progressReport.ipfs_hash, progressReportDetails.get("ipfs_hash"));
        assertEquals(progressReport.report_hash, progressReportDetails.get("report_hash"));
        assertEquals(progressReport.ipfs_link, progressReportDetails.get("ipfs_link"));
        assertEquals(progressReport.progress_report_title, progressReportDetails.get("progress_report_title"));

        List<String> progressKeys = (List<String>) cpsScore.call("getProgressKeys");
        assertEquals(List.of("Report 1"), progressKeys);

        Map<String, Object> progressReports = (Map<String, Object>) cpsScore.call("getProgressReports", WAITING, 0, 10);
        System.out.println("Progerss reports: " + progressReports);
        assertEquals(1, progressReports.get(COUNT));

        Map<String, Object> progressReportsByProposal = (Map<String, Object>) cpsScore.call("getProgressReportsByProposal", "Proposal 1");
        assertEquals(1, progressReportsByProposal.get(COUNT));


        Map<String, Object> milestone1Report = (Map<String, Object>) cpsScore.call("getMilestonesReport", "Proposal 1", 1);
        assertEquals(1, milestone1Report.get(MILESTONE_ID));
        assertEquals(MILESTONE_REPORT_COMPLETED, milestone1Report.get(STATUS));

        Map<String, Object> milestone2Report = (Map<String, Object>) cpsScore.call("getMilestonesReport", "Proposal 1", 2);
        assertEquals(2, milestone2Report.get(MILESTONE_ID));
        assertEquals(MILESTONE_REPORT_NOT_COMPLETED, milestone2Report.get(STATUS));

    }

    @DisplayName("last progress report test")
    @Test
    void checkLastProgresssReportSubmission() {
        updatePeriodAfterProposalVoting();

        // getActiveProposals

        List<Map<String, Object>> activeProposals = (List<Map<String, Object>>) cpsScore.call("getActiveProposals", owner.getAddress());
        assertEquals(activeProposals.get(0).get("last_progress_report"), false);

        List<Integer> milestoneDeadline = new ArrayList<>();
        milestoneDeadline.add(1);
        assertEquals(activeProposals.get(0).get("milestoneDeadlines"), milestoneDeadline);


        // submit first progress report
        ProgressReportAttributes progressReport = new ProgressReportAttributes();
        progressReport.ipfs_hash = "Proposal 1";
        progressReport.report_hash = "Report 1";
        progressReport.ipfs_link = "Link";
        progressReport.progress_report_title = "Progress Report Title";
        progressReport.budget_adjustment = true;
        progressReport.additional_budget = BigInteger.ZERO;
        progressReport.additional_month = 0;


        CPSCoreInterface.MilestoneSubmission milestoneSubmission1 = new CPSCoreInterface.MilestoneSubmission();
        milestoneSubmission1.id = 1;
        milestoneSubmission1.status = true; // completed milestone

        CPSCoreInterface.MilestoneSubmission[] milestoneSubmission = new CPSCoreInterface.MilestoneSubmission[]{
                milestoneSubmission1};

        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), any());
        cpsScore.invoke(owner, "toggleBudgetAdjustmentFeature");
        cpsScore.invoke(owner, "submitProgressReport", progressReport, milestoneSubmission);

        // change period to voting
        updateNextBlock();
        cpsScore.invoke(owner, "updatePeriod");

        // vote on progress report 1
        CPSCoreInterface.MilestoneVoteAttributes milestoneVoteAttributes = new CPSCoreInterface.MilestoneVoteAttributes();
        milestoneVoteAttributes.id = 1;
        milestoneVoteAttributes.vote = APPROVE;

        CPSCoreInterface.MilestoneVoteAttributes[] milestoneVoteAttributesList = new CPSCoreInterface.MilestoneVoteAttributes[]{milestoneVoteAttributes};

        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), eq(8));
        cpsScore.invoke(owner, "voteProgressReport", "Report 1", "reason", milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount.getAddress());
        cpsScore.invoke(testingAccount, "voteProgressReport", "Report 1", "reason", (Object) milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount1.getAddress());
        cpsScore.invoke(testingAccount1, "voteProgressReport", "Report 1", "reason", milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount2.getAddress());
        cpsScore.invoke(testingAccount2, "voteProgressReport", "Report 1", "reason", (Object) milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount3.getAddress());
        cpsScore.invoke(testingAccount3, "voteProgressReport", "Report 1", "reason", (Object) milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount4.getAddress());
        cpsScore.invoke(testingAccount4, "voteProgressReport", "Report 1", "reason", (Object) milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount5.getAddress());
        cpsScore.invoke(testingAccount5, "voteProgressReport", "Report 1", "reason", (Object) milestoneVoteAttributesList, "_reject", false);

        // updatePeriods
        contextMock.when(caller()).thenReturn(owner.getAddress());
        doNothing().when(scoreSpy).callScore(eq(cpsTreasury), eq("sendInstallmentToContributor"), eq("Proposal 1"), any(BigInteger.class));
        doNothing().when(scoreSpy).callScore(eq(cpsTreasury), eq("sendRewardToSponsor"), eq("Proposal 1"), eq(1));
        doNothing().when(scoreSpy).callScore(eq(BigInteger.ZERO), eq(SYSTEM_ADDRESS), eq("burn"));

        updateNextBlock();
        updatePeriods();

        // getActiveProposals

        activeProposals = (List<Map<String, Object>>) cpsScore.call("getActiveProposals", owner.getAddress());
        assertEquals(activeProposals.get(0).get("last_progress_report"), true);

        milestoneDeadline = new ArrayList<>();
        milestoneDeadline.add(2);
        assertEquals(activeProposals.get(0).get("milestoneDeadlines"), milestoneDeadline);


        // submit last progress report

        progressReport = new ProgressReportAttributes();
        progressReport.ipfs_hash = "Proposal 1";
        progressReport.report_hash = "Report 2";
        progressReport.ipfs_link = "Link";
        progressReport.progress_report_title = "Progress Report Title 2";
        progressReport.budget_adjustment = false;
        progressReport.additional_budget = BigInteger.ZERO;
        progressReport.additional_month = 0;


        CPSCoreInterface.MilestoneSubmission milestoneSubmission2 = new CPSCoreInterface.MilestoneSubmission();
        milestoneSubmission2.id = 2;
        milestoneSubmission2.status = true; // completed milestone

        milestoneSubmission = new CPSCoreInterface.MilestoneSubmission[]{
                milestoneSubmission2};

        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), any());
        cpsScore.invoke(owner, "submitProgressReport", progressReport, milestoneSubmission);

    }

    @DisplayName("trying to submit progress report before deadline")
    @Test
    void submitProgressReportBeforeDeadline() {
        updatePeriodAfterProposalVoting();

        // getActiveProposals

        List<Map<String, Object>> activeProposals = (List<Map<String, Object>>) cpsScore.call("getActiveProposals", owner.getAddress());
        assertEquals(activeProposals.get(0).get("last_progress_report"), false);

        List<Integer> milestoneDeadline = new ArrayList<>();
        milestoneDeadline.add(1);
        assertEquals(activeProposals.get(0).get("milestoneDeadlines"), milestoneDeadline);


        // submit first progress report
        ProgressReportAttributes progressReport = new ProgressReportAttributes();
        progressReport.ipfs_hash = "Proposal 1";
        progressReport.report_hash = "Report 1";
        progressReport.ipfs_link = "Link";
        progressReport.progress_report_title = "Progress Report Title";
        progressReport.budget_adjustment = true;
        progressReport.additional_budget = BigInteger.ZERO;
        progressReport.additional_month = 0;


        CPSCoreInterface.MilestoneSubmission milestoneSubmission1 = new CPSCoreInterface.MilestoneSubmission();
        milestoneSubmission1.id = 1;
        milestoneSubmission1.status = true; // completed milestone

        CPSCoreInterface.MilestoneSubmission milestoneSubmission2 = new CPSCoreInterface.MilestoneSubmission();
        milestoneSubmission2.id = 2;
        milestoneSubmission2.status = true; // completed milestone


        CPSCoreInterface.MilestoneSubmission[] milestoneSubmission = new CPSCoreInterface.MilestoneSubmission[]{
                milestoneSubmission1, milestoneSubmission2};

        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), any());
        cpsScore.invoke(owner, "toggleBudgetAdjustmentFeature");
        cpsScore.invoke(owner, "submitProgressReport", progressReport, milestoneSubmission);

        // change period to voting
        updateNextBlock();
        cpsScore.invoke(owner, "updatePeriod");

        // vote on progress report 1
        CPSCoreInterface.MilestoneVoteAttributes milestoneVoteAttributes = new CPSCoreInterface.MilestoneVoteAttributes();
        milestoneVoteAttributes.id = 1;
        milestoneVoteAttributes.vote = APPROVE;

        CPSCoreInterface.MilestoneVoteAttributes milestoneVoteAttributes2 = new CPSCoreInterface.MilestoneVoteAttributes();
        milestoneVoteAttributes2.id = 2;
        milestoneVoteAttributes2.vote = APPROVE;

        CPSCoreInterface.MilestoneVoteAttributes[] milestoneVoteAttributesList = new CPSCoreInterface.MilestoneVoteAttributes[]{milestoneVoteAttributes, milestoneVoteAttributes2};

        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), eq(8));
        cpsScore.invoke(owner, "voteProgressReport", "Report 1", "reason", milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount.getAddress());
        cpsScore.invoke(testingAccount, "voteProgressReport", "Report 1", "reason", (Object) milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount1.getAddress());
        cpsScore.invoke(testingAccount1, "voteProgressReport", "Report 1", "reason", milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount2.getAddress());
        cpsScore.invoke(testingAccount2, "voteProgressReport", "Report 1", "reason", (Object) milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount3.getAddress());
        cpsScore.invoke(testingAccount3, "voteProgressReport", "Report 1", "reason", (Object) milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount4.getAddress());
        cpsScore.invoke(testingAccount4, "voteProgressReport", "Report 1", "reason", (Object) milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount5.getAddress());
        cpsScore.invoke(testingAccount5, "voteProgressReport", "Report 1", "reason", (Object) milestoneVoteAttributesList, "_reject", false);

        // updatePeriods
        contextMock.when(caller()).thenReturn(owner.getAddress());
        doNothing().when(scoreSpy).callScore(eq(cpsTreasury), eq("sendInstallmentToContributor"), eq("Proposal 1"), any(BigInteger.class));
        doNothing().when(scoreSpy).callScore(eq(cpsTreasury), eq("sendRewardToSponsor"), eq("Proposal 1"), eq(2));
        doNothing().when(scoreSpy).callScore(eq(BigInteger.ZERO), eq(SYSTEM_ADDRESS), eq("burn"));

        updateNextBlock();
        updatePeriods();

    }


    @Test
    void setSwapCount() {
        addAdminMethod();
        cpsScore.invoke(owner, "setSwapCount", 10);
        assertEquals(10, cpsScore.call("getSwapCount"));
    }

    @Test
    void bondPercentageExceptions() {
        Executable percentageNotAdmin = () -> cpsScore.invoke(testingAccount, "setSponsorBondPercentage", BigInteger.valueOf(15));
        expectErrorMessage(percentageNotAdmin, "Reverted(0): CPS Score: Only CPF treasury can call this method");

        doReturn(cpfTreasuryScore.getAddress()).when(scoreSpy).getCpfTreasuryScore();
        Executable percentageLessTwelve = () -> cpsScore.invoke(cpfTreasuryScore, "setSponsorBondPercentage", BigInteger.valueOf(11));
        expectErrorMessage(percentageLessTwelve, "Reverted(0): CPS Score: Cannot set bond percentage less than 12%");
    }

    @Test
    void setApplicationPeriodExceptions() {
        Executable setPeriodNotAdmin = () -> cpsScore.invoke(testingAccount, "setPeriod", BigInteger.valueOf(10));
        expectErrorMessage(setPeriodNotAdmin, "Reverted(0): CPS Score: Only CPF treasury can call this method");


        doReturn(cpfTreasuryScore.getAddress()).when(scoreSpy).getCpfTreasuryScore();
        Executable periodis15 = () -> cpsScore.invoke(cpfTreasuryScore, "setPeriod", BigInteger.valueOf(28));
        expectErrorMessage(periodis15, "Reverted(0): CPS Score: Voting period must be more than or equal to 10 days");

    }

    @Test
    void setPeriod() {

        doReturn(cpfTreasuryScore.getAddress()).when(scoreSpy).getCpfTreasuryScore();
        cpsScore.invoke(cpfTreasuryScore, "setPeriod", BigInteger.valueOf(20));

        BigInteger applicationPeriod = (BigInteger) cpsScore.call("getApplicationPeriod");
        assertEquals(applicationPeriod, BigInteger.valueOf(20));

        BigInteger votingPeriod = (BigInteger) cpsScore.call("getVotingPeriod");
        assertEquals(votingPeriod, BigInteger.valueOf(10));
    }

    @Test
    void updateContributorAddress() {

        doReturn(1).when(scoreSpy).getPeriodCount();
        updatePeriodAfterProposalVoting();

        String ipfsHash = "Proposal 1";
        Map<String, Object> proposalDetails = getProposalDetailsByHash(ipfsHash);

        assertEquals(proposalDetails.get("contributor_address"), owner.getAddress());
        assertEquals(proposalDetails.get("sponsor_address"), testingAccount.getAddress());



        doNothing().when(scoreSpy).callScore(cpsTreasury, "updateContributorSponsorAddress", ipfsHash, testingAccount1.getAddress(), testingAccount2.getAddress());
        cpsScore.invoke(owner, "blockAddress", owner.getAddress());
        cpsScore.invoke(owner, "blockAddress", testingAccount.getAddress());

        /* changing contributor to testAccount1
         * changing sponsor to testAccount 2*/
        cpsScore.invoke(owner, "updateContributor", ipfsHash, testingAccount1.getAddress(), testingAccount2.getAddress());

        proposalDetails = getProposalDetailsByHash(ipfsHash);

        assertEquals(proposalDetails.get("contributor_address"), testingAccount1.getAddress());
        assertEquals(proposalDetails.get("sponsor_address"), testingAccount2.getAddress());


        /* the new contributor submits progress report */
        getPeriodStatusMethod();

        ProgressReportAttributes progressReport = new ProgressReportAttributes();
        progressReport.ipfs_hash = "Proposal 1";
        progressReport.report_hash = "Report 1";
        progressReport.ipfs_link = "Link";
        progressReport.progress_report_title = "Progress Report Title";
        progressReport.budget_adjustment = false;
        progressReport.additional_budget = BigInteger.ZERO;
        progressReport.additional_month = 0;

        CPSCoreInterface.MilestoneSubmission milestoneSubmission1 = new CPSCoreInterface.MilestoneSubmission();
        milestoneSubmission1.id = 1;
        milestoneSubmission1.status = true;

        CPSCoreInterface.MilestoneSubmission[] milestoneSubmission = new CPSCoreInterface.MilestoneSubmission[]{
                milestoneSubmission1};

        Executable call = () -> cpsScore.invoke(owner, "submitProgressReport", progressReport, milestoneSubmission);
        expectErrorMessage(call, "Reverted(0): CPS Score: Sorry, You are not the contributor for this project.");

        contextMock.when(caller()).thenReturn(testingAccount1.getAddress());
        cpsScore.invoke(testingAccount1, "submitProgressReport", progressReport, milestoneSubmission);

        /* update period to voting */
        contextMock.when(caller()).thenReturn(owner.getAddress());
        updateNextBlock();
        cpsScore.invoke(owner, "updatePeriod");


        CPSCoreInterface.MilestoneVoteAttributes milestoneVoteAttributes = new CPSCoreInterface.MilestoneVoteAttributes();
        milestoneVoteAttributes.vote = APPROVE;
        milestoneVoteAttributes.id = 1;

        CPSCoreInterface.MilestoneVoteAttributes[] milestoneVoteAttributesList = new CPSCoreInterface.MilestoneVoteAttributes[]{
                milestoneVoteAttributes};
        doNothing().when(scoreSpy).callScore(eq(cpfTreasury), eq("swapTokens"), any());
//        cpsScore.invoke(owner, "voteProgressReport",  "Report 1", "reason", milestoneVoteAttributesList ,"_reject",false);
        contextMock.when(caller()).thenReturn(testingAccount.getAddress());
//        cpsScore.invoke(testingAccount, "voteProgressReport",  "Report 1", "reason", (Object)milestoneVoteAttributesList ,"_reject",false);
        contextMock.when(caller()).thenReturn(testingAccount1.getAddress());
        cpsScore.invoke(testingAccount1, "voteProgressReport", "Report 1", "reason", milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount2.getAddress());
        cpsScore.invoke(testingAccount2, "voteProgressReport", "Report 1", "reason", (Object) milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount3.getAddress());
        cpsScore.invoke(testingAccount3, "voteProgressReport", "Report 1", "reason", (Object) milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount4.getAddress());
        cpsScore.invoke(testingAccount4, "voteProgressReport", "Report 1", "reason", (Object) milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount5.getAddress());
        cpsScore.invoke(testingAccount5, "voteProgressReport", "Report 1", "reason", (Object) milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount6.getAddress());
        cpsScore.invoke(testingAccount6, "voteProgressReport", "Report 1", "reason", (Object) milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount7.getAddress());
        cpsScore.invoke(testingAccount7, "voteProgressReport", "Report 1", "reason", (Object) milestoneVoteAttributesList, "_reject", false);
        contextMock.when(caller()).thenReturn(testingAccount8.getAddress());
        cpsScore.invoke(testingAccount8, "voteProgressReport", "Report 1", "reason", (Object) milestoneVoteAttributesList, "_reject", false);

        @SuppressWarnings("unchecked")
        Map<String, Object> progressReportVoteDetails = (Map<String, Object>) cpsScore.call("getProgressReportVoteDetails", "Report 1");

        assertEquals(10, progressReportVoteDetails.get(TOTAL_VOTERS));
        Map<String, Object> milestoneReport = (Map<String, Object>) cpsScore.call("getMilestonesReport", "Proposal 1", 1);
        assertEquals(8, milestoneReport.get(APPROVE_VOTERS));
        assertEquals(MILESTONE_REPORT_COMPLETED, milestoneReport.get(STATUS));


        /* update period to application */
        contextMock.when(caller()).thenReturn(owner.getAddress());
        updateNextBlock();

        // mocks for updating periods
        doNothing().when(scoreSpy).callScore(eq(cpsTreasury), eq("sendInstallmentToContributor"), eq("Proposal 1"), any(BigInteger.class));
        doNothing().when(scoreSpy).callScore(eq(cpsTreasury), eq("sendRewardToSponsor"), eq("Proposal 1"), eq(1));
        doNothing().when(scoreSpy).callScore(eq(BigInteger.ZERO), eq(SYSTEM_ADDRESS), eq("burn"));

        updatePeriods();

        proposalDetails = getProposalDetailsByHash(ipfsHash);
        assertEquals(proposalDetails.get("approved_reports"), 1);

        milestoneReport = (Map<String, Object>) cpsScore.call("getMilestonesReport", "Proposal 1", 1);
        assertEquals(MILESTONE_REPORT_APPROVED, milestoneReport.get(STATUS));


    }


    void getPeriodStatusMethod() {
        Map<String, ?> periodStatus = (Map<String, ?>) cpsScore.call("getPeriodStatus");

        System.out.println(periodStatus);
    }

    Map<String, Object> getProposalDetailsByHash(String ipfs_hash) {
        return (Map<String, Object>) cpsScore.call("getProposalDetailsByHash", ipfs_hash);
    }

    Map<String, Object> getVoteResult(String ipfs_hash) {
        return (Map<String, Object>) cpsScore.call("getVoteResult", ipfs_hash);
    }

    public MockedStatic.Verification caller() {
        return () -> Context.getCaller();
    }
}
