package community.icon.cps.score.cpstreasury;

import community.icon.cps.score.cpstreasury.db.ProposalData;
import score.*;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;
import scorex.util.ArrayList;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import community.icon.cps.score.cpstreasury.utils.consts;

public class CPSTreasury extends ProposalData{
    private static final String TAG = "CPS_Treasury";
    private static final String PROPOSAL_DB_PREFIX = "proposal";

    private static final String ID = "id";
    private static final String PROPOSALS_KEYS = "_proposals_keys";
    private static final String PROPOSALS_KEY_LIST_INDEX = "proposals_key_list_index";
    private static final String FUND_RECORD = "fund_record";
    private static final String INSTALLMENT_FUND_RECORD = "installment_fund_record";

    private static final String TOTAL_INSTALLMENT_COUNT = "_total_installment_count";
    private static final String TOTAL_TIMES_INSTALLMENT_PAID = "_total_times_installment_paid";
    private static final String TOTAL_TIMES_REWARD_PAID = "_total_times_reward_paid";
    private static final String TOTAL_INSTALLMENT_PAID = "_total_installment_paid";
    private static final String TOTAL_REWARD_PAID = "_total_reward_paid";
    private static final String INSTALLMENT_AMOUNT = "installment_amount";
    private static final String SPONSOR_BOND_AMOUNT = "sponsor_bond_amount";
    private static final String CPS_SCORE = "_cps_score";
    private static final String CPF_TREASURY_SCORE = "_cpf_treasury_score";
    private static final String BALANCED_DOLLAR = "balanced_dollar";

    private static final String SPONSOR_ADDRESS = "sponsor_address";
    private static final String CONTRIBUTOR_ADDRESS = "contributor_address";
    private static final String STATUS = "status";
    private static final String IPFS_HASH = "ipfs_hash";
    private static final String SPONSOR_REWARD = "sponsor_reward";
    private static final String TOTAL_BUDGET = "total_budget";

    private static final String ACTIVE = "active";
    private static final String DISQUALIFIED = "disqualified";
    private static final String COMPLETED = "completed";


    private final VarDB<String> id = Context.newVarDB(ID, String.class);
    private final ArrayDB<String> proposalsKeys = Context.newArrayDB(PROPOSALS_KEYS, String.class);
    private final DictDB<String, Integer> proposalsKeyListIndex = Context.newDictDB(PROPOSALS_KEY_LIST_INDEX, Integer.class);
    private final DictDB<String, BigInteger> fundRecord = Context.newDictDB(FUND_RECORD, BigInteger.class);
    private final BranchDB<String, DictDB<String, BigInteger>> installmentFundRecord = Context.newBranchDB(INSTALLMENT_FUND_RECORD, BigInteger.class);

    private final VarDB<Address> cpfTreasuryScore = Context.newVarDB(CPF_TREASURY_SCORE, Address.class);
    private final VarDB<Address> cpsScore = Context.newVarDB(CPS_SCORE, Address.class);
    private final VarDB<Address> balancedDollar = Context.newVarDB(BALANCED_DOLLAR, Address.class);

    public  CPSTreasury(){
    }

    @External(readonly = true)
    public String name(){
        return TAG;
    }
    @Payable
    public void fallback(){
        Context.revert(TAG + ": ICX can only be send by CPF Treasury Score");
    }

    private void setId(String _val){
        id.set(_val);
    }

    private String getId(){
        return id.get();
    }

    private String proposalPrefix(String _proposal_key){
        return PROPOSAL_DB_PREFIX + "|" + id.get() + "|" + _proposal_key;
    }

    private Boolean proposalExists(String _ipfs_key){
        return proposalsKeyListIndex.getOrDefault(_ipfs_key, null) != null;
    }

    private void validateAdmins(){
        Boolean isAdmin = callScore(Boolean.class, cpsScore.get(), "is_admin", Context.getCaller());
        Context.require(isAdmin,
                TAG + ": Only admins can call this method");

    }

    private void validateAdminScore(Address _score){
        validateAdmins();
        Context.require(_score.isContract(), TAG + "Target " + _score + " is not a score.");
    }

    private void validateCpsScore(){
        Context.require(Context.getCaller().equals(cpsScore.get()),
                TAG + ": Only CPS score " + cpsScore.get() + " can send fund using this method.");
    }

    private void validateCpfTreasuryScore(){
        Context.require(Context.getCaller().equals(cpfTreasuryScore.get()),
                TAG + ": Only CPF Treasury score " + cpfTreasuryScore.get() + " can send fund using this method.");
    }

    private void addRecord(ProposalData.ProposalAttributes _proposal){
        ProposalData proposalData = new ProposalData();
        String ipfs_hash = _proposal.ipfs_hash;
        Context.require(!proposalExists(ipfs_hash), TAG + ": Already have this project");
        proposalsKeys.add(ipfs_hash);
        proposalData.addDataToProposalDB(_proposal, ipfs_hash);
        proposalsKeyListIndex.set(ipfs_hash, proposalsKeys.size() - 1);
    }

    private Map<String, ?> getProjects(String _proposal_key){
        ProposalData proposalData = new ProposalData();
        return proposalData.getDataFromProposalDB(_proposal_key);
    }

    @External
    public void setCpsScore(Address _score){
        validateAdminScore(_score);
        cpsScore.set(_score);
    }

    @External(readonly = true)
    public Address getCpsScore(){
        return cpsScore.get();
    }

    @External
    public void setCpfTreasuryScore(Address _score){
        validateAdminScore(_score);
        cpfTreasuryScore.set(_score);
    }

    @External(readonly = true)
    public Address getCpfTreasuryScore(){
        return cpfTreasuryScore.get();
    }

    @External
    public void setBnUSDScore(Address _score){
        validateAdminScore(_score);
        balancedDollar.set(_score);
    }

    @External
    public Address getBnUSDScore(){
        return balancedDollar.get();
    }

    @External(readonly = true)
    public Map<String, ?> get_contributor_projected_fund(Address _wallet_address){
        ProposalData proposalData = new ProposalData();
        BigInteger totalAmountToBePaidICX = BigInteger.ZERO;
        BigInteger totalAmountToBePaidbnUSD = BigInteger.ZERO;
        List<Map<String, String>> projectDetails = new ArrayList<>();
        for (int i = 0; i < proposalsKeys.size(); i++){
            String _ipfs_key = proposalsKeys.get(i);
            Map<String, ?> proposal_details = proposalData.getDataFromProposalDB(_ipfs_key);
            if (proposal_details.get(consts.STATUS).equals(DISQUALIFIED)){
                if (proposal_details.get(consts.SPONSOR_ADDRESS).equals(_wallet_address.toString())){
                    int totalInstallment = (int) proposal_details.get(consts.PROJECT_DURATION);
                    int totalPaidCount = (int) proposal_details.get(consts.INSTALLMENT_COUNT);
                    if (totalPaidCount < totalInstallment){
                        String flag = (String) proposal_details.get(consts.TOKEN);
                        BigInteger totalBudget = (BigInteger) proposal_details.get(consts.TOTAL_BUDGET);
                        BigInteger totalPaidAmount = (BigInteger) proposal_details.get(consts.WITHDRAW_AMOUNT);

                        Map<String, String> project_details = Map.of(
                                consts.IPFS_HASH, _ipfs_key,
                                consts.TOKEN, flag,
                                consts.TOTAL_BUDGET, totalBudget.toString(),
                                consts.TOTAL_INSTALLMENT_PAID, totalPaidAmount.toString(),
                                consts.TOTAL_INSTALLMENT_COUNT, String.valueOf(totalInstallment),
                                consts.TOTAL_TIMES_INSTALLMENT_PAID, String.valueOf(totalPaidCount),
                                consts.INSTALLMENT_AMOUNT, totalBudget.divide(BigInteger.valueOf(totalInstallment)).toString());

                        projectDetails.add(project_details);
                        if (flag.equals(consts.ICX)){
                            totalAmountToBePaidICX = totalAmountToBePaidICX.add(totalBudget.divide(BigInteger.valueOf(totalInstallment)));
                        }
                        else {
                            totalAmountToBePaidbnUSD = totalAmountToBePaidbnUSD.add(totalBudget.divide(BigInteger.valueOf(totalInstallment)));
                        }
                    }
                }
            }
        }
        return Map.of(
                "data", projectDetails,
                "project_count", projectDetails.size(),
                "total_amount", Map.of("ICX", totalAmountToBePaidICX, "bnUSD", totalAmountToBePaidbnUSD),
                "withdraw_amount_icx", installmentFundRecord.at(_wallet_address.toString()).getOrDefault(consts.ICX, BigInteger.ZERO),
                "withdraw_amount_bnUSD", installmentFundRecord.at(_wallet_address.toString()).getOrDefault(consts.bnUSD, BigInteger.ZERO));
    }


    @External(readonly = true)
    public Map<String, ?> get_sponsor_projected_fund(Address _wallet_address){
        ProposalData proposalData = new ProposalData();
        BigInteger totalAmountToBePaidICX = BigInteger.ZERO;
        BigInteger totalAmountToBePaidbnUSD = BigInteger.ZERO;
        BigInteger totalSponsorBondICX = BigInteger.ZERO;
        BigInteger totalSponsorBondbnUSD = BigInteger.ZERO;
        List<Map<String, String>> projectDetails = new ArrayList<>();
        for (int i = 0; i < proposalsKeys.size(); i++){
            String _ipfs_key = proposalsKeys.get(i);
            Map<String, ?> proposal_details = proposalData.getDataFromProposalDB(_ipfs_key);
            if (proposal_details.get(consts.STATUS).equals(DISQUALIFIED)){
                if (proposal_details.get(consts.SPONSOR_ADDRESS).equals(_wallet_address.toString())){
                    int totalInstallment = (int) proposal_details.get(consts.PROJECT_DURATION);
                    int totalPaidCount = (int) proposal_details.get(consts.INSTALLMENT_COUNT);
                    if (totalPaidCount < totalInstallment){
                        String flag = (String) proposal_details.get(consts.TOKEN);
                        BigInteger totalBudget = (BigInteger) proposal_details.get(consts.TOTAL_BUDGET);
                        BigInteger totalPaidAmount = (BigInteger) proposal_details.get(consts.WITHDRAW_AMOUNT);
                        BigInteger depositedSponsorBond = (BigInteger) proposal_details.get(consts.TOTAL_BUDGET);

                        Map<String, String> project_details = Map.of(
                                consts.IPFS_HASH, _ipfs_key,
                                consts.TOKEN, flag,
                                consts.TOTAL_BUDGET, totalBudget.toString(),
                                consts.TOTAL_INSTALLMENT_PAID, totalPaidAmount.toString(),
                                consts.TOTAL_INSTALLMENT_COUNT, String.valueOf(totalInstallment),
                                consts.TOTAL_TIMES_INSTALLMENT_PAID, String.valueOf(totalPaidCount),
                                consts.INSTALLMENT_AMOUNT, totalBudget.divide(BigInteger.valueOf(totalInstallment)).toString(),
                                consts.SPONSOR_BOND_AMOUNT, depositedSponsorBond.toString());

                        projectDetails.add(project_details);
                        if (flag.equals(consts.ICX)){
                            totalAmountToBePaidICX = totalAmountToBePaidICX.add(totalBudget.divide(BigInteger.valueOf(totalInstallment)));
                            totalSponsorBondICX = totalSponsorBondICX.add(depositedSponsorBond);
                        }
                        else {
                            totalAmountToBePaidbnUSD = totalAmountToBePaidbnUSD.add(totalBudget.divide(BigInteger.valueOf(totalInstallment)));
                            totalSponsorBondbnUSD = totalSponsorBondbnUSD.add(depositedSponsorBond);
                        }
                    }
                }
            }
        }
        return Map.of(
                "data", projectDetails,
                "project_count", projectDetails.size(),
                "total_amount", Map.of("ICX", totalAmountToBePaidICX, "bnUSD", totalAmountToBePaidbnUSD),
                "withdraw_amount_icx", installmentFundRecord.at(_wallet_address.toString()).getOrDefault(consts.ICX, BigInteger.ZERO),
                "withdraw_amount_bnUSD", installmentFundRecord.at(_wallet_address.toString()).getOrDefault(consts.bnUSD, BigInteger.ZERO),
                "total_sponsor_bond", Map.of("ICX", totalSponsorBondICX, "bnUSD", totalSponsorBondbnUSD)
        );
    }

    private void depositProposalFund(ProposalData.ProposalAttributes _proposals, BigInteger _value){
        addRecord(_proposals);
        ProposalFundDeposited(_proposals.ipfs_hash, "Received " + _proposals.ipfs_hash + " " + _value + " " +
                consts.bnUSD + " fund from CPF");
    }

    @External
    @Payable
    public void update_proposal_fund(String _ipfs_key, BigInteger _added_budget, BigInteger _added_sponsor_reward,
                                     int _added_installment_count){
        ProposalData proposalData = new ProposalData();
        Context.require(proposalExists(_ipfs_key), TAG + ": Invalid IPFS hash.");
        String proposalPrefix = proposalPrefix(_ipfs_key);
        Map<String, ?> proposalDetails = proposalData.getDataFromProposalDB(proposalPrefix);
        BigInteger totalBudget = (BigInteger) proposalDetails.get(consts.TOTAL_BUDGET);
        BigInteger sponsorReward = (BigInteger) proposalDetails.get(consts.SPONSORS_REWARDS);
        int totalDuration = (int) proposalDetails.get(consts.PROJECT_DURATION);
        BigInteger remainingAmount = (BigInteger) proposalDetails.get(consts.REMAINING_AMOUNT);
        BigInteger sponsorRemainingAmount = (BigInteger) proposalDetails.get(consts.SPONSOR_REMAINING_AMOUNT);
        int installmentCount = (int) proposalDetails.get(consts.INSTALLMENT_COUNT);
        int sponsorRewardCount = (int) proposalDetails.get(consts.SPONSOR_REWARD_COUNT);
        String flag = (String) proposalDetails.get(consts.TOKEN);

        setTotalBudget(proposalPrefix, totalBudget.add(_added_budget));
        setSponsorReward(proposalPrefix, sponsorReward.add(_added_sponsor_reward));
        setProjectDuration(proposalPrefix, totalDuration + _added_installment_count);
        setRemainingAmount(proposalPrefix, remainingAmount.add(_added_budget));
        setSponsorRemainingAmount(proposalPrefix, sponsorRemainingAmount.add(_added_sponsor_reward));
        setInstallmentCount(proposalPrefix, installmentCount + _added_installment_count);
        setSponsorRewardCount(proposalPrefix, sponsorRewardCount+ _added_installment_count);

        ProposalFundDeposited(_ipfs_key, _ipfs_key + ": Added Budget: " + _added_budget + " " +
                flag + "and Added time: " + _added_installment_count + " Successfully");
    }

    @External
    public void send_installment_to_contributor(String _ipfs_key){
        validateCpsScore();
        Context.require(proposalExists(_ipfs_key), TAG + ": Invalid IPFS Hash.");
        BigInteger installmentAmount = BigInteger.ZERO;
        String prefix = proposalPrefix(_ipfs_key);
        Map<String, ?> proposalData = getDataFromProposalDB(prefix);

        int installmentCount = (int) proposalData.get(consts.INSTALLMENT_COUNT);
        BigInteger withdrawAmount = (BigInteger) proposalData.get(consts.WITHDRAW_AMOUNT);
        BigInteger remainingAmount = (BigInteger) proposalData.get(consts.REMAINING_AMOUNT);
        Address contributorAddress = (Address) proposalData.get(consts.CONTRIBUTOR_ADDRESS);
        String flag = (String) proposalData.get(consts.TOKEN);

        try {
            if (installmentCount == 1) {
                installmentAmount = remainingAmount;
            } else {
                installmentAmount = remainingAmount.divide(BigInteger.valueOf(installmentCount));
            }
            int newInstallmentCount = installmentCount - 1;
//            ProposalData.installmentCount.at(prefix).set(newInstallmentCount);
//            ProposalData.remainingAmount.at(prefix).set(remainingAmount.subtract(installmentAmount));
//            ProposalData.withdrawAmount.at(prefix).set(withdrawAmount.add(installmentAmount));

        setInstallmentCount(prefix, newInstallmentCount);
        setRemainingAmount(prefix, remainingAmount.subtract(installmentAmount));
        setWithdrawAmount(prefix, withdrawAmount.add(installmentAmount));
        installmentFundRecord.at(contributorAddress.toString()).set(flag,
                installmentFundRecord.at(contributorAddress.toString()).get(flag).add(installmentAmount));
        ProposalFundSent(contributorAddress, "new installment " + installmentAmount + " " + flag + " sent to contributors address.");

            if (newInstallmentCount == 0){
//                ProposalData.status.at(prefix).set(COMPLETED);
                setStatus(prefix, COMPLETED);
            }
        }
        catch (Exception e){
            Context.revert(TAG + ": Network problem. Sending project funds to contributor. " + e);
        }
    }

    @External
    public void send_reward_to_sponsor(String _ipfs_key){
        validateCpsScore();

        Context.require(proposalExists(_ipfs_key), TAG + ": Invalid IPFS Hash.");
        BigInteger installmentAmount = BigInteger.ZERO;
        String prefix = proposalPrefix(_ipfs_key);

        int sponsorRewardCount = getSponsorRewardCount(prefix);
        BigInteger sponsorWithdrawAmount = getSponsorWithdrawAmount(prefix);
        BigInteger sponsorRemainingAmount = getSponsorRemainingAmount(prefix);
        Address sponsorAddress = getSponsorAddress(prefix);
        String flag = getToken(prefix);

        try {
            if (sponsorRewardCount == 1) {
                installmentAmount = sponsorRemainingAmount;
            } else {
                installmentAmount = sponsorRemainingAmount.divide(BigInteger.valueOf(sponsorRewardCount));
            }
            int newSponsorRewardCount = sponsorRewardCount - 1;
//            ProposalData.sponsorRewardCount.at(prefix).set(newSponsorRewardCount);
//            ProposalData.sponsorWithdrawAmount.at(prefix).set(sponsorWithdrawAmount.add(installmentAmount));
//            ProposalData.sponsorRemainingAmount.at(prefix).set(sponsorRemainingAmount.subtract(installmentAmount));

        setSponsorRewardCount(prefix, newSponsorRewardCount);
        setSponsorWithdrawAmount(prefix, sponsorWithdrawAmount.add(installmentAmount));
        setSponsorRemainingAmount(prefix, sponsorRemainingAmount.subtract(installmentAmount));
        installmentFundRecord.at(sponsorAddress.toString()).set(flag, installmentFundRecord.at(sponsorAddress.toString()).get(flag).add(installmentAmount));
        ProposalFundSent(sponsorAddress, "New installment " + installmentAmount + " " +
                flag + " sent to sponsor address.");
    }

    @External
    public void disqualify_project(String _ipfs_key){
        validateCpsScore();
        Context.require(proposalExists(_ipfs_key), TAG + ": Project not found. Invalid IPFS hash.");
        String prefix = proposalPrefix(_ipfs_key);
        setStatus(prefix, DISQUALIFIED);

//        BigInteger totalBudget = ProposalData.totalBudget.at(prefix).getOrDefault(BigInteger.ZERO);
//        BigInteger withdrawAmount = ProposalData.withdrawAmount.at(prefix).getOrDefault(BigInteger.ZERO);
//        BigInteger sponsorReward = ProposalData.sponsorReward.at(prefix).getOrDefault(BigInteger.ZERO);
//        BigInteger sponsorWithdrawAmount = ProposalData.sponsorWithdrawAmount.at(prefix).getOrDefault(BigInteger.ZERO);
//        String flag = ProposalData.token.at(prefix).get();

        BigInteger totalBudget = getTotalBudget(prefix);
        BigInteger withdrawAmount = getWithdrawAmount(prefix);
        BigInteger sponsorReward = getSponsorReward(prefix);
        BigInteger sponsorWithdrawAmount = getSponsorWithdrawAmount(prefix);
        String flag = getToken(prefix);


        BigInteger remainingBudget = totalBudget.subtract(withdrawAmount);
        BigInteger remainingReward = sponsorReward.subtract(sponsorWithdrawAmount);
        BigInteger totalReturnAmount = remainingBudget.add(remainingReward);

        try{
            if (flag.equals(consts.ICX)){
                callScore(totalReturnAmount, cpfTreasuryScore.get(), "disqualify_proposal_fund", _ipfs_key);
            }
            else if(flag.equals(consts.bnUSD)){
                String _data = "" + "{\"method\":\"disqualify_project\",\"params\":{\"ipfs_key\":" + "\"" + _ipfs_key + "\"" + "}}";
                callScore(balancedDollar.get(), "transfer", cpfTreasuryScore.get(), totalReturnAmount, _data.getBytes());
            }
            else{
                Context.revert(TAG + ": Not supported token.");
            }
            ProposalDisqualified(_ipfs_key, _ipfs_key + ", Proposal disqualified");
        }
        catch (Exception e){
            Context.revert(TAG + ": Network problem. Sending proposal funds to CPF after project disqualification.");
        }
    }

    @External
    public void claim_reward(){
        BigInteger availableAmountICX = installmentFundRecord.at(Context.getCaller().toString()).get(consts.ICX);
        BigInteger availableAmountbnUSD = installmentFundRecord.at(Context.getCaller().toString()).get(consts.bnUSD);
        if (availableAmountICX.compareTo(BigInteger.ZERO) > 0){
            try{
                installmentFundRecord.at(Context.getCaller().toString()).set(consts.ICX, BigInteger.ZERO);
                Context.transfer(Context.getCaller(), availableAmountICX);
                ProposalFundWithdrawn(Context.getCaller(), availableAmountICX + " " + consts.ICX + " withdrawn to " + Context.getCaller());
            }
            catch (Exception e){
                Context.revert(TAG + ": Network problem while claiming reward.");
            }
        }
        else if(availableAmountbnUSD.compareTo(BigInteger.ZERO) > 0){
            try {
                installmentFundRecord.at(Context.getCaller().toString()).set(consts.bnUSD, BigInteger.ZERO);
                callScore(balancedDollar.get(), "transfer",Context.getCaller(), availableAmountbnUSD);
            }
            catch (Exception e){
                Context.revert(TAG + ": Network problem while claiming reward.");
            }
        }
        else{
            Context.revert(TAG + ": Claim Reward Fails. Available amount(ICX) = " + availableAmountICX + " and Available amount(bnUSD) = " + availableAmountbnUSD);
        }
    }

    @External
    public void tokenFallback(Address _from, BigInteger _value, byte[] _data){
        Context.require(_from.equals(cpfTreasuryScore.get()), TAG + "Only receiving from " + cpfTreasuryScore.get());
        String unpacked_data = new String(_data);
        JsonObject jsonObject = Json.parse(unpacked_data).asObject();
        JsonObject params = jsonObject.get("params").asObject();
        if (jsonObject.get("method").asString().equals("deposit_proposal_fund")){
            String ipfs_hash = params.get("ipfs_hash").asString();
            int project_duration = Integer.parseInt(params.get("project_duration").asString());
            BigInteger total_budget = new BigInteger(params.get("total_budget").asString());
            BigInteger sponsor_reward = new BigInteger(params.get("sponsor_reward").asString());
            String token = params.get("token").asString();
            String contributor_address = params.get("contributor_address").asString();
            String sponsor_address = params.get("sponsor_address").asString();
            ProposalAttributes proposalAttributes = new ProposalAttributes();
            proposalAttributes.ipfs_hash = ipfs_hash;
            proposalAttributes.project_duration = project_duration;
            proposalAttributes.total_budget = total_budget;
            proposalAttributes.sponsor_reward = sponsor_reward;
            proposalAttributes.token = token;
            proposalAttributes.contributor_address = contributor_address;
            proposalAttributes.sponsor_address = sponsor_address;
            proposalAttributes.status = ACTIVE;
            depositProposalFund(proposalAttributes, _value);
        }
        else if (jsonObject.get("method").asString().equals("budget_adjustment")){
            String ipfs_key = params.get("_ipfs_key").asString();
            BigInteger added_budget = new BigInteger(params.get("_added_budget").asString());
            BigInteger added_sponsor_reward = new BigInteger(params.get("_added_sponsor_reward").asString());
            int added_installment_count = Integer.parseInt(params.get("_added_installment_count").asString());

            update_proposal_fund(ipfs_key, added_budget, added_sponsor_reward, added_installment_count);
        }
        else{
            Context.revert(TAG + jsonObject.get("method").asString() + " Not a valid method.");
        }

    }


    public <T> T callScore(Class<T> t, Address address, String method, Object... params) {
        return Context.call(t, address, method, params);
    }

    public void callScore(Address address, String method, Object... params) {
        Context.call(address, method, params);
    }

    public void callScore(BigInteger amount, Address address, String method, Object... params) {
        Context.call(amount, address, method, params);
    }

    @EventLog(indexed = 1)
    public void FundReturned(Address _sponsor_address, String note){}

    @EventLog(indexed = 1)
    public void ProposalFundTransferred(String _ipfs_key, String note){}

    @EventLog(indexed = 1)
    public void ProposalDisqualified(String _ipfs_key, String note){}

    @EventLog(indexed = 1)
    public void FundReceived(Address _sponsor_address, String note){}

    @EventLog(indexed = 1)
    public void ProposalFundDeposited(String _ipfs_key, String note){}

    @EventLog(indexed = 1)
    public void ProposalFundSent(Address _receiver_address, String note){}

    @EventLog(indexed = 1)
    public void ProposalFundWithdrawn(Address _receiver_address, String note){}

}
