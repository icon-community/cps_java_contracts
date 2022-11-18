package community.icon.cps.score.lib.interfaces;

import foundation.icon.score.client.ScoreClient;
import foundation.icon.score.client.ScoreInterface;
import score.Address;
import score.annotation.EventLog;
import score.annotation.External;

import java.math.BigInteger;

@ScoreInterface
@ScoreClient
public interface DexInterface {

    @External
    void setSicxScore(Address _score);
    @EventLog
    void Deposit(Address from_token, Address from, BigInteger value);

    @EventLog(indexed = 2)
    void Swap(BigInteger _id, Address _baseToken, Address _fromToken, Address _toToken,
              Address _sender, Address _receiver, BigInteger _fromValue, BigInteger _toValue,
              BigInteger _timestamp, BigInteger _lpFees, BigInteger _balnFees, BigInteger _poolBase,
              BigInteger _poolQuote, BigInteger _endingPrice, BigInteger _effectiveFillPrice);

    @External
    void tokenFallback(Address _from, BigInteger _value, byte[] _data);
}
