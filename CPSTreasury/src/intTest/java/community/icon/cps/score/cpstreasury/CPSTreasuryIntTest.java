package community.icon.cps.score.cpstreasury;

import com.eclipsesource.json.JsonObject;
import community.icon.cps.score.lib.interfaces.*;

import community.icon.cps.score.test.integration.CPS;
import foundation.icon.icx.KeyWallet;
import foundation.icon.icx.Wallet;
import static org.junit.jupiter.api.Assertions.assertEquals;

import foundation.icon.score.client.DefaultScoreClient;
import org.junit.jupiter.api.*;
import score.Address;

import java.math.BigInteger;
import java.util.Map;

import static community.icon.cps.score.lib.interfaces.CPSCoreInterface.ProposalAttributes;
import static community.icon.cps.score.lib.interfaces.CPSCoreInterface.ProgressReportAttributes;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CPSTreasuryIntTest {
    private static CPS cps;
    private final KeyWallet tester = cps.user;
    private final KeyWallet tester2 = cps.testUser;

    private final KeyWallet prepWallet1 = cps.prepWallet1;
    private final KeyWallet prepWallet2 = cps.prepWallet2;
    private final KeyWallet prepWallet3 = cps.prepWallet3;
    private final KeyWallet prepWallet4 = cps.prepWallet4;
    private final KeyWallet prepWallet5 = cps.prepWallet5;
    private final KeyWallet prepWallet6 = cps.prepWallet6;
    private final KeyWallet prepWallet7 = cps.prepWallet7;
    public static final BigInteger EXA = BigInteger.valueOf(1_000_000_000_000_000_000L);

    private static Wallet owner;

    private static CPSTreasuryInterfaceScoreClient cpsTreasury;
    private static CPFTreasuryInterfaceScoreClient cpfTreasury;
    private static CPSCoreInterfaceScoreClient cpsMain;

    private static DexInterfaceScoreClient dex;
    private static RouterInterfaceScoreClient router;
    private static bnUSDInterfaceScoreClient bnusd;
    private static sICXInterfaceScoreClient sicx;

    DefaultScoreClient clientWithTester = new DefaultScoreClient(cps.cpsCore.endpoint(), cps.cpsCore._nid()
            , tester, cps.cpsCore._address());

    DefaultScoreClient clientWithTester2 = new DefaultScoreClient(cps.cpsCore.endpoint(), cps.cpsCore._nid()
            , tester2, cps.cpsCore._address());

    DefaultScoreClient prepClient1 = new DefaultScoreClient(cps.cpsCore.endpoint(), cps.cpsCore._nid()
            , prepWallet1, cps.cpsCore._address());

    DefaultScoreClient bnUSDClient2 = new DefaultScoreClient(cps.bnusd.endpoint(), cps.bnusd._nid()
            , prepWallet2, cps.bnusd._address());


    DefaultScoreClient prepClient2 = new DefaultScoreClient(cps.cpsCore.endpoint(), cps.cpsCore._nid()
            , prepWallet2, cps.cpsCore._address());
    DefaultScoreClient prepClient3 = new DefaultScoreClient(cps.cpsCore.endpoint(), cps.cpsCore._nid()
            , prepWallet3, cps.cpsCore._address());
    DefaultScoreClient prepClient4 = new DefaultScoreClient(cps.cpsCore.endpoint(), cps.cpsCore._nid()
            , prepWallet4, cps.cpsCore._address());
    DefaultScoreClient prepClient5 = new DefaultScoreClient(cps.cpsCore.endpoint(), cps.cpsCore._nid()
            , prepWallet5, cps.cpsCore._address());
    DefaultScoreClient prepClient6 = new DefaultScoreClient(cps.cpsCore.endpoint(), cps.cpsCore._nid()
            , prepWallet6, cps.cpsCore._address());
    DefaultScoreClient prepClient7 = new DefaultScoreClient(cps.cpsCore.endpoint(), cps.cpsCore._nid()
            , prepWallet7, cps.cpsCore._address());

    @BeforeAll
    static void setup() throws Exception {
        cps = new CPS();
        cps.setupCPS();
        owner = cps.owner;
        cpsMain = new CPSCoreInterfaceScoreClient(cps.cpsCore);
        cpsTreasury = new CPSTreasuryInterfaceScoreClient(cps.cpsTreasury);
        cpfTreasury = new CPFTreasuryInterfaceScoreClient(cps.cpfTreasury);

        dex = new DexInterfaceScoreClient(cps.dex);
        bnusd = new bnUSDInterfaceScoreClient(cps.bnusd);
        sicx = new sICXInterfaceScoreClient(cps.sicx);
        router = new RouterInterfaceScoreClient(cps.router);

    }

    CPSCoreInterfaceScoreClient cpsMainTestClient1 = new CPSCoreInterfaceScoreClient(clientWithTester);
    CPSCoreInterfaceScoreClient cpsMainTestClient2 = new CPSCoreInterfaceScoreClient(clientWithTester2);
    CPSCoreInterfaceScoreClient prepSender1 = new CPSCoreInterfaceScoreClient(prepClient1);
    bnUSDInterfaceScoreClient bnUSDSender2 = new bnUSDInterfaceScoreClient(bnUSDClient2);
    CPSCoreInterfaceScoreClient prepSender2 = new CPSCoreInterfaceScoreClient(prepClient2);
    CPSCoreInterfaceScoreClient prepSender3 = new CPSCoreInterfaceScoreClient(prepClient3);
    CPSCoreInterfaceScoreClient prepSender4 = new CPSCoreInterfaceScoreClient(prepClient4);
    CPSCoreInterfaceScoreClient prepSender5 = new CPSCoreInterfaceScoreClient(prepClient5);
    CPSCoreInterfaceScoreClient prepSender6 = new CPSCoreInterfaceScoreClient(prepClient6);
    CPSCoreInterfaceScoreClient prepSender7 = new CPSCoreInterfaceScoreClient(prepClient7);

    @Test
    @Order(1)
    void name(){
        assertEquals(cpsTreasury.name(), "CPS_TREASURY");
        assertEquals(cpfTreasury.name(), "CPF_TREASURY");
        assertEquals(cpsMain.name(), "CPS Score");
    }

    void addAdminMethod(){
        cpsMain.add_admin(Address.fromString(tester.getAddress().toString()));
        cpsMain.add_admin(Address.fromString(owner.getAddress().toString()));
    }

    private void setInitialBlockMethod(){
        cpsMain.set_initialBlock();
    }

    void setScores(){
        addAdminMethod();
        cpsMain.set_cps_treasury_score(cpsTreasury._address());
        assertEquals(cpsMain.getCpsTreasuryScore(), cpsTreasury._address());
        cpsMain.set_cpf_treasury_score(cpfTreasury._address());
        assertEquals(cpsMain.get_cpf_treasury_score(), cpfTreasury._address());
        cpsMain.set_bnUSD_score(bnusd._address());
        assertEquals(cpsMain.get_bnUSD_score(), bnusd._address());

        cpsTreasury.setCpsScore(cpsMain._address());
        assertEquals(cpsTreasury.getCpsScore(), cpsMain._address());
        cpsTreasury.setCpfTreasuryScore(cpfTreasury._address());
        assertEquals(cpsTreasury.getCpfTreasuryScore(), cpfTreasury._address());
        cpsTreasury.setBnUSDScore(bnusd._address());
        assertEquals(cpsTreasury.getBnUSDScore(), bnusd._address());

        cpfTreasury.setCpsScore(cpsMain._address());
        assertEquals(cpfTreasury.getCpsScore(), cpsMain._address());
        cpfTreasury.setCpsTreasuryScore(cpsTreasury._address());
        assertEquals(cpfTreasury.getCpsTreasuryScore(), cpsTreasury._address());
        cpfTreasury.setBnUSDScore(bnusd._address());
        assertEquals(cpfTreasury.getBnUSDScore(), bnusd._address());
        cpfTreasury.setRouterScore(router._address());
        assertEquals(cpfTreasury.getRouterScore(), router._address());
        cpfTreasury.setDexScore(dex._address());
        assertEquals(cpfTreasury.getDexScore(), dex._address());
        cpfTreasury.setSicxScore(sicx._address());
        assertEquals(cpfTreasury.getSicxScore(), sicx._address());

        dex.setSicxScore(sicx._address());
    }

    void setCPFTreasuryContract(){
        setScores();
        cpfTreasury.setMaximumTreasuryFundBnusd(BigInteger.valueOf(1000).multiply(EXA));
        cpfTreasury.setMaximumTreasuryFundIcx(BigInteger.valueOf(1000).multiply(EXA));

        cpfTreasury.add_fund(BigInteger.valueOf(1000).multiply(EXA));
        System.out.println(cpfTreasury.get_remaining_swap_amount());

        setRouterScore();
        setDexScore();
        cpfTreasury.swapIcxBnusd(BigInteger.valueOf(200).multiply(EXA));
        System.out.println(bnusd.balanceOf(cpfTreasury._address()));
    }

    void setRouterScore(){
//        setScores();
        bnusd.transfer(router._address(), BigInteger.valueOf(100000).multiply(EXA), null);
        bnusd.transfer(Address.fromString(prepWallet2.getAddress().toString()), BigInteger.valueOf(1000).multiply(EXA), null);
        System.out.println(bnusd.balanceOf(router._address()));
    }

    void setDexScore(){
//        setScores();
        sicx.transfer(dex._address(), BigInteger.valueOf(100000).multiply(EXA), null);
        System.out.println(sicx.balanceOf(dex._address()));
    }

    private void registerPrepMethod(){
        setInitialBlockMethod();
        cpsMain.toggle_maintenance();
        prepSender1.register_prep();
        prepSender2.register_prep();
        prepSender3.register_prep();
        prepSender4.register_prep();
        prepSender5.register_prep();
        prepSender6.register_prep();
        prepSender7.register_prep();
    }

    @Test
    @Order(2)
    void submitProposal(){
        setCPFTreasuryContract();
        registerPrepMethod();
        ProposalAttributes proposalAttributes = new ProposalAttributes();
        proposalAttributes.ipfs_hash = "Proposal 1";
        proposalAttributes.project_title = "Proposal 1 title";
        proposalAttributes.ipfs_link = "Link";
        proposalAttributes.project_duration = 3;
        proposalAttributes.token = "bnUSD";
        proposalAttributes.sponsor_address = Address.fromString(prepWallet2.getAddress().toString());
        proposalAttributes.total_budget = BigInteger.valueOf(100);

        cpsMain.submit_proposal(BigInteger.valueOf(50).multiply(EXA), proposalAttributes);

        Map<String, Object> proposalDetails = cpsMain.get_proposal_details_by_hash("Proposal 1");
        assertEquals(proposalDetails.get("ipfs_hash"), "Proposal 1");
        assertEquals(proposalDetails.get("project_duration"), "0x" + Integer.toHexString(3));
        assertEquals(proposalDetails.get("project_title"), "Proposal 1 title");
        assertEquals(proposalDetails.get("status"), "_sponsor_pending");
        assertEquals(proposalDetails.get("token"), "bnUSD");
        String totalBudgetString = ((String) proposalDetails.get("total_budget")).substring(2);
        assertEquals(new BigInteger(totalBudgetString, 16), BigInteger.valueOf(100).multiply(EXA));
    }

    @Test
    @Order(3)
    void voteProposal(){
        JsonObject sponsorVoteParams = new JsonObject();
        sponsorVoteParams.add("method", "sponsor_vote");
        JsonObject params = new JsonObject();
        params.add("ipfs_hash", "Proposal 1");
        params.add("vote", "_accept");
        params.add("vote_reason", "sponsor_reason");
        sponsorVoteParams.add("params", params);
        logger("sponsor vote start");
        bnUSDSender2.transfer(cpsMain._address(), BigInteger.valueOf(10).multiply(EXA), sponsorVoteParams.toString().getBytes());
        logger("sponosr vote complete");

        cpsMain.update_next_block(0);
        logger("period ended");

        cpsMain.update_period();
        logger("updated to next period");

        prepSender1.vote_proposal("Proposal 1", "_approve", "reason", false);
        prepSender2.vote_proposal("Proposal 1", "_approve", "reason", false);
        prepSender3.vote_proposal("Proposal 1", "_approve", "reason", false);
        prepSender4.vote_proposal("Proposal 1", "_approve", "reason", false);
        prepSender5.vote_proposal("Proposal 1", "_approve", "reason", false);
        prepSender6.vote_proposal("Proposal 1", "_reject", "reason", false);
        prepSender7.vote_proposal("Proposal 1", "_reject", "reason", false);

        logger("completed vote");

        Map<String, Object> proposalDetails = cpsMain.get_proposal_details_by_hash("Proposal 1");
        logger(proposalDetails);

        assertEquals("0x" + Integer.toHexString(5), proposalDetails.get("approve_voters"));
        assertEquals("0x" + Integer.toHexString(2), proposalDetails.get("reject_voters"));
        assertEquals("0x" + Integer.toHexString(7), proposalDetails.get("total_voters"));
        assertEquals(BigInteger.valueOf(4000).multiply(EXA), new BigInteger(((String) proposalDetails.get("approved_votes")).substring(2), 16));
        assertEquals(BigInteger.valueOf(1600).multiply(EXA), new BigInteger(((String) proposalDetails.get("rejected_votes")).substring(2), 16));
        assertEquals(BigInteger.valueOf(5600).multiply(EXA), new BigInteger(((String) proposalDetails.get("total_votes")).substring(2), 16));
    }

    @Test
    @Order(4)
    void votePriority(){
        String[] priorityVote = {"Proposal 1"};
        logger("priority voting started");
        prepSender1.votePriority(priorityVote);
        prepSender2.votePriority(priorityVote);
        prepSender3.votePriority(priorityVote);
        prepSender4.votePriority(priorityVote);
        prepSender5.votePriority(priorityVote);
        prepSender6.votePriority(priorityVote);
        prepSender7.votePriority(priorityVote);
        logger("priority vote complete");
        Map<String, Integer> priorityVoteResult = cpsMain.getPriorityVoteResult();
        logger(priorityVoteResult);
        assertEquals(7, priorityVoteResult.get("Proposal 1"));
    }

    @Test
    @Order(5)
    void updatePeriodAfterVoting(){
        cpsMain.update_next_block(0);
        logger("end period");

        cpsMain.update_period();
        logger("end first update period period");

        cpsMain.update_period();
        logger("end second update period period");

        cpsMain.update_period();
        logger("end third update period period");

        cpsMain.update_period();
        logger("end fourth update period period");

        Map<String, Object> proposalDetails = cpsMain.get_proposal_details_by_hash("Proposal 1");
        logger(proposalDetails);

        assertEquals("bond_approved", proposalDetails.get("sponsor_deposit_status"));
        assertEquals("_active", proposalDetails.get("status"));
    }

    @Test
    @Order(6)
    void submitProgressReport(){
        ProgressReportAttributes progress = new ProgressReportAttributes();
        progress.ipfs_hash = "Proposal 1";
        progress.report_hash = "Report 1";
        progress.progress_report_title = "Progress Report Title";
        progress.ipfs_link = "Link";
        progress.additional_budget = BigInteger.valueOf(50);
        progress.budget_adjustment = Boolean.TRUE;
        progress.percentage_completed = 10;
        progress.additional_month = 2;
        cpsMain.toggleBudgetAdjustmentFeature();
        cpsMain.submit_progress_report(progress);

        Map<String, Object> progressReports = cpsMain.get_progress_reports_by_hash("Report 1");
        logger(progressReports);

        assertEquals("Report 1", progressReports.get("report_hash"));
        assertEquals("_waiting", progressReports.get("status"));
        assertEquals("Progress Report Title", progressReports.get("progress_report_title"));
        assertEquals("Proposal 1", progressReports.get("ipfs_hash"));
        assertEquals("_pending", progressReports.get("budget_adjustment_status"));
        assertEquals("0x" + Integer.toHexString(2), progressReports.get("additional_month"));
        assertEquals(BigInteger.valueOf(50).multiply(EXA), new BigInteger(((String)progressReports.get("additional_budget")).substring(2), 16));
    }

    @Test
    @Order(7)
    void vote_progress_report(){
        cpsMain.update_next_block(0);
        logger("period ended");

        logger(cpsMain.get_PReps());

        cpsMain.update_period();
        logger("updated to next period");

        logger(cpsMain.get_period_status());

        prepSender1.vote_progress_report("Proposal 1", "Report 1", "_approve", "vote reason", "_approve", false);
        prepSender2.vote_progress_report("Proposal 1", "Report 1", "_approve", "vote reason", "_approve", false);
        prepSender3.vote_progress_report("Proposal 1", "Report 1", "_approve", "vote reason", "_approve", false);
        prepSender4.vote_progress_report("Proposal 1", "Report 1", "_approve", "vote reason", "_approve", false);
        prepSender5.vote_progress_report("Proposal 1", "Report 1", "_reject", "vote reason", "_reject", false);
        prepSender6.vote_progress_report("Proposal 1", "Report 1", "_reject", "vote reason", "_reject", false);
        prepSender7.vote_progress_report("Proposal 1", "Report 1", "_approve", "vote reason", "_approve", false);

        logger("completed vote");

        Map<String, Object> progressReports = cpsMain.get_progress_reports_by_hash("Report 1");
        logger(progressReports);

        assertEquals(BigInteger.valueOf(5600).multiply(EXA), new BigInteger(((String)progressReports.get("total_votes")).substring(2), 16));
        assertEquals(BigInteger.valueOf(4000).multiply(EXA), new BigInteger(((String)progressReports.get("approved_votes")).substring(2), 16));
        assertEquals(BigInteger.valueOf(1600).multiply(EXA), new BigInteger(((String)progressReports.get("rejected_votes")).substring(2), 16));
        assertEquals(BigInteger.valueOf(4000).multiply(EXA), new BigInteger(((String)progressReports.get("budget_approved_votes")).substring(2), 16));
        assertEquals(BigInteger.valueOf(1600).multiply(EXA), new BigInteger(((String)progressReports.get("budget_rejected_votes")).substring(2), 16));
        assertEquals("0x" + Integer.toHexString(7), progressReports.get("total_voters"));
        assertEquals("0x" + Integer.toHexString(5), progressReports.get("approve_voters"));
        assertEquals("0x" + Integer.toHexString(2), progressReports.get("reject_voters"));
        assertEquals("0x" + Integer.toHexString(5), progressReports.get("budget_approve_voters"));
        assertEquals("0x" + Integer.toHexString(2), progressReports.get("budget_reject_voters"));
    }

    @Test
    @Order(8)
    void update_period_after_voting_progress_reports(){
        cpsMain.update_next_block(0);
        logger("end period");

        cpsMain.update_period();
        logger("end first update period period");

        cpsMain.update_period();
        logger("end second update period period");

        cpsMain.update_period();
        logger("end third update period period");

        cpsMain.update_period();
        logger("end fourth update period period");

        Map<String, Object> proposalDetails = cpsMain.get_proposal_details_by_hash("Proposal 1");
        logger(proposalDetails);

        Map<String, Object> progressDetails = cpsMain.get_progress_reports_by_hash("Report 1");
        logger(progressDetails);
    }
    <T> void logger(T log){
        System.out.println(log);
    }


}
