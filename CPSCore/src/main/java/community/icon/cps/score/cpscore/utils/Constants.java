package community.icon.cps.score.cpscore.utils;

import score.Address;

import java.math.BigInteger;
import java.util.List;

public class Constants {
    public static final String TAG = "CPS Score";


    public static final BigInteger EXA = BigInteger.valueOf(1000000000000000000L);
    public static final Integer MINIMUM_PREPS = 7;

    public static final Integer MAX_PROJECT_PERIOD = 12;
    public static final double MAJORITY = 0.67;
    public static final BigInteger DAY_COUNT = BigInteger.valueOf(15);
    public static final BigInteger BLOCKS_DAY_COUNT = BigInteger.valueOf(43120);

    public static final String PROPOSAL_DB_PREFIX = "proposal";
    public static final String PROGRESS_REPORT_DB_PREFIX = "progressReport";

    public static final String APPLICATION_PERIOD = "Application Period";
    public static final String VOTING_PERIOD = "Voting Period";
    public static final String TRANSITION_PERIOD = "Transition Period";

    //                     Bond Status
    public static final String BOND_RECEIVED = "bond_received";
    public static final String BOND_APPROVED = "bond_approved";
    public static final String BOND_RETURNED = "bond_returned";
    public static final String BOND_CANCELLED = "bond_cancelled";

    //                   SCOREs Constants
    public static final String CPS_TREASURY_SCORE = "_cps_treasury_score";
    public static final String CPF_SCORE = "_cpf_score";
    public static final String BALANCED_DOLLAR = "balanced_dollar";

    //                      PERIOD CONSTANTS
    public static final String INITIAL_BLOCK = "initial_block";
    public static final String PERIOD_DETAILS = "_period_details";
    public static final String PERIOD_NAME = "period_name";
    public static final String PREVIOUS_PERIOD_NAME = "previous_period_name";
    public static final String PERIOD_SPAN = "period_span";
    public static final String LASTBLOCK = "last_block";
    public static final String CURRENTBLOCK = "current_block";
    public static final String NEXTBLOCK = "next_block";
    public static final String REMAINING_TIME = "remaining_time";
    public static final String UPDATE_PERIOD_INDEX = "update_period_index";

    //                    PREPS Related Constants
    public static final String MAIN_PREPS = "main_preps";
    public static final String ALL_PREPS = "_all_preps";
    public static final String ADMINS = "admins";
    public static final String UNREGISTERED_PREPS = "unregistered_preps";
    public static final String REGISTERED_PREPS = "registered_preps";
    public static final String INACTIVE_PREPS = "inactive_preps";
    public static final String PREP_NAME = "prep_name";

    public static final String ICX = "ICX";
    public static final String bnUSD = "bnUSD";

    public static final String MAINTENANCE = "maintenance";
    public static final String MESSAGE = "message";

    //VarDB/ArrayDB Params
    public static final String PROPOSALS_KEY_LIST = "proposals_key_list";
    public static final String PROPOSALS_KEY_LIST_INDEX = "proposals_key_list_index";
    public static final String PROGRESS_KEY_LIST = "progress_key_list";
    public static final String PROGRESS_KEY_LIST_INDEX = "progress_key_list_index";
    public static final String CONTRIBUTORS = "contributors";
    public static final String SPONSORS = "sponsors";
    public static final String BUDGET_APPROVALS_LIST = "budget_approvals_list";
    public static final String SPONSOR_ADDRESS = "sponsor_address";
    public static final String TOTAL_BUDGET = "total_budget";
    public static final String ACTIVE_PROPOSALS = "active_proposals";
    public static final String AMOUNT = "_total_amount";
    public static final String ADDRESS = "address";

    //    Proposals and Progress reports keys
    public static final String PROPOSAL = "proposal";
    public static final String PROGRESS_REPORTS = "progress_reports";
    public static final String NEW_PROGRESS_REPORT = "new_progress_report";
    public static final String PROJECT_TITLE = "project_title";
    public static final String PROGRESS_REPORT_TITLE = "progress_report_title";
    public static final String TOTAL_VOTES = "total_votes";
    public static final String TOTAL_VOTERS = "total_voters";
    public static final String REJECTED_VOTES = "rejected_votes";
    public static final String APPROVED_VOTES = "approved_votes";
    public static final String ABSTAINED_VOTES = "abstained_votes";
    public static final String REJECT_VOTERS = "reject_voters";
    public static final String APPROVE_VOTERS = "approve_voters";
    public static final String ABSTAIN_VOTERS = "abstain_voters";
    public static final String VOTERS_REASON = "voters_reasons";
    public static final String SUBMIT_PROGRESS_REPORT = "submit_progress_report";
    public static final String SPONSORED_TIMESTAMP = "sponsored_timestamp";
    public static final String SPONSOR_DEPOSIT_STATUS = "sponsor_deposit_status";
    public static final String SPONSOR_VOTE_REASON = "sponsor_vote_reason";
    public static final String VOTERS_LIST = "voters_list";
    public static final String VOTERS_LIST_INDEXES = "voters_list_indexes";
    public static final String BUDGET_VOTERS_LIST_INDICES = "budget_voters_list_indexes";
    public static final String MILESTONE_SUBMITTED_COUNT = "milestone_submitted_count";
    public static final String VOTE_CHANGE = "vote_change";
    public static final String COMPLETION_PERIOD = "completionPeriod";
    public static final String BUDGET = "budget";
    public static final String EXTENSION_FLAG = "extensionFlag";


    public static final String TIMESTAMP = "timestamp";
    public static final String TOKEN = "token";
    public static final String CONTRIBUTOR_ADDRESS = "contributor_address";
    public static final String TX_HASH = "tx_hash";
    public static final String IPFS_HASH = "ipfs_hash";
    public static final String REPORT_HASH = "report_hash";
    public static final String MILESTONE_ID= "milestoneID";
    public static final String PROJECT_DURATION = "project_duration";
    public static final String MILESTONE_COUNT = "milestoneCount";
    public static final String APPROVED_REPORTS = "approved_reports";
    public static final String IPFS_LINK = "ipfs_link";
    public static final String PERCENTAGE_COMPLETED = "percentage_completed";
    public static final String ADDITIONAL_BUDGET = "additional_budget";
    public static final String ADDITIONAL_DURATION = "additional_month";
    public static final String BUDGET_ADJUSTMENT = "budget_adjustment";
    public static final String IS_MILESTONE = "isMilestone";
    public static final String PROPOSAL_PERIOD = "proposalPeriod";
    public static final String BUDGETADJUSTMENT = "budgetAdjustment";
    public static final String BUDGET_ADJUSTMENT_STATUS = "budget_adjustment_status";
    public static final String BUDGET_APPROVED_VOTES = "budget_approved_votes";
    public static final String BUDGET_REJECTED_VOTES = "budget_rejected_votes";
    public static final String BUDGET_APPROVE_VOTERS = "budget_approve_voters";
    public static final String BUDGET_REJECT_VOTERS = "budget_reject_voters";
    public static final String SPONSOR_DEPOSIT_AMOUNT = "sponsor_deposit_amount";
    public static final String SPONSOR_DEPOSIT = "sponsor_deposit";
    public static final String PREPS_DENYLIST_STATUS = "preps_denylist_status";
    public static final String DENYLIST = "denylist";
    public static final String PENALTY_AMOUNT = "penalty_amount";
    public static final String STATUS = "status";
    public static final String DATA = "data";
    public static final String COUNT = "count";
    public static final String VOTING_PREP = "votingPRep";
    public static final String PAY_PENALTY = "payPenalty";
    public static final String IS_REGISTERED = "isRegistered";
    public static final String IS_PREP = "isPRep";
    public static final String PENALTY_AMOUNT1 = "penaltyAmount";
    public static final String TRANSFER = "transfer";
    public static final String METHOD = "method";

    public static final String DELEGATION_SNAPSHOT = "delegation_snapshot";
    public static final String MAX_DELEGATION = "max_delegation";
    public static final String TOTAL_DELEGATION_SNAPSHOT = "totalDelegationSnapshot";
    public static final String PROPOSAL_FEES = "proposal_fees";
    public static final String SWAP_BLOCK_HEIGHT = "swap_block_height";
    public static final String SWAP_COUNT = "swap_count";
    public static final String PERIOD_COUNT = "period_count";

    public static final String PROPOSAL_RANK = "proposal_rank";
    public static final String PRIORITY_VOTED_PREPS = "priority_voted_preps";
    public static final String BLOCKED_ADDRESSES = "blocked_addresses";
    public static final String SPONSOR_PROJECTS = "sponsor_projects";
    public static final String CONTRIBUTOR_PROJECTS = "contributor_projects";
    public static final String SPONSOR_BOND_PERCENTAGE = "sponsor_bond_percentage";
    public static final String PERIOD = "period";


    //    VOTE KEYS
    public static final String VOTE = "vote";
    public static final String INDEX = "index";
    public static final String CHANGE_VOTE = "change_vote";
    public static final String VOTE_REASON = "vote_reason";
    public static final String APPROVE = "_approve";
    public static final String REJECT = "_reject";
    public static final String ABSTAIN = "_abstain";
    public static final String ACCEPT = "_accept";

    public static final Integer APPROVE_ = 1;
    public static final Integer REJECT_ = 2;
    public static final Integer ABSTAIN_ = 3;

    public static final Integer VOTED = 1;
    public static final Integer NOT_VOTED = 0;

    //    Sponsor Fee
    public static final Integer APPLICATION_FEE = 50;
    public static final BigInteger SWAP_BLOCK_DIFF = BigInteger.valueOf(5);


    public static final String SPONSOR_PENDING = "_sponsor_pending";
    public static final String PENDING = "_pending";
    public static final String ACTIVE = "_active";
    public static final String PAUSED = "_paused";
    public static final String DISQUALIFIED = "_disqualified";
    public static final String REJECTED = "_rejected";
    public static final String COMPLETED = "_completed";
    public static final List<String> STATUS_TYPE = List.of(SPONSOR_PENDING, PENDING, ACTIVE, PAUSED, DISQUALIFIED, REJECTED, COMPLETED);

    public static final String WAITING = "_waiting";
    public static final String APPROVED = "_approved";
    public static final String PROGRESS_REPORT_REJECTED = "_progress_report_rejected";
    public static final List<String> PROGRESS_REPORT_STATUS_TYPE = List.of(APPROVED, WAITING, PROGRESS_REPORT_REJECTED);

    public static final String SPONSOR_BOND_RETURN = "sponsor_bond_return";

    public static final Address SYSTEM_ADDRESS = Address.fromString("cx0000000000000000000000000000000000000000");

    public static final Integer PENALTY_LEVELS = 3;

    //    migration
    public static final Integer MILESTONE_REPORT_COMPLETED = 1;
    public static final Integer MILESTONE_REPORT_REJECTED = 2;
    public static final Integer MILESTONE_REPORT_APPROVED = 3;
    public static final Integer MILESTONE_REPORT_NOT_COMPLETED = 4;

    //     new flags
    public static final String MAJORITY_FLAG = "majority_flag";
    public static final String COUNCIL_FLAG = "council_flag";


    public static final BigInteger TOTAL_PERIOD = BigInteger.valueOf(30);
}