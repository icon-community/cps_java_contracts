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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static community.icon.cps.score.cpstreasury.CPSTreasury.*;
import static community.icon.cps.score.cpstreasury.utils.consts.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class CPSTreasuryTest extends TestBase {
    private static final Address score_address = Address.fromString("cx0000000000000000000000000000000000000000");
    private static final Address cpfTreasury = Address.fromString("cx0000000000000000000000000000000000000001");
    private static final Address bnUSDScore = Address.fromString("cx0000000000000000000000000000000000000002");

    private static final String name = "CPS_TREASURY";
    public static final String TAG = "CPS_TREASURY";
    private static final BigInteger MULTIPLIER = new BigInteger("1000000000000000000");


    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account testing_account = sm.createAccount();
    private static final Account testing_account2 = sm.createAccount();

    private Score tokenScore;

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
        doReturn(BigInteger.valueOf(15)).when(scoreSpy).callScore(eq(BigInteger.class),any(),eq("getSponsorBondPercentage"));
        BigInteger onSetPayment = BigInteger.valueOf(10);
        contextMock.when(caller()).thenReturn(cpfTreasury);
        tokenScore.invoke(owner,"setOnsetPayment",onSetPayment);
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
        /* totalBudget = 100, onsetPayment = 10% installmentCount = 2
         * remainingBudgetAfter onsetPayment = 90
         * installMentAmount = 50 and 40
         */
        setOnsetPayment();
        setCpsScoreMethod();

        depositProposalFundMethod();

        List<Map<String,?>> remainingMilestone = new ArrayList<>();
        remainingMilestone.add(Map.of(BUDGET,BigInteger.valueOf(50).multiply(ICX)));
        remainingMilestone.add(Map.of(BUDGET,BigInteger.valueOf(40).multiply(ICX)));
        doReturn(remainingMilestone).when(scoreSpy).callScore(eq(List.class),eq(score_address),eq("getRemainingMilestones"),any());
        @SuppressWarnings("unchecked")
        Map<String, ?> proposalDataDetails = (Map<String, ?>) tokenScore.call("getContributorProjectedFund", testing_account2.getAddress());
        @SuppressWarnings("unchecked")
        List<Map<String, ?>> proposalDetails = (List<Map<String, ?>>) proposalDataDetails.get("data");
        Map<String, ?> expectedData = Map.of(
                consts.IPFS_HASH, "Proposal 1",
                consts.TOKEN, "bnUSD",
                consts.TOTAL_BUDGET, BigInteger.valueOf(100).multiply(ICX),
                TOTAL_INSTALLMENT_PAID, BigInteger.TEN.multiply(ICX),
                TOTAL_INSTALLMENT_COUNT, 2,
                TOTAL_TIMES_INSTALLMENT_PAID, 0,
                consts.INSTALLMENT_AMOUNT, BigInteger.valueOf(50).multiply(ICX));
        assertEquals(proposalDetails.get(0), expectedData);

        assertEquals(proposalDataDetails.get(PROJECT_COUNT),1);
        assertEquals(proposalDataDetails.get(WITHDRAWN_BNUSD),BigInteger.TEN.multiply(ICX));

        Map<String,BigInteger> totalAmount = Map.of(consts.bnUSD,BigInteger.valueOf(50).multiply(ICX));
        assertEquals(proposalDataDetails.get(TOTAL_AMOUNT),totalAmount);

    }

    private void depositProposalFundMethod() {
        JsonObject depositProposal = new JsonObject();
        depositProposal.add("method", "depositProposalFund");
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
                    totalBudget = 100  installmentCount = 2 onsetPayment = 10
                    installmentAmount = (100-10)/2 = 45
           after budget adjustment :
                    totalBudget = 200 installCount = 3 onsetPayment = 10
                    each installmentAmount euqally divided = (200-10)/3 = 63.333
        * */
        setOnsetPayment();
        setCpsScoreMethod();
        depositProposalFundMethod();

        List<Map<String,?>> remainingMilestone = new ArrayList<>();
        remainingMilestone.add(Map.of(BUDGET,new BigInteger("63333333333333333333")));
        doReturn(remainingMilestone).when(scoreSpy).callScore(eq(List.class),eq(score_address),eq("getRemainingMilestones"),any());

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
                TOTAL_INSTALLMENT_PAID, BigInteger.TEN.multiply(ICX),
                TOTAL_INSTALLMENT_COUNT, 3,
                TOTAL_TIMES_INSTALLMENT_PAID, 0,
                consts.INSTALLMENT_AMOUNT, new BigInteger("63333333333333333333"));
        assertEquals(proposalDetails.get(0), expectedData);

        assertEquals(proposalDataDetails.get(PROJECT_COUNT),1);
        assertEquals(proposalDataDetails.get(WITHDRAWN_BNUSD),BigInteger.TEN.multiply(ICX));

        Map<String,BigInteger> totalAmount = Map.of(consts.bnUSD,new BigInteger("63333333333333333333"));
        assertEquals(proposalDataDetails.get(TOTAL_AMOUNT),totalAmount);
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

    private void  depositProposalFund_MilestoneCheck() {
        /* Sponsor reward = 2% intital payement = 10%
         * milestone count  = 4 */
        JsonObject depositProposal = new JsonObject();
        depositProposal.add("method", "depositProposalFund");
        JsonObject params = new JsonObject();
        params.add("ipfs_hash", "Proposal 1");
        params.add("project_duration", 4); // milestone count
        params.add("sponsor_address", testing_account.getAddress().toString());
        params.add("contributor_address", testing_account2.getAddress().toString());
        params.add("total_budget", BigInteger.valueOf(100).multiply(ICX).toString(16));
        params.add("sponsor_reward", BigInteger.valueOf(2).multiply(ICX).toString(16));
        params.add("token", "bnUSD");
        depositProposal.add("params", params);
        setCPFTreasuryScoreMethod();
        tokenScore.invoke(owner, "tokenFallback", cpfTreasury, BigInteger.valueOf(102).multiply(MULTIPLIER), depositProposal.toString().getBytes());
    }

    @Test
    void sendInstallmentToContributor() { // send first installment
        /* total budget = 100 intital payement = 10
         * each milestone payment =(100-10)/4 = 22.5 */

        setOnsetPayment();
        depositProposalFund_MilestoneCheck();
        setCpsScoreMethod();

        List<Map<String,?>> remainingMilestone = new ArrayList<>();
        remainingMilestone.add(Map.of(BUDGET,BigInteger.valueOf(225).multiply(ICX).divide(BigInteger.valueOf(10))));
        doReturn(remainingMilestone).when(scoreSpy).callScore(eq(List.class),eq(score_address),eq("getRemainingMilestones"),any());

        // proposal details after submission
        Map<String, ?> proposalDataDetails_before = (Map<String, ?>) tokenScore.call("getContributorProjectedFund", testing_account2.getAddress());
        assertEquals(proposalDataDetails_before.get("withdraw_amount_bnUSD"), BigInteger.valueOf(10).multiply(ICX));

        List<Map<String, ?>> proposalDetailsData = (List<Map<String, ?>>) proposalDataDetails_before.get("data");
        assertEquals(proposalDetailsData.get(0).get("total_installment_paid"), BigInteger.valueOf(10).multiply(ICX));
        assertEquals(proposalDetailsData.get(0).get("total_installment_count"), 4);
        assertEquals(proposalDetailsData.get(0).get("installment_amount"),  BigInteger.valueOf(225).multiply(ICX).divide(BigInteger.valueOf(10)));
        assertEquals(proposalDetailsData.get(0).get("total_times_installment_paid"), 0);

        contextMock.when(caller()).thenReturn(score_address);
        tokenScore.invoke(owner, "sendInstallmentToContributor", "Proposal 1", BigInteger.valueOf(225).multiply(ICX).divide(BigInteger.valueOf(10)));
        tokenScore.invoke(owner, "sendRewardToSponsor", "Proposal 1", 1);
        @SuppressWarnings("unchecked")
        Map<String, ?> proposalDataDetails_after = (Map<String, ?>) tokenScore.call("getContributorProjectedFund", testing_account2.getAddress());

        proposalDetailsData = (List<Map<String, ?>>) proposalDataDetails_after.get("data");
        assertEquals(proposalDetailsData.get(0).get("total_installment_paid"), BigInteger.valueOf(325).multiply(ICX).divide(BigInteger.valueOf(10)));
        assertEquals(proposalDetailsData.get(0).get("total_installment_count"), 4);
        assertEquals(proposalDetailsData.get(0).get("installment_amount"),  BigInteger.valueOf(225).multiply(ICX).divide(BigInteger.valueOf(10)));
        assertEquals(proposalDetailsData.get(0).get("total_times_installment_paid"), 1);


        assertEquals(proposalDataDetails_after.get(PROJECT_COUNT),1);
        assertEquals(proposalDataDetails_after.get(WITHDRAWN_BNUSD),BigInteger.valueOf(325).multiply(ICX).divide(BigInteger.valueOf(10)));

        Map<String,BigInteger> totalAmount = Map.of(consts.bnUSD,BigInteger.valueOf(225).multiply(ICX).divide(BigInteger.valueOf(10)));
        assertEquals(proposalDataDetails_after.get(TOTAL_AMOUNT),totalAmount);

    }


    @Test
    void sendInstallmentToContributor_SecondInstallment() { // send second installment
        sendInstallmentToContributor();

        tokenScore.invoke(owner, "sendInstallmentToContributor", "Proposal 1", BigInteger.valueOf(225).multiply(ICX).divide(BigInteger.valueOf(10)));
        tokenScore.invoke(owner, "sendRewardToSponsor", "Proposal 1", 1);
        @SuppressWarnings("unchecked")
        Map<String, ?> proposalDataDetails_after = (Map<String, ?>) tokenScore.call("getContributorProjectedFund", testing_account2.getAddress());

        List<Map<String, ?>> proposalDetailsData = (List<Map<String, ?>>) proposalDataDetails_after.get("data");
        assertEquals(proposalDetailsData.get(0).get("total_installment_paid"), BigInteger.valueOf(55).multiply(ICX));
        assertEquals(proposalDetailsData.get(0).get("total_installment_count"), 4);
        assertEquals(proposalDetailsData.get(0).get("installment_amount"),  BigInteger.valueOf(225).multiply(ICX).divide(BigInteger.valueOf(10)));
        assertEquals(proposalDetailsData.get(0).get("total_times_installment_paid"), 2);


        assertEquals(proposalDataDetails_after.get(PROJECT_COUNT),1);
        assertEquals(proposalDataDetails_after.get(WITHDRAWN_BNUSD),BigInteger.valueOf(55).multiply(ICX));

        Map<String,BigInteger> totalAmount = Map.of(consts.bnUSD,BigInteger.valueOf(225).multiply(ICX).divide(BigInteger.valueOf(10)));
        assertEquals(proposalDataDetails_after.get(TOTAL_AMOUNT),totalAmount);

    }

    @Test
    void sendInstallmentToContributor_Completed(){
        // total Milestone = 4, remaining milestone = 2
        sendInstallmentToContributor_SecondInstallment();

        tokenScore.invoke(owner, "sendInstallmentToContributor", "Proposal 1", BigInteger.valueOf(45).multiply(ICX));
        tokenScore.invoke(owner, "sendRewardToSponsor", "Proposal 1", 2);
        @SuppressWarnings("unchecked")
        Map<String, ?> proposalDataDetails_after = (Map<String, ?>) tokenScore.call("getContributorProjectedFund", testing_account2.getAddress());
        assertEquals(proposalDataDetails_after.get("withdraw_amount_bnUSD"), BigInteger.valueOf(100).multiply(ICX));


        List<Map<String, ?>> proposalDetailsData = (List<Map<String, ?>>) proposalDataDetails_after.get("data");
        assertEquals(proposalDetailsData.size(),0);

        assertEquals(proposalDataDetails_after.get(PROJECT_COUNT),0);
        assertEquals(proposalDataDetails_after.get(WITHDRAWN_BNUSD),BigInteger.valueOf(100).multiply(ICX));

        Map<String,BigInteger> totalAmount = Map.of(consts.bnUSD,BigInteger.ZERO);
        assertEquals(proposalDataDetails_after.get(TOTAL_AMOUNT),totalAmount);

    }

    @Test
    void lastReportWithIncorrectMilestone(){
        /* total Milestone = 4, remaining milestone = 2
        * each milestone = 22.5  , for 2 milestone = 45 */
        sendInstallmentToContributor_SecondInstallment();

        Map<String, ?> proposalDataDetails = (Map<String, ?>) tokenScore.call("getContributorProjectedFund", testing_account2.getAddress());
        System.out.println("proposal details "+ proposalDataDetails);
        assertEquals(proposalDataDetails.get("withdraw_amount_bnUSD"), BigInteger.valueOf(55).multiply(ICX));

        List<Map<String, ?>> proposalDetailsData = (List<Map<String, ?>>) proposalDataDetails.get("data");
        assertEquals(proposalDetailsData.get(0).get("total_budget"), BigInteger.valueOf(100).multiply(ICX));

        BigInteger remainingAmount = BigInteger.valueOf(100).multiply(ICX).subtract(BigInteger.valueOf(55).multiply(ICX));
        assertEquals(remainingAmount,BigInteger.valueOf(45).multiply(ICX));

        // requesting more than 45ICX
        tokenScore.invoke(owner, "sendInstallmentToContributor", "Proposal 1", BigInteger.valueOf(50).multiply(ICX));
        tokenScore.invoke(owner, "sendRewardToSponsor", "Proposal 1", 2);
        @SuppressWarnings("unchecked")
        Map<String, ?> proposalDataDetails_after = (Map<String, ?>) tokenScore.call("getContributorProjectedFund", testing_account2.getAddress());
        assertEquals(proposalDataDetails_after.get("withdraw_amount_bnUSD"), BigInteger.valueOf(100).multiply(ICX));


        proposalDetailsData = (List<Map<String, ?>>) proposalDataDetails_after.get("data");
        assertEquals(proposalDetailsData.size(),0);

        assertEquals(proposalDataDetails_after.get(PROJECT_COUNT),0);
        // at the completion of proposal the withdrawn amount is still 100ICX
        assertEquals(proposalDataDetails_after.get(WITHDRAWN_BNUSD),BigInteger.valueOf(100).multiply(ICX));

        Map<String,BigInteger> totalAmount = Map.of(consts.bnUSD,BigInteger.ZERO);
        assertEquals(proposalDataDetails_after.get(TOTAL_AMOUNT),totalAmount);
    }

    @Test
    void sendRewardToSponsor() {
        setOnsetPayment();
        depositProposalFund_MilestoneCheck();
        setCpsScoreMethod();
        doReturn(BigInteger.valueOf(15)).when(scoreSpy).callScore(BigInteger.class, score_address, "getSponsorBondPercentage");
        @SuppressWarnings("unchecked")
        Map<String, ?> proposalDataDetails = (Map<String, ?>) tokenScore.call("getSponsorProjectedFund", testing_account.getAddress());
        assertEquals(proposalDataDetails.get(WITHDRAWN_BNUSD), BigInteger.valueOf(2).multiply(ICX).divide(BigInteger.TEN));
        assertEquals(proposalDataDetails.get(PROJECT_COUNT), 1);
        assertEquals(proposalDataDetails.get(TOTAL_SPONSOR_BOND), Map.of(consts.bnUSD,BigInteger.valueOf(15).multiply(ICX)));
        assertEquals(proposalDataDetails.get(TOTAL_AMOUNT), Map.of(consts.bnUSD,BigInteger.valueOf(45).multiply(ICX).divide(BigInteger.valueOf(100))));

        sendRewardToSponsorMethod();
        Map<String, ?> proposalDataDetails2 = (Map<String, ?>) tokenScore.call("getSponsorProjectedFund", testing_account.getAddress());
        assertEquals(proposalDataDetails2.get(WITHDRAWN_BNUSD), BigInteger.valueOf(65).multiply(ICX).divide(BigInteger.valueOf(100)));


        List<Map<String, ?>> proposalDetailsData = (List<Map<String, ?>>) proposalDataDetails2.get("data");
        assertEquals(proposalDetailsData.get(0).get(TOTAL_BUDGET), BigInteger.valueOf(2).multiply(ICX));
        assertEquals(proposalDetailsData.get(0).get(TOTAL_INSTALLMENT_PAID), BigInteger.valueOf(65).multiply(ICX).divide(BigInteger.valueOf(100)));
        assertEquals(proposalDetailsData.get(0).get(TOTAL_INSTALLMENT_COUNT), 4);
        assertEquals(proposalDetailsData.get(0).get(TOTAL_TIMES_INSTALLMENT_PAID), 1);
        assertEquals(proposalDetailsData.get(0).get(INSTALLMENT_AMOUNT),  BigInteger.valueOf(45).multiply(ICX).divide(BigInteger.valueOf(100)));
        assertEquals(proposalDetailsData.get(0).get(SPONSOR_BOND_AMOUNT),  BigInteger.valueOf(15).multiply(ICX));


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
         * on proposal submission : contributor_reward = 10 and sponsor_reward = 0.2
         * remainning budget = 102 -10-0.2 = 91.8
         *  */
        BigInteger remainingBudget = BigInteger.valueOf(918).multiply(ICX).divide(BigInteger.valueOf(10));
        doNothing().when(scoreSpy).callScore(eq(bnUSDScore), eq("transfer"), eq(cpfTreasury),
                eq(remainingBudget),any());
        contextMock.when(caller()).thenReturn(score_address);
        tokenScore.invoke(owner, "disqualifyProject", "Proposal 1");
        JsonObject disqualifyProjectParams = new JsonObject();
        disqualifyProjectParams.add("method", "disqualifyProject");
        JsonObject params = new JsonObject();
        params.add("ipfs_key", "Proposal 1");
        disqualifyProjectParams.add("params", params);

    }

    @Test
    void claimReward(){
        /* total budget = 100 sponsor reward = 2
         * initial payment = 10%
         * 10 % of 2 = 0.2 */
        setScoresMethod();
        setCpsScoreMethod();
        depositProposalFundMethod();
        setBnUSDScoreMethod();
        BigInteger reward = BigInteger.valueOf(2).multiply(MULTIPLIER).divide(BigInteger.valueOf(10));
        doReturn(false).when(scoreSpy).callScore(eq(Boolean.class),any(), eq("getMaintenanceMode"));
        doReturn(List.of()).when(scoreSpy).callScore(List.class, score_address, "getBlockedAddresses");
        doNothing().when(scoreSpy).callScore(eq(bnUSDScore),eq("transfer"),eq(testing_account.getAddress()),eq(reward));
        contextMock.when(caller()).thenReturn(testing_account.getAddress());

        // claimed by sponsor
        tokenScore.invoke(testing_account, "claimReward");


    }
}