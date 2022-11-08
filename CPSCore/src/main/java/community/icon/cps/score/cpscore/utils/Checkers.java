package community.icon.cps.score.cpscore.utils;

import score.Address;
import score.Context;

import static community.icon.cps.score.cpscore.utils.Constants.TAG;
import community.icon.cps.score.cpscore.SetterGetter;
import community.icon.cps.score.cpscore.CPSCore;

public class Checkers {
    public static void onlyOwner() {
        Address caller = Context.getCaller();
        Address owner = Context.getOwner();
        Context.require(caller.equals(owner), "SenderNotScoreOwner: Sender=" + caller + " Owner=" + owner);
    }

    public static void validateAdmins() {
        CPSCore cpsCore = new CPSCore();
        Context.require(cpsCore.isAdmin(Context.getCaller()),
                TAG + ": Only Admins can call this method");

    }

    public static void validateAdminScore(Address scoreAddress) {
        validateAdmins();
        Context.require(scoreAddress.isContract(), scoreAddress + " is not a SCORE Address");

    }

    public static void checkMaintenance() {
        SetterGetter setterGetter = new SetterGetter();
        Context.require(!setterGetter.maintenance.get(), "Maintenance mode is on. Will resume soon.");
    }
}
