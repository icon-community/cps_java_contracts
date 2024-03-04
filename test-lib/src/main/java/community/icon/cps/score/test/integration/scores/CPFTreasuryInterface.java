package community.icon.cps.score.test.integration.scores;


import score.Address;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;
import score.annotation.Payable;

import java.math.BigInteger;
import java.util.Map;

public interface CPFTreasuryInterface {
    @External(readonly = true)
    String name();

    @External
    void setCpsScore(Address score);

    @External(readonly = true)
    Address getCpsScore();

    @External
    void setCpsTreasuryScore(Address score);

    @External(readonly = true)
    Address getCpsTreasuryScore();

    @External
    void setBnUSDScore(Address score);

    @External(readonly = true)
    Address getBnUSDScore();

    @External
    void setSicxScore(Address score);

    @External(readonly = true)
    Address getSicxScore();

    @External
    void setDexScore(Address score);

    @External(readonly = true)
    Address getDexScore();

    @External
    void setRouterScore(Address score);

    @External(readonly = true)
    Address getRouterScore();

    @External
    void setOracleAddress(Address score);

    @External(readonly = true)
    Address getOracleAddress();

    @External
    void setSponsorBondPercentage(BigInteger bondValue);

    @External
    void setPeriod(BigInteger applicationPeriod);

    @External
    void setOnsetPayment(BigInteger paymentPercentage);

    @External(readonly = true)
    int getSlippagePercentage();

    @External(readonly = true)
    BigInteger getEmergencyFund();

    @External
    void setMaximumTreasuryFundIcx(BigInteger value);

    @External
    void setMaximumTreasuryFundBnusd(BigInteger value);

    @External
    void setSwapLimitAmount(BigInteger value);

    @External(readonly = true)
    Map<String, BigInteger> getTotalFunds();

    @External(readonly = true)
    Map<String, BigInteger> getRemainingSwapAmount();

    @External
    void transferProposalFundToCpsTreasury(String ipfsKey, int projectDuration,
                                           Address sponsorAddress, Address contributorAddress,
                                           String tokenFlag, BigInteger totalBudget);

    @External
    void updateProposalFund(String ipfsKey, @Optional String flag, @Optional BigInteger addedBudget,
                            @Optional int totalInstallmentCount);

    @External
    void withdrawFromEmergencyFund(BigInteger value, Address address, String purpose);

    @External
    void allocateEmergencyFund(BigInteger value);

    @External
    void setOraclePercentageDifference(int value);

    @External
    @Payable
    void addFund();

    @External
    void swapICXToBnUSD(BigInteger amount, @Optional BigInteger _minReceive);

    @External
    void swapTokens(int _count);

    @External(readonly = true)
    Map<String, Integer> getSwapStateStatus();

    @External
    void resetSwapState();

    @External(readonly = true)
    Map<String, Object> getProposalDetails(@Optional int startIndex, @Optional int endIndex);

    @External
    void tokenFallback(Address from, BigInteger value, byte[] _data);

    @Payable
    void fallback();
    @External
    void toggleSwapFlag();

    //EventLogs
    @EventLog(indexed = 1)
    void FundReturned(Address _sponsor_address, String note);

    @EventLog(indexed = 1)
    void ProposalFundTransferred(String _ipfs_key, String note);

    @EventLog(indexed = 1)
    void ProposalDisqualified(String _ipfs_key, String note);

    @EventLog(indexed = 1)
    void FundReceived(Address _sponsor_address, String note);

    @EventLog(indexed = 1)
    void EmergencyFundTransferred(Address _address, BigInteger _value, String _purpose);
}
