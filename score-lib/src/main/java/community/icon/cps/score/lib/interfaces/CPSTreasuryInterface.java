package community.icon.cps.score.lib.interfaces;

import foundation.icon.score.client.ScoreClient;
import foundation.icon.score.client.ScoreInterface;

import score.Address;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;
import score.annotation.Payable;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@ScoreClient
@ScoreInterface
public interface CPSTreasuryInterface {
    @External(readonly = true)
    String name();

    @Payable
    void fallback();

    @External
    void setCpsScore(Address _score);

    @External(readonly = true)
        //Todo java convention in get methods??
    Address getCpsScore();

    @External
    void setCpfTreasuryScore(Address _score);

    @External(readonly = true)
    Address getCpfTreasuryScore();

    @External
    void setBnUSDScore(Address _score);

    @External(readonly = true)
    Address getBnUSDScore();

    @External(readonly = true)
    Map<String, ?> get_contributor_projected_fund(Address _wallet_address);

    @External(readonly = true)
    List<String> getContributorProjects(Address address);

    @External(readonly = true)
    List<String> getSponsorProjects(Address address);

    @External(readonly = true)
    Map<String, ?> get_sponsor_projected_fund(Address _wallet_address);

    @External
    @Payable
    void update_proposal_fund(String ipfs_key, BigInteger added_budget, BigInteger _added_sponsor_reward,
                              int _added_installment_count);

    @External
    void send_installment_to_contributor(String _ipfs_key);

    @External
    void send_reward_to_sponsor(String _ipfs_key);

    @External
    void disqualify_project(String _ipfs_key);

    @External
    void claim_reward();

    @External
    void tokenFallback(Address from, BigInteger value, byte[] _data);

    //    for migration into java contract
    @External
    void updateSponsorAndContributorProjects();

    @EventLog(indexed = 1)
    void ProposalDisqualified(String _ipfs_key, String note);

    @EventLog(indexed = 1)
    void ProposalFundDeposited(String _ipfs_key, String note);

    @EventLog(indexed = 1)
    void ProposalFundSent(Address _receiver_address, String note);

    @EventLog(indexed = 1)
    void ProposalFundWithdrawn(Address _receiver_address, String note);
}
