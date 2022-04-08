//package com.iconloop.score.example;
//
//import com.iconloop.score.test.Account;
//import com.iconloop.score.test.Score;
//import com.iconloop.score.test.ServiceManager;
//import com.iconloop.score.test.TestBase;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.function.Executable;
//import score.Address;
//
//import java.math.BigInteger;
//import static java.math.BigInteger.TEN;
//
//import static org.mockito.Mockito.spy;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.verify;
//
//public class IRC2PausableTest extends TestBase {
//    private static final String name ="Pausable";
//    private static final String symbol = "PAS";
//    private static final int decimals = 18;
//    private static final BigInteger initialSupply = BigInteger.valueOf(1000);
//    private static final BigInteger totalSupply = initialSupply.multiply(BigInteger.TEN.pow(decimals));
//
//    private static final ServiceManager sm = getServiceManager();
//    private static final Account owner = sm.createAccount();
//    private static final Account testAccount = sm.createAccount();
//    private static Score tokenScore;
//    private static IRC2PausableToken tokenSpy;
//
//    @BeforeAll
//    public static void setup() throws Exception{
//        tokenScore = sm.deploy(owner,IRC2PausableToken.class,
//                name,symbol,decimals,initialSupply);
//        owner.addBalance(symbol,totalSupply);
//
//        tokenSpy = (IRC2PausableToken) spy(tokenScore.getInstance());
//        tokenScore.setInstance(tokenSpy);
//    }
//
//    public void transfer(Address address, BigInteger amount){
//
//        try {
//            tokenScore.invoke(owner,"transfer",address,amount,"transfer".getBytes());
//        }
//        catch (AssertionError error){
//            throw  error;
//        }
//    }
//
//    public void expectErrorMessage(Executable contractCall, String errorMessage) {
//        AssertionError e = Assertions.assertThrows(AssertionError.class, contractCall);
//        assertEquals(errorMessage, e.getMessage());
//    }
//
//    @DisplayName("set pause status")
//    @Test
//    void set_pause(){
//        tokenScore.invoke(owner,"setPause",true);
//        assertEquals(true,tokenScore.call("setPause",true));
//
//        tokenScore.invoke(owner,"setPause", false);
//        assertEquals(false,tokenScore.call("setPause",false));
//    }
//
//    @DisplayName("get pause status")
//    @Test
//    void get_pause(){
//        // setting value false
//        tokenScore.invoke(owner,"setPause",false);
//        assertEquals(false,tokenScore.call("getPause"));
//
//        // setting value true
//        tokenScore.invoke(owner,"setPause",true);
//        assertEquals(true,tokenScore.call("getPause"));
//    }
//
//    @DisplayName("pause contract")
//    @Test
//    void pause_contract(){
//        tokenScore.invoke(owner,"pause");
//        assertEquals(true,tokenScore.call("getPause"));
//    }
//
//    @DisplayName("unpause contract")
//    @Test
//    void unpause_contract(){
//        tokenScore.invoke(owner,"unpause");
//        assertEquals(false,tokenScore.call("getPause"));
//    }
//
//    @DisplayName("token transfer when contract is not paused")
//    @Test
//    void transfer_when_not_paused(){
//        // contract not paused
//        BigInteger amount = TEN.pow(decimals);
//        System.out.println(amount);
//        tokenScore.invoke(owner,"transfer",testAccount.getAddress(), amount, "transfer".getBytes());
//
//        testAccount.addBalance(symbol,amount);
//        assertEquals(testAccount.getBalance(symbol),tokenScore.call("balanceOf",testAccount.getAddress()));
//
//        owner.subtractBalance(symbol,amount);
//        assertEquals(owner.getBalance(symbol),tokenScore.call("balanceOf",owner.getAddress()));
//
//        verify(tokenSpy).Transfer(owner.getAddress(),testAccount.getAddress(),amount,"transfer".getBytes());
//
//    }
//
//    @DisplayName("token transfer when contract is paused")
//    @Test
//    void transfer_when_paused(){
//        BigInteger amount = TEN.pow(decimals);
//
//        tokenScore.invoke(owner,"setPause",true);
//
//        Executable transfer_when_paused_call= () -> transfer(testAccount.getAddress(),amount);
//        String expectedErrorMessage = "Transfer: Paused token can not be transferred";
//        expectErrorMessage(transfer_when_paused_call,expectedErrorMessage);
//
//    }
//}
