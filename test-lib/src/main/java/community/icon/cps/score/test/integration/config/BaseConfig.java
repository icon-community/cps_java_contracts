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

    public static final BigInteger EXA = BigInteger.valueOf(1000000000000000000L);
    public BaseConfig(CPSClient cpsClient){
        this.cpsClient = cpsClient;
        this.addressMap = cpsClient.getContractAddresses();
    }


    public void call(){
        System.out.println("--------init base setup for cps-----------");
        System.out.println("--------admin set -----------");
        this.cpsClient.cpsCore.addAdmin(this.cpsClient.getAddress());


        System.out.println("------setting scores in cpscore------");
        cpsClient.cpsCore.setCpsTreasuryScore(addressMap.get("cpsTreasury"));
        cpsClient.cpsCore.setCpfTreasuryScore(addressMap.get("cpfTreasury"));
        cpsClient.cpsCore.setBnusdScore(addressMap.get("bnUSD"));

        System.out.println("------setting scores in cps treasury-----");
        cpsClient.cpsTreasury.setCpfTreasuryScore(addressMap.get("cpfTreasury"));
        cpsClient.cpsTreasury.setBnUSDScore(addressMap.get("bnUSD"));

        System.out.println("--------setting score in cpf treasury -------");
        cpsClient.cpfTreasury.setCpsTreasuryScore(addressMap.get("cpsTreasury"));
        cpsClient.cpfTreasury.setBnUSDScore(addressMap.get("bnUSD"));
        cpsClient.cpfTreasury.setDexScore(addressMap.get("dex"));
        cpsClient.cpfTreasury.setOracleAddress(addressMap.get("oracle"));
        cpsClient.cpfTreasury.setRouterScore(addressMap.get("router"));
        cpsClient.cpfTreasury.setSicxScore(addressMap.get("sICX"));

        System.out.println("------setting funds--------");
        cpsClient.cpfTreasury.setMaximumTreasuryFundIcx(BigInteger.valueOf(1000).multiply(EXA));
        cpsClient.cpfTreasury.setMaximumTreasuryFundBnusd(BigInteger.valueOf(10000).multiply(EXA));
//        this.cpsClient.bnUSD.setMinter(this.cpsClient.getAddress());
        cpsClient.bnUSD.mintTo(addressMap.get("cpfTreasury"),BigInteger.valueOf(5000).multiply(EXA) );
        cpsClient.cpfTreasury.toggleSwapFlag();

        cpsClient.cpsCore.toggleMaintenance();
        cpsClient.cpsCore.setInitialBlock();


        System.out.println("------system score------- " + addressMap.get("systemScore"));

    }
}
