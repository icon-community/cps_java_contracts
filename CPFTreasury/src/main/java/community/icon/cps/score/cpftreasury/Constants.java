package community.icon.cps.score.cpftreasury;

import score.Address;

import java.math.BigInteger;

public class Constants {
    public static final BigInteger EXA = BigInteger.valueOf(1_000_000_000_000_000_000L);
    public static final String TAG = "CPF_TREASURY";
    public static final String ICX = "ICX";
    public static final String bnUSD = "bnUSD";
    public static final String METHOD = "method";
    public static final String PARAMS = "params";
    public static final String TRANSFER = "transfer";

    public static final String PROPOSAL_BUDGETS = "_proposals_budgets";
    public static final String PROPOSALS_KEYS = "_proposals_keys";
    public static final String TREASURY_FUND = "treasury_fund";
    public static final String EMERGENCY_FUND = "emergencyFund";
    public static final String REWARD_POOL = "rewardPool";

    public static final String INITIAL_FUND = "initialFund";
    public static final String FINAL_FUND = "finalFund";

    public static final String AVAILABLE_BALANCE = "availableBalance";
    public static final String TREASURY_FUND_BNUSD = "treasury_fund_bnusd";

    public static final String CPS_TREASURY_SCORE = "_cps_treasury_score";
    public static final String CPS_SCORE = "_cps_score";
    public static final String IPFS_HASH = "_ipfs_hash";
    public static final String TOTAL_BUDGET = "_budget_transfer";
    public static final String BALANCED_DOLLAR = "balanced_dollar";
    public static final String DEX_SCORE = "dex_score";
    public static final String SICX_SCORE = "sicx_score";
    public static final String ROUTER_SCORE = "router_score";
    public static final String ORACLE_ADDRESS = "oracle_address";
    public static final String BNUSD_BALANCE = "bnusdBalance";
    public static final String SWAP_STATE = "swap_state";
    public static final String SWAP_COUNT = "swap_count";
    public static final String SWAP_LIMIT_AMOUNT = "swap_limit_amount";
    public static final String ORACLE_PERCENTAGE_DIFF = "oracle_percentage_diff";
    public static final String SWAP_FLAG = "swap_flag";
    public static final String MAX_CAP = "maxCap";
    public static final String REMAINING_TO_SWAP = "remainingToSwap";
    public static final String PROJECT_IPFS_HASH = "ipfs_hash";
    public static final String PROJECT_DURATION = "project_duration";
    public static final String SPONSOR_ADDRESS = "sponsor_address";
    public static final String CONTRIBUTOR_ADDRESS = "contributor_address";
    public static final String PROJECT_TOTAL_BUDGET = "total_budget";
    public static final String SPONSOR_REWARD = "sponsor_reward";
    public static final String TOKEN = "token";
    public static final String COUNT = "count";
    public static final String DATA = "data";
    public static final String STATE = "state";
    public static final Address SYSTEM_ADDRESS = Address.fromString("cx0000000000000000000000000000000000000000");

    public static final String COUNCIL_FLAG = "council_flag";
    public static final String COUNCIL_MANAGERS = "council_managers";
    public static final String COUNCIL_MANAGERS_REWARD = "councilManagersReward";
    public static final String COUNCIL_MANAGERS_REWARD_PERCENTAGE = "councilManagersRewardPercentage";

    public static final int sICXICXPoolID = 1;
    public static final int sICXBNUSDPoolID = 2;

    public static final int SwapContinue = 0;
    public static final int SwapCompleted = 1;
    public static final int SwapReset = 0;

}
