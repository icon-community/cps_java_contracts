package community.icon.cps.score.test.integration;

import foundation.icon.icx.KeyWallet;
import foundation.icon.icx.Wallet;
import foundation.icon.jsonrpc.Address;
import foundation.icon.jsonrpc.model.TransactionResult;
import foundation.icon.score.client.DefaultScoreClient;
import foundation.icon.score.client.RevertedException;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.function.Executable;
import score.UserRevertedException;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static community.icon.cps.score.test.integration.Environment.godClient;
import static foundation.icon.jsonrpc.IconJsonModule.hexToBytes;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("integration")
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
public interface ScoreIntegrationTest {

    static KeyWallet createWalletWithBalance(BigInteger amount) throws Exception {
        KeyWallet wallet = KeyWallet.create();
        Address address = DefaultScoreClient.address(wallet.getAddress().toString());
        transfer(address, amount);
        return wallet;
    }

    static void transfer(Address address, BigInteger amount) {
        godClient._transfer(address, amount, null);
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

    static <T> List<T> eventLogs(TransactionResult txr,
                                 String signature,
                                 Address scoreAddress,
                                 Function<TransactionResult.EventLog, T> mapperFunc,
                                 Predicate<T> filter) {
        Predicate<TransactionResult.EventLog> predicate =
                (el) -> el.getIndexed().get(0).equals(signature);
        if (scoreAddress != null) {
            predicate = predicate.and((el) -> el.getScoreAddress().equals(scoreAddress));
        }
        Stream<T> stream = txr.getEventLogs().stream()
                .filter(predicate)
                .map(mapperFunc);
        if (filter != null) {
            stream = stream.filter(filter);
        }
        return stream.collect(Collectors.toList());
    }

    static void waitByNumOfBlock(long numOfBlock) {
        waitByHeight(godClient._lastBlockHeight().add(BigInteger.valueOf(numOfBlock)));
    }

    static void waitByHeight(long waitHeight) {
        waitByHeight(BigInteger.valueOf(waitHeight));
    }

    static void waitByHeight(BigInteger waitHeight) {
        BigInteger height = godClient._lastBlockHeight();
        while (height.compareTo(waitHeight) < 0) {
            System.out.println("height: " + height + ", waitHeight: " + waitHeight);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            height = godClient._lastBlockHeight();
        }
    }

    static void balanceCheck(Address address, BigInteger value, Executable executable) {
        BigInteger balance = godClient._balance(address);
        try {
            executable.execute();
        } catch (UserRevertedException | RevertedException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        assertEquals(balance.add(value), godClient._balance(address));
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

    static <T> Consumer<TransactionResult> eventLogsChecker(
            Address address, EventLogsSupplier<T> supplier, Consumer<List<T>> consumer) {
        return (txr) -> {
            List<T> eventLogs = supplier.apply(txr, address, null);
            if (consumer != null) {
                consumer.accept(eventLogs);
            }
        };
    }

    Wallet tester = getOrGenerateWallet("tester.", System.getProperties());

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

    interface Faker {

        com.github.javafaker.Faker faker = new com.github.javafaker.Faker();
        Random random = new Random();

        static Address address(Address.Type type) {
            byte[] body = hexToBytes(faker.crypto().sha256().substring(0, (Address.LENGTH - 1) * 2));
            return new Address(type, body);
        }

        static byte[] bytes(int length) {
            byte[] bytes = new byte[length];
            random.nextBytes(bytes);
            return bytes;
        }
    }

}


