package dummy.contract.oracle;

import score.annotation.External;

import java.math.BigInteger;

public class Oracle {

    @External(readonly = true)
    public BigInteger getReferenceData(String _base, String _quote) {
        return BigInteger.ONE;
    }


}
