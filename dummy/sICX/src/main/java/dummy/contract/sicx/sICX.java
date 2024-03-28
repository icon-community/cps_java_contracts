package dummy.contract.sicx;

import community.icon.cps.score.lib.tokens.IRC2Base;
import score.annotation.External;
import score.Address;
import score.Context;
import community.icon.cps.score.lib.interfaces.sICXInterface;
import score.VarDB;
import score.annotation.Optional;


import java.math.BigInteger;

public class sICX extends IRC2Base implements sICXInterface {

    private static final String TAG = "sICX";
    private static final String TOKEN_NAME = "Staked ICX";
    private static final String SYMBOL_NAME = "sICX";
    private static final BigInteger DECIMALS = BigInteger.valueOf(18);
    private static final String STAKING = "staking";
    public static final String STATUS_MANAGER = "status_manager";
    private static final String VERSION = "version";
    private static final String SICX_VERSION = "v0.0.1";

    static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);

    private static final VarDB<Address> stakingAddress = Context.newVarDB(STAKING, Address.class);
    private final VarDB<Address> statusManager = Context.newVarDB(STATUS_MANAGER, Address.class);

    private final VarDB<String> currentVersion = Context.newVarDB(VERSION, String.class);

    public sICX(Address _admin) {
        super(TOKEN_NAME, SYMBOL_NAME, DECIMALS);
        if (stakingAddress.get() == null) {
            stakingAddress.set(_admin);
        }

        if (currentVersion.getOrDefault("").equals(SICX_VERSION)) {
            Context.revert("Can't Update same version of code");
        }
        currentVersion.set(SICX_VERSION);
    }

    @External(readonly = true)
    public String version() {
        return currentVersion.getOrDefault("");
    }

    @External(readonly = true)
    public String getPeg() {
        return TAG;
    }

    @External
    public void setStaking(Address _address) {
        onlyOwner();
        stakingAddress.set(_address);
    }

    @External(readonly = true)
    public Address getStaking() {
        return stakingAddress.get();
    }

    @External
    public void setEmergencyManager(Address _address) {
        onlyOwner();
        statusManager.set(_address);
    }

    @External(readonly = true)
    public Address getEmergencyManager() {
        return statusManager.get();
    }

    @External(readonly = true)
    public BigInteger priceInLoop() {
        return (BigInteger) Context.call(stakingAddress.get(), "getTodayRate");
    }

    @External(readonly = true)
    public BigInteger lastPriceInLoop() {
        return priceInLoop();
    }

    @External
    public void govTransfer(Address _from, Address _to, BigInteger _value, @Optional byte[] _data) {
        onlyOwner();
        _transfer(_from, _to, _value, _data);
    }

    @Override
    @External
    public void transfer(Address _to, BigInteger _value, @Optional byte[] _data) {
        _transfer(Context.getCaller(), _to, _value, _data);
    }

    private void _transfer(Address _from, Address _to, BigInteger _value, @Optional byte[] _data) {
        Address _stakingAddress = stakingAddress.get();

//        if (!_to.equals(_stakingAddress)) {
//            Context.call(_stakingAddress, "transferUpdateDelegations", _from, _to, _value);
//        }
        transfer(_from, _to, _value, _data);
    }

    public static void onlyOwner() {
        Address caller = Context.getCaller();
        Address owner = Context.getOwner();
        Context.require(caller.equals(owner), "SenderNotScoreOwner: Sender=" + caller + "Owner=" + owner);
    }

    @External
    public void mintWithTokenFallBack(Address _to, BigInteger _amount, byte[] _data){
        onlyOwner();
        mint(_to,_amount);
        byte[] data = (_data == null) ? new byte[0] : _data;
        if (_to.isContract()) {
            Context.call(_to, "tokenFallback", ZERO_ADDRESS, _amount, data);
        }
    }
}

