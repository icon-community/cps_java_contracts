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
//
//import static java.math.BigInteger.TEN;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.spy;
//import static org.mockito.Mockito.verify;
//
//public class IRC2CappedTest extends TestBase {
//    private static final String name = "Capped";
//    private static final String symbol = "CAP";
//    private static final int decimals = 18;
//    private static final BigInteger initialSupply = BigInteger.valueOf(1000);
//    private static final BigInteger capped = BigInteger.valueOf(1000000);
//
//    private static BigInteger totalSupply = initialSupply.multiply(TEN.pow(decimals));
//    private static final ServiceManager sm = getServiceManager();
//    private static final Account owner = sm.createAccount();
//    private static final Account testAccount = sm.createAccount();
//    private static Score tokenScore;
//    private static IRC2CappedToken tokenSpy;
//
//
//    @BeforeAll
//    public static void setup() throws Exception{
//        tokenScore = sm.deploy(owner,
//                IRC2CappedToken.class,name,symbol,decimals,initialSupply,capped);
//        owner.addBalance(symbol,totalSupply);
//
//        tokenSpy = (IRC2CappedToken) spy(tokenScore.getInstance());
//        tokenScore.setInstance(tokenSpy);
//    }
//
//    public void mint(Account owner, BigInteger amount){
//        try {
//            tokenScore.invoke(owner,"mint",amount);
//        }
//        catch (AssertionError error){
//            throw error;
//        }
//    }
//
//    public void mintTo(Address caller, BigInteger amount){
//        try {
//            tokenScore.invoke(owner,"mintTo",caller,amount);
//        }
//        catch (AssertionError error){
//            throw error;
//        }
//    }
//
//    public void expectErrorMessage(Executable contractCall, String errorMessage) {
//        AssertionError e = Assertions.assertThrows(AssertionError.class, contractCall);
//        assertEquals(errorMessage, e.getMessage());
//    }
//
//    @DisplayName("Mint token by owner")
//    @Test
//    void mint_by_owner(){
//        final Address zeroAddress = new Address(new byte[Address.LENGTH]);
//        BigInteger amount = TEN.pow(decimals);
//        tokenScore.invoke(owner,"mint",amount);
//        owner.addBalance(symbol,amount);
//        assertEquals(owner.getBalance(symbol),tokenScore.call("balanceOf",owner.getAddress()));
//        totalSupply = totalSupply.add(amount);
//        assertEquals(totalSupply,tokenScore.call("totalSupply"));
//        verify(tokenSpy).Transfer(zeroAddress,owner.getAddress(),amount,"mint".getBytes());
//
//    }
//
//    @DisplayName("Mint token by other accounts exceeding cap limit")
//    @Test
//    void mint_exceed_capped_token(){
//        BigInteger amount = BigInteger.valueOf(1000000000).pow(decimals);
//        Executable mintExceedCapCaller = () ->mint(testAccount,amount);
//        String expectedErrorMessage = "Only owner can mint tokens";
//        expectErrorMessage(mintExceedCapCaller,expectedErrorMessage);
//    }
//
//    @DisplayName("Mint token by owner exceeding cap limit ")
//    @Test
//    void mint_exceed_capped_token_by_owner(){
//        BigInteger amount = BigInteger.valueOf(1000000000).pow(decimals);
//        Executable mintExceedCapCaller = () ->mint(owner,amount);
//        String expectedErrorMessage = "Cap:capped exceeded";
//        expectErrorMessage(mintExceedCapCaller,expectedErrorMessage);
//    }
//
//    @DisplayName("Mint to another address by owner")
//    @Test
//    void mintTo_by_owner(){
//        final Address zeroAddress = new Address(new byte[Address.LENGTH]);
//        BigInteger amount = TEN.pow(decimals);
//        tokenScore.invoke(owner,"mintTo", testAccount.getAddress(),amount);
//
//        testAccount.addBalance(symbol,amount);
//        assertEquals(testAccount.getBalance(symbol),tokenScore.call("balanceOf",testAccount.getAddress()));
//        verify(tokenSpy).Transfer(zeroAddress,testAccount.getAddress(),amount,"mint".getBytes());
//    }
//
//    @DisplayName("Mint to another address by owner exceeding cap limit")
//    @Test
//    void mintTo_capped_exceed_by_owner(){
//        BigInteger amount = BigInteger.valueOf(1000000000).pow(decimals);
//        // tokenScore.invoke(owner,"mintTo", testAccount.getAddress(),amount);
//        Executable mintToExceedCapCaller = () ->mintTo(testAccount.getAddress(),amount);
//        String expectedErrorMessage = "Cap:capped exceeded";
//        expectErrorMessage(mintToExceedCapCaller,expectedErrorMessage);
//
//    }
//
//}
