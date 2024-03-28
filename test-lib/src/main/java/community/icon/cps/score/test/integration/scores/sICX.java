package community.icon.cps.score.test.integration.scores;

import score.Address;
import score.annotation.External;

import java.math.BigInteger;

public interface sICX {

    @External
    void mintWithTokenFallBack(Address _to, BigInteger _amount, byte[] _data);

    @External(readonly = true)
    BigInteger balanceOf(Address _owner);
}
