package community.icon.cps.score.cpscore;

import score.ArrayDB;
import score.Context;

import java.util.Map;

import static community.icon.cps.score.cpscore.utils.Constants.ACTIVE;
import static community.icon.cps.score.cpscore.utils.Constants.APPROVED;
import static community.icon.cps.score.cpscore.utils.Constants.COMPLETED;
import static community.icon.cps.score.cpscore.utils.Constants.DISQUALIFIED;
import static community.icon.cps.score.cpscore.utils.Constants.PAUSED;
import static community.icon.cps.score.cpscore.utils.Constants.PENDING;
import static community.icon.cps.score.cpscore.utils.Constants.PROGRESS_REPORT_REJECTED;
import static community.icon.cps.score.cpscore.utils.Constants.REJECTED;
import static community.icon.cps.score.cpscore.utils.Constants.SPONSOR_PENDING;
import static community.icon.cps.score.cpscore.utils.Constants.WAITING;

public class Status {

    public final ArrayDB<String> sponsorPending = Context.newArrayDB(SPONSOR_PENDING, String.class);
    public final ArrayDB<String> pending = Context.newArrayDB(PENDING, String.class);
    public final ArrayDB<String> active = Context.newArrayDB(ACTIVE, String.class);
    public final ArrayDB<String> paused = Context.newArrayDB(PAUSED, String.class);
    public final ArrayDB<String> completed = Context.newArrayDB(COMPLETED, String.class);
    // TODO : find use case for this
    public final ArrayDB<String> rejected = Context.newArrayDB(REJECTED, String.class);
    public final ArrayDB<String> disqualified = Context.newArrayDB(DISQUALIFIED, String.class);
    public final Map<String, ArrayDB<String>> proposalStatus = Map.of(SPONSOR_PENDING, sponsorPending,
            PENDING, pending,
            ACTIVE, active,
            PAUSED, paused,
            COMPLETED, completed,
            REJECTED, rejected,
            DISQUALIFIED, disqualified);


    public final ArrayDB<String> waitingProgressReports = Context.newArrayDB(WAITING, String.class);
    public final ArrayDB<String> approvedProgressReports = Context.newArrayDB(APPROVED, String.class);
    public final ArrayDB<String> progressRejected = Context.newArrayDB(PROGRESS_REPORT_REJECTED, String.class);
    public final Map<String, ArrayDB<String>> progressReportStatus = Map.of(WAITING, waitingProgressReports,
            APPROVED, approvedProgressReports,
            PROGRESS_REPORT_REJECTED, progressRejected
    );

}
