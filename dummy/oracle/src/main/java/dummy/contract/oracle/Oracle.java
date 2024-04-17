package dummy.contract.oracle;

import score.annotation.External;

import java.math.BigInteger;
import java.util.Map;

public class Oracle {

    @External(readonly = true)
    public Map<String,BigInteger> get_reference_data(String _base, String _quote) {
        return Map.of("rate",BigInteger.ONE);
    }


}
