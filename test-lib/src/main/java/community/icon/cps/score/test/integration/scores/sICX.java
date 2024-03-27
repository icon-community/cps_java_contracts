package community.icon.cps.score.test.integration.scores;

import score.Address;
import score.annotation.External;
import score.annotation.Optional;

import java.math.BigInteger;

public interface sICX {

    @External
    void mintTo(Address _account, BigInteger _amount);

    @External
    void transfer(Address _to, BigInteger _value, @Optional byte[] _data);
}
