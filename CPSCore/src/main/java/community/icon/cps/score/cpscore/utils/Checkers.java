package community.icon.cps.score.cpscore.utils;

import community.icon.cps.score.cpscore.SetterGetter;
import score.Address;
import score.Context;

public class Checkers {
    public static void onlyOwner() {
        Address caller = Context.getCaller();
        Address owner = Context.getOwner();
        Context.require(caller.equals(owner), "SenderNotScoreOwner: Sender=" + caller + " Owner=" + owner);
    }


    public static void checkMaintenance() {
        SetterGetter setterGetter = new SetterGetter();
        Context.require(!setterGetter.maintenance.get(), "Maintenance mode is on. Will resume soon.");
    }
}
