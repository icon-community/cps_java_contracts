package community.icon.cps.score.lib.interfaces;

import foundation.icon.score.client.ScoreClient;
import foundation.icon.score.client.ScoreInterface;

import score.Address;
import score.annotation.EventLog;
import score.annotation.External;
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
    void setCpsScore(Address score);

    @External(readonly = true)
        //Todo java convention in get methods??
    Address getCpsScore();

    @External
    void setCpfTreasuryScore(Address score);

    @External(readonly = true)
    Address getCpfTreasuryScore();

    @External
    void setBnUSDScore(Address score);

    @External(readonly = true)
    Address getBnUSDScore();

    @External(readonly = true)
    Map<String, ?> getContributorProjectedFund(Address walletAddress);

    @External(readonly = true)
    List<String> getContributorProjects(Address address);

    @External(readonly = true)
    List<String> getSponsorProjects(Address address);

    @External(readonly = true)
    Map<String, ?> getSponsorProjectedFund(Address walletAddress);

    @External
    @Payable
    void update_proposal_fund(String ipfs_key, BigInteger added_budget, BigInteger _added_sponsor_reward,
                              int _added_installment_count);

    @External
    void send_installment_to_contributor(String _ipfs_key,int installment_count);

    @External
    void send_reward_to_sponsor(String _ipfs_key,int installment_count);

    @External
    void disqualify_project(String _ipfs_key);

    @External
    void claimReward();

    @External
    void tokenFallback(Address from, BigInteger value, byte[] _data);

    //    for migration into java contract
    @External
    void updateSponsorAndContributorProjects();

    @External
    void setOnsetPayment(BigInteger paymentPercentage);

    @External(readonly = true)
    BigInteger getOnsetPayment();

    @EventLog(indexed = 1)
    void ProposalDisqualified(String _ipfs_key, String note);

    @EventLog(indexed = 1)
    void ProposalFundDeposited(String _ipfs_key, String note);

    @EventLog(indexed = 1)
    void ProposalFundSent(Address _receiver_address, String note);

    @EventLog(indexed = 1)
    void ProposalFundWithdrawn(Address _receiver_address, String note);
}
