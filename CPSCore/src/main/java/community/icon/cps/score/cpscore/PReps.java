package community.icon.cps.score.cpscore;

import score.Address;
import score.ArrayDB;
import score.Context;
import score.DictDB;

import java.math.BigInteger;

import static community.icon.cps.score.cpscore.utils.Constants.DENYLIST;
import static community.icon.cps.score.cpscore.utils.Constants.INACTIVE_PREPS;
import static community.icon.cps.score.cpscore.utils.Constants.MAIN_PREPS;
import static community.icon.cps.score.cpscore.utils.Constants.PENALTY_AMOUNT;
import static community.icon.cps.score.cpscore.utils.Constants.PREPS_DENYLIST_STATUS;
import static community.icon.cps.score.cpscore.utils.Constants.REGISTERED_PREPS;
import static community.icon.cps.score.cpscore.utils.Constants.UNREGISTERED_PREPS;

public class PReps {
    public final ArrayDB<Address> validPreps = Context.newArrayDB(MAIN_PREPS, Address.class);
    public final ArrayDB<Address> unregisteredPreps = Context.newArrayDB(UNREGISTERED_PREPS, Address.class);
    public final ArrayDB<Address> registeredPreps = Context.newArrayDB(REGISTERED_PREPS, Address.class);
    public final ArrayDB<Address> inactivePreps = Context.newArrayDB(INACTIVE_PREPS, Address.class);
    public final ArrayDB<Address> denylist = Context.newArrayDB(DENYLIST, Address.class);

    public final ArrayDB<BigInteger> penaltyAmount = Context.newArrayDB(PENALTY_AMOUNT, BigInteger.class);

    public final DictDB<String, Integer> prepsDenylistStatus = Context.newDictDB(PREPS_DENYLIST_STATUS, Integer.class);
}
