package community.icon.cps.score.cpstreasury.utils;
import score.Address;
import score.annotation.EventLog;


public class Eventlogs {
    @EventLog(indexed = 1)
    public static void ProposalSubmitted(Address _sender_address, String note){}

    @EventLog(indexed = 1)
    public static void VotedSuccessfully(Address _sender_address, String note){}

    @EventLog(indexed = 1)
    public static void ProgressReportSubmitted(Address _sender_address, String note){}

    @EventLog(indexed = 1)
    public static void PeriodUpdate(String note){}
}
