///*
// * Copyright 2020 ICONLOOP Inc.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.iconloop.score.example;
//
//import com.iconloop.score.test.Account;
//import com.iconloop.score.test.Score;
//import com.iconloop.score.test.ServiceManager;
//import com.iconloop.score.test.TestBase;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import score.Address;
//import org.junit.jupiter.api.function.Executable;
//import com.iconloop.score.example.CPFTreasury;
//import java.math.BigInteger;
//import java.security.SecureRandom;
//import java.util.Map;
//import org.mockito.MockedStatic;
//import org.mockito.Mockito;
//import score.Context;
//
//import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//public class CPFTTreasuryTest extends TestBase {
//    private static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
//    private static final Address treasury_score = new Address(new byte[Address.LENGTH]);
//    private static final Address score_address = Address.fromString("cx0000000000000000000000000000000000000000");
//    private static final String name = "CPFTreasury";
//    private static final String symbol = "CPFTreasury";
//    public static final String TAG = "CPF_TREASURY";
//    CPFTreasury cpfTreasury;
//
//
//    private static final ServiceManager sm = getServiceManager();
//    private static final Account owner = sm.createAccount();
//    private static final Account testing_account = sm.createAccount();
//    private static final Account testing_account2 = sm.createAccount();
//
//    private Score tokenScore;
//    private final SecureRandom secureRandom = new SecureRandom();
//
//    @BeforeEach
//    public void setup() throws Exception {
//        tokenScore = sm.deploy(owner, CPFTreasury.class, name, symbol);
//    }
//
//    @Test
//    void name() {
//        assertEquals(name, tokenScore.call("name"));
//    }
//
//    @Test
//    void symbol() {
//        assertEquals(symbol, tokenScore.call("symbol"));
//    }
//
//    @Test
//    void setMaximumTreasuryFundICX(){
//        tokenScore.invoke(owner, "set_maximum_treasury_fund_icx", BigInteger.valueOf(100 * 10^18));
//        assertEquals(BigInteger.valueOf(100 * 10^18), tokenScore.call("get_maximum_treasury_fund_icx"));
//    }
//
//    @Test
//    void setMaximumTreasuryFundBNUSD(){
//        tokenScore.invoke(owner, "set_maximum_treasury_fund_bnusd", BigInteger.valueOf(100 * 10^18));
//        assertEquals(BigInteger.valueOf(100 * 10^18), tokenScore.call("get_maximum_treasury_fund_bnusd"));
//    }
//
//    public void setMaximumTreasuryFundICXExceptions(Account address){
//        try{
//            tokenScore.invoke(address, "set_maximum_treasury_fund_icx", BigInteger.valueOf(100 * 10^18));
//        }
//        catch (Exception e){
//            throw e;
//        }
//    }
//
//    public void setMaximumTreasuryFundBNUSDExceptions(Account address){
//        try{
//            tokenScore.invoke(address, "set_maximum_treasury_fund_bnusd", BigInteger.valueOf(100 * 10^18));
//        }
//        catch (Exception e){
//            throw e;
//        }
//    }
//
//    @Test
//    void setMaximumTreasuryFundICXNotOwner(){
//        Executable setMaximumTreasuryFundICXNotOwner = () -> setMaximumTreasuryFundICXExceptions(testing_account);
//        expectErrorMessage(setMaximumTreasuryFundICXNotOwner, TAG + ": Only owner can call this method");
//    }
//
//    @Test
//    void setMaximumTreasuryFundBNUSDNotOwner(){
//        Executable setMaximumTreasuryFundBNUSDNotOwner = () -> setMaximumTreasuryFundBNUSDExceptions(testing_account);
//        expectErrorMessage(setMaximumTreasuryFundBNUSDNotOwner, TAG + ": Only owner can call this method");
//    }
//
//    @Test
//    void setCPSScore(){
//        tokenScore.invoke(owner, "set_cps_score", score_address);
//        assertEquals(score_address, tokenScore.call("get_cps_score"));
//    }
//
//    @Test
//    void setCPSTreasuryScore(){
//        tokenScore.invoke(owner, "set_cps_treasury_score", score_address);
//        assertEquals(score_address, tokenScore.call("get_cps_treasury_score"));
//    }
//
//    @Test
//    void setBMUSDScore(){
//        tokenScore.invoke(owner, "set_bnUSD_score", score_address);
//        assertEquals(score_address, tokenScore.call("get_bnUSD_score"));
//    }
//
//    @Test
//    void setSICXScore(){
//        tokenScore.invoke(owner, "set_sicx_score", score_address);
//        assertEquals(score_address, tokenScore.call("get_sicx_score"));
//    }
//
//    @Test
//    void setDEXScore(){
//        tokenScore.invoke(owner, "set_dex_score", score_address);
//        assertEquals(score_address, tokenScore.call("get_dex_score"));
//    }
//
//    @Test
//    void setRouterScore(){
//        tokenScore.invoke(owner, "set_router_score", score_address);
//        assertEquals(score_address, tokenScore.call("get_router_score"));
//    }
//
//    void setCPSScoreExceptions(Account address, Address _score){
//        try{
//            tokenScore.invoke(address, "set_cps_score", _score);
//        }
//        catch (Exception e){
//            throw e;
//        }
//    }
//
//    void setCPSTreasuryScoreExceptions(Account address, Address _score){
//        try{
//            tokenScore.invoke(address, "set_cps_treasury_score", _score);
//        }
//        catch (Exception e){
//            throw e;
//        }
//    }
//
//    void setBNUSDScoreExceptions(Account address, Address _score){
//        try{
//            tokenScore.invoke(address, "set_bnUSD_score", _score);
//        }
//        catch (Exception e){
//            throw e;
//        }
//    }
//
//    void setSICXScoreExceptions(Account address, Address _score){
//        try{
//            tokenScore.invoke(address, "set_sicx_score", _score);
//        }
//        catch (Exception e){
//            throw e;
//        }
//    }
//
//    void setDEXScoreExceptions(Account address, Address _score){
//        try{
//            tokenScore.invoke(address, "set_dex_score", _score);
//        }
//        catch (Exception e){
//            throw e;
//        }
//    }
//
//    void setRouterScoreExceptions(Account address, Address _score){
//        try{
//            tokenScore.invoke(address, "set_router_score", _score);
//        }
//        catch (Exception e){
//            throw e;
//        }
//    }
//
//    @Test
//    void setCPSScoreNotOwner(){
//        Executable setCPSScoreNotOwner = () -> setCPSScoreExceptions(testing_account, score_address);
//        expectErrorMessage(setCPSScoreNotOwner, TAG + ": Only owner can call this method");
//    }
//
//    @Test
//    void setCPSTreasuryScoreNotOwner(){
//        Executable setCPSTreasuryScoreNotOwner = () -> setCPSTreasuryScoreExceptions(testing_account, score_address);
//        expectErrorMessage(setCPSTreasuryScoreNotOwner, TAG + ": Only owner can call this method");
//    }
//
//    @Test
//    void setBNUSDScoreNotOwner(){
//        Executable setbnUSDScoreNotOwner = () -> setBNUSDScoreExceptions(testing_account, score_address);
//        expectErrorMessage(setbnUSDScoreNotOwner, TAG + ": Only owner can call this method");
//    }
//
//    @Test
//    void setSICXScoreNotOwner(){
//        Executable setSICXScoreNotOwner = () -> setSICXScoreExceptions(testing_account, score_address);
//        expectErrorMessage(setSICXScoreNotOwner, TAG + ": Only owner can call this method");
//    }
//
//    @Test
//    void setDEXScoreNotOwner(){
//        Executable setDEXScoreNotOwner = () -> setDEXScoreExceptions(testing_account, score_address);
//        expectErrorMessage(setDEXScoreNotOwner, TAG + ": Only owner can call this method");
//    }
//
//    @Test
//    void setRouterScoreNotOwner(){
//        Executable setRouterScoreNotOwner = () -> setRouterScoreExceptions(testing_account, score_address);
//        expectErrorMessage(setRouterScoreNotOwner, TAG + ": Only owner can call this method");
//    }
//
////    @Test
////    void checkMockitoTest(){
////        try(MockedStatic<Context> theMock = Mockito.mockStatic(Context.class)){
////            theMock.
////                    when(() -> Context.getCaller()).
////                    thenReturn(score_address);
////            cpfTreasury = new CPFTreasury("cpf", "cpf");
////            cpfTreasury.is_contract();
////        }
////        catch (Exception e){
////            Context.println(e.toString());
////        }
//////        tokenScore.invoke(owner, "is_contract");
////
////    }
//
//    public void setTreasuryScore(Account owner) {
//        try {
//            tokenScore.invoke(owner, "setTreasuryScore", treasury_score);
//        } catch (Exception e) {
//            throw e;
//        }
//    }
//
//    public void createIBPNPExceptions(Account address, String username) {
//        try {
//            tokenScore.invoke(address, "createIBPNP", username);
//        } catch (Exception e) {
//            throw e;
//        }
//    }
//
//    public void expectErrorMessage(Executable contractCall, String errorMessage) {
//        AssertionError e = Assertions.assertThrows(AssertionError.class, contractCall);
//        assertEquals(errorMessage, e.getMessage());
//    }
//
//    @Test
//    void setTreasuryScoreOwner() {
//        tokenScore.invoke(owner, "setTreasuryScore", treasury_score);
//        assertEquals(treasury_score, tokenScore.call("getTreasuryScore"));
//    }
//
//    @Test
//    void setTreasuryScoreNotOwner() {
//        Executable setTreasuryScoreNotOwner = () -> setTreasuryScore(testing_account);
//        expectErrorMessage(setTreasuryScoreNotOwner, "Only owner can call this method " + TAG);
//    }
//
//    @Test
//    void getTreasuryScore() {
//        tokenScore.invoke(owner, "setTreasuryScore", treasury_score);
//        assertEquals(treasury_score, tokenScore.call("getTreasuryScore"));
//    }
//
//    @Test
//    void createIBPNP() {
//        tokenScore.invoke(owner, "createIBPNP", "Test User1");
//        assertEquals(1, tokenScore.call("balanceOf", owner.getAddress()));
//        assertEquals(owner.getAddress(), tokenScore.call("getWalletByUsername", "TestUser1"));
//        assertEquals("testuser1", tokenScore.call("getUsernameByWallet", owner.getAddress()));
//        Map<String, String> userdata = Map.ofEntries(Map.entry("username", "testuser1"),
//                Map.entry("wallet_address", owner.getAddress().toString()),
//                Map.entry("tokenId", BigInteger.ONE.toString()),
//                Map.entry("amount_wagered", BigInteger.ZERO.toString()),
//                Map.entry("amount_won", BigInteger.ZERO.toString()),
//                Map.entry("amount_lost", BigInteger.ZERO.toString()),
//                Map.entry("bets_won", BigInteger.ZERO.toString()),
//                Map.entry("bets_lost", BigInteger.ZERO.toString()),
//                Map.entry("largest_bet", BigInteger.ZERO.toString()),
//                Map.entry("wager_level", BigInteger.ONE.toString()),
//                Map.entry("linked_wallet", "None")
//        );
//        assertEquals(userdata, tokenScore.call("getUserData", owner.getAddress()));
//    }
//
//    @Test
//    void createIBPNPUserAlreadyHasProfile() {
//        tokenScore.invoke(owner, "createIBPNP", "TestUser1");
//        Executable createIBPNPUserAlreadyHasProfile = () -> createIBPNPExceptions(owner, "TestUser2");
//        expectErrorMessage(createIBPNPUserAlreadyHasProfile, "Reverted(0): This user already has an IconBet Player NFT Profile");
//    }
//
//    @Test
//    void createIBPNPInvalidUserName() {
//        Executable createIBPNPUserAlreadyHasProfile = () -> createIBPNPExceptions(owner, "");
//        expectErrorMessage(createIBPNPUserAlreadyHasProfile, "Reverted(0): Username cannot be an empty string. IconBet Player NFT Profile");
//    }
//
//    @Test
//    void createIBPNPInvalidUserName2() {
//        Executable createIBPNPUserAlreadyHasProfile = () -> createIBPNPExceptions(owner, " TestUser1");
//        expectErrorMessage(createIBPNPUserAlreadyHasProfile, "Reverted(0): Username cannot start or end with a white space.");
//
//    }
//
//    @Test
//    void createIBPNPUsernameAlreadyTaken() {
//        tokenScore.invoke(owner, "createIBPNP", "TestUser1");
//        Executable createIBPNPUserAlreadyHasProfile = () -> createIBPNPExceptions(testing_account, "TestUser1");
//        expectErrorMessage(createIBPNPUserAlreadyHasProfile, "Reverted(0): This username is already taken.");
//    }
//
//
////    @Test
////    void addGameData() {
////        tokenScore.invoke(owner, "setTreasuryScore", treasury_score);
////        tokenScore.invoke(owner, "createIBPNP", "TestUser1");
////        tokenScore.invoke(owner, "addGameData", BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, owner.getAddress(), "take_wager");
////        Map<String, String> userdata = Map.ofEntries(Map.entry("username", "TestUser1"),
////                Map.entry("wallet_address", owner.getAddress().toString()),
////                Map.entry("tokenId", BigInteger.ONE.toString()),
////                Map.entry("amount_wagered", BigInteger.ONE.toString()),
////                Map.entry("amount_won", BigInteger.ZERO.toString()),
////                Map.entry("amount_lost", BigInteger.ONE.toString()),
////                Map.entry("bets_won", BigInteger.ZERO.toString()),
////                Map.entry("bets_lost", BigInteger.ONE.toString()),
////                Map.entry("largest_bet", BigInteger.ONE.toString()),
////                Map.entry("wager_level", BigInteger.ONE.toString()),
////                Map.entry("linked_wallet", "None")
////        );
////        assertEquals(userdata, tokenScore.call("getUserData", owner.getAddress()));
////
////        tokenScore.invoke(owner, "addGameData", BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, owner.getAddress(), "wager_payout");
////        Map<String, String> userdata_payout = Map.ofEntries(Map.entry("username", "TestUser1"),
////                Map.entry("wallet_address", owner.getAddress().toString()),
////                Map.entry("tokenId", BigInteger.ONE.toString()),
////                Map.entry("amount_wagered", BigInteger.ONE.toString()),
////                Map.entry("amount_won", BigInteger.ONE.toString()),
////                Map.entry("amount_lost", BigInteger.ZERO.toString()),
////                Map.entry("bets_won", BigInteger.ONE.toString()),
////                Map.entry("bets_lost", BigInteger.ZERO.toString()),
////                Map.entry("largest_bet", BigInteger.ONE.toString()),
////                Map.entry("wager_level", BigInteger.ONE.toString()),
////                Map.entry("linked_wallet", "None")
////        );
////        assertEquals(userdata_payout, tokenScore.call("getUserData", owner.getAddress()));
////    }
//
//    void requestLinkingWalletExceptions(Account requesting_wallet, Account requested_wallet) {
//        try {
//            tokenScore.invoke(requesting_wallet, "requestLinkingWallet", requested_wallet.getAddress());
//        } catch (Exception e) {
//            throw e;
//        }
//    }
//
//    @Test
//    void requestLinkingWalletRequestSentToOwnWallet() {
//        Executable requestLinkingWalletRequestSentToOwnWallet = () -> requestLinkingWalletExceptions(owner, owner);
//        expectErrorMessage(requestLinkingWalletRequestSentToOwnWallet, "Can not request own account for linking.");
//    }
//
//    @Test
//    void requestLinkingWalletWalletNotHaveIBPNPProfile() {
//        Executable requestLinkingWalletRequestSentToOwnWallet = () -> requestLinkingWalletExceptions(owner, testing_account);
//        expectErrorMessage(requestLinkingWalletRequestSentToOwnWallet, "Both requesting and requested wallet should hae an IBPNP profile. " + TAG);
//    }
//
//    @Test
//    void requestLinkingWallet() {
//        requestLinkingWalletMethod();
//        //noinspection unchecked
//        Map<String, String> wallet_link_data = (Map<String, String>) tokenScore.call("getLinkWalletStatus", owner.getAddress());
//        assertEquals("_pending", wallet_link_data.get("request_status"));
//        assertEquals(testing_account.getAddress().toString(), wallet_link_data.get("requested_wallet"));
//    }
//
//    private void requestLinkingWalletMethod() {
//        tokenScore.invoke(owner, "createIBPNP", "TestUser1");
//        tokenScore.invoke(testing_account, "createIBPNP", "TestUser2");
//        tokenScore.invoke(testing_account2, "createIBPNP", "TestUser3");
//        tokenScore.invoke(owner, "requestLinkingWallet", testing_account.getAddress());
//    }
//
//    void respondToLinkRequestExceptions(Account requested_wallet, Account requesting_wallet, String response) {
//        try {
//            tokenScore.invoke(requested_wallet, "respondToLinkRequest", requesting_wallet.getAddress(), response);
//        } catch (Exception e) {
//            throw e;
//        }
//    }
//
//    @Test
//    void respondToLinkRequest() {
//        requestLinkingWalletMethod();
//        tokenScore.invoke(testing_account, "respondToLinkRequest", owner.getAddress(), "_approve");
//        //noinspection unchecked
//        Map<String, String> wallet_link_data = (Map<String, String>) tokenScore.call("getLinkWalletStatus", owner.getAddress());
//        assertEquals(wallet_link_data.get("request_status"), "_approve");
//        //noinspection unchecked
//        Map<String, String> wallet_link_data_requested_wallet = (Map<String, String>) tokenScore.call("getLinkWalletStatus", owner.getAddress());
//        assertEquals(wallet_link_data_requested_wallet.get("request_status"), "_approve");
//    }
//
//    @Test
//    void respondToLinkRequestRequestStatusOfRequestingWalletNotPending() {
//        requestLinkingWalletMethod();
//        tokenScore.invoke(testing_account, "respondToLinkRequest", owner.getAddress(), "_reject");
//        Executable respondToLinkRequestRequestStatusOfRequestingWalletNotPending = () -> respondToLinkRequestExceptions(testing_account, owner, "_approve");
//        expectErrorMessage(respondToLinkRequestRequestStatusOfRequestingWalletNotPending, "The request status of the requesting wallet should be pending. " + TAG);
//    }
//
//    @Test
//    void respondToLinkRequestWalletAlreadyLinked() {
//        requestLinkingWalletMethod();
//        tokenScore.invoke(testing_account, "respondToLinkRequest", owner.getAddress(), "_approve");
//        tokenScore.invoke(testing_account2, "requestLinkingWallet", testing_account.getAddress());
//        Executable respondToLinkRequestRequestStatusOfRequestingWalletNotPending = () -> respondToLinkRequestExceptions(testing_account, testing_account2, "_approve");
//        expectErrorMessage(respondToLinkRequestRequestStatusOfRequestingWalletNotPending, "The requesting or requested wallet is already to linked to another account.");
//    }
//
//    void changeUsernameExceptions(String new_username){
//        try{
//            tokenScore.invoke(owner, "changeUsername", new_username);
//        }
//        catch (Exception e){
//            throw e;
//        }
//    }
//
//    @Test
//    void changeUsernameWalletHasNoIBPNPProfile(){
//        Executable changeUsernameWalletHasNoIBPNPProfile = () -> changeUsernameExceptions("TestUser2");
//        expectErrorMessage(changeUsernameWalletHasNoIBPNPProfile, "This sender does not have an IBPNP profile." + TAG);
//    }
//
//    @Test
//    void changeUsernameNewUsernameIsSameAsOldUsername(){
//        tokenScore.invoke(owner, "createIBPNP", "TestUser1");
//        Executable changeUsernameWalletHasNoIBPNPProfile = () -> changeUsernameExceptions("Test user1");
//        expectErrorMessage(changeUsernameWalletHasNoIBPNPProfile, "Cannot change into the same username. " + TAG);
//    }
//
//    @Test
//    void changeUsername(){
//        tokenScore.invoke(owner, "createIBPNP", "TestUser1");
//        tokenScore.invoke(owner, "changeUsername", "TestUser2");
//        Map<String, String> userdata = Map.ofEntries(Map.entry("username", "testuser2"),
//                Map.entry("wallet_address", owner.getAddress().toString()),
//                Map.entry("tokenId", BigInteger.ONE.toString()),
//                Map.entry("amount_wagered", BigInteger.ZERO.toString()),
//                Map.entry("amount_won", BigInteger.ZERO.toString()),
//                Map.entry("amount_lost", BigInteger.ZERO.toString()),
//                Map.entry("bets_won", BigInteger.ZERO.toString()),
//                Map.entry("bets_lost", BigInteger.ZERO.toString()),
//                Map.entry("largest_bet", BigInteger.ZERO.toString()),
//                Map.entry("wager_level", BigInteger.ONE.toString()),
//                Map.entry("linked_wallet", "None")
//        );
//        assertEquals(userdata, tokenScore.call("getUserData", owner.getAddress()));
//        assertEquals(owner.getAddress(), tokenScore.call("getWalletByUsername", "testUser2"));
//        assertEquals("testuser2", tokenScore.call("getUsernameByWallet", owner.getAddress()));
//    }
//
//    void unlinkWalletsException(){
//        try{
//            tokenScore.invoke(owner, "unlinkWallets");
//        }
//        catch (Exception e){
//            throw e;
//        }
//    }
//
//    @Test
//    void unlinkWalletsWalletNotLinkedToAnyWallets(){
//        tokenScore.invoke(owner, "createIBPNP", "TestUser1");
//        tokenScore.invoke(testing_account, "createIBPNP", "TestUser2");
//
////        tokenScore.invoke(owner, "requestLinkingWallet", testing_account.getAddress());
////
////        tokenScore.invoke(testing_account, "respondToLinkRequest", owner.getAddress(), "_approve");
//
//        //noinspection LambdaBodyCanBeCodeBlock,Convert2MethodRef
//        Executable unlinkWalletsWalletNotLinkedToAnyWallets = () -> unlinkWalletsException();
//        expectErrorMessage(unlinkWalletsWalletNotLinkedToAnyWallets, "The wallet is not linked to any other wallets. IconBet Player NFT Profile");
//
////        System.out.println(tokenScore.call("getUserData", owner.getAddress()));
////        System.out.println(tokenScore.call("getLinkWalletStatus", owner.getAddress()));
////        tokenScore.invoke(owner, "requestLinkingWallet", testing_account.getAddress());
//    }
//
//    @Test
//    void unlinkWallets(){
//        tokenScore.invoke(owner, "createIBPNP", "TestUser1");
//        tokenScore.invoke(testing_account, "createIBPNP", "TestUser2");
//        tokenScore.invoke(owner, "requestLinkingWallet", testing_account.getAddress());
//        tokenScore.invoke(testing_account, "respondToLinkRequest", owner.getAddress(), "_approve");
//        //noinspection unchecked
//        Map<String, String> userdataowner = (Map<String, String>) tokenScore.call("getUserData", owner.getAddress());
//        //noinspection unchecked
//        Map<String, String> userdatatesting = (Map<String, String>) tokenScore.call("getUserData", testing_account.getAddress());
//        //noinspection unchecked
//        Map<String, String> wallet_link_data_owner = (Map<String, String>) tokenScore.call("getLinkWalletStatus", owner.getAddress());
//        //noinspection unchecked
//        Map<String, String> wallet_link_data_testing = (Map<String, String>) tokenScore.call("getLinkWalletStatus", testing_account.getAddress());
//
//        assertEquals(Address.fromString(userdataowner.get("linked_wallet")), testing_account.getAddress());
//        assertEquals(Address.fromString(userdatatesting.get("linked_wallet")), owner.getAddress());
//        assertEquals(Address.fromString(wallet_link_data_owner.get("requested_wallet")), testing_account.getAddress());
//        assertEquals(Address.fromString(wallet_link_data_testing.get("requested_wallet")), owner.getAddress());
//        assertEquals(wallet_link_data_owner.get("request_status"), "_approve");
//        assertEquals(wallet_link_data_owner.get("request_status"), "_approve");
//
//        tokenScore.invoke(owner, "unlinkWallets");
//        //noinspection unchecked
//        Map<String, String> userdataowner1 = (Map<String, String>) tokenScore.call("getUserData", owner.getAddress());
//        //noinspection unchecked
//        Map<String, String> userdatatesting1 = (Map<String, String>) tokenScore.call("getUserData", testing_account.getAddress());
//        //noinspection unchecked
//        Map<String, String> wallet_link_data_owner1 = (Map<String, String>) tokenScore.call("getLinkWalletStatus", owner.getAddress());
//        //noinspection unchecked
//        Map<String, String> wallet_link_data_testing1 = (Map<String, String>) tokenScore.call("getLinkWalletStatus", testing_account.getAddress());
//
//        assertEquals(userdataowner1.getOrDefault("linked_wallet", "None"), "None");
//        assertEquals(userdatatesting1.getOrDefault("linked_wallet", "None"), "None");
//        assertEquals(Address.fromString(wallet_link_data_owner1.get("requested_wallet")), testing_account.getAddress());
//        assertEquals(Address.fromString(wallet_link_data_testing1.get("requested_wallet")), owner.getAddress());
//        assertEquals(wallet_link_data_owner1.get("request_status"), "_unlinked");
//        assertEquals(wallet_link_data_owner1.get("request_status"), "_unlinked");
//    }
//
//}
