package community.icon.cps.score.lib.interfaces;

import foundation.icon.score.client.ScoreClient;
import foundation.icon.score.client.ScoreInterface;
import score.Address;
import score.annotation.External;
import score.annotation.Optional;
import score.annotation.Payable;

import java.math.BigInteger;

@ScoreInterface
@ScoreClient
public interface RouterInterface {
    @Payable
    @External
    void route(Address[] _path, @Optional BigInteger _minReceive);
}
