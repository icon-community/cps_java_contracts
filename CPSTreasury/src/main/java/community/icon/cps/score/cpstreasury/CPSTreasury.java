package community.icon.cps.score.cpstreasury;

import community.icon.cps.score.cpstreasury.db.ProposalData;
import score.*;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import score.Address;
import score.ArrayDB;
import score.BranchDB;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;
import scorex.util.ArrayList;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import community.icon.cps.score.lib.interfaces.CPSTreasuryInterface;

import community.icon.cps.score.cpstreasury.utils.consts;

public class CPSTreasury extends ProposalData implements CPSTreasuryInterface {
    private static final String TAG = "CPS_TREASURY";
    private static final String PROPOSAL_DB_PREFIX = "proposal";
    private static final String PROPOSALS_KEYS = "_proposals_keys";
    private static final String PROPOSALS_KEY_LIST_INDEX = "proposals_key_list_index";
    private static final String INSTALLMENT_FUND_RECORD = "installment_fund_record";

    private static final String CPS_SCORE = "_cps_score";
    private static final String CPF_TREASURY_SCORE = "_cpf_treasury_score";
    private static final String BALANCED_DOLLAR = "balanced_dollar";

    private static final String ACTIVE = "active";
    private static final String DISQUALIFIED = "disqualified";
    private static final String COMPLETED = "completed";
    private static final String CONTRIBUTOR_PROJECTS = "contributor_projects";
    private static final String SPONSOR_PROJECTS = "sponsor_projects";
    public static final String ONSET_PAYMENT = "onset_payment";

    private final ArrayDB<String> proposalsKeys = Context.newArrayDB(PROPOSALS_KEYS, String.class);
    private final DictDB<String, Integer> proposalsKeyListIndex = Context.newDictDB(PROPOSALS_KEY_LIST_INDEX, Integer.class);
    private final BranchDB<String, DictDB<String, BigInteger>> installmentFundRecord = Context.newBranchDB(INSTALLMENT_FUND_RECORD, BigInteger.class);

    private final VarDB<Address> cpfTreasuryScore = Context.newVarDB(CPF_TREASURY_SCORE, Address.class);
    private final VarDB<Address> cpsScore = Context.newVarDB(CPS_SCORE, Address.class);
    private final VarDB<Address> balancedDollar = Context.newVarDB(BALANCED_DOLLAR, Address.class);
    private final BranchDB<String, ArrayDB<String>> contributorProjects = Context.newBranchDB(CONTRIBUTOR_PROJECTS, String.class);
    private final BranchDB<String, ArrayDB<String>> sponsorProjects = Context.newBranchDB(SPONSOR_PROJECTS, String.class);
    private final VarDB<Integer> batchSize = Context.newVarDB("batch_size", Integer.class);
    private final VarDB<BigInteger> onsetPaymentPercentage = Context.newVarDB(ONSET_PAYMENT,BigInteger.class);

    private static final BigInteger HUNDRED = BigInteger.valueOf(100);
    public static final BigInteger MAX_ONSET_PAYMENT = BigInteger.valueOf(20);

    public CPSTreasury() {
    }

    @Override
    @External(readonly = true)
    public String name() {
        return TAG;
    }

    @Override
    @Payable
    public void fallback() {
        Context.revert(TAG + ": ICX can only be send by CPF Treasury Score");
    }

    private String proposalPrefix(String _proposal_key) {
        return PROPOSAL_DB_PREFIX + "|" + "|" + _proposal_key;
    }

    private Boolean proposalExists(String _ipfs_key) {
        return proposalsKeyListIndex.getOrDefault(_ipfs_key, null) != null;
    }

    private void validateAdmins() {
        Boolean isAdmin = callScore(Boolean.class, cpsScore.get(), "isAdmin", Context.getCaller());
        Context.require(isAdmin, TAG + ": Only admins can call this method");

    }

    private void validateAdminScore(Address _score) {
        validateAdmins();
        Context.require(_score.isContract(), TAG + "Target " + _score + " is not a score.");
    }

    private void validateCpsScore() {
        Context.require(Context.getCaller().equals(cpsScore.get()), TAG + ": Only CPS score " + cpsScore.get() + " can send fund using this method.");
    }

    private void addRecord(ProposalAttributes _proposal) {
        String ipfs_hash = _proposal.ipfs_hash;
        Context.require(!proposalExists(ipfs_hash), TAG + ": Already have this project");
        proposalsKeys.add(ipfs_hash);
        sponsorProjects.at(_proposal.sponsor_address).add(ipfs_hash);
        contributorProjects.at(_proposal.contributor_address).add(ipfs_hash);
        String proposalPrefix = proposalPrefix(ipfs_hash);
        addDataToProposalDB(_proposal, proposalPrefix);
        proposalsKeyListIndex.set(ipfs_hash, proposalsKeys.size() - 1);
    }

    @Override
    @External
    public void setCpsScore(Address score) {
        validateAdminScore(score);
        cpsScore.set(score);
    }

    @Override
    @External(readonly = true)
    public Address getCpsScore() {
        return cpsScore.get();
    }

    @Override
    @External
    public void setCpfTreasuryScore(Address score) {
        validateAdminScore(score);
        cpfTreasuryScore.set(score);
    }

    @Override
    @External(readonly = true)
    public Address getCpfTreasuryScore() {
        return cpfTreasuryScore.get();
    }

    @Override
    @External
    public void setBnUSDScore(Address score) {
        validateAdminScore(score);
        balancedDollar.set(score);
    }

    @Override
    @External
    public Address getBnUSDScore() {
        return balancedDollar.get();
    }

    @External
    public void setOnsetPayment(BigInteger paymentPercentage){
        Context.require(Context.getCaller().equals(cpfTreasuryScore.get()), TAG + ": Only receiving from  " +
                cpfTreasuryScore.get());
        Context.require(paymentPercentage.compareTo(MAX_ONSET_PAYMENT) <= 0,
                TAG + ": Initial payment cannot be greater than " + MAX_ONSET_PAYMENT + " percentage");

        BigInteger bondPercentage = callScore(BigInteger.class,getCpsScore(),"getSponsorBondPercentage");
        Context.require(paymentPercentage.compareTo(bondPercentage) <=0,
                TAG+": Payment cannot be greater than sponsor bond percentage");

        onsetPaymentPercentage.set(paymentPercentage);
    }

    @External(readonly = true)
    public BigInteger getOnsetPayment(){
        return onsetPaymentPercentage.get();
    }


    @Override
    @External(readonly = true)
    public Map<String, ?> getContributorProjectedFund(Address walletAddress) {
        BigInteger totalAmountToBePaidICX = BigInteger.ZERO;
        BigInteger totalAmountToBePaidbnUSD = BigInteger.ZERO;
        List<Map<String, ?>> projectDetails = new ArrayList<>();
        ArrayDB<String> proposalKeysArray = contributorProjects.at(walletAddress.toString());
        int proposalKeysSize = proposalKeysArray.size();
        for (int i = 0; i < proposalKeysSize; i++) {
            String _ipfs_key = proposalKeysArray.get(i);
            String proposalPrefix = proposalPrefix(_ipfs_key);
            Map<String, ?> proposal_details = getDataFromProposalDB(proposalPrefix);
            if (!proposal_details.get(consts.STATUS).equals(DISQUALIFIED)) {
                if (proposal_details.get(consts.CONTRIBUTOR_ADDRESS).equals(walletAddress)) {
                    int totalInstallment = (int) proposal_details.get(consts.PROJECT_DURATION);
                    int totalPaidCount = totalInstallment - (int) proposal_details.get(consts.INSTALLMENT_COUNT);

                    if (totalPaidCount < totalInstallment) {
                        String flag = (String) proposal_details.get(consts.TOKEN);
                        BigInteger totalBudget = (BigInteger) proposal_details.get(consts.TOTAL_BUDGET);
                        BigInteger totalPaidAmount = (BigInteger) proposal_details.get(consts.WITHDRAW_AMOUNT);

                        BigInteger remaingAmount = totalBudget.subtract(totalPaidAmount);
                        Map<String, ?> project_details = Map.of(
                                consts.IPFS_HASH, _ipfs_key,
                                consts.TOKEN, flag,
                                consts.TOTAL_BUDGET, totalBudget,
                                consts.TOTAL_INSTALLMENT_PAID, totalPaidAmount,
                                consts.TOTAL_INSTALLMENT_COUNT, totalInstallment,
                                consts.TOTAL_TIMES_INSTALLMENT_PAID, totalPaidCount,
                                consts.INSTALLMENT_AMOUNT, remaingAmount.divide(BigInteger.valueOf(totalInstallment)));

                        projectDetails.add(project_details);
                        if (flag.equals(consts.ICX)) {
                            totalAmountToBePaidICX = totalAmountToBePaidICX.add(totalBudget.divide(BigInteger.valueOf(totalInstallment)));
                        } else {
                            totalAmountToBePaidbnUSD = totalAmountToBePaidbnUSD.add(totalBudget.divide(BigInteger.valueOf(totalInstallment)));
                        }
                    }
                }
            }
        }
        DictDB<String, BigInteger> installmentRecord = installmentFundRecord.at(walletAddress.toString());
        return Map.of(
                "data", projectDetails,
                "project_count", projectDetails.size(),
                "total_amount", Map.of("ICX", totalAmountToBePaidICX, "bnUSD", totalAmountToBePaidbnUSD),
                "withdraw_amount_icx", installmentRecord.getOrDefault(consts.ICX, BigInteger.ZERO),
                "withdraw_amount_bnUSD", installmentRecord.getOrDefault(consts.bnUSD, BigInteger.ZERO));
    }

    @Override
    @External(readonly = true)
    public List<String> getContributorProjects(Address address) {
        List<String> contributorProjects = new ArrayList<>();
        for (int i = 0; i < this.contributorProjects.at(address.toString()).size(); i++) {
            contributorProjects.add(this.contributorProjects.at(address.toString()).get(i));
        }
        return contributorProjects;
    }

    @Override
    @External(readonly = true)
    public List<String> getSponsorProjects(Address address) {
        List<String> sponsorProjects = new ArrayList<>();
        for (int i = 0; i < this.sponsorProjects.at(address.toString()).size(); i++) {
            sponsorProjects.add(this.sponsorProjects.at(address.toString()).get(i));
        }
        return sponsorProjects;
    }

    @Override
    @External(readonly = true)
    public Map<String, ?> getSponsorProjectedFund(Address walletAddress) {
        ProposalData proposalData = new ProposalData();
        BigInteger totalAmountToBePaidICX = BigInteger.ZERO;
        BigInteger totalAmountToBePaidbnUSD = BigInteger.ZERO;
        BigInteger totalSponsorBondICX = BigInteger.ZERO;
        BigInteger totalSponsorBondbnUSD = BigInteger.ZERO;
        List<Map<String, ?>> projectDetails = new ArrayList<>();
        ArrayDB<String> proposalKeysArray = sponsorProjects.at(walletAddress.toString());
        int proposalKeysSize = proposalKeysArray.size();
        for (int i = 0; i < proposalKeysSize; i++) {
            String _ipfs_key = proposalKeysArray.get(i);
            String proposalPrefix = proposalPrefix(_ipfs_key);
            Map<String, ?> proposal_details = proposalData.getDataFromProposalDB(proposalPrefix);
            if (!proposal_details.get(consts.STATUS).equals(DISQUALIFIED)) {
                if (proposal_details.get(consts.SPONSOR_ADDRESS).equals(walletAddress)) {
                    int totalInstallment = (int) proposal_details.get(consts.PROJECT_DURATION);
                    int totalPaidCount = totalInstallment - (int) proposal_details.get(consts.SPONSOR_REWARD_COUNT);
                    if (totalPaidCount < totalInstallment) {
                        String flag = (String) proposal_details.get(consts.TOKEN);
                        BigInteger totalBudget = (BigInteger) proposal_details.get(consts.SPONSOR_REWARD);
                        BigInteger totalPaidAmount = (BigInteger) proposal_details.get(consts.WITHDRAW_AMOUNT);
                        BigInteger depositedSponsorBond = ((BigInteger) proposal_details.get(consts.TOTAL_BUDGET)).divide(BigInteger.TEN);
                        BigInteger remaingAmount = totalBudget.subtract(totalPaidAmount);
                        // TODO: won't work for second
                        Map<String, ?> project_details = Map.of(
                                consts.IPFS_HASH, _ipfs_key,
                                consts.TOKEN, flag,
                                consts.TOTAL_BUDGET, totalBudget,
                                consts.TOTAL_INSTALLMENT_PAID, totalPaidAmount,
                                consts.TOTAL_INSTALLMENT_COUNT, totalInstallment,
                                consts.TOTAL_TIMES_INSTALLMENT_PAID, totalPaidCount,
                                consts.INSTALLMENT_AMOUNT, remaingAmount.divide(BigInteger.valueOf(totalInstallment)),
                                consts.SPONSOR_BOND_AMOUNT, depositedSponsorBond);

                        projectDetails.add(project_details);
                        if (flag.equals(consts.ICX)) {
                            totalAmountToBePaidICX = totalAmountToBePaidICX.add(totalBudget.divide(BigInteger.valueOf(totalInstallment)));
                            totalSponsorBondICX = totalSponsorBondICX.add(depositedSponsorBond);
                        } else {
                            totalAmountToBePaidbnUSD = totalAmountToBePaidbnUSD.add(totalBudget.divide(BigInteger.valueOf(totalInstallment)));
                            totalSponsorBondbnUSD = totalSponsorBondbnUSD.add(depositedSponsorBond);
                        }
                    }
                }
            }
        }
        DictDB<String, BigInteger> installmentRecord = installmentFundRecord.at(walletAddress.toString());
        return Map.of(
                "data", projectDetails,
                "project_count", projectDetails.size(),
                "total_amount", Map.of("ICX", totalAmountToBePaidICX, "bnUSD", totalAmountToBePaidbnUSD),
                "withdraw_amount_icx", installmentRecord.getOrDefault(consts.ICX, BigInteger.ZERO),
                "withdraw_amount_bnUSD", installmentRecord.getOrDefault(consts.bnUSD, BigInteger.ZERO),
                "total_sponsor_bond", Map.of("ICX", totalSponsorBondICX, "bnUSD", totalSponsorBondbnUSD)
        );
    }

    private void depositProposalFund(ProposalData.ProposalAttributes _proposals, BigInteger _value) {
        addRecord(_proposals);
        ProposalFundDeposited(_proposals.ipfs_hash, "Received " + _proposals.ipfs_hash + " " + _value + " " +
                consts.bnUSD + " fund from CPF");
    }

    @Override
    @External
    @Payable
    public void update_proposal_fund(String _ipfs_key, BigInteger _added_budget, BigInteger _added_sponsor_reward,
                                     int _added_installment_count) {
        ProposalData proposalData = new ProposalData();
        Context.require(proposalExists(_ipfs_key), TAG + ": Invalid IPFS hash.");
        String proposalPrefix = proposalPrefix(_ipfs_key);
        Map<String, ?> proposalDetails = proposalData.getDataFromProposalDB(proposalPrefix);
        BigInteger totalBudget = (BigInteger) proposalDetails.get(consts.TOTAL_BUDGET);
        BigInteger sponsorReward = (BigInteger) proposalDetails.get(consts.SPONSOR_REWARD);
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
        setSponsorRewardCount(proposalPrefix, sponsorRewardCount + _added_installment_count);

        ProposalFundDeposited(_ipfs_key, _ipfs_key + ": Added Budget: " + _added_budget + " " +
                flag + "and Added time: " + _added_installment_count + " Successfully");
    }

    @Override
    @External
    public void send_installment_to_contributor(String _ipfs_key, int installment_count) {
        validateCpsScore();
        Context.require(proposalExists(_ipfs_key), TAG + ": Invalid IPFS Hash.");
        BigInteger installmentAmount;
        String prefix = proposalPrefix(_ipfs_key);
        Map<String, ?> proposalData = getDataFromProposalDB(prefix);

        int installmentCount = (int) proposalData.get(consts.INSTALLMENT_COUNT);
        BigInteger withdrawAmount = (BigInteger) proposalData.get(consts.WITHDRAW_AMOUNT);
        BigInteger remainingAmount = (BigInteger) proposalData.get(consts.REMAINING_AMOUNT);
        Address contributorAddress = (Address) proposalData.get(consts.CONTRIBUTOR_ADDRESS);
        String flag = (String) proposalData.get(consts.TOKEN);

        if (installmentCount == 1) {
            installmentAmount = remainingAmount;
        } else {
            installmentAmount = remainingAmount.divide(BigInteger.valueOf(installmentCount)).multiply(BigInteger.valueOf(installment_count));
        }
        int newInstallmentCount = installmentCount - installment_count; // TODO: check for negative value

        setInstallmentCount(prefix, newInstallmentCount);
        setRemainingAmount(prefix, remainingAmount.subtract(installmentAmount));
        setWithdrawAmount(prefix, withdrawAmount.add(installmentAmount));
        DictDB<String, BigInteger> installmentFund = this.installmentFundRecord.at(contributorAddress.toString());
        BigInteger installmentFundAmount = installmentFund.getOrDefault(flag, BigInteger.ZERO);
        installmentFund.set(flag, installmentFundAmount.add(installmentAmount));
        ProposalFundSent(contributorAddress, "new installment " + installmentAmount + " " + flag + " sent to contributors address.");

        if (newInstallmentCount == 0) {
            setStatus(prefix, COMPLETED);
        }
    }

private void onsetPaymentContributor(String _ipfs_key){
        Context.require(proposalExists(_ipfs_key), TAG + ": Invalid IPFS Hash.");
        String prefix = proposalPrefix(_ipfs_key);
        Map<String, ?> proposalData = getDataFromProposalDB(prefix);
        Address contributorAddress = (Address) proposalData.get(consts.CONTRIBUTOR_ADDRESS);
        String flag = (String) proposalData.get(consts.TOKEN);

        BigInteger totalBudget = (BigInteger) proposalData.get(consts.TOTAL_BUDGET);
        BigInteger withdrawAmount = (BigInteger) proposalData.get(consts.WITHDRAW_AMOUNT);

        BigInteger onSetPaymentPercentage = getOnsetPayment();
        BigInteger onsetAmount = (totalBudget.multiply(getOnsetPayment()).divide(HUNDRED));

        setRemainingAmount(prefix,totalBudget.subtract(onsetAmount));

        setWithdrawAmount(prefix,withdrawAmount.add(onsetAmount));
        DictDB<String, BigInteger> installmentFund = this.installmentFundRecord.at(contributorAddress.toString());

        BigInteger installmentFundAmount = installmentFund.getOrDefault(flag, BigInteger.ZERO);
        installmentFund.set(flag, installmentFundAmount.add(onsetAmount));

        InitialPaymentSent(contributorAddress,"initial payment of " + onSetPaymentPercentage + "% is send to contributor address");
    }

    @Override
    @External
    public void send_reward_to_sponsor(String _ipfs_key) {
        validateCpsScore();

        Context.require(proposalExists(_ipfs_key), TAG + ": Invalid IPFS Hash.");
        BigInteger installmentAmount;
        String prefix = proposalPrefix(_ipfs_key);

        int sponsorRewardCount = getSponsorRewardCount(prefix);
        BigInteger sponsorWithdrawAmount = getSponsorWithdrawAmount(prefix);
        BigInteger sponsorRemainingAmount = getSponsorRemainingAmount(prefix);
        Address sponsorAddress = getSponsorAddress(prefix);
        String flag = getToken(prefix);

        if (sponsorRewardCount == 1) {
            installmentAmount = sponsorRemainingAmount;
        } else {
            installmentAmount = sponsorRemainingAmount.divide(BigInteger.valueOf(sponsorRewardCount));
        }
        int newSponsorRewardCount = sponsorRewardCount - 1;

        setSponsorRewardCount(prefix, newSponsorRewardCount);
        setSponsorWithdrawAmount(prefix, sponsorWithdrawAmount.add(installmentAmount));
        setSponsorRemainingAmount(prefix, sponsorRemainingAmount.subtract(installmentAmount));
        DictDB<String, BigInteger> installmentFunds = installmentFundRecord.at(sponsorAddress.toString());
        installmentFunds.set(flag, installmentFunds.getOrDefault(flag, BigInteger.ZERO).add(installmentAmount));
        ProposalFundSent(sponsorAddress, "New installment " + installmentAmount + " " +
                flag + " sent to sponsor address.");
    }

    private void onsetPaymentSponsor(String _ipfs_key){
        Context.require(proposalExists(_ipfs_key), TAG + ": Invalid IPFS Hash.");
        String prefix = proposalPrefix(_ipfs_key);

        BigInteger sponsorWithdrawAmount = getSponsorWithdrawAmount(prefix);
        BigInteger sponsorRemainingAmount = getSponsorRemainingAmount(prefix);
        Address sponsorAddress = getSponsorAddress(prefix);
        String flag = getToken(prefix);

        BigInteger onSetPaymentPercentage = getOnsetPayment();
        BigInteger onsetAmount = (sponsorRemainingAmount.multiply(onSetPaymentPercentage).divide(HUNDRED));

        setSponsorRemainingAmount(prefix,sponsorRemainingAmount.subtract(onsetAmount));
        setSponsorWithdrawAmount(prefix,sponsorWithdrawAmount.add(onsetAmount));

        DictDB<String, BigInteger> installmentFunds = installmentFundRecord.at(sponsorAddress.toString());
        installmentFunds.set(flag, installmentFunds.getOrDefault(flag, BigInteger.ZERO).add(onsetAmount));
        installmentFunds = installmentFundRecord.at(sponsorAddress.toString());


        InitialPaymentSent(sponsorAddress, "initial amount of " + onSetPaymentPercentage +
                "%  sent to sponsor address.");

    }

    @Override
    @External
    public void disqualify_project(String _ipfs_key) {
        validateCpsScore();
        Context.require(proposalExists(_ipfs_key), TAG + ": Project not found. Invalid IPFS hash.");
        String prefix = proposalPrefix(_ipfs_key);
        setStatus(prefix, DISQUALIFIED);

        BigInteger totalBudget = getTotalBudget(prefix);
        BigInteger withdrawAmount = getWithdrawAmount(prefix);
        BigInteger sponsorReward = getSponsorReward(prefix);
        BigInteger sponsorWithdrawAmount = getSponsorWithdrawAmount(prefix);
        String flag = getToken(prefix);

        BigInteger remainingBudget = totalBudget.subtract(withdrawAmount);
        BigInteger remainingReward = sponsorReward.subtract(sponsorWithdrawAmount);
        BigInteger totalReturnAmount = remainingBudget.add(remainingReward);

        Address cpfTreasuryAddres = cpfTreasuryScore.get();
        if (flag.equals(consts.ICX)) {
            callScore(totalReturnAmount, cpfTreasuryAddres, "disqualify_proposal_fund", _ipfs_key);
        } else if (flag.equals(consts.bnUSD)) {
            JsonObject disqualifyProjectParams = new JsonObject();
            disqualifyProjectParams.add("method", "disqualify_project");
            JsonObject params = new JsonObject();
            params.add("ipfs_key", _ipfs_key);
            disqualifyProjectParams.add("params", params);

            callScore(balancedDollar.get(), "transfer", cpfTreasuryAddres, totalReturnAmount, disqualifyProjectParams.toString().getBytes());
        } else {
            Context.revert(TAG + ": Not supported token.");
        }
        ProposalDisqualified(_ipfs_key, _ipfs_key + ", Proposal disqualified");
    }


    @Override
    @External
    public void claimReward() {
        Address caller = Context.getCaller();
        DictDB<String, BigInteger> installmentFundRecord = this.installmentFundRecord.at(caller.toString());
        BigInteger availableAmountICX = installmentFundRecord.getOrDefault(consts.ICX, BigInteger.ZERO);
        BigInteger availableAmountbnUSD = installmentFundRecord.getOrDefault(consts.bnUSD, BigInteger.ZERO);
        if (availableAmountICX.compareTo(BigInteger.ZERO) > 0) {
            installmentFundRecord.set(consts.ICX, BigInteger.ZERO);
            Context.transfer(caller, availableAmountICX);
            ProposalFundWithdrawn(caller, availableAmountICX + " " + consts.ICX +
                    " withdrawn to " + caller);
        } else if (availableAmountbnUSD.compareTo(BigInteger.ZERO) > 0) {
            installmentFundRecord.set(consts.bnUSD, BigInteger.ZERO);
            callScore(balancedDollar.get(), "transfer", caller, availableAmountbnUSD);
        } else {
            Context.revert(TAG + ": Claim Reward Fails. Available amount(ICX) = " + availableAmountICX +
                    " and Available amount(bnUSD) = " + availableAmountbnUSD);
        }
    }

    @Override
    @External
    public void tokenFallback(Address _from, BigInteger _value, byte[] _data) {
        Context.require(_from.equals(cpfTreasuryScore.get()), TAG + "Only receiving from " + cpfTreasuryScore.get());
        String unpacked_data = new String(_data);
        JsonObject jsonObject = Json.parse(unpacked_data).asObject();
        JsonObject params = jsonObject.get("params").asObject();
        String methodName = jsonObject.get("method").asString();
        if (methodName.equals("deposit_proposal_fund")) {
            String ipfs_hash = params.get("ipfs_hash").asString();
            int project_duration = params.get("project_duration").asInt();
            int milestone_count = params.get("milestone_count").asInt();
            BigInteger total_budget = new BigInteger(params.get("total_budget").asString(), 16);
            BigInteger sponsor_reward = new BigInteger(params.get("sponsor_reward").asString(), 16);
            String token = params.get("token").asString();
            String contributor_address = params.get("contributor_address").asString();
            String sponsor_address = params.get("sponsor_address").asString();
            ProposalAttributes proposalAttributes = new ProposalAttributes();
            proposalAttributes.ipfs_hash = ipfs_hash;
            proposalAttributes.project_duration = project_duration;
            proposalAttributes.milestoneCount = milestone_count;
            proposalAttributes.total_budget = total_budget;
            proposalAttributes.sponsor_reward = sponsor_reward;
            proposalAttributes.token = token;
            proposalAttributes.contributor_address = contributor_address;
            proposalAttributes.sponsor_address = sponsor_address;
            proposalAttributes.status = ACTIVE;
            depositProposalFund(proposalAttributes, _value);
            onsetPaymentContributor(proposalAttributes.ipfs_hash);
            onsetPaymentSponsor(proposalAttributes.ipfs_hash);

        } else if (methodName.equals("budget_adjustment")) {
            String ipfs_key = params.get("_ipfs_key").asString();
            BigInteger added_budget = new BigInteger(params.get("_added_budget").asString(), 16);
            BigInteger added_sponsor_reward = new BigInteger(params.get("_added_sponsor_reward").asString(), 16);
            int added_installment_count = params.get("_added_installment_count").asInt();

            update_proposal_fund(ipfs_key, added_budget, added_sponsor_reward, added_installment_count);
        } else {
            Context.revert(TAG + methodName + " Not a valid method.");
        }
    }

    //    for migration into java contract
    @Override
    @External
    public void updateSponsorAndContributorProjects() {
        validateAdmins();
        int startIndex = batchSize.getOrDefault(0);
        int size = proposalsKeys.size();
        int endIndex = startIndex + 10;
        if (endIndex > size) {
            endIndex = size;
        }
        for (int i = startIndex; i < endIndex; i++) {
            String proposalKey = proposalsKeys.get(i);
            String proposalPrefix = proposalPrefix(proposalKey);
            Address contributorAddress = getContributorAddress(proposalPrefix);
            Address sponsorAddress = getSponsorAddress(proposalPrefix);
            contributorProjects.at(contributorAddress.toString()).add(proposalKey);
            sponsorProjects.at(sponsorAddress.toString()).add(proposalKey);
            batchSize.set(endIndex);
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

    @Override
    @EventLog(indexed = 1)
    public void ProposalDisqualified(String _ipfs_key, String note) {
    }

    @Override
    @EventLog(indexed = 1)
    public void ProposalFundDeposited(String _ipfs_key, String note) {
    }

    @Override
    @EventLog(indexed = 1)
    public void ProposalFundSent(Address _receiver_address, String note) {
    }

    @EventLog(indexed = 1)
    public void InitialPaymentSent(Address _receiver_address, String note) {
    }



    @Override
    @EventLog(indexed = 1)
    public void ProposalFundWithdrawn(Address _receiver_address, String note) {
    }

    /*-----------------------------------------------------------------------------------------------------------------
    ***************************************************** to be removed in production**********************************
     -----------------------------------------------------------------------------------------------------------------*/

    @External
    public void depositProposalFunds(ProposalData.ProposalAttributes proposals){
        addRecord(proposals);
    }

    @External(readonly = true)
    public String returnString(){
        return "";
    }

//    @External
//    public void removeArrayItems(){
//        ArrayDBUtils.remove_array_item_address(contributorProjects);
//    }
}
