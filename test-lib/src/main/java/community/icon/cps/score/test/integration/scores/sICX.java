package community.icon.cps.score.test.integration.scores;

import score.Address;
import score.annotation.External;

import score.annotation.Optional;


import java.math.BigInteger;

public interface sICX {

    @External

    void mintWithTokenFallBack(Address _to, BigInteger _amount, byte[] _data);

    @External(readonly = true)
    BigInteger balanceOf(Address _owner);

    void mintTo(Address _account, BigInteger _amount);

    @External
    void transfer(Address _to, BigInteger _value, @Optional byte[] _data);

}
