package community.icon.cps.score.test.integration.scores;

import score.Address;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public interface CPSTreasuryInterface {
    @External(readonly = true)
    String name();

    @Payable
    void fallback();

    @External
    void setCpsScore(Address score);

    @External(readonly = true)
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
    void updateProposalFund(String ipfsKey, BigInteger addedBudget, BigInteger addedSponsorReward,
                            int addedInstallmentCount);

    @External
    void sendInstallmentToContributor(String ipfsKey, BigInteger milestoneBudget);

    @External
    void sendRewardToSponsor(String ipfsKey, int installmentCount);

    @External
    void disqualifyProject(String ipfsKey);

    @External
    void claimReward();

    @External
    void tokenFallback(Address from, BigInteger value, byte[] _data);

    @External
    void setOnsetPayment(BigInteger paymentPercentage);

    @External(readonly = true)
    BigInteger getOnsetPayment();

    @External
    void updateContributorSponsorAddress(String _ipfs_key, Address _new_contributor_address,
                                         Address _new_sponsor_address);

    @EventLog(indexed = 1)
    void ProposalDisqualified(String _ipfs_key, String note);

    @EventLog(indexed = 1)
    void ProposalFundDeposited(String _ipfs_key, String note);

    @EventLog(indexed = 1)
    void ProposalFundSent(Address _receiver_address, String note);

    @EventLog(indexed = 1)
    void ProposalFundWithdrawn(Address _receiver_address, String note);
}

