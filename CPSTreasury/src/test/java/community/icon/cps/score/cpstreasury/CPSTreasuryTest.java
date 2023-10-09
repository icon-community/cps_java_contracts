package community.icon.cps.score.cpstreasury;

import com.eclipsesource.json.JsonObject;
import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import community.icon.cps.score.cpstreasury.utils.consts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import score.Address;
import score.Context;
import score.DictDB;
import score.VarDB;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class CPSTreasuryTest extends TestBase {
    private static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
    private static final Address treasury_score = new Address(new byte[Address.LENGTH]);
    private static final Address score_address = Address.fromString("cx0000000000000000000000000000000000000000");
    private static final Address cpfTreasury = Address.fromString("cx0000000000000000000000000000000000000001");
    private static final Address bnUSDScore = Address.fromString("cx0000000000000000000000000000000000000002");
    private static final Address dexScore = Address.fromString("cx0000000000000000000000000000000000000003");
    private static final Address cpsTreasuryScore = Address.fromString("cx0000000000000000000000000000000000000004");

    private static final String name = "CPS_TREASURY";
    public static final String TAG = "CPS_TREASURY";
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
    public static MockedStatic<Context> contextMock;

    CPSTreasury scoreSpy;

    @BeforeEach
    public void setup() throws Exception {
        tokenScore = sm.deploy(owner, CPSTreasury.class);
        CPSTreasury instance = (CPSTreasury) tokenScore.getInstance();
        scoreSpy = spy(instance);
        tokenScore.setInstance(scoreSpy);
        contextMock.reset();
//        setScoresMethod();
    }

    @BeforeAll
    public static void init(){
        contextMock = Mockito.mockStatic(Context.class, CALLS_REAL_METHODS);
    }

    @Test
    void name() {
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
    void fallback() {
        Executable fallback = () -> fallbackExceptions(owner);
        expectErrorMessage(fallback, "Reverted(0):" + " " + TAG + ": ICX can only be send by CPF Treasury Score");
    }

    @Test
    public void setScoresMethod(){
        setCpsScore();
        setCPFTreasuryScoreMethod();
        setBnUSDScoreMethod();
        setOnsetPayment();
    }

    @Test
    void setCpsScore() {
        setCpsScoreMethod();
    }

    private void setCpsScoreMethod() {
        contextMock.when(caller()).thenReturn(owner.getAddress());
        doReturn(Boolean.TRUE).when(scoreSpy).callScore(eq(Boolean.class), any(), eq("isAdmin"),eq(owner.getAddress()));
        tokenScore.invoke(owner, "setCpsScore", score_address);
        assertEquals(score_address, tokenScore.call("getCpsScore"));
    }

    @Test
    void setCPFTreasuryScore() {
        setCPFTreasuryScoreMethod();
    }

    private void setCPFTreasuryScoreMethod() {
        contextMock.when(caller()).thenReturn(owner.getAddress());
        doReturn(Boolean.TRUE).when(scoreSpy).callScore(eq(Boolean.class), any(), eq("isAdmin"), eq(owner.getAddress()));
        tokenScore.invoke(owner, "setCpfTreasuryScore", cpfTreasury);
        assertEquals(cpfTreasury, tokenScore.call("getCpfTreasuryScore"));
    }

    @Test
    void setBnUSDScore() {
        setBnUSDScoreMethod();
    }
    @Test
    void setOnsetPayment(){
        setCPFTreasuryScoreMethod();
        doReturn(BigInteger.valueOf(10)).when(scoreSpy).callScore(eq(BigInteger.class),any(),eq("getSponsorBondPercentage"));
        BigInteger per = BigInteger.ONE;
        contextMock.when(caller()).thenReturn(cpfTreasury);
        tokenScore.invoke(owner,"setOnsetPayment",per);
    }

    public MockedStatic.Verification caller(){
        return () -> score.Context.getCaller();
    }

    private void setBnUSDScoreMethod() {
        contextMock.when(caller()).thenReturn(owner.getAddress());
        doReturn(Boolean.TRUE).when(scoreSpy).callScore(eq(Boolean.class), any(), eq("isAdmin"), eq(owner.getAddress()));
        tokenScore.invoke(owner, "setBnUSDScore", bnUSDScore);
        assertEquals(bnUSDScore, tokenScore.call("getBnUSDScore"));
    }

    void setCpsScoreExceptions(Boolean isAdmin, Address score_address) {
        contextMock.when(caller()).thenReturn(owner.getAddress());
        doReturn(isAdmin).when(scoreSpy).callScore(eq(Boolean.class), any(), eq("isAdmin"), eq(owner.getAddress()));
        tokenScore.invoke(owner, "setCpsScore", score_address);
    }

    void setCpfTreasuryScoreExceptions(Boolean isAdmin, Address score_address) {
        contextMock.when(caller()).thenReturn(owner.getAddress());
        doReturn(isAdmin).when(scoreSpy).callScore(eq(Boolean.class), any(), eq("isAdmin"), eq(owner.getAddress()));
        tokenScore.invoke(owner, "setCpfTreasuryScore", score_address);
    }

    void setBnUSDScoreExceptions(Boolean isAdmin, Address score_address) {
        contextMock.when(caller()).thenReturn(owner.getAddress());
        doReturn(isAdmin).when(scoreSpy).callScore(eq(Boolean.class), any(), eq("isAdmin"), eq(owner.getAddress()));
        tokenScore.invoke(owner, "setBnUSDScore", score_address);
    }

    @Test
    void setCpsScoreNotAdmin() {
        setScoresMethod();
        Executable setCpsScoreNotAdmin = () -> setCpsScoreExceptions(false, score_address);
        expectErrorMessage(setCpsScoreNotAdmin, "Reverted(0): " + TAG + ": Only admins can call this method");
    }

    @Test
    void setCpfTreasuryScoreNotAdmin() {
        setScoresMethod();
        Executable setCpfTreasuryScoreNotAdmin = () -> setCpfTreasuryScoreExceptions(false, score_address);
        expectErrorMessage(setCpfTreasuryScoreNotAdmin, "Reverted(0): " + TAG + ": Only admins can call this method");
    }

    @Test
    void setBnUSDScoreNotAdmin() {
        setScoresMethod();
        Executable setBnUSDScoreNotAdmin = () -> setBnUSDScoreExceptions(false, score_address);
        expectErrorMessage(setBnUSDScoreNotAdmin, "Reverted(0): " + TAG + ": Only admins can call this method");
    }

    @Test
    void setCPSScoreNotContract() {
        Executable setCpsScoreNotAdmin = () -> setCpsScoreExceptions(true, testing_account.getAddress());
        expectErrorMessage(setCpsScoreNotAdmin, "Reverted(0): " + TAG + "Target " + testing_account.getAddress() + " is not a score.");
    }

    @Test
    void setCPFTreasuryScoreNotContract() {
        Executable setCpfTreasuryScoreNotContract = () -> setCpfTreasuryScoreExceptions(true, testing_account.getAddress());
        expectErrorMessage(setCpfTreasuryScoreNotContract, "Reverted(0): " + TAG + "Target " + testing_account.getAddress() + " is not a score.");
    }

    @Test
    void setBnUSDScoreNotContract() {
        Executable setBnUSDScoreContract = () -> setBnUSDScoreExceptions(true, testing_account.getAddress());
        expectErrorMessage(setBnUSDScoreContract, "Reverted(0): " + TAG + "Target " + testing_account.getAddress() + " is not a score.");
    }

    @Test
    void depositProposalFund() {
        /* totalBudget = 100, onsetPayment = 1% installmentCount = 2
        * remainingBudgetAfter onsetPayment = 99
        * installMentAmount = 99/2 =49.5
        */
        setOnsetPayment();
        depositProposalFundMethod();
        @SuppressWarnings("unchecked")
        Map<String, ?> proposalDataDetails = (Map<String, ?>) tokenScore.call("getContributorProjectedFund", testing_account2.getAddress());
        @SuppressWarnings("unchecked")
        List<Map<String, ?>> proposalDetails = (List<Map<String, ?>>) proposalDataDetails.get("data");
        Map<String, ?> expectedData = Map.of(
                consts.IPFS_HASH, "Proposal 1",
                consts.TOKEN, "bnUSD",
                consts.TOTAL_BUDGET, BigInteger.valueOf(100).multiply(MULTIPLIER),
                consts.TOTAL_INSTALLMENT_PAID, BigInteger.ONE.multiply(MULTIPLIER),
                consts.TOTAL_INSTALLMENT_COUNT, 2,
                consts.TOTAL_TIMES_INSTALLMENT_PAID, 0,
                consts.INSTALLMENT_AMOUNT, BigInteger.valueOf(495).multiply(MULTIPLIER).divide(BigInteger.TEN));
        assertEquals(proposalDetails.get(0), expectedData);
    }

    private void depositProposalFundMethod() {
        JsonObject depositProposal = new JsonObject();
        depositProposal.add("method", "depositProposalFund");
        JsonObject params = new JsonObject();
        params.add("ipfs_hash", "Proposal 1");
        params.add("project_duration", 2);
        params.add("milestone_count", 2);
        params.add("sponsor_address", testing_account.getAddress().toString());
        params.add("contributor_address", testing_account2.getAddress().toString());
        params.add("total_budget", BigInteger.valueOf(100).multiply(MULTIPLIER).toString(16));
        params.add("sponsor_reward", BigInteger.valueOf(2).multiply(MULTIPLIER).toString(16));
        params.add("token", "bnUSD");
        depositProposal.add("params", params);
        setCPFTreasuryScoreMethod();
        tokenScore.invoke(owner, "tokenFallback", cpfTreasury, BigInteger.valueOf(102).multiply(MULTIPLIER), depositProposal.toString().getBytes());
    }

    void depositProposalFundExceptions(){
        try{
            depositProposalFundMethod();
        }
        catch (Exception e){
            throw e;
        }
    }
    @Test
    void depositProposalFundProposalAlreadyExists(){
        setOnsetPayment();
        depositProposalFundMethod();
        Executable depositProposalFundProposalAlreadyExists = () -> depositProposalFundExceptions();
        expectErrorMessage(depositProposalFundProposalAlreadyExists, "Reverted(0): " + "CPS_TREASURY: Already have this project");
    }

    @Test
    void updateProposalFund() {
        /* before budget adjustment:
                    totalBudget = 100  installmentCount = 2 onsetPayment = 1
                    installmentAmount = (100-1)/2 = 49.5
           after budget adjustment :
                    totalBudget = 200 installCount = 3 onsetPayment = 1
                    installmentAmount = (200-1)/3 = 66.33
        * */
        setOnsetPayment();
        depositProposalFundMethod();
        JsonObject budgetAdjustmentData = new JsonObject();
        budgetAdjustmentData.add("method", "budgetAdjustment");
        JsonObject params = new JsonObject();
        params.add("_ipfs_key", "Proposal 1");
        params.add("_added_budget", BigInteger.valueOf(100).multiply(MULTIPLIER).toString(16));
        params.add("_added_sponsor_reward", BigInteger.valueOf(2).multiply(MULTIPLIER).toString(16));
        params.add("_added_installment_count", 1);
        budgetAdjustmentData.add("params", params);

        tokenScore.invoke(owner, "tokenFallback", cpfTreasury, BigInteger.valueOf(102).multiply(MULTIPLIER), budgetAdjustmentData.toString().getBytes());

        @SuppressWarnings("unchecked")
        Map<String, ?> proposalDataDetails = (Map<String, ?>) tokenScore.call("getContributorProjectedFund", testing_account2.getAddress());
        @SuppressWarnings("unchecked")
        List<Map<String, ?>> proposalDetails = (List<Map<String, ?>>) proposalDataDetails.get("data");
        Map<String, ?> expectedData = Map.of(
                consts.IPFS_HASH, "Proposal 1",
                consts.TOKEN, "bnUSD",
                consts.TOTAL_BUDGET, BigInteger.valueOf(200).multiply(MULTIPLIER),
                consts.TOTAL_INSTALLMENT_PAID, BigInteger.ONE.multiply(ICX),
                consts.TOTAL_INSTALLMENT_COUNT, 3,
                consts.TOTAL_TIMES_INSTALLMENT_PAID, 0,
                consts.INSTALLMENT_AMOUNT, new BigInteger("66333333333333333333"));
        assertEquals(proposalDetails.get(0), expectedData);
    }

    void updateProposalFundProposalException(){
        try {
            setCPFTreasuryScoreMethod();
            JsonObject budgetAdjustmentData = new JsonObject();
            budgetAdjustmentData.add("method", "budgetAdjustment");
            JsonObject params = new JsonObject();
            params.add("_ipfs_key", "Proposal 1");
            params.add("_added_budget", BigInteger.valueOf(100).multiply(MULTIPLIER).toString(16));
            params.add("_added_sponsor_reward", BigInteger.valueOf(2).multiply(MULTIPLIER).toString(16));
            params.add("_added_installment_count", 1);
            budgetAdjustmentData.add("params", params);

            tokenScore.invoke(owner, "tokenFallback", cpfTreasury, BigInteger.valueOf(102).multiply(MULTIPLIER), budgetAdjustmentData.toString().getBytes());
        }
        catch (Exception e){
            throw e;
        }
    }

    @Test
    void updateProposalFundProposalDoesnotExist(){
        Executable updateProposalFundProposalDoesnotExist = () -> updateProposalFundProposalException();
        expectErrorMessage(updateProposalFundProposalDoesnotExist, "Reverted(0): " + "CPS_TREASURY: Invalid IPFS hash.");
    }

    private void depositProposalFund_MilestoneCheck() {
        /* Sponsor bond = 10 % intital payement = 1%
        * duration = 2 months milestone = 3 */
        JsonObject depositProposal = new JsonObject();
        depositProposal.add("method", "depositProposalFund");
        JsonObject params = new JsonObject();
        params.add("ipfs_hash", "Proposal 1");
        params.add("project_duration", 3);
        params.add("milestone_count", 4);
        params.add("sponsor_address", testing_account.getAddress().toString());
        params.add("contributor_address", testing_account2.getAddress().toString());
        params.add("total_budget", BigInteger.valueOf(100).multiply(MULTIPLIER).toString(16));
        params.add("sponsor_reward", BigInteger.valueOf(10).multiply(MULTIPLIER).toString(16));
        params.add("token", "bnUSD");
        depositProposal.add("params", params);
        setCPFTreasuryScoreMethod();
        tokenScore.invoke(owner, "tokenFallback", cpfTreasury, BigInteger.valueOf(102).multiply(MULTIPLIER), depositProposal.toString().getBytes());
    }

    @Test
    void sendInstallmentToContributor() { // send first installment
        setOnsetPayment();
        depositProposalFund_MilestoneCheck();
        setCpsScoreMethod();

        // proposal details after submission
        Map<String, ?> proposalDataDetails_before = (Map<String, ?>) tokenScore.call("getContributorProjectedFund", testing_account2.getAddress());
        assertEquals(proposalDataDetails_before.get("withdraw_amount_bnUSD"), BigInteger.valueOf(1).multiply(MULTIPLIER));

        List<Map<String, ?>> proposalDetailsData = (List<Map<String, ?>>) proposalDataDetails_before.get("data");
        assertEquals(proposalDetailsData.get(0).get("total_installment_paid"), BigInteger.valueOf(1).multiply(MULTIPLIER));
        assertEquals(proposalDetailsData.get(0).get("total_installment_count"), 4);
        assertEquals(proposalDetailsData.get(0).get("installment_amount"),  BigInteger.valueOf(2475).multiply(MULTIPLIER).divide(BigInteger.valueOf(100)));

        contextMock.when(caller()).thenReturn(score_address);
        tokenScore.invoke(owner, "sendInstallmentToContributor", "Proposal 1", 1);
        @SuppressWarnings("unchecked")
        Map<String, ?> proposalDataDetails_after = (Map<String, ?>) tokenScore.call("getContributorProjectedFund", testing_account2.getAddress());
        assertEquals(proposalDataDetails_after.get("withdraw_amount_bnUSD"), BigInteger.valueOf(2575).multiply(MULTIPLIER).divide(BigInteger.valueOf(100)));

        proposalDetailsData = (List<Map<String, ?>>) proposalDataDetails_after.get("data");
        assertEquals(proposalDetailsData.get(0).get("total_installment_paid"), BigInteger.valueOf(2575).multiply(MULTIPLIER).divide(BigInteger.valueOf(100)));
        assertEquals(proposalDetailsData.get(0).get("total_installment_count"), 3);
        assertEquals(proposalDetailsData.get(0).get("installment_amount"),  BigInteger.valueOf(2475).multiply(MULTIPLIER).divide(BigInteger.valueOf(100)));

    }


    @Test
    void sendInstallmentToContributor_SecondInstallment() { // send first installment
        sendInstallmentToContributor();

        tokenScore.invoke(owner, "sendInstallmentToContributor", "Proposal 1", 1);
        @SuppressWarnings("unchecked")
        Map<String, ?> proposalDataDetails_after = (Map<String, ?>) tokenScore.call("getContributorProjectedFund", testing_account2.getAddress());
        assertEquals(proposalDataDetails_after.get("withdraw_amount_bnUSD"), BigInteger.valueOf(505).multiply(MULTIPLIER).divide(BigInteger.valueOf(10)));

        List<Map<String, ?>> proposalDetailsData = (List<Map<String, ?>>) proposalDataDetails_after.get("data");
        assertEquals(proposalDetailsData.get(0).get("total_installment_paid"), BigInteger.valueOf(505).multiply(MULTIPLIER).divide(BigInteger.valueOf(10)));
        assertEquals(proposalDetailsData.get(0).get("total_installment_count"), 2);
        assertEquals(proposalDetailsData.get(0).get("installment_amount"),  BigInteger.valueOf(2475).multiply(MULTIPLIER).divide(BigInteger.valueOf(100)));

    }

    @Test
    void sendInstallmentToContributor_Completed(){
        // total Milestone = 4, remaining milestone = 2
        sendInstallmentToContributor_SecondInstallment();

        tokenScore.invoke(owner, "sendInstallmentToContributor", "Proposal 1", 2);
        @SuppressWarnings("unchecked")
        Map<String, ?> proposalDataDetails_after = (Map<String, ?>) tokenScore.call("getContributorProjectedFund", testing_account2.getAddress());
        assertEquals(proposalDataDetails_after.get("withdraw_amount_bnUSD"), BigInteger.valueOf(100).multiply(MULTIPLIER));

    }

    @Test
    void sendRewardToSponsor() {
        setOnsetPayment();
        depositProposalFund_MilestoneCheck();
        setCpsScoreMethod();
        @SuppressWarnings("unchecked")
        Map<String, ?> proposalDataDetails = (Map<String, ?>) tokenScore.call("getSponsorProjectedFund", testing_account.getAddress());
        assertEquals(proposalDataDetails.get("withdraw_amount_bnUSD"), BigInteger.valueOf(1).multiply(MULTIPLIER).divide(BigInteger.TEN));

        sendRewardToSponsorMethod();
        Map<String, ?> proposalDataDetails2 = (Map<String, ?>) tokenScore.call("getSponsorProjectedFund", testing_account.getAddress());
        assertEquals(proposalDataDetails2.get("withdraw_amount_bnUSD"), BigInteger.valueOf(2575).multiply(MULTIPLIER).divide(BigInteger.valueOf(1000)));


        List<Map<String, ?>> proposalDetailsData = (List<Map<String, ?>>) proposalDataDetails2.get("data");
        assertEquals(proposalDetailsData.get(0).get("total_installment_paid"), BigInteger.valueOf(2575).multiply(MULTIPLIER).divide(BigInteger.valueOf(1000)));
        assertEquals(proposalDetailsData.get(0).get("total_installment_count"), 4);
        assertEquals(proposalDetailsData.get(0).get("installment_amount"),  BigInteger.valueOf(2475).multiply(MULTIPLIER).divide(BigInteger.valueOf(1000)));

    }

    private void sendRewardToSponsorMethod() {
        contextMock.when(caller()).thenReturn(score_address);
        tokenScore.invoke(owner, "sendRewardToSponsor", "Proposal 1", 1);
    }

    @Test
    void disqualifyProject(){
        setOnsetPayment();
        depositProposalFundMethod();
        setCPFTreasuryScoreMethod();
        setCpsScoreMethod();
        setBnUSDScoreMethod();

        /* total budget = 100 sponsor reward = 2
        * on proposal submission : contributor_reward = 1 and sponsor_reward = 0.02
        * remainning budget = 102 -1-0.02 = 100.98
        *  */
        BigInteger remainingBudget = BigInteger.valueOf(10098).multiply(MULTIPLIER).divide(BigInteger.valueOf(100));
        contextMock.when(caller()).thenReturn(score_address);
        tokenScore.invoke(owner, "disqualifyProject", "Proposal 1");
        JsonObject disqualifyProjectParams = new JsonObject();
        disqualifyProjectParams.add("method", "disqualifyProject");
        JsonObject params = new JsonObject();
        params.add("ipfs_key", "Proposal 1");
        disqualifyProjectParams.add("params", params);
        doNothing().when(scoreSpy).callScore(eq(bnUSDScore), eq("transfer"), eq(cpfTreasury),
                eq(remainingBudget),any());

    }

    @Test
    void claimReward(){
        setScoresMethod();
        depositProposalFundMethod();
//        sendRewardToSponsorMethod();
        setBnUSDScoreMethod();
        contextMock.when(caller()).thenReturn(testing_account.getAddress());
        tokenScore.invoke(testing_account, "claimReward");
        BigInteger reward = BigInteger.valueOf(2).multiply(MULTIPLIER).divide(BigInteger.valueOf(100));

        doNothing().when(scoreSpy).callScore(eq(bnUSDScore),eq("transfer"),eq(testing_account.getAddress()),eq(reward));

    }
}