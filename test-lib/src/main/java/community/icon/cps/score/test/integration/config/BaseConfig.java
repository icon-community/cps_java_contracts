package community.icon.cps.score.test.integration.config;

import community.icon.cps.score.test.integration.CPS;
import community.icon.cps.score.test.integration.CPSClient;
import community.icon.cps.score.test.integration.Environment;
import score.Address;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static community.icon.cps.score.test.integration.Environment.preps;

public class BaseConfig {

    protected Map<String, Address> addressMap;
    protected CPSClient cpsClient;

    public BaseConfig(CPSClient cpsClient){
        this.cpsClient = cpsClient;
        this.addressMap = cpsClient.getContractAddresses();
    }


    public void call(){
        System.out.println("--------init base setup for cps-----------");
        this.cpsClient.cpsCore.addAdmin(this.cpsClient.getAddress());
        System.out.println("--------admin set -----------");
        cpsClient.cpsCore.toggleMaintenance();
        cpsClient.cpsCore.setInitialBlock();

        System.out.println("------setting scores in cpscore------");
        cpsClient.cpsCore.setCpsTreasuryScore(addressMap.get("cpsTreasury"));
        cpsClient.cpsCore.setCpfTreasuryScore(addressMap.get("cpfTreasury"));

    }
}
