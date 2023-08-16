package community.icon.cps.score.cpstreasury;

import com.eclipsesource.json.JsonObject;
import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import community.icon.cps.score.cpstreasury.utils.consts;
import org.junit.jupiter.api.Assertions;
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
    private static final Account cpfTreasuryScore = Account.newScoreAccount(5);;
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

    CPSTreasury scoreSpy;

    @BeforeEach
    public void setup() throws Exception {
        tokenScore = sm.deploy(owner, CPSTreasury.class);
        CPSTreasury instance = (CPSTreasury) tokenScore.getInstance();
        scoreSpy = spy(instance);
        tokenScore.setInstance(scoreSpy);
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
    void setCpsScore() {
        setCpsScoreMethod();
    }

    private void setCpsScoreMethod() {
        doReturn(Boolean.TRUE).when(scoreSpy).callScore(eq(Boolean.class), any(), eq("is_admin"), eq(owner.getAddress()));
        tokenScore.invoke(owner, "setCpsScore", score_address);
        assertEquals(score_address, tokenScore.call("getCpsScore"));
    }

    @Test
    void setCPFTreasuryScore() {
        setCPFTreasuryScoreMethod();
    }

    private void setCPFTreasuryScoreMethod() {
        doReturn(Boolean.TRUE).when(scoreSpy).callScore(eq(Boolean.class), any(), eq("is_admin"), eq(owner.getAddress()));
        tokenScore.invoke(owner, "setCpfTreasuryScore", cpfTreasuryScore.getAddress());
        assertEquals(cpfTreasuryScore.getAddress(), tokenScore.call("getCpfTreasuryScore"));
    }

    @Test
    void setBnUSDScore() {
        setBnUSDScoreMethod();
    }

    private void setBnUSDScoreMethod() {
        doReturn(Boolean.TRUE).when(scoreSpy).callScore(eq(Boolean.class), any(), eq("is_admin"), eq(owner.getAddress()));
        tokenScore.invoke(owner, "setBnUSDScore", bnUSDScore);
        assertEquals(bnUSDScore, tokenScore.call("getBnUSDScore"));
    }

    void setCpsScoreExceptions(Boolean is_admin, Address score_address) {
        doReturn(is_admin).when(scoreSpy).callScore(eq(Boolean.class), any(), eq("is_admin"), eq(owner.getAddress()));
        tokenScore.invoke(owner, "setCpsScore", score_address);
    }

    void setCpfTreasuryScoreExceptions(Boolean is_admin, Address score_address) {
        doReturn(is_admin).when(scoreSpy).callScore(eq(Boolean.class), any(), eq("is_admin"), eq(owner.getAddress()));
        tokenScore.invoke(owner, "setCpfTreasuryScore", score_address);
    }

    void setBnUSDScoreExceptions(Boolean is_admin, Address score_address) {
        doReturn(is_admin).when(scoreSpy).callScore(eq(Boolean.class), any(), eq("is_admin"), eq(owner.getAddress()));
        tokenScore.invoke(owner, "setBnUSDScore", score_address);
    }

    @Test
    void setCpsScoreNotAdmin() {
        Executable setCpsScoreNotAdmin = () -> setCpsScoreExceptions(false, score_address);
        expectErrorMessage(setCpsScoreNotAdmin, "Reverted(0): " + TAG + ": Only admins can call this method");
    }

    @Test
    void setCpfTreasuryScoreNotAdmin() {
        Executable setCpfTreasuryScoreNotAdmin = () -> setCpfTreasuryScoreExceptions(false, score_address);
        expectErrorMessage(setCpfTreasuryScoreNotAdmin, "Reverted(0): " + TAG + ": Only admins can call this method");
    }

    @Test
    void setBnUSDScoreNotAdmin() {
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
        depositProposalFundMethod();
        @SuppressWarnings("unchecked")
        Map<String, ?> proposalDataDetails = (Map<String, ?>) tokenScore.call("get_contributor_projected_fund", testing_account2.getAddress());
        @SuppressWarnings("unchecked")
        List<Map<String, ?>> proposalDetails = (List<Map<String, ?>>) proposalDataDetails.get("data");
        Map<String, ?> expectedData = Map.of(
                consts.IPFS_HASH, "Proposal 1",
                consts.TOKEN, "bnUSD",
                consts.TOTAL_BUDGET, BigInteger.valueOf(100).multiply(MULTIPLIER),
                consts.TOTAL_INSTALLMENT_PAID, BigInteger.valueOf(10).multiply(MULTIPLIER),
                consts.TOTAL_INSTALLMENT_COUNT, 2,
                consts.TOTAL_TIMES_INSTALLMENT_PAID, 0,
                consts.INSTALLMENT_AMOUNT, BigInteger.valueOf(45).multiply(MULTIPLIER));
        assertEquals(proposalDetails.get(0), expectedData);
    }

    private void depositProposalFundMethod() {
        JsonObject depositProposal = new JsonObject();
        depositProposal.add("method", "deposit_proposal_fund");
        JsonObject params = new JsonObject();
        params.add("ipfs_hash", "Proposal 1");
        params.add("project_duration", 2);
        params.add("sponsor_address", testing_account.getAddress().toString());
        params.add("contributor_address", testing_account2.getAddress().toString());
        params.add("total_budget", BigInteger.valueOf(100).multiply(MULTIPLIER).toString(16));
        params.add("sponsor_reward", BigInteger.valueOf(2).multiply(MULTIPLIER).toString(16));
        params.add("token", "bnUSD");
        depositProposal.add("params", params);
        setCPFTreasuryScoreMethod();
        doReturn(BigInteger.TEN).when(scoreSpy).getOnsetPayment();
        tokenScore.invoke(owner, "tokenFallback", cpfTreasuryScore.getAddress(), BigInteger.valueOf(102).multiply(MULTIPLIER), depositProposal.toString().getBytes());
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
        depositProposalFundMethod();
        Executable depositProposalFundProposalAlreadyExists = () -> depositProposalFundExceptions();
        expectErrorMessage(depositProposalFundProposalAlreadyExists, "Reverted(0): " + "CPS_TREASURY: Already have this project");
    }

    @Test
    void updateProposalFund() {
        depositProposalFundMethod();
        JsonObject budgetAdjustmentData = new JsonObject();
        budgetAdjustmentData.add("method", "budget_adjustment");
        JsonObject params = new JsonObject();
        params.add("_ipfs_key", "Proposal 1");
        params.add("_added_budget", BigInteger.valueOf(100).multiply(MULTIPLIER).toString(16));
        params.add("_added_sponsor_reward", BigInteger.valueOf(2).multiply(MULTIPLIER).toString(16));
        params.add("_added_installment_count", 1);
        budgetAdjustmentData.add("params", params);

        tokenScore.invoke(owner, "tokenFallback", cpfTreasuryScore.getAddress(), BigInteger.valueOf(102).multiply(MULTIPLIER), budgetAdjustmentData.toString().getBytes());

        @SuppressWarnings("unchecked")
        Map<String, ?> proposalDataDetails = (Map<String, ?>) tokenScore.call("get_contributor_projected_fund", testing_account2.getAddress());
        @SuppressWarnings("unchecked")
        List<Map<String, ?>> proposalDetails = (List<Map<String, ?>>) proposalDataDetails.get("data");
        Map<String, ?> expectedData = Map.of(
                consts.IPFS_HASH, "Proposal 1",
                consts.TOKEN, "bnUSD",
                consts.TOTAL_BUDGET, BigInteger.valueOf(200).multiply(MULTIPLIER),
                consts.TOTAL_INSTALLMENT_PAID, BigInteger.valueOf(10).multiply(MULTIPLIER),
                consts.TOTAL_INSTALLMENT_COUNT, 3,
                consts.TOTAL_TIMES_INSTALLMENT_PAID, 0,
                consts.INSTALLMENT_AMOUNT, new BigInteger("63333333333333333333"));
        assertEquals(proposalDetails.get(0), expectedData);
    }

    void updateProposalFundProposalException(){
        try {
            setCPFTreasuryScoreMethod();
            JsonObject budgetAdjustmentData = new JsonObject();
            budgetAdjustmentData.add("method", "budget_adjustment");
            JsonObject params = new JsonObject();
            params.add("_ipfs_key", "Proposal 1");
            params.add("_added_budget", BigInteger.valueOf(100).multiply(MULTIPLIER).toString(16));
            params.add("_added_sponsor_reward", BigInteger.valueOf(2).multiply(MULTIPLIER).toString(16));
            params.add("_added_installment_count", 1);
            budgetAdjustmentData.add("params", params);

            tokenScore.invoke(owner, "tokenFallback", cpfTreasuryScore.getAddress(), BigInteger.valueOf(102).multiply(MULTIPLIER), budgetAdjustmentData.toString().getBytes());
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

    @Test
    void sendInstallmentToContributor() {
        depositProposalFundMethod();
        setCpsScoreMethod();
        try (MockedStatic<Context> theMock = Mockito.mockStatic(Context.class)) {
            theMock.when(() -> Context.getCaller()).thenReturn(score_address);
            tokenScore.invoke(owner, "send_installment_to_contributor", "Proposal 1");
        }
        @SuppressWarnings("unchecked")
        Map<String, ?> proposalDataDetails = (Map<String, ?>) tokenScore.call("get_contributor_projected_fund", testing_account2.getAddress());
        assertEquals(proposalDataDetails.get("withdraw_amount_bnUSD"), BigInteger.valueOf(55).multiply(MULTIPLIER));

        try (MockedStatic<Context> theMock = Mockito.mockStatic(Context.class)) {
            theMock.when(() -> Context.getCaller()).thenReturn(score_address);
            tokenScore.invoke(owner, "send_installment_to_contributor", "Proposal 1");
        }
        @SuppressWarnings("unchecked")
        Map<String, ?> proposalDataDetails2 = (Map<String, ?>) tokenScore.call("get_contributor_projected_fund", testing_account2.getAddress());
        assertEquals(proposalDataDetails2.get("withdraw_amount_bnUSD"), BigInteger.valueOf(100).multiply(MULTIPLIER));
    }

    @Test
    void sendRewardToSponsor() {
        depositProposalFundMethod();
        @SuppressWarnings("unchecked")
        Map<String, ?> proposalDataDetailsAfterProposal = (Map<String, ?>) tokenScore.call("get_sponsor_projected_fund", testing_account.getAddress());
        assertEquals(proposalDataDetailsAfterProposal.get("withdraw_amount_bnUSD"), BigInteger.valueOf(2).multiply(MULTIPLIER).divide(BigInteger.TEN));

        setCpsScoreMethod();
        sendRewardToSponsorMethod();
        @SuppressWarnings("unchecked")
        Map<String, ?> proposalDataDetails = (Map<String, ?>) tokenScore.call("get_sponsor_projected_fund", testing_account.getAddress());
        assertEquals(proposalDataDetails.get("withdraw_amount_bnUSD"), BigInteger.valueOf(11).multiply(MULTIPLIER).divide(BigInteger.TEN));

        try (MockedStatic<Context> theMock = Mockito.mockStatic(Context.class)) {
            theMock.when(() -> Context.getCaller()).thenReturn(score_address);
            tokenScore.invoke(owner, "send_reward_to_sponsor", "Proposal 1");
        }
        @SuppressWarnings("unchecked")
        Map<String, ?> proposalDataDetails2 = (Map<String, ?>) tokenScore.call("get_sponsor_projected_fund", testing_account.getAddress());
        assertEquals(proposalDataDetails2.get("withdraw_amount_bnUSD"), BigInteger.valueOf(2).multiply(MULTIPLIER));
    }

    private void sendRewardToSponsorMethod() {
        try (MockedStatic<Context> theMock = Mockito.mockStatic(Context.class)) {
            theMock.when(() -> Context.getCaller()).thenReturn(score_address);
            tokenScore.invoke(owner, "send_reward_to_sponsor", "Proposal 1");
        }
    }

    @Test
    void disqualifyProject(){
        depositProposalFundMethod();
        setCPFTreasuryScoreMethod();
        setCpsScoreMethod();
        setBnUSDScoreMethod();
        /* project budget -> 100, sponsor reward -> 2 --> total budget -> 102
         * upfront payment -> 10(contributor) and 0.2 to sponsor --> total upfront payment -> 10.2
         * after disqualify -> 102-10.2= 91.8
         * */
        try (MockedStatic<Context> theMock = Mockito.mockStatic(Context.class)) {
            theMock.when(() -> Context.getCaller()).thenReturn(score_address);
            tokenScore.invoke(owner, "disqualify_project", "Proposal 1");
            JsonObject disqualifyProjectParams = new JsonObject();
            disqualifyProjectParams.add("method", "disqualify_project");
            JsonObject params = new JsonObject();
            params.add("ipfs_key", "Proposal 1");
            disqualifyProjectParams.add("params", params);
            theMock.verify(() -> Context.call(bnUSDScore, "transfer", cpfTreasuryScore.getAddress(), BigInteger.valueOf(918).multiply(MULTIPLIER).divide(BigInteger.TEN), disqualifyProjectParams.toString().getBytes()), times(1));
        }
    }

    @Test
    void claimReward(){
        depositProposalFundMethod();
        sendRewardToSponsorMethod();
        setBnUSDScoreMethod();
        try(MockedStatic<Context> theMock = Mockito.mockStatic(Context.class)) {
            theMock.when(() -> Context.getCaller()).thenReturn(testing_account.getAddress());
            tokenScore.invoke(testing_account, "claim_reward");
            theMock.verify(
                    () -> Context.call(bnUSDScore, "transfer", testing_account.getAddress(), BigInteger.valueOf(11).multiply(MULTIPLIER).divide(BigInteger.TEN)), times(1));
        }
    }

    @Test
    void setOnsetPayment(){

        setCPFTreasuryScoreMethod();
        setCpsScoreMethod();
        doReturn(BigInteger.TEN).when(scoreSpy).callScore(eq(BigInteger.class),any(),eq("getSponsorBondPercentage"));
        tokenScore.invoke(cpfTreasuryScore, "setOnsetPayment", BigInteger.valueOf(10));

        BigInteger onSetPayment = (BigInteger) tokenScore.call("getOnsetPayment");
        assertEquals(onSetPayment,BigInteger.TEN);

    }



    @Test
    void onsetPaymentExceptions(){
        setCPFTreasuryScoreMethod();
        Executable setOnsetPaymentNotAdmin = () -> tokenScore.invoke(owner, "setOnsetPayment", BigInteger.valueOf(10));
        expectErrorMessage(setOnsetPaymentNotAdmin,
                "Reverted(0): CPS_TREASURY: Only receiving from  cx0000000000000000000000000000000000000005");


        setCpsScoreMethod();
        Executable setOnsetPaymentMoreThanMax = () -> tokenScore.invoke(cpfTreasuryScore, "setOnsetPayment", BigInteger.valueOf(30));
        expectErrorMessage(setOnsetPaymentMoreThanMax, "Reverted(0): CPS_TREASURY: Initial payment " +
                "cannot be greater than 20 percentage");

        doReturn(BigInteger.TEN).when(scoreSpy).callScore(eq(BigInteger.class),any(),eq("getSponsorBondPercentage"));

        Executable setOnsetPaymentMoreThanBond = () -> tokenScore.invoke(cpfTreasuryScore, "setOnsetPayment", BigInteger.valueOf(18));
        expectErrorMessage(setOnsetPaymentMoreThanBond, "Reverted(0): CPS_TREASURY: Payment cannot be " +
                "greater than sponsor bond percentage");
    }
}