package community.icon.cps.score.test.integration.scores;

import score.Address;
import score.annotation.External;
import score.annotation.Optional;

import java.math.BigInteger;
import java.util.Map;

public interface DummyDex {

    void mint(BigInteger _id, BigInteger _supply);

    void mintTo(BigInteger _id, Address _account, BigInteger _supply);

    Map<String, ?> getPoolStats(BigInteger _id);

    BigInteger getPriceByName(String _name);

    BigInteger lookupPid(String _name);

    BigInteger getBalnPrice();

    @External(readonly = true)
    BigInteger getPrice(BigInteger _id);

    void transfer(Address _to, BigInteger _value, BigInteger _id, @Optional byte[] _data);
    BigInteger balanceOf(Address _owner, BigInteger _id);


    BigInteger[] balanceOfBatch(Address[] _owners, BigInteger[] _ids);

    String tokenURI(BigInteger _id);

    void transferFrom(Address _from, Address _to, BigInteger _id, BigInteger _value, @Optional byte[] _data);

    void transferFromBatch(Address _from, Address _to, BigInteger[] _ids, BigInteger[] _values, @Optional byte[] _data);


    void setApprovalForAll(Address _operator, boolean _approved);


    boolean isApprovedForAll(Address _owner, Address _operator);



    void TransferSingle(Address _operator, Address _from, Address _to, BigInteger _id, BigInteger _value);

    void TransferBatch(Address _operator, Address _from, Address _to, byte[] _ids, byte[] _values);


    void ApprovalForAll(Address _owner, Address _operator, boolean _approved);


    void URI(BigInteger _id, String _value);

    void setSicxScore(Address _score);
}
