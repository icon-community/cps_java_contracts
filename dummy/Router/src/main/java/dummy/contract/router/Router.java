package dummy.contract.router;

import score.Address;
import score.Context;
import score.annotation.External;
import score.annotation.Optional;
import score.annotation.Payable;

import java.math.BigInteger;
import community.icon.cps.score.lib.interfaces.RouterInterface;

public class Router implements RouterInterface {
    private static final String TAG = "Router";

    public Router(){}
    private void route(Address from, Address startToken, Address[] _path, BigInteger _minReceive) {
        Address currentToken = _path[1];


        BigInteger balance = (BigInteger) Context.call(currentToken, "balanceOf", Context.getAddress());
        Context.require(balance.compareTo(_minReceive) >= 0,
                TAG + ": Below minimum receive amount of " + _minReceive);
        Context.call(currentToken, "transfer", from, _minReceive);

    }

    @Override
    @Payable
    @External
    public void route(Address[] _path, @Optional BigInteger _minReceive) {
        if (_minReceive == null) {
            _minReceive = BigInteger.ZERO;
        }

        Context.require(_minReceive.signum() >= 0, TAG + ": Must specify a positive number for minimum to receive");

        Context.require(_path.length <= 2,
                TAG + ": Passed max swaps of " + 2);

        route(Context.getCaller(), null, _path, Context.getValue());
    }

    @External
    public void tokenFallback(Address _from, BigInteger _value, byte[] _data){
    }
}
