package community.icon.cps.score.test.integration;

import community.icon.cps.score.test.integration.scores.SystemInterfaceScoreClient;
import community.icon.cps.score.test.integration.model.Score;
import community.icon.cps.score.test.integration.scores.SystemInterface;
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
        decentralizeChain();
        this.addresses = new ScoreDeployer(this,contracts).deployContracts();

        ownerClient = new CPSClient(this,owner);
        testClient = new CPSClient(this, createWalletWithBalance(BigInteger.TEN.pow(24)));
    }

    private void decentralizeChain(){
//        if (isPRepRegistered()) {
//            return;
//        }
        setStake();
        setDelegationOfPreps();
        setBonderList();
        setBondsofPreps();
        setGodStake();
    }

    public void setGodStake(){
        SYSTEM_INTERFACE = new SystemInterfaceScoreClient(godClient);
        SYSTEM_INTERFACE.setStake(BigInteger.valueOf(9_000_000).multiply(BigInteger.TEN.pow(18)));
        SystemInterface.Delegation[] delegation = new SystemInterface.Delegation[1];
        delegation[0] = new SystemInterface.Delegation();
        delegation[0].address = score.Address.fromString(chain.godWallet.getAddress().toString());
        delegation[0].value = BigInteger.valueOf(8000000).multiply(BigInteger.TEN.pow(18));
        SYSTEM_INTERFACE.setDelegation(delegation);

        score.Address[] addresses = new score.Address[1];
        addresses[0] = score.Address.fromString(chain.godWallet.getAddress().toString());
        SYSTEM_INTERFACE.setBonderList(addresses);

        SystemInterface.Bond[] bonds = new SystemInterface.Bond[1];
        bonds[0] = new SystemInterface.Bond();
        bonds[0].address = score.Address.fromString(chain.godWallet.getAddress().toString());
        bonds[0].value = BigInteger.valueOf(1000000).multiply(BigInteger.TEN.pow(18));
        SYSTEM_INTERFACE.setBond(bonds);
        System.out.println("god wallet setup done");
    }

    private void setStake(){
        int count = 0;
        for (Map.Entry<Address, String> prep : preps.entrySet()) {
            if (prep.getKey().equals(Address.fromString("hxb6b5791be0b5ef67063b3c10b840fb81514db2fd"))){
                continue;
            }
            if (count < 7) {
                KeyWallet wallet = KeyWallet.load(new Bytes(prep.getValue()));
                var client = new DefaultScoreClient(
                        chain.getEndpointURL(),
                        chain.networkId,
                        wallet,
                        DefaultScoreClient.ZERO_ADDRESS
                );
                SYSTEM_INTERFACE = new SystemInterfaceScoreClient(client);
                SYSTEM_INTERFACE.setStake(BigInteger.valueOf(100_000).multiply(BigInteger.TEN.pow(18)));
                count++;
            }
            else {
                break;
            }
        }
        System.out.println("set is done");
    }

    public void setDelegationOfPreps(){
        int count = 0;
        for (Map.Entry<Address, String> prep : preps.entrySet()) {
            if (prep.getKey().equals(Address.fromString("hxb6b5791be0b5ef67063b3c10b840fb81514db2fd"))){
                continue;
            }
            if (count < 7) {
                KeyWallet wallet = KeyWallet.load(new Bytes(prep.getValue()));
                SystemInterface.Delegation[] delegations = new SystemInterface.Delegation[1];
                delegations[0] = new SystemInterface.Delegation();
                delegations[0].address = score.Address.fromString(prep.getKey().toString());
                delegations[0].value = BigInteger.valueOf(80_000).multiply(BigInteger.TEN.pow(18));
                var client = new DefaultScoreClient(
                        chain.getEndpointURL(),
                        chain.networkId,
                        wallet,
                        DefaultScoreClient.ZERO_ADDRESS
                );
                SYSTEM_INTERFACE = new SystemInterfaceScoreClient(client);
                SYSTEM_INTERFACE.setDelegation(delegations);
                count++;
            }
            else {
                break;
            }
        }


        System.out.println("delegation of preps done ");
    }

    public void setBonderList(){
        int count = 0;
        for (Map.Entry<Address, String> prep : preps.entrySet()) {
            if (prep.getKey().equals(Address.fromString("hxb6b5791be0b5ef67063b3c10b840fb81514db2fd"))){
                continue;
            }
            if (count < 7) {
                KeyWallet wallet = KeyWallet.load(new Bytes(prep.getValue()));
                score.Address[] bonderList = new score.Address[1];
                bonderList[0] = score.Address.fromString(prep.getKey().toString());
                var client = new DefaultScoreClient(
                        chain.getEndpointURL(),
                        chain.networkId,
                        wallet,
                        DefaultScoreClient.ZERO_ADDRESS
                );
                SYSTEM_INTERFACE = new SystemInterfaceScoreClient(client);
                SYSTEM_INTERFACE.setBonderList(bonderList);
                count++;



            }
            else {
                break;
            }


        }
        System.out.println("the bonder list");

    }

    public void setBondsofPreps(){
        int count = 0;
        for (Map.Entry<Address, String> prep : preps.entrySet()) {
            if (prep.getKey().equals(Address.fromString("hxb6b5791be0b5ef67063b3c10b840fb81514db2fd"))){
                continue;
            }
            if (count < 7){
                KeyWallet wallet = KeyWallet.load(new Bytes(prep.getValue()));
                SystemInterface.Bond[] bond = new SystemInterface.Bond[1];
                bond[0] = new SystemInterface.Bond();
                bond[0].address = score.Address.fromString(prep.getKey().toString());
                bond[0].value = BigInteger.valueOf(10_000).multiply(BigInteger.TEN.pow(18));
                var client = new DefaultScoreClient(
                        chain.getEndpointURL(),
                        chain.networkId,
                        wallet,
                        DefaultScoreClient.ZERO_ADDRESS
                );
                SYSTEM_INTERFACE = new SystemInterfaceScoreClient(client);
                SYSTEM_INTERFACE.setBond(bond);
                count++;
            }
            else {
                break;
            }
        }

        System.out.println("bond of preps done ");
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

    public CPSClient newClient(BigInteger balance) throws Exception {
        CPSClient client = new CPSClient(this, createWalletWithBalance(balance));
        cpsClients.put(client.getAddress(), client);
        return client;
    }

    public CPSClient customClient(String privateKey){
        CPSClient client = new CPSClient(this,KeyWallet.load(new Bytes(privateKey)));
        cpsClients.put(client.getAddress(),client);
        return client;
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
