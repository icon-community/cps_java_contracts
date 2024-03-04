package community.icon.cps.score.test.integration;

import community.icon.cps.score.lib.interfaces.SystemInterfaceScoreClient;
import community.icon.cps.score.test.integration.model.Score;
import community.icon.cps.score.test.integration.utils.DefaultICONClient;
import foundation.icon.icx.KeyWallet;
import foundation.icon.icx.data.Bytes;
import foundation.icon.score.client.DefaultScoreClient;


import score.Address;


import java.math.BigInteger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static community.icon.cps.score.test.integration.Environment.*;
import static community.icon.cps.score.test.integration.ScoreIntegrationTest.createWalletWithBalance;
import static community.icon.cps.score.test.integration.ScoreIntegrationTest.transfer;


public class CPS {

    public KeyWallet owner;
    public CPSClient ownerClient;
    public CPSClient testClient;

    private Map<String, foundation.icon.jsonrpc.Address> addresses;
    public DefaultICONClient iconClient;

    HashMap<Address, CPSClient> cpsClients;

    private String contracts;

    public CPS(String contracts) throws Exception {

        this.contracts = contracts;
        cpsClients = new HashMap<>();
        owner = createWalletWithBalance(BigInteger.TEN.pow(24));
        iconClient =  new DefaultICONClient(chain);
    }

    public void setupCPS() throws Exception {
        deployPrep();
        this.addresses = new ScoreDeployer(this,contracts).deployContracts();

        ownerClient = new CPSClient(this,owner);
        testClient = new CPSClient(this, createWalletWithBalance(BigInteger.TEN.pow(24)));
    }


    public CPSClient defaultClient() {
        return ownerClient;
    }

    public CPSClient testClient() {
        return testClient;
    }

    public void send(foundation.icon.jsonrpc.Address address, String method, Map<String, Object> params) {
        iconClient.send(owner, address, BigInteger.ZERO, method, params, DefaultICONClient.DEFAULT_RESULT_TIMEOUT);
    }

    public foundation.icon.jsonrpc.Address deployAddressManager() {
        return iconClient.deploy(owner, DefaultICONClient.ZERO_ADDRESS, getScorePath("AddressManager"),
                new HashMap<>());
    }

    public String getScorePath(String key) {
        String path = System.getProperty(key);
        if (path == null) {
            throw new IllegalArgumentException("No such property: " + key);
        }
        return path;
    }

    public Callable<foundation.icon.jsonrpc.Address> deploy(Score score) {
        return () -> _deploy(score);
    }

    public foundation.icon.jsonrpc.Address _deploy(Score score) {
        return iconClient.deploy(owner, DefaultICONClient.ZERO_ADDRESS, score.getPath(), score.getParams());
    }

    public boolean isPRepRegistered() {
        try {
            SYSTEM_INTERFACE = new SystemInterfaceScoreClient(godClient);
            Map<String, Object> result = SYSTEM_INTERFACE.getPReps(BigInteger.ONE, BigInteger.valueOf(100));
            List<Object> registeredPReps = (List<Object>) result.get("preps");
            if (registeredPReps.size() >= 100) {
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }

    public void deployPrep() {
        if (isPRepRegistered()) {
            return;
        }
        try {

            for (Map.Entry<Address, String> prep : preps.entrySet()) {
                KeyWallet wallet = KeyWallet.load(new Bytes(prep.getValue()));
                transfer(foundation.icon.jsonrpc.Address.of(wallet), BigInteger.TEN.pow(24));
                var client = new DefaultScoreClient(
                        chain.getEndpointURL(),
                        chain.networkId,
                        wallet,
                        DefaultScoreClient.ZERO_ADDRESS
                );
                SYSTEM_INTERFACE = new SystemInterfaceScoreClient(client);
                ((SystemInterfaceScoreClient) SYSTEM_INTERFACE).registerPRep(
                        BigInteger.valueOf(2000).multiply(BigInteger.TEN.pow(18)), prep.getKey().toString(),
                        "kokoa@example.com",
                        "USA",
                        "New York", "https://icon.kokoa.com", "https://icon.kokoa.com/json/details.json",
                        "localhost:9082");
            }
        } catch (Exception e) {

        }
    }

    public Map<String, foundation.icon.jsonrpc.Address> getAddresses() {
        return this.addresses;
    }

    public foundation.icon.jsonrpc.Address getAddress(String key) {

        return this.addresses.get(key);
    }
}
