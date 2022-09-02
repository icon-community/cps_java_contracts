package community.icon.cps.score.test.integration;

import foundation.icon.icx.KeyWallet;
import foundation.icon.score.client.DefaultScoreClient;

import community.icon.cps.score.lib.interfaces.*;

import static community.icon.cps.score.test.integration.ScoreIntegrationTest.chain;

public class CPSClient {
    private final KeyWallet wallet;
    public CPSTreasuryInterfaceScoreClient cpsTreasury;
    public CPFTreasuryInterfaceScoreClient cpfTreasury;

    public CPSClient(CPS cps, KeyWallet wallet){
        this.wallet = wallet;
        cpsTreasury = new CPSTreasuryInterfaceScoreClient(chain.getEndpointURL(), chain.networkId, wallet,
                cps.cpsTreasury._address());
        cpfTreasury = new CPFTreasuryInterfaceScoreClient(chain.getEndpointURL(), chain.networkId, wallet, cps.cpfTreasury._address());
    }

    public score.Address getAddress() {
        return score.Address.fromString(wallet.getAddress().toString());
    }
}
