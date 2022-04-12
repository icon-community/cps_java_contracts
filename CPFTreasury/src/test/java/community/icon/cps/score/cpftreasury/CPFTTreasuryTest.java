/*
 * Copyright 2020 ICONLOOP Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package community.icon.cps.score.cpftreasury;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import score.Address;
import org.junit.jupiter.api.function.Executable;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import score.Context;
import score.DictDB;
import score.VarDB;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class CPFTTreasuryTest extends TestBase {
    private static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
    private static final Address treasury_score = new Address(new byte[Address.LENGTH]);
    private static final Address score_address = Address.fromString("cx0000000000000000000000000000000000000000");
    private static final Address sicxScore = Address.fromString("cx0000000000000000000000000000000000000001");
    private static final Address bnUSDScore = Address.fromString("cx0000000000000000000000000000000000000002");
    private static final Address dexScore = Address.fromString("cx0000000000000000000000000000000000000003");
    private static final Address cpsTreasuryScore = Address.fromString("cx0000000000000000000000000000000000000004");

    private static final String name = "CPF_TREASURY";
    private static final String symbol = "CPF_TREASURY";
    public static final String TAG = "CPF_TREASURY";
    CPFTreasury cpfTreasury;
    private static final BigInteger MULTIPLIER = new BigInteger("1000000000000000000");


    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account testing_account = sm.createAccount();
    private static final Account testing_account2 = sm.createAccount();

    private Score tokenScore;
    private final SecureRandom secureRandom = new SecureRandom();

    DictDB<String, BigInteger> proposalBudgets = Mockito.mock(DictDB.class);
    VarDB<Integer> swapState = Mockito.mock(VarDB.class);
    VarDB<Integer> swapCount = Mockito.mock(VarDB.class);

    @BeforeEach
    public void setup() throws Exception {
        tokenScore = sm.deploy(owner, CPFTreasury.class);
    }

    @Test
    void name() {
        assertEquals(name, tokenScore.call("name"));
    }


    public void setMaximumTreasuryFundICXExceptions(Account address) {
        try {
            tokenScore.invoke(address, "setMaximumTreasuryFundBnusd", BigInteger.valueOf(100));
        } catch (Exception e) {
            throw e;
        }
    }

    public void setMaximumTreasuryFundBNUSDExceptions(Account address) {
        try {
            tokenScore.invoke(address, "set_maximum_treasury_fund_bnusd", BigInteger.valueOf(100 * 10 ^ 18));
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    void setCPSScore() {
        tokenScore.invoke(owner, "setCpsScore", score_address);
        assertEquals(score_address, tokenScore.call("getCpsScore"));
    }

    @Test
    void setCPSTreasuryScore() {
        tokenScore.invoke(owner, "setCpsTreasuryScore", score_address);
        assertEquals(score_address, tokenScore.call("getCpsTreasuryScore"));
    }

    @Test
    void setBMUSDScore() {
        tokenScore.invoke(owner, "setBnUSDScore", score_address);
        assertEquals(score_address, tokenScore.call("getBnUSDScore"));
    }

    @Test
    void setSICXScore() {
        tokenScore.invoke(owner, "setSicxScore", score_address);
        assertEquals(score_address, tokenScore.call("getSicxScore"));
    }

    @Test
    void setDEXScore() {
        tokenScore.invoke(owner, "setDexScore", score_address);
        assertEquals(score_address, tokenScore.call("getDexScore"));
    }

    @Test
    void setRouterScore() {
        tokenScore.invoke(owner, "setRouterScore", score_address);
        assertEquals(score_address, tokenScore.call("getRouterScore"));
    }

    void setCPSScoreExceptions(Account address, Address _score) {
        try {
            tokenScore.invoke(address, "setCpsScore", _score);
        } catch (Exception e) {
            throw e;
        }
    }

    void setCPSTreasuryScoreExceptions(Account address, Address _score) {
        try {
            tokenScore.invoke(address, "setCpsTreasuryScore", _score);
        } catch (Exception e) {
            throw e;
        }
    }

    void setBNUSDScoreExceptions(Account address, Address _score) {
        try {
            tokenScore.invoke(address, "setBnUSDScore", _score);
        } catch (Exception e) {
            throw e;
        }
    }

    void setSICXScoreExceptions(Account address, Address _score) {
        try {
            tokenScore.invoke(address, "setSicxScore", _score);
        } catch (Exception e) {
            throw e;
        }
    }

    void setDEXScoreExceptions(Account address, Address _score) {
        try {
            tokenScore.invoke(address, "setDexScore", _score);
        } catch (Exception e) {
            throw e;
        }
    }

    void setRouterScoreExceptions(Account address, Address _score) {
        try {
            tokenScore.invoke(address, "setRouterScore", _score);
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    void setCPSScoreNotOwner() {
        Executable setCPSScoreNotOwner = () -> setCPSScoreExceptions(testing_account, score_address);
        expectErrorMessage(setCPSScoreNotOwner, TAG + ": Only owner can call this method");
    }

    @Test
    void setCPSTreasuryScoreNotOwner() {
        Executable setCPSTreasuryScoreNotOwner = () -> setCPSTreasuryScoreExceptions(testing_account, score_address);
        expectErrorMessage(setCPSTreasuryScoreNotOwner, TAG + ": Only owner can call this method");
    }

    @Test
    void setBNUSDScoreNotOwner() {
        Executable setbnUSDScoreNotOwner = () -> setBNUSDScoreExceptions(testing_account, score_address);
        expectErrorMessage(setbnUSDScoreNotOwner, TAG + ": Only owner can call this method");
    }

    @Test
    void setSICXScoreNotOwner() {
        Executable setSICXScoreNotOwner = () -> setSICXScoreExceptions(testing_account, score_address);
        expectErrorMessage(setSICXScoreNotOwner, TAG + ": Only owner can call this method");
    }

    @Test
    void setDEXScoreNotOwner() {
        Executable setDEXScoreNotOwner = () -> setDEXScoreExceptions(testing_account, score_address);
        expectErrorMessage(setDEXScoreNotOwner, TAG + ": Only owner can call this method");
    }

    @Test
    void setRouterScoreNotOwner() {
        Executable setRouterScoreNotOwner = () -> setRouterScoreExceptions(testing_account, score_address);
        expectErrorMessage(setRouterScoreNotOwner, TAG + ": Only owner can call this method");
    }

    @Test
    void transferProposalFundToCPSTreasury() {
        tokenScore.invoke(owner, "setCpsScore", score_address);
        VarDB<Address> balancedDollar = Mockito.mock(VarDB.class);

        try (MockedStatic<Context> theMock = Mockito.mockStatic(Context.class)) {
            theMock.
                    when(() -> Context.getCaller()).
                    thenReturn(score_address);
            Mockito.when(proposalBudgets.getOrDefault("Proposal 1", null)).thenReturn(BigInteger.valueOf(10));
            theMock.when(() -> Context.getAddress()).thenReturn(tokenScore.getAddress());
            theMock.when(() -> Context.call(balancedDollar.get(), "balanceOf", tokenScore.getAddress())).thenReturn(BigInteger.valueOf(1000 * 10 ^ 18));
            tokenScore.invoke(owner, "transfer_proposal_fund_to_cps_treasury", "Proposal 1", 2, testing_account.getAddress(), testing_account2.getAddress(), "bnUSD", BigInteger.valueOf(10));
            System.out.println(tokenScore.call("getProposalKeys"));
            System.out.println(tokenScore.call("getProposalBudgets", "Proposal 1"));
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    void updateProposalFund() {
        VarDB<Address> balancedDollar = mock(VarDB.class);
        transferProposalFundMethod();
        try (MockedStatic<Context> theMock = Mockito.mockStatic(Context.class)) {
            theMock.when(() -> Context.getCaller()).thenReturn(score_address);
            theMock.when(() -> Context.getBalance(Context.getAddress())).thenReturn(BigInteger.valueOf(1000).multiply(MULTIPLIER));
            theMock.when(() -> Context.call(balancedDollar.get(), "balanceOf", Context.getAddress())).thenReturn(BigInteger.valueOf(1000).multiply(MULTIPLIER));
            tokenScore.invoke(owner, "update_proposal_fund", "Proposal 1", "bnUSD", BigInteger.valueOf(100).multiply(MULTIPLIER), BigInteger.valueOf(1));
            List<Map<String, ?>> details = (List<Map<String, ?>>) tokenScore.call("get_proposal_details", 0, 5);
            Map<String, ?> expected = Map.of("_budget_transfer", BigInteger.valueOf(204).multiply(MULTIPLIER).divide(BigInteger.ONE), "_ipfs_hash", "Proposal 1");
            assertEquals(details.get(0).get("_budget_transfer"), expected.get("_budget_transfer").toString());

        }
    }

    void setMaxCapIcxAndBnusd(){
        VarDB<Address> cpsScore = mock(VarDB.class);
        try(MockedStatic<Context> theMock = Mockito.mockStatic(Context.class)){
            theMock.when(() -> Context.call(cpsScore.get(), "is_admin", Context.getCaller())).thenReturn(true);
            tokenScore.invoke(owner, "setMaximumTreasuryFundIcx", BigInteger.valueOf(2000).multiply(MULTIPLIER));
            tokenScore.invoke(owner, "setMaximumTreasuryFundBnusd", BigInteger.valueOf(2000).multiply(MULTIPLIER));

        }
    }

    @Test
    void setMaxCapIcxAndBnusdTest(){
        VarDB<Address> cpsScore = mock(VarDB.class);
        try(MockedStatic<Context> theMock = Mockito.mockStatic(Context.class)){
            theMock.when(() -> Context.call(cpsScore.get(), "is_admin", Context.getCaller())).thenReturn(true);
            tokenScore.invoke(owner, "setMaximumTreasuryFundIcx", BigInteger.valueOf(2000).multiply(MULTIPLIER));
            tokenScore.invoke(owner, "setMaximumTreasuryFundBnusd", BigInteger.valueOf(2000).multiply(MULTIPLIER));

        }
    }

    @Test
    void disqualifyProposalFund() {
        setMaxCapIcxAndBnusd();

        transferProposalFundMethod();
        VarDB<Address> balancedDollar = mock(VarDB.class);

        tokenScore.invoke(owner, "setCpsTreasuryScore", score_address);
        try(MockedStatic<Context> theMock = Mockito.mockStatic(Context.class)){
            theMock.when(() -> Context.getBalance(Context.getAddress())).thenReturn(BigInteger.valueOf(1000).multiply(MULTIPLIER));
            theMock.when(() -> Context.call(balancedDollar.get(), "balanceOf", Context.getAddress())).thenReturn(BigInteger.valueOf(1000).multiply(MULTIPLIER));
            tokenScore.invoke(owner, "disqualify_proposal_fund", "Proposal 1", BigInteger.valueOf(80).multiply(MULTIPLIER), "bnUSD", score_address);
        }
        System.out.println(tokenScore.call("get_proposal_details",  0,5));

    }

    @Test
    void swapTokens() {
        VarDB<Address> balancedDollar = mock(VarDB.class);
        VarDB<Address> dexScore = mock(VarDB.class);
        VarDB<Address> cpsScore = mock(VarDB.class);

        tokenScore.invoke(owner, "setCpsTreasuryScore", score_address);
        tokenScore.invoke(owner, "setProposalKeysAndBudgets");

        try (MockedStatic<Context> theMock = Mockito.mockStatic(Context.class)) {
            theMock.when(() -> Context.call(cpsScore.get(), "is_admin", Context.getCaller())).thenReturn(true);
            tokenScore.invoke(owner, "setMaximumTreasuryFundIcx", BigInteger.valueOf(2000).multiply(MULTIPLIER));
            tokenScore.invoke(owner, "setMaximumTreasuryFundBnusd", BigInteger.valueOf(2000).multiply(MULTIPLIER));
            System.out.println("balue" + BigInteger.valueOf(2000).multiply(MULTIPLIER));
            System.out.println(tokenScore.call("getProposalBudgets", "Proposal 1"));
            theMock.when(() -> Context.getCaller()).thenReturn(score_address);
            theMock.when(() -> Context.call(dexScore.get(), "getPrice", 1)).thenReturn(BigInteger.valueOf(12).multiply(MULTIPLIER).divide(BigInteger.TEN));
            theMock.when(() -> Context.call(dexScore.get(), "getPrice", 2)).thenReturn(BigInteger.valueOf(8).multiply(MULTIPLIER).divide(BigInteger.TEN));
            theMock.when(() -> Context.call(balancedDollar.get(), "balanceOf", Context.getAddress())).thenReturn(BigInteger.valueOf(1000).multiply(MULTIPLIER));
            Mockito.when(swapState.getOrDefault(0)).thenReturn(10);

            Mockito.when(swapCount.getOrDefault(0)).thenReturn(0);
            theMock.when(() -> Context.getBalance(Context.getAddress())).thenReturn(BigInteger.valueOf(10000).multiply(MULTIPLIER));
            tokenScore.invoke(owner, "swap_tokens", 10);
        }
    }

    @Test
    void resetSwapState() {
        setMaxCapIcxAndBnusd();
        VarDB<Address> cpsScore = mock(VarDB.class);
        VarDB<Integer> swapState = mock(VarDB.class);
        swapState.set(10);
        tokenScore.invoke(owner, "setCpsScore", score_address);
        try (MockedStatic<Context> theMock = Mockito.mockStatic(Context.class)) {
            theMock.when(() -> Context.getCaller()).thenReturn(testing_account.getAddress());
            theMock.when(() -> Context.call(score_address, "is_admin", Context.getCaller())).thenReturn(true);
            tokenScore.invoke(owner, "reset_swap_state");
            assertEquals(tokenScore.call("get_swap_state_status"), Map.of("state", 0, "count", 0));
        }
    }

    private void transferProposalFundMethod() {
        VarDB<Address> balancedDollar = mock(VarDB.class);

        tokenScore.invoke(owner, "setCpsScore", score_address);
        try (MockedStatic<Context> theMock = Mockito.mockStatic(Context.class)) {
            theMock.when(() -> Context.getCaller()).thenReturn(score_address);
            theMock.when(() -> Context.call(balancedDollar.get(), "balanceOf", Context.getAddress())).thenReturn(BigInteger.valueOf(1000).multiply(MULTIPLIER));
            tokenScore.invoke(owner, "transfer_proposal_fund_to_cps_treasury", "Proposal 1", 2, testing_account.getAddress(), testing_account2.getAddress(), "bnUSD", BigInteger.valueOf(100).multiply(MULTIPLIER));
            tokenScore.invoke(owner, "transfer_proposal_fund_to_cps_treasury", "Proposal 2", 2, testing_account.getAddress(), testing_account2.getAddress(), "bnUSD", BigInteger.valueOf(110).multiply(MULTIPLIER));


        }
        System.out.println(tokenScore.call("get_proposal_details", 0, 5));
    }

    @Test
    void returnFundAmountExtraBnUSD(){
        setMaxCapIcxAndBnusd();
        VarDB<Address> balancedDollar = mock(VarDB.class);
        tokenScore.invoke(owner, "setCpsScore", score_address);
        tokenScore.invoke(owner, "setBnUSDScore", score_address);
        tokenScore.invoke(owner, "setDexScore", score_address);
        tokenScore.invoke(owner, "setSicxScore", score_address);

        try(MockedStatic<Context> theMock = Mockito.mockStatic(Context.class)){
            theMock.when(() -> Context.getBalance(Context.getAddress())).thenReturn(BigInteger.valueOf(2000).multiply(MULTIPLIER));
            theMock.when(() -> Context.call(score_address, "balanceOf", Context.getAddress())).thenReturn(BigInteger.valueOf(2001).multiply(MULTIPLIER));
            tokenScore.invoke(owner, "return_fund_amount", owner.getAddress(), score_address, "bnUSD", BigInteger.valueOf(2001).multiply(MULTIPLIER));

        }
    }

    @Test
    void returnFundAmountExtraICX(){
        setMaxCapIcxAndBnusd();
        VarDB<Address> balancedDollar = mock(VarDB.class);
        tokenScore.invoke(owner, "setCpsScore", score_address);
        tokenScore.invoke(owner, "setBnUSDScore", score_address);
        tokenScore.invoke(owner, "setDexScore", score_address);
        tokenScore.invoke(owner, "setSicxScore", score_address);

        try(MockedStatic<Context> theMock = Mockito.mockStatic(Context.class)){
            theMock.when(() -> Context.getBalance(Context.getAddress())).thenReturn(BigInteger.valueOf(2001).multiply(MULTIPLIER));
            theMock.when(() -> Context.call(score_address, "balanceOf", Context.getAddress())).thenReturn(BigInteger.valueOf(2000).multiply(MULTIPLIER));
            tokenScore.invoke(owner, "return_fund_amount", owner.getAddress(), score_address, "bnUSD", BigInteger.valueOf(2001).multiply(MULTIPLIER));

        }
    }

    @Test
    void addFundExtraICX(){
        setMaxCapIcxAndBnusd();
        VarDB<Address> balancedDollar = mock(VarDB.class);
        tokenScore.invoke(owner, "setCpsScore", score_address);
        tokenScore.invoke(owner, "setBnUSDScore", score_address);
        tokenScore.invoke(owner, "setDexScore", score_address);
        tokenScore.invoke(owner, "setSicxScore", score_address);

        try(MockedStatic<Context> theMock = Mockito.mockStatic(Context.class)){
            theMock.when(() -> Context.getBalance(Context.getAddress())).thenReturn(BigInteger.valueOf(2001).multiply(MULTIPLIER));
            theMock.when(() -> Context.call(score_address, "balanceOf", Context.getAddress())).thenReturn(BigInteger.valueOf(2000).multiply(MULTIPLIER));
            tokenScore.invoke(owner, "add_fund");

        }
    }

    @Test
    void addFundExtraBnUSD(){
        setMaxCapIcxAndBnusd();
        VarDB<Address> balancedDollar = mock(VarDB.class);
        tokenScore.invoke(owner, "setCpsScore", score_address);
        tokenScore.invoke(owner, "setBnUSDScore", score_address);
        tokenScore.invoke(owner, "setDexScore", score_address);
        tokenScore.invoke(owner, "setSicxScore", score_address);

        try(MockedStatic<Context> theMock = Mockito.mockStatic(Context.class)){
            theMock.when(() -> Context.getBalance(Context.getAddress())).thenReturn(BigInteger.valueOf(2000).multiply(MULTIPLIER));
            theMock.when(() -> Context.call(score_address, "balanceOf", Context.getAddress())).thenReturn(BigInteger.valueOf(2001).multiply(MULTIPLIER));
            tokenScore.invoke(owner, "add_fund");

        }
    }

    public void expectErrorMessage(Executable contractCall, String errorMessage) {
        AssertionError e = Assertions.assertThrows(AssertionError.class, contractCall);
        assertEquals(errorMessage, e.getMessage());
    }
}


















































