package community.icon.cps.score.cpstreasury;
import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import score.Address;
import score.DictDB;
import score.VarDB;

import java.math.BigInteger;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class CPSTreasuryTest extends TestBase{
    private static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
    private static final Address treasury_score = new Address(new byte[Address.LENGTH]);
    private static final Address score_address = Address.fromString("cx0000000000000000000000000000000000000000");
    private static final Address sicxScore = Address.fromString("cx0000000000000000000000000000000000000001");
    private static final Address bnUSDScore = Address.fromString("cx0000000000000000000000000000000000000002");
    private static final Address dexScore = Address.fromString("cx0000000000000000000000000000000000000003");
    private static final Address cpsTreasuryScore = Address.fromString("cx0000000000000000000000000000000000000004");

    private static final String name = "CPS_Treasury";
    public static final String TAG = "CPS_Treasury";
    CPSTreasury cpsTreasury;
    private static final BigInteger MULTIPLIER = new BigInteger("1000000000000000000");


    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account testing_account = sm.createAccount();
    private static final Account testing_account2 = sm.createAccount();

    private Score tokenScore;
    private final SecureRandom secureRandom = new SecureRandom();

    DictDB<String, BigInteger> proposalBudgets = Mockito.mock(DictDB.class);
    VarDB<Integer> swapState = Mockito.mock(VarDB.class);
    VarDB<Integer> swapCount = Mockito.mock(VarDB.class);

    CPSTreasury scoreSpy;

    @BeforeEach
    public void setup() throws Exception {
        tokenScore = sm.deploy(owner, CPSTreasury.class);
        CPSTreasury instance = (CPSTreasury) tokenScore.getInstance();
        scoreSpy = spy(instance);
        tokenScore.setInstance(scoreSpy);
    }

    @Test
    void name(){
        assertEquals(tokenScore.call("name"), name);
    }


    public void expectErrorMessage(Executable contractCall, String errorMessage) {
        AssertionError e = Assertions.assertThrows(AssertionError.class, contractCall);
        assertEquals(errorMessage, e.getMessage());
    }

    void fallbackExceptions(Account address) {
        try {
            sm.call(address, BigInteger.valueOf(1000).multiply(MULTIPLIER), tokenScore.getAddress(), "fallback");
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    void fallback(){
        Executable fallback = () -> fallbackExceptions(owner);
        expectErrorMessage(fallback, "Reverted(0):" + " " + TAG + ": ICX can only be send by CPF Treasury Score");
    }

    @Test
    void setCpsScore(){
        doReturn(Boolean.TRUE).when(scoreSpy).callScore(eq(Boolean.class), any(), eq("is_admin"), eq(owner.getAddress()));
        tokenScore.invoke(owner, "setCpsScore", score_address);
    }

    @Test
    void setCPFTreasuryScore(){
        doReturn(Boolean.TRUE).when(scoreSpy).callScore(eq(Boolean.class), any(), eq("is_admin"), eq(owner.getAddress()));
        tokenScore.invoke(owner, "setCpfTreasuryScore", score_address);
    }

    @Test
    void setBnUSDScore(){
        doReturn(Boolean.TRUE).when(scoreSpy).callScore(eq(Boolean.class), any(), eq("is_admin"), eq(owner.getAddress()));
        tokenScore.invoke(owner, "setBnUSDScore", score_address);
    }

    void setCpsScoreExceptions(Boolean isAdmin, Address score_address){
        doReturn(isAdmin).when(scoreSpy).callScore(eq(Boolean.class), any(), eq("is_admin"), eq(owner.getAddress()));
        tokenScore.invoke(owner, "setCpsScore", score_address);
    }

    void setCpfTreasuryScoreExceptions(Boolean isAdmin, Address score_address){
        doReturn(isAdmin).when(scoreSpy).callScore(eq(Boolean.class), any(), eq("is_admin"), eq(owner.getAddress()));
        tokenScore.invoke(owner, "setCpfTreasuryScore", score_address);
    }

    void setBnUSDScoreExceptions(Boolean isAdmin, Address score_address){
        doReturn(isAdmin).when(scoreSpy).callScore(eq(Boolean.class), any(), eq("is_admin"), eq(owner.getAddress()));
        tokenScore.invoke(owner, "setBnUSDScore", score_address);
    }

    @Test
    void setCpsScoreNotAdmin() {
        Executable setCpsScoreNotAdmin = () -> setCpsScoreExceptions(false, score_address);
        expectErrorMessage(setCpsScoreNotAdmin, TAG + ": Only admins can call this method");
    }

    @Test
    void setCpfTreasuryScoreNotAdmin() {
        Executable setCpfTreasuryScoreNotAdmin = () -> setCpfTreasuryScoreExceptions(false, score_address);
        expectErrorMessage(setCpfTreasuryScoreNotAdmin, TAG + ": Only admins can call this method");
    }

    @Test
    void setBnUSDScoreNotAdmin() {
        Executable setBnUSDScoreNotAdmin = () -> setBnUSDScoreExceptions(false, score_address);
        expectErrorMessage(setBnUSDScoreNotAdmin, TAG + ": Only admins can call this method");
    }

    @Test
    void setCPSScoreNotContract(){
        Executable setCpsScoreNotAdmin = () -> setCpsScoreExceptions(true, testing_account.getAddress());
        expectErrorMessage(setCpsScoreNotAdmin, TAG + "Target " + testing_account.getAddress() + " is not a score.");
    }

    @Test
    void setCPFTreasuryScoreNotContract(){
        Executable setCpfTreasuryScoreNotContract = () -> setCpfTreasuryScoreExceptions(true, testing_account.getAddress());
        expectErrorMessage(setCpfTreasuryScoreNotContract, TAG + "Target " + testing_account.getAddress() + " is not a score.");
    }

    @Test
    void setBnUSDScoreNotContract(){
        Executable setBnUSDScoreContract = () -> setBnUSDScoreExceptions(true, testing_account.getAddress());
        expectErrorMessage(setBnUSDScoreContract, TAG + "Target " + testing_account.getAddress() + " is not a score.");
    }
}
