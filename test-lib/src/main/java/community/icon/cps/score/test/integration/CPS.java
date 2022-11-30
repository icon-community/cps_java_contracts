package community.icon.cps.score.test.integration;

import foundation.icon.icx.KeyWallet;
import foundation.icon.icx.data.Bytes;
import foundation.icon.score.client.DefaultScoreClient;

import community.icon.cps.score.lib.interfaces.CPSTreasuryInterfaceScoreClient;
import community.icon.cps.score.lib.interfaces.CPFTreasuryInterfaceScoreClient;
import community.icon.cps.score.lib.interfaces.CPSCoreInterfaceScoreInterface;

import community.icon.cps.score.lib.interfaces.DexInterfaceScoreClient;
import community.icon.cps.score.lib.interfaces.sICXInterfaceScoreClient;
import community.icon.cps.score.lib.interfaces.bnUSDInterfaceScoreClient;
import community.icon.cps.score.lib.interfaces.RouterInterfaceScoreClient;
import score.Address;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static community.icon.cps.score.test.integration.ScoreIntegrationTest.*;

public class CPS {
    public KeyWallet user;
    public KeyWallet testUser;

    public KeyWallet owner;

    /*
    Prep wallets loaded from test-lib/src/main/java/community/icon/cps/score/test/wallets
     */

    public KeyWallet prepWallet1;
    public KeyWallet prepWallet2;
    public KeyWallet prepWallet3;
    public KeyWallet prepWallet4;
    public KeyWallet prepWallet5;
    public KeyWallet prepWallet6;
    public KeyWallet prepWallet7;

    public CPSClient ownerClient;
    public DefaultScoreClient cpsTreasury;
    public DefaultScoreClient cpfTreasury;
    public DefaultScoreClient cpsCore;

    public DefaultScoreClient dex;
    public DefaultScoreClient router;
    public DefaultScoreClient bnusd;
    public DefaultScoreClient sicx;

    public DefaultScoreClient governanceBalanced;

    public CPSTreasuryInterfaceScoreClient cpsTreasuryScore;
    public CPFTreasuryInterfaceScoreClient cpfTreasuryScore;
    public CPSCoreInterfaceScoreInterface cpsMainScore;


    Map<String, CPSClient> cpsClients;

    public List<KeyWallet> prepList;

    public CPS() throws Exception{
        cpsClients = new HashMap<>();
        owner = createWalletWithBalance(BigInteger.TEN.pow(24));
        user = createWalletWithBalance(BigInteger.TEN.pow(24));
        testUser = createWalletWithBalance(BigInteger.TEN.pow(24));
        BufferedReader br = new BufferedReader(new FileReader("privateKey.txt"));
//        System.out.println("Reading the content of the file in cpsMain: " + br.readLine());
        String longPrivateKey = br.readLine();
        String privateKey0 = longPrivateKey.substring(0, 66);
        String privateKey1 = longPrivateKey.substring(66, 132);
        String privateKey2 = longPrivateKey.substring(132, 198);
        String privateKey3 = longPrivateKey.substring(198, 264);
        String privateKey4 = longPrivateKey.substring(264, 330);
        String privateKey5 = longPrivateKey.substring(330, 396);
        String privateKey6 = longPrivateKey.substring(396, 462);

        prepWallet1 = KeyWallet.load(new Bytes(privateKey1));
        prepWallet2 = KeyWallet.load(new Bytes(privateKey2));
        prepWallet3 = KeyWallet.load(new Bytes(privateKey3));
        prepWallet4 = KeyWallet.load(new Bytes(privateKey4));
        prepWallet5 = KeyWallet.load(new Bytes(privateKey5));
        prepWallet6 = KeyWallet.load(new Bytes(privateKey6));
        prepWallet7 = KeyWallet.load(new Bytes(privateKey0));
    }

    public void setupCPS() throws Exception{
        registerPreps();
        deployContracts();
//        setStakeOfPreps();
//        setDelegationOfPreps();
//        setBonderListOfPReps();
//        setBondOfPreps();
//        registerGodPrep();
//        setGodStake();
    }

    public void registerGodPrep(){
        registerGodClient();
    }

    public void setGodPrep(){
        setGodStake();
    }

    public void setStakeOfPreps(){
        KeyWallet[] keyWallets = {prepWallet1, prepWallet2, prepWallet3, prepWallet4, prepWallet5, prepWallet6, prepWallet7};
        setStake(keyWallets);
    }

    public void setDelegationOfPreps(){
        KeyWallet[] keyWallets = {prepWallet1, prepWallet2, prepWallet3, prepWallet4, prepWallet5, prepWallet6, prepWallet7};
        setDelegation(keyWallets);
    }

    public void setBondOfPreps(){
        KeyWallet[] keyWallets = {prepWallet1, prepWallet2, prepWallet3, prepWallet4, prepWallet5, prepWallet6, prepWallet7};
        setBond(keyWallets);
    }

    public void setBonderListOfPReps(){
        KeyWallet[] keyWallets = {prepWallet1, prepWallet2, prepWallet3, prepWallet4, prepWallet5, prepWallet6, prepWallet7};
        setBonderList(keyWallets);
    }

    public void deployContracts(){
        cpsCore = deploy(owner, "CPSCore", null);

        Map<String, Object> cpsScoreAddress = Map.of("cps_score", cpsCore._address());
        cpsTreasury = deploy(owner, "CPSTreasury", cpsScoreAddress);
        cpfTreasury = deploy(owner, "CPFTreasury", cpsScoreAddress);


        dex = deploy(owner, "Dex", null);

        Map<String, Object> bnUSDParams = Map.of(
                "_name", "Balanced Dollar",
                "_symbol", "bnUSD",
                "_decimals", BigInteger.valueOf(18),
                "_initialSupply", BigInteger.valueOf(100000000));
        bnusd = deploy(owner, "bnUSD", bnUSDParams);

        sicx = deploy(owner, "sICX", bnUSDParams);

        router = deploy(owner, "Router", null);
    }

    public void setScoreAddresses(){
        // CPS Main
    }
}
