package community.icon.cps.score.test.integration.scores;

import score.Address;
import score.annotation.External;
import score.annotation.Optional;

import java.math.BigInteger;

public interface BalancedDollar {

    @External(readonly = true)
    BigInteger balanceOf(Address _owner);

    @External
    void mint(BigInteger _amount, @Optional byte[] _data);

    @External
    void mintTo(Address to, BigInteger amount);

    @External
    void setMinter(Address _address);

    @External(readonly = true)
    Address getMinter();
}
