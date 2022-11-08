package community.icon.cps.score.lib.interfaces;

import foundation.icon.score.client.ScoreClient;
import foundation.icon.score.client.ScoreInterface;

import score.Address;
import score.Context;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;
import score.annotation.Payable;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@ScoreClient
@ScoreInterface
public interface CPFTreasuryInterface {
    @External(readonly = true)
    String name();

    @External
    void setCpsScore(Address _score);

    @External(readonly = true)
    Address getCpsScore();

    @External
    void setCpsTreasuryScore(Address _score);

    @External(readonly = true)
    Address getCpsTreasuryScore();

    @External
    void setBnUSDScore(Address _score);

    @External(readonly = true)
    Address getBnUSDScore();

    @External
    void setSicxScore(Address _score);

    @External(readonly = true)
    Address getSicxScore();

    @External
    void setDexScore(Address _score);

    @External(readonly = true)
    Address getDexScore();

    @External
    void setRouterScore(Address _score);

    @External(readonly = true)
    Address getRouterScore();

    @External
    void setMaximumTreasuryFundIcx(BigInteger _value);

    @External
    void setMaximumTreasuryFundBnusd(BigInteger _value);

    @External(readonly = true)
    Map<String, BigInteger> get_total_funds();

    @External(readonly = true)
    Map<String, BigInteger> get_remaining_swap_amount();

    @External
    void transfer_proposal_fund_to_cps_treasury(String ipfs_key, int total_installment_count,
                                                Address sponsor_address, Address contributor_address,
                                                String token_flag, BigInteger _total_budget);

    @External
    void update_proposal_fund(String ipfs_key, @Optional String flag, @Optional BigInteger _added_budget,
                              @Optional int _total_installment_count);

    @External
    @Payable
    void add_fund();

    @External
    void swapIcxBnusd(BigInteger amount);

    @External
    void swap_tokens(int _count);

    @External(readonly = true)
    Map<String, Integer> get_swap_state_status();

    @External
    void reset_swap_state();

    @External(readonly = true)
    Map<String, Object> get_proposal_details(@Optional int start_index, @Optional int end_index);

    @External
    void tokenFallback(Address from, BigInteger value, byte[] _data);

    @Payable
    void fallback();

    //EventLogs
    @EventLog(indexed = 1)
    void FundReturned(Address _sponsor_address, String note);

    @EventLog(indexed = 1)
    void ProposalFundTransferred(String _ipfs_key, String note);

    @EventLog(indexed = 1)
    void ProposalDisqualified(String _ipfs_key, String note);

    @EventLog(indexed = 1)
    void FundReceived(Address _sponsor_address, String note);
}
