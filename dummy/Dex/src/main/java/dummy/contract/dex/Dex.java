package dummy.contract.dex;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import score.Address;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import scorex.util.ArrayList;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class Dex implements community.icon.cps.score.lib.interfaces.DexInterface {
    private static final String TAG = "Balanced DEX";
    private final VarDB<Address> sicx = Context.newVarDB("sicx", Address.class);
    public static final BigInteger EXA = BigInteger.valueOf(1_000_000_000_000_000_000L);

    public Dex(){}

    @Override
    @External
    public void setSicxScore(Address _score){
        this.sicx.set(_score);
    }

    @External(readonly = true)
    public BigInteger getPrice(int poolId){
        return BigInteger.ONE;
    }

    @Override
    @EventLog
    public void Deposit(Address from_token, Address from, BigInteger value){}

    @Override
    @EventLog(indexed = 2)
    public void Swap(BigInteger _id, Address _baseToken, Address _fromToken, Address _toToken,
                     Address _sender, Address _receiver, BigInteger _fromValue, BigInteger _toValue,
                     BigInteger _timestamp, BigInteger _lpFees, BigInteger _balnFees, BigInteger _poolBase,
                     BigInteger _poolQuote, BigInteger _endingPrice, BigInteger _effectiveFillPrice) {
    }

    @Override
    @External
    public void tokenFallback(Address _from, BigInteger _value, byte[] _data) {
        // Parse the transaction data submitted by the user
        String unpackedData = new String(_data);
        Context.require(!unpackedData.equals(""), "Token Fallback: Data can't be empty");
        if (Arrays.equals(_data, "None".getBytes())){
            return;
        }
        JsonObject json = Json.parse(unpackedData).asObject();

        String method = json.get("method").asString();
        Address fromToken = Context.getCaller();

        Context.require(_value.compareTo(BigInteger.ZERO) > 0, TAG + ": Invalid token transfer value");

        // Call an internal method based on the "method" param sent in tokenFallBack
        switch (method) {
            case "_swap_icx": {
                Context.require(fromToken.equals(sicx.get()),
                        TAG + ": InvalidAsset: _swap_icx can only be called with sICX");
                swapIcx(_from, _value);
                break;

            }
            case "_swap": {

                // Parse the slippage sent by the user in minimumReceive.
                // If none is sent, use the maximum.
                JsonObject params = json.get("params").asObject();
                BigInteger minimumReceive = BigInteger.ZERO;
                if (params.contains("minimumReceive")) {
                    minimumReceive = BigInteger.valueOf(1000).multiply(EXA);
                    Context.require(minimumReceive.signum() >= 0,
                            TAG + ": Must specify a positive number for minimum to receive");
                }

                // Check if an alternative recipient of the swap is set.
                Address receiver;
                if (params.contains("receiver")) {
                    receiver = Address.fromString(params.get("receiver").asString());
                } else {
                    receiver = _from;
                }

                // Get destination coin from the swap
                Context.require(params.contains("toToken"), TAG + ": No toToken specified in swap");
                Address toToken = Address.fromString(params.get("toToken").asString());

                // Perform the swap
                exchange(fromToken, toToken, _from, receiver, _value, minimumReceive);

                break;
            }
            default:
                // If no supported method was sent, revert the transaction
                Context.revert(100, TAG + ": Unsupported method supplied");
                break;
        }
    }

    void swapIcx(Address sender, BigInteger value) {
        Context.transfer(sender, value);
    }

    void exchange(Address fromToken, Address toToken, Address sender,
                  Address receiver, BigInteger value, BigInteger minimumReceive) {

        if (minimumReceive == null) {
            minimumReceive = BigInteger.ZERO;
        }

        // Send the trader their funds
        Context.call(toToken, "transfer", receiver, value);

        Swap(BigInteger.valueOf(0), fromToken, fromToken, toToken, sender, receiver, value, value,
                BigInteger.valueOf(Context.getBlockTimestamp()), BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO
                , BigInteger.ZERO);
    }
}
