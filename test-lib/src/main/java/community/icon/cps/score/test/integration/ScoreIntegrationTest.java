package community.icon.cps.score.test.integration;

import community.icon.cps.score.lib.interfaces.SystemInterface;
import foundation.icon.icx.KeyWallet;
import foundation.icon.icx.Wallet;
import foundation.icon.icx.data.Bytes;
import foundation.icon.jsonrpc.Address;
import foundation.icon.jsonrpc.model.Hash;
import foundation.icon.jsonrpc.model.TransactionResult;
import foundation.icon.score.client.DefaultScoreClient;
import foundation.icon.score.client.RevertedException;

import community.icon.cps.score.lib.interfaces.SystemInterfaceScoreClient;
import net.bytebuddy.pool.TypePool;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.function.Executable;
import score.UserRevertedException;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static community.icon.cps.score.test.integration.Env.Chain;
import static community.icon.cps.score.test.integration.Env.getDefaultChain;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static community.icon.cps.score.lib.interfaces.SystemInterface.Delegation;
import static community.icon.cps.score.lib.interfaces.SystemInterface.Bond;

@Tag("integration")
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
public interface ScoreIntegrationTest {
    Chain chain = getDefaultChain();
    DefaultScoreClient godClient = new DefaultScoreClient(chain.getEndpointURL(), chain.networkId, chain.godWallet,
            DefaultScoreClient.ZERO_ADDRESS);
    DefaultScoreClient client = new DefaultScoreClient(chain.getEndpointURL(), chain.networkId, null, null);

    SystemInterfaceScoreClient systemScore = new SystemInterfaceScoreClient(godClient);
    public static final BigInteger EXA = BigInteger.valueOf(1_000_000_000_000_000_000L);

    @SuppressWarnings("unchecked")
    static void registerPreps() throws Exception {

        Map<String, Object> getPreps;
        String privateKey = "";

        try {
            getPreps = systemScore.getPReps(BigInteger.ONE, BigInteger.valueOf(7));
        } catch (Exception e) {
            privateKey = registerPrep();
            getPreps = systemScore.getPReps(BigInteger.ONE, BigInteger.valueOf(7));
        }

        List<Map<String, Object>> prepList = (List<Map<String, Object>>) getPreps.get("preps");
        int prepCount = prepList.size();
        if (prepCount >= 7) {
            return;
        }
        int remainingPrepsToRegister = 7 - prepCount;
        for (int i = 0; i < remainingPrepsToRegister; i++) {
            privateKey = privateKey + registerPrep();
        }
        PrintStream out = new PrintStream(new FileOutputStream("privateKey.txt"));
        System.out.println("private keys: " + privateKey);
        out.println(privateKey);
        BufferedReader br = new BufferedReader(new FileReader("privateKey.txt"));
        System.out.println("Reading the content of the file: " + br.readLine());
    }

    private static String registerPrep() throws Exception {
        KeyWallet owner = createWalletWithBalance(BigInteger.TEN.pow(24));
        String privateKey = String.valueOf(owner.getPrivateKey());
//            PrintStream out = new PrintStream(new FileOutputStream("privateKey.txt"));
//        System.out.println("private keys: " + privateKey);
//            out.println(privateKey);
        DefaultScoreClient godClient = new DefaultScoreClient(chain.getEndpointURL(), chain.networkId, owner,
                DefaultScoreClient.ZERO_ADDRESS);
        SystemInterfaceScoreClient systemScore = new SystemInterfaceScoreClient(godClient);
        systemScore.registerPRep(BigInteger.valueOf(2000).multiply(BigInteger.TEN.pow(18)), "test",
                "kokoa@example.com", "USA", "New York", "https://icon.kokoa.com",
                "https://icon.kokoa.com/json/details.json", "localhost:9082");
        return privateKey;
    }

    public static void registerGodClient() {
        systemScore.registerPRep(BigInteger.valueOf(2000).multiply(BigInteger.TEN.pow(18)), "test",
                "kokoa@example.com", "USA", "New York", "https://icon.kokoa.com",
                "https://icon.kokoa.com/json/details.json", "localhost:9082");

    }

    public static void registerPrepByPrivateKey(KeyWallet owner){
        DefaultScoreClient godClient = new DefaultScoreClient(chain.getEndpointURL(), chain.networkId, owner,
                DefaultScoreClient.ZERO_ADDRESS);
        SystemInterfaceScoreClient systemScore = new SystemInterfaceScoreClient(godClient);
        systemScore.registerPRep(BigInteger.valueOf(2000).multiply(BigInteger.TEN.pow(18)), "test",
                "kokoa@example.com", "USA", "New York", "https://icon.kokoa.com",
                "https://icon.kokoa.com/json/details.json", "localhost:9082");
    }

    public static void setStake(KeyWallet[] owner) {
        for (int i = 0; i < owner.length; i++) {
            DefaultScoreClient godClient = new DefaultScoreClient(chain.getEndpointURL(), chain.networkId, owner[i],
                    DefaultScoreClient.ZERO_ADDRESS);
            SystemInterfaceScoreClient systemScore = new SystemInterfaceScoreClient(godClient);
            systemScore.setStake(BigInteger.valueOf(1000).multiply(EXA));
        }
    }

    public static void setGodStake(){
        systemScore.setStake(BigInteger.valueOf(9000000).multiply(EXA));
        Delegation[] delegation = new Delegation[1];
        delegation[0] = new Delegation();
        delegation[0].address = score.Address.fromString(chain.godWallet.getAddress().toString());
        delegation[0].value = BigInteger.valueOf(8000000).multiply(EXA);
        systemScore.setDelegation(delegation);

        score.Address[] addresses = new score.Address[1];
        addresses[0] = score.Address.fromString(chain.godWallet.getAddress().toString());
        systemScore.setBonderList(addresses);

        Bond[] bonds = new Bond[1];
        bonds[0] = new Bond();
        bonds[0].address = score.Address.fromString(chain.godWallet.getAddress().toString());
        bonds[0].value = BigInteger.valueOf(1000000).multiply(EXA);
        systemScore.setBond(bonds);
    }
    public static void setDelegation(KeyWallet[] owner){
        for (int i = 0; i < owner.length; i++) {
            Delegation[] delegations = new Delegation[1];
            delegations[0] = new Delegation();
            delegations[0].address = score.Address.fromString(owner[i].getAddress().toString());
            delegations[0].value = BigInteger.valueOf(400).multiply(EXA);
            DefaultScoreClient godClient = new DefaultScoreClient(chain.getEndpointURL(), chain.networkId, owner[i],
                    DefaultScoreClient.ZERO_ADDRESS);
            SystemInterfaceScoreClient systemScore = new SystemInterfaceScoreClient(godClient);
            systemScore.setDelegation(delegations);
        }
    }

    public static void setBond(KeyWallet[] owner) {
        for (int i = 0; i < owner.length; i++) {
            Bond[] bond = new Bond[1];
            bond[0] = new Bond();
            bond[0].address = score.Address.fromString(owner[i].getAddress().toString());
            bond[0].value = BigInteger.valueOf(400).multiply(EXA);
            DefaultScoreClient godClient = new DefaultScoreClient(chain.getEndpointURL(), chain.networkId, owner[i],
                    DefaultScoreClient.ZERO_ADDRESS);
            SystemInterfaceScoreClient systemScore = new SystemInterfaceScoreClient(godClient);
            systemScore.setBond(bond);
        }
    }

    public static void setBonderList(KeyWallet[] owner){
        for (int i = 0; i < owner.length; i++) {
            score.Address[] bonderList = new score.Address[1];
            bonderList[0] = score.Address.fromString(owner[i].getAddress().toString());
            DefaultScoreClient godClient = new DefaultScoreClient(chain.getEndpointURL(), chain.networkId, owner[i],
                    DefaultScoreClient.ZERO_ADDRESS);
            SystemInterfaceScoreClient systemScore = new SystemInterfaceScoreClient(godClient);
            systemScore.setBonderList(bonderList);
        }
    }

    static KeyWallet createWalletWithBalance(BigInteger amount) throws Exception {
        KeyWallet wallet = KeyWallet.create();
        Address address = DefaultScoreClient.address(wallet.getAddress().toString());
        transfer(address, amount);
        return wallet;
    }

    static void transfer(Address address, BigInteger amount) {
        godClient._transfer(address, amount, null);
    }

    static DefaultScoreClient deploy(Wallet wallet, String name, Map<String, Object> params) {
        String path = getFilePath(name);
        return DefaultScoreClient._deploy(chain.getEndpointURL(), chain.networkId, wallet, path, params);
    }

    static Hash deployAsync(Wallet wallet, String name, Map<String, Object> params) {
        String path = getFilePath(name);
        return DefaultScoreClient._deployAsync(chain.getEndpointURL(), chain.networkId, wallet, path, params);
    }

    static DefaultScoreClient getDeploymentResult(Wallet wallet, Hash hash) {
        return  DefaultScoreClient.getDeploymentResult(chain.getEndpointURL(), chain.networkId, wallet, hash);
    }

    static String getFilePath(String key) {
        String path = System.getProperty(key);
        if (path == null) {
            throw new IllegalArgumentException("No such property: " + key);
        }
        return path;
    }

    static <T> int indexOf(T[] array, T value) {
        return indexOf(array, value::equals);
    }

    static <T> int indexOf(T[] array, Predicate<T> predicate) {
        for (int i = 0; i < array.length; i++) {
            if (predicate.test(array[i])) {
                return i;
            }
        }
        return -1;
    }

    static boolean contains(Map<String, Object> map, String key, Object value) {
        return contains(map, key, value::equals);
    }

    static <T> boolean contains(Map<String, T> map, String key, Predicate<T> predicate) {
        return map.containsKey(key) && predicate.test(map.get(key));
    }

    static <T> List<T> eventLogs(TransactionResult txr, String signature, Address scoreAddress,
                                 Function<TransactionResult.EventLog, T> mapperFunc, Predicate<T> filter) {
        Predicate<TransactionResult.EventLog> predicate = (el) -> el.getIndexed().get(0).equals(signature);
        if (scoreAddress != null) {
            predicate = predicate.and((el) -> el.getScoreAddress().toString().equals(scoreAddress.toString()));
        }
        Stream<T> stream = txr.getEventLogs().stream().filter(predicate).map(mapperFunc);
        if (filter != null) {
            stream = stream.filter(filter);
        }
        return stream.collect(Collectors.toList());
    }

    static void waitByNumOfBlock(long numOfBlock) {
        waitByHeight(client._lastBlockHeight().add(BigInteger.valueOf(numOfBlock)));
    }

    static void waitByHeight(long waitHeight) {
        waitByHeight(BigInteger.valueOf(waitHeight));
    }

    static void waitByHeight(BigInteger waitHeight) {
        BigInteger height = client._lastBlockHeight();
        while (height.compareTo(waitHeight) < 0) {
            System.out.println("height: " + height + ", waitHeight: " + waitHeight);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            height = client._lastBlockHeight();
        }
    }

    static void balanceCheck(Address address, BigInteger value, Executable executable) {
        BigInteger balance = client._balance(address);
        try {
            executable.execute();
        } catch (UserRevertedException | RevertedException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        assertEquals(balance.add(value), client._balance(address));
    }

    @FunctionalInterface
    interface EventLogsSupplier<T> {
        List<T> apply(TransactionResult txr, Address address, Predicate<T> filter);
    }

    static <T> Consumer<TransactionResult> eventLogChecker(
            Address address, EventLogsSupplier<T> supplier, Consumer<T> consumer) {
        return (txr) -> {
            List<T> eventLogs = supplier.apply(txr, address, null);
            assertEquals(1, eventLogs.size());
            if (consumer != null) {
                consumer.accept(eventLogs.get(0));
            }
        };
    }

    static <T> Consumer<TransactionResult> eventLogsChecker(Address address, EventLogsSupplier<T> supplier,
                                                            Consumer<List<T>> consumer) {
        return (txr) -> {
            List<T> eventLogs = supplier.apply(txr, address, null);
            if (consumer != null) {
                consumer.accept(eventLogs);
            }
        };
    }

    static Consumer<TransactionResult> dummyConsumer() {
        return (txr) -> {

        };
    }

    static Wallet getOrGenerateWallet(String prefix, Properties properties) {
        Wallet wallet = DefaultScoreClient.wallet(prefix, properties);
        return wallet == null ? generateWallet() : wallet;
    }

    static KeyWallet generateWallet() {
        try {
            return KeyWallet.create();
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }
}


