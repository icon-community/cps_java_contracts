package dummy.contract.bnusd;

import community.icon.cps.score.lib.interfaces.bnUSDInterface;
import community.icon.cps.score.lib.tokens.IRC2Base;
import score.Address;
import score.Context;
import score.annotation.External;

import java.math.BigInteger;

public class bnUSD extends IRC2Base implements bnUSDInterface {
    public bnUSD(String _name, String _symbol, int _decimals, BigInteger _initialSupply) {
        super(_name, _symbol, BigInteger.valueOf(_decimals));

        // mint the initial token supply here
        Context.require(_initialSupply.compareTo(BigInteger.ZERO) >= 0);
        mint(Context.getCaller(), _initialSupply);
    }

    @External
    public void mintTo(Address to, BigInteger amount) {
        mint(to, amount);
    }

    private static BigInteger pow10(int exponent) {
        BigInteger result = BigInteger.ONE;
        for (int i = 0; i < exponent; i++) {
            result = result.multiply(BigInteger.TEN);
        }
        return result;
    }


}

