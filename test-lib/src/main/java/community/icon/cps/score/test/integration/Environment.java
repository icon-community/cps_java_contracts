package community.icon.cps.score.test.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import community.icon.cps.score.test.integration.scores.SystemInterface;
import foundation.icon.icx.KeyWallet;
import foundation.icon.icx.Wallet;
import foundation.icon.icx.crypto.KeystoreException;

import foundation.icon.score.client.DefaultScoreClient;
import foundation.icon.score.client.ScoreClient;

import java.io.*;
import java.math.BigInteger;

import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.ClassOrderer;
import score.Address;




public class Environment {

    static {
        String envFile = System.getProperty("env.props","conf/env.props");
        Properties props = new Properties();
        try {
            InputStream is = ClassOrderer.ClassName.class.getClassLoader()
                    .getResourceAsStream(envFile);
            props.load(is);
            assert is != null;
            is.close();
        } catch (IOException e) {
            System.err.printf("'%s' does not exist\n", envFile);
            throw new IllegalArgumentException(e.getMessage());
        }

//        FileInputStream configReader;
//        try {
//            configReader = new FileInputStream(envFile);
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//        Properties props = new Properties();
//        try {
//            props.load(configReader);
//            configReader.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        String confPath = Path.of(envFile).getParent().toString() + "/";
        readProperties(props, confPath);
    }

    public static Chain chain;
    public static DefaultScoreClient godClient;
    public static Map<Address,String> preps;

    @ScoreClient
    public static SystemInterface SYSTEM_INTERFACE;

    private static void readProperties(Properties props, String confPath){
        String chainName = "chain";
        String nid = props.getProperty(chainName +".nid");
        if (nid == null){
            throw new IllegalArgumentException("nid not found");
        }

        String godWalletPath = confPath + props.getProperty(chainName + ".godWallet");


        String godPassword = props.getProperty(chainName + ".godPassword");
        KeyWallet godWallet;
        try {
            godWallet = readWalletFromFile(godWalletPath, godPassword);
            loadPREPs(confPath + "preps.json");
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        String nodeName = "node";
        String url = props.getProperty(nodeName + ".url");
        if (url == null) {
            throw new IllegalArgumentException("node url not found");
        }

        String apiVersion = props.getProperty(nodeName + ".apiVersion");
        if (apiVersion == null) {
            throw new IllegalArgumentException("apiVersion not found");
        }
        chain = new Chain(BigInteger.valueOf(Integer.parseInt(nid.substring(2), 16)), godWallet, url, apiVersion);
        godClient = new DefaultScoreClient(
                chain.getEndpointURL(),
                chain.networkId,
                chain.godWallet,
                DefaultScoreClient.ZERO_ADDRESS
        );
    }

    private static void loadPREPs(String path) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream is = ClassLoader.getSystemResource(path).openStream();

        preps = objectMapper.readValue(is, new TypeReference<Map<Address, String>>() {
        });

    }

    private static KeyWallet readWalletFromFile(String path, String password) throws IOException {
        InputStream is = ClassLoader.getSystemResource(path).openStream();
        try {
            File file = File.createTempFile("tmp", ".tmp");
            FileUtils.copyInputStreamToFile(is, file);
            return KeyWallet.load(password, file);
        } catch (KeystoreException e) {
            e.printStackTrace();
            throw new IOException("Key load failed!");
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static class Chain {

        public final BigInteger networkId;
        public final Wallet godWallet;
        private final String nodeUrl;
        private final String apiVersion;

        public Chain(BigInteger networkId, Wallet godWallet, String url, String apiVersion) {
            this.networkId = networkId;
            this.godWallet = godWallet;
            this.nodeUrl = url;
            this.apiVersion = apiVersion;
        }

        public String getEndpointURL() {
            return this.nodeUrl + "/api/v" + apiVersion;
        }
    }
}
