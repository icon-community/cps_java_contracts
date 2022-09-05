package community.icon.cps.score.test.integration;

import foundation.icon.icx.KeyWallet;
import foundation.icon.jsonrpc.model.Hash;
import foundation.icon.jsonrpc.model.TransactionResult;
import foundation.icon.score.client.DefaultScoreClient;

import community.icon.cps.score.lib.interfaces.CPSTreasuryInterfaceScoreClient;
import community.icon.cps.score.lib.interfaces.CPFTreasuryInterfaceScoreClient;

import score.Address;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static community.icon.cps.score.test.integration.ScoreIntegrationTest.*;
public class CPS {
    public KeyWallet user;
    public KeyWallet testUser;

    public KeyWallet owner;

    public CPSClient ownerClient;
    public DefaultScoreClient cpsTreasury;
    public DefaultScoreClient cpfTreasury;

    public CPSTreasuryInterfaceScoreClient cpsTreasuryScore;
    public CPFTreasuryInterfaceScoreClient cpfTreasuryScore;

    Map<String, CPSClient> cpsClients;

    public CPS() throws Exception{
        cpsClients = new HashMap<>();
        owner = createWalletWithBalance(BigInteger.TEN.pow(24));
        user = createWalletWithBalance(BigInteger.TEN.pow(24));
        testUser = createWalletWithBalance(BigInteger.TEN.pow(24));
    }

    public void setupCPS() throws Exception{
        registerPreps();
        deployContracts();
    }

    public void deployContracts(){
        cpsTreasury = deploy(owner, "CPSTreasury", null);
        cpfTreasury = deploy(owner, "CPFTreasury", null);
    }
}
