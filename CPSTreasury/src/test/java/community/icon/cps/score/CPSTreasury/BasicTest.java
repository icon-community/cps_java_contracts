//package com.iconloop.score.example;
//
//import com.iconloop.score.test.Account;
//import com.iconloop.score.test.Score;
//import com.iconloop.score.test.ServiceManager;
//import com.iconloop.score.test.TestBase;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//
//import java.math.BigInteger;
//
//import static java.math.BigInteger.TEN;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//public class BasicTest extends TestBase {
//    private static final String name = "MyIRC2Token";
//    private static final String symbol = "MST";
//    private static final int decimals = 18;
//    private static final BigInteger initialSupply = BigInteger.valueOf(1000);
//    private static final BigInteger totalSupply = initialSupply.multiply(TEN.pow(decimals));
//
//    private static final ServiceManager sm = getServiceManager();
//    private static final Account owner = sm.createAccount();
//    private static Score tokenScore;
//
//    @BeforeAll
//    public static void setup() throws Exception{
//        tokenScore = sm.deploy(owner,IRC2BasicToken.class,name,symbol,decimals,initialSupply);
//        owner.addBalance(symbol,totalSupply);
//    }
//
//    @Test
//    void name(){
//        assertEquals(name,tokenScore.call("name"));
//    }
//
//    @Test
//    void symbol(){
//        assertEquals(symbol,tokenScore.call("symbol"));
//    }
//
//    @Test
//    void decimals(){
//        assertEquals(decimals,tokenScore.call("decimals"));
//    }
//
//    @Test
//    void totalSupply(){
//        assertEquals(totalSupply,tokenScore.call("totalSupply"));
//    }
//
//    @Test
//    void balanceOf(){
//        assertEquals(owner.getBalance(symbol),
//                tokenScore.call("balanceOf",tokenScore.getOwner().getAddress()));
//    }
//
//    @Test
//    void transfer(){
//        Account account1 = sm.createAccount();
//        BigInteger value = TEN.pow(decimals);
//        tokenScore.invoke(owner,"transfer",account1.getAddress(),value, "to account1".getBytes());
//        owner.subtractBalance(symbol,value);
//        assertEquals(owner.getBalance(symbol),
//                tokenScore.call("balanceOf",tokenScore.getOwner().getAddress()));
//        assertEquals(value,tokenScore.call("balanceOf",account1.getAddress()));
//
//        // transfer self
//        tokenScore.invoke(account1,"transfer",account1.getAddress(),value,"self transfer".getBytes());
//        assertEquals(value,tokenScore.call("balanceOf",account1.getAddress()));
//
//    }
//}
