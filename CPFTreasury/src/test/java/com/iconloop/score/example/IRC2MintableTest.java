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
//import static java.math.BigInteger.*;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.spy;
//import static org.mockito.Mockito.verify;
//
//public class IRC2MintableTest extends TestBase {
//    private static final String name = "Mintable";
//    private static final String symbol = "MIT";
//    private static final int decimals = 18;
//    private static final BigInteger initialSupply = BigInteger.valueOf(1000);
//    private static BigInteger totalSupply = initialSupply.multiply(TEN.pow(decimals));
//
//    private static final ServiceManager sm = getServiceManager();
//    private static final Account owner = sm.createAccount();
//    private static final Account testAccount = sm.createAccount();
//    private static Score tokenScore;
//    private static IRC2MintableToken tokenSpy;
//
//    @BeforeAll
//    public static void setup() throws Exception{
//        tokenScore = sm.deploy(owner, IRC2MintableToken.class,
//                name, symbol, decimals, initialSupply);
//        owner.addBalance(symbol,totalSupply);
//
//        tokenSpy = (IRC2MintableToken) spy(tokenScore.getInstance());
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
//    public void isMinter(Address minter){
//        try {
//            tokenScore.call("isMinter",minter);
//        }
//        catch (AssertionError error){
//            throw error;
//        }
//    }
//
//    public void setMinter(Account test,Address minter){
//        try {
//            tokenScore.invoke(test,"setMinter",minter);
//        }
//        catch (AssertionError error){
//            throw (error);
//        }
//    }
//
//    public void expectErrorMessage(Executable contractCall, String errorMessage) {
//        AssertionError e = Assertions.assertThrows(AssertionError.class, contractCall);
//        assertEquals(errorMessage, e.getMessage());
//    }
//
//    @DisplayName("Minting new tokens")
//    @Test
//    void mint_test_by_owner(){
//        final Address zeroAddress = new Address(new byte[Address.LENGTH]);
//        BigInteger amount = TEN.pow(decimals);
//        tokenScore.invoke(owner,"mint",amount);
//
//        owner.addBalance(symbol,amount);
//        assertEquals(owner.getBalance(symbol),tokenScore.call("balanceOf",owner.getAddress()));
//
//        totalSupply = totalSupply.add(amount);
//        assertEquals(totalSupply,tokenScore.call("totalSupply"));
//
//        verify(tokenSpy).Transfer(zeroAddress,owner.getAddress(),amount,"mint".getBytes());
//    }
//
//    @DisplayName("Trying to mint token by other account")
//    @Test
//    void mint_test(){
//        BigInteger amount = TEN.pow(decimals);
//        Executable mintNotByOwnerCall = () -> mint(testAccount,amount);
//        String expectedErrorMessage = "Mint: minters or owners can only mint new tokens";
//        expectErrorMessage(mintNotByOwnerCall,expectedErrorMessage);
//
//    }
//
//    @DisplayName("Minting tokens to others address")
//    @Test
//    void mintTo_test(){
//        final Address zeroAddress = new Address(new byte[Address.LENGTH]);
//        BigInteger amount = TEN.pow(decimals);
//        tokenScore.invoke(owner,"mintTo",testAccount.getAddress(),amount);
//
//        testAccount.addBalance(symbol,amount);
//        assertEquals(testAccount.getBalance(symbol),tokenScore.call("balanceOf",testAccount.getAddress()));
//
//        totalSupply = totalSupply.add(amount);
//        assertEquals(totalSupply,tokenScore.call("totalSupply"));
//        verify(tokenSpy).Transfer(zeroAddress,testAccount.getAddress(),amount,"mint".getBytes());
//    }
//
//    @DisplayName("Setting minter by owner")
//    @Test
//    void set_minter_test_by_owner(){
//        tokenScore.invoke(owner,"setMinter",testAccount.getAddress());
//        assertEquals(testAccount.getAddress(),tokenScore.call("getMinter"));
//    }
//
//    @DisplayName("Setting minter by another account/ not by owner")
//    @Test
//    void set_minter(){
//        Executable setMinterByOwnerCall = () -> setMinter(testAccount,owner.getAddress());
//        String expectedErrorMessage = "Minter: only owners can set minters";
//        expectErrorMessage(setMinterByOwnerCall,expectedErrorMessage);
//    }
//
//
//}
