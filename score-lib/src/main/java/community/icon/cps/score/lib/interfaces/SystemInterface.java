package community.icon.cps.score.lib.interfaces;

import foundation.icon.score.client.ScoreClient;
import foundation.icon.score.client.ScoreInterface;
import score.Address;
import score.annotation.Payable;

import javax.management.MBeanServerInvocationHandler;
import java.math.BigInteger;
import java.util.Map;

//@ScoreClient
@ScoreInterface(suffix = "Client")
public interface SystemInterface {
    public static class Delegation{
        public Address address;
        public BigInteger value;
    }

    public static class Bond{
        public Address address;
        public BigInteger value;
    }
    Map<String, Object> getIISSInfo();

    Map<String, Object> queryIScore(Address address);

    Map<String, Object> getStake(Address address);

    Map<String, Object> getDelegation(Address address);

    Map<String, Object> getPReps(BigInteger startRanking, BigInteger endRanking);

    void setStake(BigInteger value);

    void setDelegation(Delegation[] delegations);
    Map<String, Object> getPRepTerm();

    void setBond(Bond[] bonds);

    void setBonderList(Address[] bonderList);

    @Payable
    void registerPRep(String name, String email, String country, String city, String website, String details, String p2pEndpoint);
}
