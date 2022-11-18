package community.icon.cps.score.lib.interfaces;

import score.Address;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;
import score.annotation.Payable;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import foundation.icon.score.client.ScoreClient;
import foundation.icon.score.client.ScoreInterface;

@ScoreClient
@ScoreInterface
public interface CPSCoreInterface {

    public static class ProposalAttributes {
        public String ipfs_hash;
        public String project_title;
        public int project_duration;
        public BigInteger total_budget;
        public String token;
        public Address sponsor_address;
        public String ipfs_link;
    }

    public static class ProgressReportAttributes {
        public String ipfs_hash;
        public String report_hash;
        public String ipfs_link;
        public String progress_report_title;
        public Boolean budget_adjustment;
        public BigInteger additional_budget;
        public int additional_month;
        public int percentage_completed;
    }
    @External(readonly = true)
    String name();

    String proposalPrefix(String proposalKey);

    String progressReportPrefix(String progressKey);

    
    @External
    void set_cps_treasury_score(Address _score);

    @External
    void setCpsTreasuryScore(Address score);

    
    @External(readonly = true)
    Address get_cps_treasury_score();

    @External(readonly = true)
    Address getCpsTreasuryScore();

    
    @External
    void set_cpf_treasury_score(Address _score);

    @External
    void setCpfTreasuryScore(Address score);

    
    @External(readonly = true)
    Address get_cpf_treasury_score();

    @External(readonly = true)
    Address getCpfTreasuryScore();

    
    @External
    void set_bnUSD_score(Address _score);

    
    @External(readonly = true)
    Address get_bnUSD_score();

    
    @External(readonly = true)
    boolean isAdmin(Address address);

    @External
    void toggleBudgetAdjustmentFeature();

    @External(readonly = true)
    boolean getBudgetAdjustmentFeature();

    
    @External
    void toggleMaintenance();

    @External(readonly = true)
    boolean getMaintenanceMode();

    @Payable
    void fallback();

    
    @External
    void addAdmin(Address address);

    
    @External
    void removeAdmin(Address address);

    
    @External
    void unregister_prep();

    
    @External
    void register_prep();

    @External(readonly = true)
    boolean checkPriorityVoting(Address prep);

    @External(readonly = true)
    List<String> sortPriorityProposals();

    @External(readonly = true)
    Map<String, Integer> getPriorityVoteResult();

    @External
    void votePriority(String[] proposals);

    
    @External
    void set_prep_penalty_amount(BigInteger[] _penalty);

    
    @External
    void set_initialBlock();

    
    @External(readonly = true)
    Map<String, BigInteger> login_prep(Address _address);

    
    @External(readonly = true)
    List<Address> get_admins();

    @SuppressWarnings("unchecked")
    
    @External(readonly = true)
    Map<String, BigInteger> get_remaining_fund();

    
    @External(readonly = true)
    List<Map<String, Object>> get_PReps();

    
    @External(readonly = true)
    List<Address> get_denylist();

    
    @External(readonly = true)
    Map<String, ?> get_period_status();

    
    @External(readonly = true)
    List<Address> get_contributors();

    
    @External(readonly = true)
    Map<String, BigInteger> check_claimable_sponsor_bond(Address _address);

    
    @Payable
    @External
    void submit_proposal(ProposalAttributes _proposals);

    
    @External
    void vote_proposal(String _ipfs_key, String _vote, String _vote_reason, @Optional boolean _vote_change);

    
    @External
    void submit_progress_report(ProgressReportAttributes _progress_report);

    
    @External
    void vote_progress_report(String _ipfs_key, String _report_key, String _vote, String _vote_reason, @Optional String _budget_adjustment_vote, @Optional boolean _vote_change);

    
    @External(readonly = true)
    List<String> get_proposals_keys_by_status(String _status);

    
    @External(readonly = true)
    int check_change_vote(Address _address, String _ipfs_hash, String _proposal_type);

    
    @External(readonly = true)
    Map<String, ?> get_project_amounts();

    
    @External(readonly = true)
    Map<String, Integer> get_sponsors_record();

    @External
    void update_period();

    
    @External(readonly = true)
    Map<String, ?> get_proposal_details(String _status, @Optional Address _wallet_address, @Optional int _start_index, @Optional int _end_index);

    @External(readonly = true)
    Map<String, Object> get_proposal_details_by_hash(String _ipfs_key);

    
    @External(readonly = true)
    Map<String, ?> get_progress_reports(String _status, @Optional int _start_index, @Optional int _end_index);

    
    @External(readonly = true)
    Map<String, Object> get_progress_reports_by_hash(String _report_key);

    
    @External(readonly = true)
    Map<String, Object> get_progress_reports_by_proposal(String _ipfs_key);

    
    @External(readonly = true)
    Map<String, Object> get_sponsors_requests(String _status, Address _sponsor_address, @Optional int _start_index, @Optional int _end_index);

    @External(readonly = true)
    
    Map<String, Object> get_vote_result(String _ipfs_key);

    @External(readonly = true)
    
    Map<String, Object> get_progress_report_result(String _report_key);

    @External(readonly = true)
    
    Map<String, Object> get_budget_adjustment_vote_result(String _report_key);

    @External
    
    void tokenFallback(Address _from, BigInteger _value, byte[] _data);

    @External
    
    void remove_denylist_preps();

    @External
    
    void claim_sponsor_bond();

    @External
    
    void set_swap_count(int value);

    @External
    
    void update_next_block(int blockCount);

    
    @External(readonly = true)
    List<Map<String, Object>> get_active_proposals();

    @External(readonly = true)
    Map<String, Object> get_proposal_detail_by_wallet(Address _wallet_address);

    List<Map<String, Object>> getProposalsHistory();

    //    EventLogs
    @EventLog(indexed = 1)
    void ProposalSubmitted(Address _sender_address, String note);

    @EventLog(indexed = 1)
    void ProgressReportSubmitted(Address _sender_address, String _project_title);

    @EventLog(indexed = 1)
    void SponsorBondReceived(Address _sender_address, String _notes);

    @EventLog(indexed = 1)
    void SponsorBondRejected(Address _sender_address, String _notes);

    @EventLog(indexed = 1)
    void VotedSuccessfully(Address _sender_address, String _notes);

    @EventLog(indexed = 1)
    void PRepPenalty(Address _prep_address, String _notes);

    @EventLog(indexed = 1)
    void UnRegisterPRep(Address _sender_address, String _notes);

    @EventLog(indexed = 1)
    void RegisterPRep(Address _sender_address, String _notes);

    @EventLog(indexed = 1)
    void SponsorBondReturned(Address _sender_address, String _notes);

    @EventLog(indexed = 1)
    void PeriodUpdate(String _notes);

    @EventLog(indexed = 1)
    void SponsorBondClaimed(Address _receiver_address, BigInteger _fund, String note);

    @EventLog(indexed = 1)
    void PriorityVote(Address _address, String note);

    @External(readonly = true)
    int getPeriodCount();
}