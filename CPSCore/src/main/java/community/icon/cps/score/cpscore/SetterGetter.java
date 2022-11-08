package community.icon.cps.score.cpscore;

import score.Address;
import score.Context;
import score.VarDB;
import score.annotation.External;

import java.math.BigInteger;

//import static community.icon.cps.score.cpscore.utils.Checkers.validateAdminScore;
//import static community.icon.cps.score.cpscore.utils.Checkers.validateAdmins;
import static community.icon.cps.score.cpscore.utils.Constants.*;

public class SetterGetter {
    public final VarDB<Address> cpsTreasuryScore = Context.newVarDB(CPS_TREASURY_SCORE, Address.class);
    public final VarDB<Address> cpfScore = Context.newVarDB(CPF_SCORE, Address.class);
    public final VarDB<Address> balancedDollar = Context.newVarDB(BALANCED_DOLLAR, Address.class);
    public final VarDB<Boolean> budgetAdjustment = Context.newVarDB(BUDGETADJUSTMENT, Boolean.class);
    public final VarDB<Boolean> maintenance = Context.newVarDB(MAINTENANCE, Boolean.class);


}
