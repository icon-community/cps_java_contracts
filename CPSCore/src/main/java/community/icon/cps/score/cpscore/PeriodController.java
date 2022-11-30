package community.icon.cps.score.cpscore;

import score.Context;
import score.VarDB;

import java.math.BigInteger;

import static community.icon.cps.score.cpscore.utils.Constants.*;

public class PeriodController {
    public final VarDB<BigInteger> initialBlock = Context.newVarDB(INITIAL_BLOCK, BigInteger.class);
    public final VarDB<String> periodName = Context.newVarDB(PERIOD_NAME, String.class);
    public final VarDB<String> previousPeriodName = Context.newVarDB(PREVIOUS_PERIOD_NAME, String.class);
    public final VarDB<BigInteger> nextBlock = Context.newVarDB(NEXTBLOCK, BigInteger.class);
    public final VarDB<Integer> updatePeriodIndex = Context.newVarDB(UPDATE_PERIOD_INDEX, Integer.class);
    public final VarDB<Integer> periodCount = Context.newVarDB(PERIOD_COUNT, Integer.class);
}
