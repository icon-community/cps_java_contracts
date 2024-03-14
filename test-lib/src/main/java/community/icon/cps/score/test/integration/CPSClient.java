package community.icon.cps.score.test.integration;

import community.icon.cps.score.test.integration.scores.SystemInterfaceScoreClient;
import community.icon.cps.score.test.integration.scores.CPFTreasuryInterface;
import community.icon.cps.score.test.integration.scores.CPFTreasuryInterfaceScoreClient;
import community.icon.cps.score.test.integration.scores.CPSCoreInterface;
import community.icon.cps.score.test.integration.scores.CPSCoreInterfaceScoreClient;
import community.icon.cps.score.test.integration.scores.CPSTreasuryInterface;
import community.icon.cps.score.test.integration.scores.CPSTreasuryInterfaceScoreClient;
import community.icon.cps.score.test.integration.scores.SystemInterface;
import foundation.icon.icx.KeyWallet;

import foundation.icon.jsonrpc.Address;
import foundation.icon.score.client.ScoreClient;

import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static community.icon.cps.score.test.integration.Environment.chain;


public class CPSClient {
    private final KeyWallet wallet;
    private CPS cps;
    private Map<String, score.Address> addressMap;

    @ScoreClient
    public CPSTreasuryInterface cpsTreasury;

    @ScoreClient
    public CPFTreasuryInterface cpfTreasury;

    @ScoreClient
    public CPSCoreInterface cpsCore;

    @ScoreClient
    public SystemInterface systemScore;


    public CPSClient(CPS cps, KeyWallet wallet){
        this.cps = cps;
        this.wallet = wallet;
        init();
    }

    private void init(){
        for (Entry<String, Address> entry : this.cps.getAddresses().entrySet()) {
            System.out.println("---------------is system queried----------------" + entry.getKey());
            switch (entry.getKey()){
                case "cpsCore":
                    cpsCore = new CPSCoreInterfaceScoreClient(chain.getEndpointURL(), chain.networkId, wallet,
                            entry.getValue());
                    break;
                case "cpsTreasury":
                    cpsTreasury = new CPSTreasuryInterfaceScoreClient(chain.getEndpointURL(), chain.networkId, wallet,
                            entry.getValue());
                    break;
                case "cpfTreasury":
                    cpfTreasury = new CPFTreasuryInterfaceScoreClient(chain.getEndpointURL(), chain.networkId, wallet,
                            entry.getValue());
                    break;
                case "systemScore":
                    systemScore = new SystemInterfaceScoreClient(chain.getEndpointURL(), chain.networkId, wallet,
                            entry.getValue());
                    break;
                default:
                    throw new NoSuchElementException(entry.getKey() + " score not found!!");

            }

        }
    }

    public Map<String, score.Address> getContractAddresses() {
        if (addressMap == null) {
            addressMap = this.cps.getAddresses().entrySet()
                    .stream()
                    .collect(Collectors.toMap(Entry::getKey,
                            entry -> score.Address.fromString(entry.getValue().toString())));
        }
        return addressMap;
    }

    public score.Address getAddress() {
        return score.Address.fromString(wallet.getAddress().toString());
    }

    public KeyWallet getWallet() {
        return wallet;
    }
}
