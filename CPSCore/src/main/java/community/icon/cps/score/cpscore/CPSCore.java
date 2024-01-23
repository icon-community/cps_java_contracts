package community.icon.cps.score.cpscore;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import community.icon.cps.score.cpscore.db.MilestoneDb;
import community.icon.cps.score.cpscore.db.ProgressReportDataDb;
import community.icon.cps.score.cpscore.db.ProposalDataDb;
import community.icon.cps.score.cpscore.utils.ArrayDBUtils;
import community.icon.cps.score.lib.interfaces.CPSCoreInterface;
import score.*;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;
import score.annotation.Payable;
import scorex.util.ArrayList;
import scorex.util.HashMap;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static community.icon.cps.score.cpscore.db.MilestoneDb.addDataToMilestoneDb;
import static community.icon.cps.score.cpscore.db.MilestoneDb.getDataFromMilestoneDB;
import static community.icon.cps.score.cpscore.db.ProgressReportDataDb.*;
import static community.icon.cps.score.cpscore.db.ProposalDataDb.*;
import static community.icon.cps.score.cpscore.utils.ArrayDBUtils.*;
import static community.icon.cps.score.cpscore.utils.Checkers.checkMaintenance;
import static community.icon.cps.score.cpscore.utils.Checkers.onlyOwner;
import static community.icon.cps.score.cpscore.utils.Constants.*;

public class CPSCore implements CPSCoreInterface {

    private final ArrayDB<String> proposalsKeyList = Context.newArrayDB(PROPOSALS_KEY_LIST, String.class);
    private final DictDB<String, Integer> proposalsKeyListIndex = Context.newDictDB(PROPOSALS_KEY_LIST_INDEX, Integer.class);
    private final ArrayDB<String> progressKeyList = Context.newArrayDB(PROGRESS_KEY_LIST, String.class);
    private final DictDB<String, Integer> progressKeyListIndex = Context.newDictDB(PROGRESS_KEY_LIST_INDEX, Integer.class);
    private final ArrayDB<String> budgetApprovalsList = Context.newArrayDB(BUDGET_APPROVALS_LIST, String.class);

    private final ArrayDB<String> activeProposals = Context.newArrayDB(ACTIVE_PROPOSALS, String.class);

    private final ArrayDB<Address> contributors = Context.newArrayDB(CONTRIBUTORS, Address.class);
    private final ArrayDB<Address> sponsors = Context.newArrayDB(SPONSORS, Address.class);
    private static final ArrayDB<Address> admins = Context.newArrayDB(ADMINS, Address.class);
    public final DictDB<String, BigInteger> period = Context.newDictDB(PERIOD, BigInteger.class);
    public final VarDB<BigInteger> sponsorBondPercentage = Context.newVarDB(SPONSOR_BOND_PERCENTAGE, BigInteger.class);
    private final BranchDB<String, DictDB<String, BigInteger>> sponsorBondReturn = Context.newBranchDB(SPONSOR_BOND_RETURN, BigInteger.class);
    private final DictDB<Address, BigInteger> delegationSnapshot = Context.newDictDB(DELEGATION_SNAPSHOT, BigInteger.class);
    private final VarDB<BigInteger> maxDelegation = Context.newVarDB(MAX_DELEGATION, BigInteger.class);
    private final VarDB<BigInteger> totalDelegationSnapshot = Context.newVarDB(TOTAL_DELEGATION_SNAPSHOT, BigInteger.class);
    private final VarDB<BigInteger> proposalFees = Context.newVarDB(PROPOSAL_FEES, BigInteger.class);
    private final VarDB<BigInteger> swapBlockHeight = Context.newVarDB(SWAP_BLOCK_HEIGHT, BigInteger.class);
    private final VarDB<Integer> swapCount = Context.newVarDB(SWAP_COUNT, Integer.class);
    private final DictDB<String, Integer> proposalRank = Context.newDictDB(PROPOSAL_RANK, Integer.class);
    private final ArrayDB<Address> priorityVotedPreps = Context.newArrayDB(PRIORITY_VOTED_PREPS, Address.class);
    private final BranchDB<Address, ArrayDB<String>> sponsorProjects = Context.newBranchDB(SPONSOR_PROJECTS, String.class);
    private final BranchDB<Address, ArrayDB<String>> contributorProjects = Context.newBranchDB(CONTRIBUTOR_PROJECTS, String.class);
    private static final BigInteger HUNDRED = BigInteger.valueOf(100);

    public CPSCore(@Optional BigInteger bondValue, @Optional BigInteger applicationPeriod) {
        if (sponsorBondPercentage.get() == null) {
            sponsorBondPercentage.set(bondValue);
            this.period.set(APPLICATION_PERIOD, applicationPeriod);
            this.period.set(VOTING_PERIOD, TOTAL_PERIOD.subtract(applicationPeriod));
        }
    }

    @Override
    @External(readonly = true)
    public String name() {
        return TAG;
    }

    @Override
    public String proposalPrefix(String proposalKey) {
        return PROPOSAL_DB_PREFIX + "|" + "|" + proposalKey;
    }

    public String mileStonePrefix(String proposalKey, int id) {
        return proposalKey + "|" + "|" + id;
    }

    @Override
    public String progressReportPrefix(String progressKey) {
        return PROGRESS_REPORT_DB_PREFIX + "|" + "|" + progressKey;
    }


    @Override
    @External
    public void setCpsTreasuryScore(Address score) {
        validateAdminScore(score);
        SetterGetter setterGetter = new SetterGetter();
        setterGetter.cpsTreasuryScore.set(score);
    }


    @Override
    @External(readonly = true)
    public Address getCpsTreasuryScore() {
        SetterGetter setterGetter = new SetterGetter();
        return setterGetter.cpsTreasuryScore.get();
    }


    @Override
    @External
    public void setCpfTreasuryScore(Address score) {
        validateAdminScore(score);
        SetterGetter setterGetter = new SetterGetter();
        setterGetter.cpfScore.set(score);
    }


    @Override
    @External(readonly = true)
    public Address getCpfTreasuryScore() {
        SetterGetter setterGetter = new SetterGetter();
        return setterGetter.cpfScore.get();
    }


    @Override
    @External
    public void setBnusdScore(Address score) {
        validateAdminScore(score);
        SetterGetter setterGetter = new SetterGetter();
        setterGetter.balancedDollar.set(score);
    }


    @Override
    @External(readonly = true)
    public Address getBnusdScore() {
        SetterGetter setterGetter = new SetterGetter();
        return setterGetter.balancedDollar.get();
    }

    @External(readonly = true)
    public BigInteger getSponsorBondPercentage() {
        return sponsorBondPercentage.get();
    }

    @External
    public void setSponsorBondPercentage(BigInteger bondValue) {
        onlyCPFTreasury();
        Context.require(bondValue.compareTo(BigInteger.valueOf(12)) >= 0, TAG +
                ": Cannot set bond percentage less than 12%");
        sponsorBondPercentage.set(bondValue);
    }

    @External(readonly = true)
    public BigInteger getVotingPeriod() {
        return period.get(VOTING_PERIOD);
    }

    @External(readonly = true)
    public BigInteger getApplicationPeriod() {
        return period.get(APPLICATION_PERIOD);
    }

    @External
    public void setPeriod(BigInteger applicationPeriod) {
        onlyCPFTreasury();
        BigInteger votingPeriod = TOTAL_PERIOD.subtract(applicationPeriod);

        Context.require(
                (votingPeriod.compareTo(BigInteger.TEN) >= 0),
                TAG + ": Voting period must be more than or equal to 10 days");

        this.period.set(APPLICATION_PERIOD, applicationPeriod);
        this.period.set(VOTING_PERIOD, votingPeriod);
    }


    private boolean proposalKeyExists(String key) {
        return proposalsKeyListIndex.get(key) != null;
    }

    private boolean progressKeyExists(String key) {
        return progressKeyListIndex.get(key) != null;
    }

    @External(readonly = true)
    public boolean isAdmin(Address address) {
        return ArrayDBUtils.containsInArrayDb(address, admins);
    }


    @Override
    @External
    public void toggleBudgetAdjustmentFeature() {
        validateAdmins();
        SetterGetter setterGetter = new SetterGetter();
        setterGetter.budgetAdjustment.set(!setterGetter.budgetAdjustment.getOrDefault(false));
    }

    @Override
    @External(readonly = true)
    public boolean getBudgetAdjustmentFeature() {
        SetterGetter setterGetter = new SetterGetter();
        return setterGetter.budgetAdjustment.getOrDefault(true);
    }

    @External
    public void toggleMaintenance() {
        validateAdmins();
        SetterGetter setterGetter = new SetterGetter();
        setterGetter.maintenance.set(!setterGetter.maintenance.getOrDefault(Boolean.TRUE));
    }


    @Override
    @External(readonly = true)
    public boolean getMaintenanceMode() {
        SetterGetter setterGetter = new SetterGetter();
        return setterGetter.maintenance.getOrDefault(false);
    }


    @Override
    @Payable
    public void fallback() {
        Context.revert(TAG + ": ICX can only be sent while submitting a proposal or paying the penalty.");
    }

    private void burn(BigInteger amount, @Optional Address token) {
        if (token == null) {
            token = SYSTEM_ADDRESS;
        }
        SetterGetter setterGetter = new SetterGetter();
        if (token.equals(SYSTEM_ADDRESS)) {
            callScore(amount, SYSTEM_ADDRESS, "burn");
        } else {
            Address bnUSDScore = setterGetter.balancedDollar.get();
            if (token.equals(bnUSDScore)) {
                JsonObject burnTokens = new JsonObject();
                burnTokens.add(METHOD, "burnAmount");
                callScore(bnUSDScore, TRANSFER, setterGetter.cpfScore.get(), amount, burnTokens.toString().getBytes());
            } else {
                Context.revert(TAG + ": Not a supported token.");
            }
        }
    }

    @Override
    @External(readonly = true)
    public int getPeriodCount() {
        PeriodController periodController = new PeriodController();
        return periodController.periodCount.getOrDefault(0);
    }

    @External
    public void addAdmin(Address address) {
//        TODO check governance contract
        onlyOwner();
        boolean check = ArrayDBUtils.containsInArrayDb(address, admins);
        if (!check) {
            admins.add(address);
        }
    }


    @External
    public void removeAdmin(Address address) { // change made
        onlyOwner();
        Context.require(address != Context.getOwner(), "Owner cannot be removed from admin list.");
        boolean check = ArrayDBUtils.containsInArrayDb(address, admins);
        Context.require(check, TAG + ": Address not registered as admin.");
        ArrayDBUtils.removeArrayItem(admins, address);
    }

    @Override
    @External
    public void unregisterPrep() {
        checkMaintenance();
        updatePeriod();
        Address caller = Context.getCaller();
        PReps pReps = new PReps();
        PeriodController period = new PeriodController();
        Context.require(ArrayDBUtils.containsInArrayDb(caller, pReps.validPreps) &&
                ArrayDBUtils.containsInArrayDb(caller, pReps.registeredPreps), "P-Rep is not registered yet.");
        Context.require(period.periodName.get().equals(APPLICATION_PERIOD),
                "P-Reps can only be unregister on Application Period");
        ArrayDBUtils.removeArrayItem(pReps.validPreps, caller);
        ArrayDBUtils.removeArrayItem(pReps.registeredPreps, caller);
        pReps.unregisteredPreps.add(caller);
        UnRegisterPRep(caller, "P-Rep has ben unregistered successfully.");

    }

    @Override
    @External
    public void registerPrep() {
        checkMaintenance();
        updatePeriod();
        Address caller = Context.getCaller();
        List<Address> prepList = getPrepsAddress();
        PReps pReps = new PReps();

        Context.require(prepList.contains(caller),
                TAG + ": Not a P-Rep.");
        Context.require(!ArrayDBUtils.containsInArrayDb(caller, pReps.registeredPreps),
                TAG + ": P-Rep is already registered.");
        Context.require(!ArrayDBUtils.containsInArrayDb(caller, pReps.denylist),
                TAG + ": You are in denylist. To register, You've to pay Penalty.");

        if (ArrayDBUtils.containsInArrayDb(caller, pReps.unregisteredPreps)) {
            ArrayDBUtils.removeArrayItem(pReps.unregisteredPreps, caller);
        }
        pReps.registeredPreps.add(caller);
        RegisterPRep(caller, "P-Rep Registered.");
        PeriodController period = new PeriodController();
        if (period.periodName.get().equals(APPLICATION_PERIOD)) {
            pReps.validPreps.add(caller);
        }

    }


    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getPrepTerm() {
        Map<String, Object> prepDict = callScore(Map.class, SYSTEM_ADDRESS, "getPRepTerm");
        return (List<Map<String, Object>>) prepDict.get("preps");
    }

    private Map<String, Object> getPRepInfo(Address address) {
        return callScore(Map.class, SYSTEM_ADDRESS, "getPRep", address);
    }


    private List<Address> getPrepsAddress() {
        List<Address> prepsList = new ArrayList<>();
        for (Map<String, Object> preps : getPrepTerm()) {
            Address prepAddress = (Address) preps.get("address");
            prepsList.add(prepAddress);
        }
        return prepsList;
    }

    private String getPrepName(Address address) {
        return (String) getPRepInfo(address).get("name");
    }

    private BigInteger getStake(Address address) {
        return (BigInteger) getPRepInfo(address).get("power");
    }

    private BigInteger getDelegation(Address address) {
        return delegationSnapshot.get(address);
    }

    private void setPreps() {
        PReps pReps = new PReps();
        ArrayDBUtils.clearArrayDb(pReps.validPreps);
        List<Address> prepsList = getPrepsAddress();

        for (Address prep : prepsList) {
            if (!ArrayDBUtils.containsInArrayDb(prep, pReps.denylist) &&
                    !ArrayDBUtils.containsInArrayDb(prep, pReps.unregisteredPreps) &&
                    ArrayDBUtils.containsInArrayDb(prep, pReps.registeredPreps)) {
                pReps.validPreps.add(prep);
            }
        }

    }

    private void removeSponsor(Address address, String ipfsHash) {
        Context.require(ArrayDBUtils.containsInArrayDb(address, sponsors),
                address + " not on sponsor list.");
        ArrayDBUtils.removeArrayItem(sponsors, address);
        ArrayDBUtils.removeArrayItem(sponsorProjects.at(address), ipfsHash);
    }

    private void removeContributor(Address address, String ipfsHash) {
        Context.require(ArrayDBUtils.containsInArrayDb(address, contributors),
                address + " not on contributor list.");
        ArrayDBUtils.removeArrayItem(contributors, address);
        ArrayDBUtils.removeArrayItem(contributorProjects.at(address), ipfsHash);
    }

    @External(readonly = true)
    public List<String> getProposalKeys() {
        List<String> proposalKeys = new ArrayList<>();
        for (int i = 0; i < proposalsKeyList.size(); i++) {
            proposalKeys.add(proposalsKeyList.get(i));

        }
        return proposalKeys;

    }

    @External(readonly = true)
    public List<String> getProgressKeys() {
        List<String> progressKeys = new ArrayList<>();
        for (int i = 0; i < progressKeyList.size(); i++) {
            progressKeys.add(progressKeyList.get(i));

        }
        return progressKeys;

    }

    private BigInteger getPenaltyAmount(Address address) {
        PReps pReps = new PReps();
        Integer count = pReps.prepsDenylistStatus.getOrDefault(address.toString(), 0);
        Context.require(count != 0, address + " doesn't need to pay any penalty.");

        int idx = count < 3 ? count - 1 : 2;
        BigInteger amount = pReps.penaltyAmount.get(idx);
        BigInteger delegationAmount = getStake(address);
        return delegationAmount.multiply(amount).divide(maxDelegation.get());
    }

    @Override
    @External(readonly = true)
    public boolean checkPriorityVoting(Address _prep) {
        int count = (int) getActiveProposalsList(0).get(COUNT);
        if (count > 0) {
            return ArrayDBUtils.containsInArrayDb(_prep, priorityVotedPreps);
        }
        return false;
    }

    @Override
    @External(readonly = true)
    public List<String> sortPriorityProposals() {
        Status status = new Status();
        int size = status.pending.size();
        String[] pendingProposals = new String[size];
        for (int i = 0; i < size; i++) {
            pendingProposals[i] = status.pending.get(i);
        }
        mergeSort(pendingProposals, 0, size - 1, getPriorityVoteResult());
        return arrayToList(pendingProposals);
    }

    @Override
    @External(readonly = true)
    public Map<String, Integer> getPriorityVoteResult() {
        Map<String, Integer> priorityVoteResult = new HashMap<>();
        Status status = new Status();

        int size = status.pending.size();
        for (int i = 0; i < size; i++) {
            String prop = status.pending.get(i);
            priorityVoteResult.put(prop, proposalRank.getOrDefault(prop, 0));

        }
        return priorityVoteResult;
    }


    @Override
    @External
    public void votePriority(String[] _proposals) {
        PeriodController period = new PeriodController();
        Context.require(period.periodName.get().equals(VOTING_PERIOD), TAG + ": Voting can only be done in Voting Period.");
        Address caller = Context.getCaller();
        PReps pReps = new PReps();
        Context.require(ArrayDBUtils.containsInArrayDb(caller, pReps.validPreps), "Voting can only be done by registered P-Reps");
        Context.require(!checkPriorityVoting(caller), "Already voted for Priority Ranking.");

        priorityVotedPreps.add(caller);
        int size = _proposals.length;
        Status status = new Status();
        for (int i = 0; i < size; i++) {
            String proposal = _proposals[i];
            Context.require(ArrayDBUtils.containsInArrayDb(proposal, status.pending),
                    proposal + " not in pending state.");
            proposalRank.set(proposal, proposalRank.getOrDefault(proposal, 0) + size - i);
        }
        PriorityVote(caller, "Priority voting done successfully.");
    }

    @Override
    @External
    public void setPrepPenaltyAmount(BigInteger[] penalty) {
        checkMaintenance();
        validateAdmins();
        Context.require(penalty.length == PENALTY_LEVELS, TAG + ": Exactly 3 Penalty amount Required.");
        PReps pReps = new PReps();
        for (int i = 0; i < PENALTY_LEVELS; i++) {
            BigInteger amount = penalty[i];
            Context.require(amount.compareTo(BigInteger.ZERO) >= 0, "Invalid amount" + amount);
            pReps.penaltyAmount.add(amount.multiply(EXA));
        }

    }


    @Override
    @External
    public void setInitialBlock() {
        validateAdmins();
        setPreps();
        PeriodController period = new PeriodController();
        period.initialBlock.set(BigInteger.valueOf(Context.getBlockHeight()));
        period.nextBlock.set(BigInteger.valueOf(Context.getBlockHeight()).add(BLOCKS_DAY_COUNT.multiply(DAY_COUNT)));
        period.periodName.set(APPLICATION_PERIOD);
        period.previousPeriodName.set("None");
        period.periodCount.set(0);
    }

    @Override
    @External(readonly = true)
    public Map<String, BigInteger> loginPrep(Address address) {
        Map<String, BigInteger> loginData = new HashMap<>();
        PeriodController period = new PeriodController();
        List<Address> allPreps;
        PReps pReps = new PReps();
        if (period.periodName.get().equals(APPLICATION_PERIOD)) {
            allPreps = getPrepsAddress();
        } else {
            allPreps = ArrayDBUtils.arrayDBtoList(pReps.validPreps);
        }

        loginData.put(IS_PREP, BigInteger.ZERO);
        loginData.put(IS_REGISTERED, BigInteger.ZERO);
        loginData.put(PAY_PENALTY, BigInteger.ZERO);
        loginData.put(VOTING_PREP, BigInteger.ZERO);

        if (allPreps.contains(address)) {
            loginData.put(IS_PREP, BigInteger.ONE);
            if (ArrayDBUtils.containsInArrayDb(address, pReps.denylist)) {
                loginData.put(PAY_PENALTY, BigInteger.ONE);
                loginData.put(PENALTY_AMOUNT1, getPenaltyAmount(address));
            } else if (ArrayDBUtils.containsInArrayDb(address, pReps.registeredPreps)) {
                loginData.put(IS_REGISTERED, BigInteger.ONE);

                if (ArrayDBUtils.containsInArrayDb(address, pReps.validPreps)) {
                    loginData.put(VOTING_PREP, BigInteger.ONE);
                }
            }

        }
        return loginData;
    }


    @Override
    @External(readonly = true)
    public List<Address> getAdmins() {
        return ArrayDBUtils.arrayDBtoList(admins);

    }


    @Override
    @External(readonly = true)
    public Map<String, BigInteger> getRemainingFund() {
        SetterGetter setterGetter = new SetterGetter();
        //noinspection unchecked
        return callScore(Map.class, setterGetter.cpfScore.get(), "getTotalFunds");
    }


    @Override
    @External(readonly = true)
    public List<Map<String, Object>> getPReps() {
        List<Map<String, Object>> prepsList = new ArrayList<>();
        PReps pReps = new PReps();
        for (int i = 0; i < pReps.validPreps.size(); i++) {
            Address prep = pReps.validPreps.get(i);
            Map<String, Object> prepData = new HashMap<>();
            prepData.put("name", getPrepName(prep));
            prepData.put("address", prep.toString());
            prepData.put("delegated", getStake(prep).toString());
            prepsList.add(prepData);
        }
        return prepsList;
    }

    @Override
    @External(readonly = true)
    public List<Address> getDenylist() {
        List<Address> denyList = new ArrayList<>();
        PReps pReps = new PReps();
        for (int i = 0; i < pReps.denylist.size(); i++) {
            denyList.add(pReps.denylist.get(i));
        }
        return denyList;

    }

    @Override
    @External(readonly = true)
    public Map<String, ?> getPeriodStatus() {
        PeriodController period = new PeriodController();
        BigInteger remainingTime = period.nextBlock.getOrDefault(BigInteger.ZERO).subtract(BigInteger.valueOf(Context.getBlockHeight())).multiply(BigInteger.valueOf(2));
        if (remainingTime.compareTo(BigInteger.ZERO) < 0) {
            remainingTime = BigInteger.ZERO;
        }
        return Map.of(CURRENTBLOCK, Context.getBlockHeight(),
                NEXTBLOCK, period.nextBlock.getOrDefault(BigInteger.valueOf(0)),
                REMAINING_TIME, remainingTime,
                PERIOD_NAME, period.periodName.getOrDefault("None"),
                PREVIOUS_PERIOD_NAME, period.previousPeriodName.getOrDefault("None"),
                PERIOD_SPAN, BLOCKS_DAY_COUNT.multiply(TOTAL_PERIOD));
    }

    @Override
    @External(readonly = true)
    public List<Address> getContributors() {
        return ArrayDBUtils.arrayDBtoList(this.contributors);
    }


    @Override
    @External(readonly = true)
    public Map<String, BigInteger> checkClaimableSponsorBond(Address address) {
        DictDB<String, BigInteger> userAmounts = sponsorBondReturn.at(address.toString());
        return Map.of(ICX, userAmounts.getOrDefault(ICX, BigInteger.ZERO),
                bnUSD, userAmounts.getOrDefault(bnUSD, BigInteger.ZERO));

    }


    @SuppressWarnings("unchecked")
    private BigInteger getMaxCapBNUsd() {
        SetterGetter setterGetter = new SetterGetter();
        Map<String, BigInteger> cpfAmount = callScore(Map.class, setterGetter.cpfScore.get(), "getRemainingSwapAmount");
        return cpfAmount.get("maxCap");
    }

    @Override
    @Payable
    @External
    public void submitProposal(ProposalAttributes proposals, MilestonesAttributes[] milestones) {
        checkMaintenance();
        updatePeriod();
        PeriodController period = new PeriodController();
        Context.require(period.periodName.get().equals(APPLICATION_PERIOD),
                TAG + ": Proposals can only be submitted on Application Period ");
        Context.require(!proposalKeyExists(proposals.ipfs_hash), TAG + ": Proposal key already exists.");
        Context.require(!Context.getCaller().isContract(), TAG + ": Contract Address not supported.");
        Context.require(proposals.project_duration <= MAX_PROJECT_PERIOD,
                TAG + ": Maximum Project Duration exceeds " + MAX_PROJECT_PERIOD + " months.");
        Context.require(proposals.milestoneCount == milestones.length, TAG + ": Milestone count mismatch");
        BigInteger projectBudget = proposals.total_budget.multiply(EXA);
        BigInteger maxCapBNUsd = getMaxCapBNUsd();
        Context.require(projectBudget.compareTo(maxCapBNUsd) < 0,
                TAG + ": " + projectBudget + "is greater than MAX CAP " + maxCapBNUsd);
        PReps pReps = new PReps();
        Context.require(ArrayDBUtils.containsInArrayDb(proposals.sponsor_address, pReps.validPreps),
                TAG + ": Sponsor P-Rep not a Top 100 P-Rep.");
        Context.require(Context.getValue().equals(BigInteger.valueOf(APPLICATION_FEE).multiply(EXA)),
                TAG + ": Deposit " + APPLICATION_FEE + " ICX to submit a proposal.");
        String tokenFlag = proposals.token;
        Context.require(tokenFlag.equals(bnUSD), TAG + ": " + tokenFlag + " Not a supported token.");

        String ipfsHash = proposals.ipfs_hash;
        String ipfsHashPrefix = proposalPrefix(ipfsHash);

        addDataToProposalDB(proposals, ipfsHashPrefix);
        BigInteger initialPaymentPercentage = callScore(BigInteger.class, getCpsTreasuryScore(), "getOnsetPayment");

        BigInteger totalBudget = proposals.total_budget.multiply(EXA);
        BigInteger milestoneBudget = totalBudget.subtract(totalBudget.multiply(initialPaymentPercentage).divide(HUNDRED));

        BigInteger totalMilestoneBudget = BigInteger.ZERO;
        for (MilestonesAttributes milestone : milestones) {
            totalMilestoneBudget = totalMilestoneBudget.add(milestone.budget);
            String milestonePrefix = mileStonePrefix(ipfsHash, milestone.id);
            addDataToMilestoneDb(milestone, milestonePrefix);
            milestoneIds.at(ipfsHashPrefix).add(milestone.id);
        }
        Context.require(milestoneBudget.equals(totalMilestoneBudget), TAG + ":: Total milestone budget and " +
                "project budget is not equal");

        proposalsKeyList.add(proposals.ipfs_hash);
        proposalsKeyListIndex.set(ipfsHash, proposalsKeyList.size() - 1);
        Status status = new Status();
        status.sponsorPending.add(ipfsHash);
        contributors.add(Context.getCaller());
        contributorProjects.at(Context.getCaller()).add(ipfsHash);
        ProposalSubmitted(Context.getCaller(), "Successfully submitted a Proposal.");

        BigInteger totalFund = proposalFees.getOrDefault(BigInteger.ZERO);
        BigInteger halfProposalFee = Context.getValue().divide(BigInteger.TWO);
        proposalFees.set(totalFund.add(halfProposalFee));
        burn(halfProposalFee, null);
        swapBNUsdToken();
    }


    @Override
    @External
    public void voteProposal(String ipfsKey, String vote, String voteReason, @Optional boolean voteChange) {
        checkMaintenance();
        updatePeriod();
        PeriodController period = new PeriodController();
        Context.require(period.periodName.get().equals(VOTING_PERIOD),
                TAG + ": Proposals can be voted only on Voting Period.");
        Address caller = Context.getCaller();
        PReps pReps = new PReps();
        Context.require(ArrayDBUtils.containsInArrayDb(caller, pReps.validPreps),
                TAG + ": Voting can only be done by registered P-Reps.");
        Context.require(List.of(APPROVE, REJECT, ABSTAIN).contains(vote),
                TAG + ": Vote should be either _approve, _reject or _abstain");

        Map<String, Object> proposalDetails = getProposalDetails(ipfsKey);
        String proposalPrefix = proposalPrefix(ipfsKey);
        String status = (String) proposalDetails.get(STATUS);

        ArrayDB<Address> voterList = ProposalDataDb.votersList.at(proposalPrefix);

        if (!voteChange && (ArrayDBUtils.containsInArrayDb(caller, voterList))) {
            Context.revert(TAG + ":: Already Voted");

        }
        Context.require(status.equals(PENDING), TAG + ": Proposal must be done in Voting state.");

        BigInteger voterStake = getDelegation(caller);
        Map<String, Object> proposalVoteDetails = getVoteResult(ipfsKey);
        BigInteger totalVotes = (BigInteger) proposalVoteDetails.get(TOTAL_VOTES);
        BigInteger approvedVotes = (BigInteger) proposalVoteDetails.get(APPROVED_VOTES);
        BigInteger rejectedVotes = (BigInteger) proposalVoteDetails.get(REJECTED_VOTES);
        BigInteger abstainedVotes = (BigInteger) proposalVoteDetails.get(ABSTAINED_VOTES);
        Integer totalVoter = (Integer) proposalVoteDetails.get(TOTAL_VOTERS);
        if (totalVoter == 0 || totalVotes.equals(BigInteger.ZERO)) {
            ProposalDataDb.totalVoters.at(proposalPrefix).set(pReps.validPreps.size());
            ProposalDataDb.totalVotes.at(proposalPrefix).set(totalDelegationSnapshot.getOrDefault(BigInteger.ZERO));
        }

        DictDB<String, Integer> votersIndexDb = votersListIndex.at(proposalPrefix).at(caller);

        if (!voteChange) {
            ProposalDataDb.votersList.at(proposalPrefix).add(caller);
            votersIndexDb.set(INDEX, ProposalDataDb.votersList.at(proposalPrefix).size());
            ProposalDataDb.votersReasons.at(proposalPrefix).add(voteReason);
        } else {
            Context.require(votersIndexDb.getOrDefault(CHANGE_VOTE, 0) == 0,
                    TAG + ": Vote change can be done only once.");
            votersIndexDb.set(CHANGE_VOTE, VOTED);
            int index = votersIndexDb.getOrDefault(INDEX, 0);
            int voteIndex = votersIndexDb.getOrDefault(VOTE, 0);
            ProposalDataDb.votersReasons.at(proposalPrefix).set(index - 1, voteReason);
            if (voteIndex == APPROVE_) {
                if (vote.equals(APPROVE)) {
                    Context.revert(TAG + ":: Cannot cast same vote. Change your vote");
                }
                ArrayDBUtils.removeArrayItem(ProposalDataDb.approveVoters.at(proposalPrefix), caller);
                ProposalDataDb.approvedVotes.at(proposalPrefix).set(approvedVotes.subtract(voterStake));
            } else if (voteIndex == REJECT_) {
                if (vote.equals(REJECT)) {
                    Context.revert(TAG + ":: Cannot cast same vote. Change your vote");
                }
                ArrayDBUtils.removeArrayItem(ProposalDataDb.rejectVoters.at(proposalPrefix), caller);
                ProposalDataDb.rejectedVotes.at(proposalPrefix).set(rejectedVotes.subtract(voterStake));
            } else {
                ArrayDBUtils.removeArrayItem(abstainVoters.at(proposalPrefix), caller);
                ProposalDataDb.abstainedVotes.at(proposalPrefix).set(abstainedVotes.subtract(voterStake));
            }
            approvedVotes = ProposalDataDb.approvedVotes.at(proposalPrefix).getOrDefault(BigInteger.ZERO);
            rejectedVotes = ProposalDataDb.rejectedVotes.at(proposalPrefix).getOrDefault(BigInteger.ZERO);
            abstainedVotes = ProposalDataDb.abstainedVotes.at(proposalPrefix).getOrDefault(BigInteger.ZERO);

        }
        if (vote.equals(APPROVE)) {
            ProposalDataDb.approveVoters.at(proposalPrefix).add(caller);
            votersIndexDb.set(VOTE, APPROVE_);
            ProposalDataDb.approvedVotes.at(proposalPrefix).set(approvedVotes.add(voterStake));
        } else if (vote.equals(REJECT)) {
            ProposalDataDb.rejectVoters.at(proposalPrefix).add(caller);
            votersIndexDb.set(VOTE, REJECT_);
            ProposalDataDb.rejectedVotes.at(proposalPrefix).set(rejectedVotes.add(voterStake));

        } else {
            abstainVoters.at(proposalPrefix).add(caller);
            votersIndexDb.set(VOTE, ABSTAIN_);
            ProposalDataDb.abstainedVotes.at(proposalPrefix).set(abstainedVotes.add(voterStake));
        }
        VotedSuccessfully(caller, "Proposal Vote for " + proposalDetails.get(PROJECT_TITLE) + " Successful.");
        swapBNUsdToken();
    }


    private List<Integer> getMilestoneDeadline(String ipfsHash) {
        String ipfsHAshPRefix = proposalPrefix(ipfsHash);
        ArrayDB<Integer> milestoneIDs = milestoneIds.at(ipfsHAshPRefix);

        List<Integer> milestoneIdList = new ArrayList<>();
        for (int i = 0; i < milestoneIDs.size(); i++) {
            int milestoneId = milestoneIDs.get(i);
            String milestonePrefix = mileStonePrefix(ipfsHash, milestoneId);
            int proposalTotalPeriod = proposalPeriod.at(ipfsHAshPRefix).getOrDefault(0);
            int completionPeriod = MilestoneDb.completionPeriod.at(milestonePrefix).getOrDefault(0);

            int computedCompletionPeriod = proposalTotalPeriod + completionPeriod;

            int currentPeriod = getPeriodCount();
            if (currentPeriod >= computedCompletionPeriod) {
                milestoneIdList.add(milestoneId);
            }
        }
        return milestoneIdList;
    }


    @Override
    @External
    public void submitProgressReport(ProgressReportAttributes progressReport, MilestoneSubmission[] milestoneSubmissions) {
        checkMaintenance();
        updatePeriod();
        PeriodController period = new PeriodController();
        Context.require(period.periodName.get().equals(APPLICATION_PERIOD),
                TAG + ": Proposals can only be submitted on Application Period ");

        Address caller = Context.getCaller();
        Context.require(!caller.isContract(), TAG + ": Contract Address not supported.");

        String ipfsHashPrefix = proposalPrefix(progressReport.ipfs_hash);
        String tokenFlag = ProposalDataDb.token.at(ipfsHashPrefix).getOrDefault("");
        Context.require(tokenFlag.equals(bnUSD), TAG + ": " + tokenFlag + " Not a supported token.");

        Address contributorAddress = ProposalDataDb.contributorAddress.at(ipfsHashPrefix).get();
        Context.require(caller.equals(contributorAddress),
                TAG + ": Sorry, You are not the contributor for this project.");

        String status = ProposalDataDb.status.at(ipfsHashPrefix).get();
        Context.require(List.of(ACTIVE, PAUSED).contains(status),
                TAG + ": Sorry, This project is not found on active state.");

        Boolean progressSubmitted = ProposalDataDb.submitProgressReport.at(ipfsHashPrefix).get();
        Context.require(!progressSubmitted, TAG + ": Progress Report is already submitted this cycle.");

        String reportHash = progressReport.report_hash;
        String ipfsHash = progressReport.ipfs_hash;
        Context.require(!progressKeyExists(reportHash), TAG + ": Report key already exists.");
        Context.require(proposalKeyExists(ipfsHash), TAG + ": Invalid proposal key");
        addNewProgressReportKey(ipfsHash, reportHash);
        String reportHashPrefix = progressReportPrefix(reportHash);

        addDataToProgressReportDB(progressReport, reportHashPrefix);
        int totalMilestoneCount = ProposalDataDb.getMilestoneCount(ipfsHashPrefix);
        int currentPeriod = getPeriodCount();
        int duration = projectDuration.at(ipfsHashPrefix).get();
        int totalPeriod = duration + proposalPeriod.at(ipfsHashPrefix).getOrDefault(0);

        if (totalMilestoneCount != 0) {
            boolean lastProgressReport = totalPeriod - currentPeriod == 0;
            int approvedReports = ProposalDataDb.approvedReports.at(ipfsHashPrefix).getOrDefault(0);
            if ((lastProgressReport) && (milestoneSubmissions.length + approvedReports != totalMilestoneCount)) {
                Context.revert(TAG + ":: Submit progress report for all milestones.");
            }


            Context.require(milestoneSubmissions.length + approvedReports <= totalMilestoneCount,
                    TAG + ":: Submitted milestone is greater than recorded on proposal.");

            for (MilestoneSubmission milestoneAttr : milestoneSubmissions) {
                if (getMilestoneDeadline(ipfsHash).size() > 0) {
                    Context.require(getMilestoneDeadline(ipfsHash).contains(milestoneAttr.id), TAG +
                            ": Submit milestone report for milestone id " + getMilestoneDeadline(ipfsHash));
                }
                int milestoneStatus = getMileststoneStatusOf(ipfsHash, milestoneAttr.id);
                if (milestoneStatus == MILESTONE_REPORT_APPROVED || milestoneStatus == MILESTONE_REPORT_COMPLETED) {
                    Context.revert(TAG + " Milestone already completed/submitted " + milestoneStatus);
                }
                int stats = MILESTONE_REPORT_NOT_COMPLETED;
                if (milestoneAttr.status) {
                    stats = MILESTONE_REPORT_COMPLETED;
                }
                String milestonePrefix = mileStonePrefix(ipfsHash, milestoneAttr.id);
                MilestoneDb.status.at(milestonePrefix).set(stats);
                MilestoneDb.progressReportHash.at(milestonePrefix).set(reportHash);
                milestoneSubmitted.at(reportHashPrefix).add(milestoneAttr.id);
            }

        }

        if (progressReport.budget_adjustment) {
            Context.require(getBudgetAdjustmentFeature(),
                    TAG + ": Budget Adjustment feature is disabled for the moment.");

            Boolean budgetAdjustment = ProposalDataDb.budgetAdjustment.at(ipfsHashPrefix).get();
            Context.require(!budgetAdjustment,
                    TAG + ": Budget Adjustment Already submitted for this proposal.");

            int projectDuration = ProposalDataDb.projectDuration.at(ipfsHashPrefix).get();
            Context.require(progressReport.additional_month + projectDuration <= MAX_PROJECT_PERIOD,
                    TAG + ": Maximum period for a project is " + MAX_PROJECT_PERIOD + " months.");

            budgetApprovalsList.add(reportHash);
            ProgressReportDataDb.budgetAdjustmentStatus.at(reportHashPrefix).set(PENDING);
            ProposalDataDb.budgetAdjustment.at(ipfsHashPrefix).set(true);
        }
        progressKeyList.add(reportHash);
        progressKeyListIndex.set(reportHash, progressKeyList.size() - 1);

        submitProgressReport.at(ipfsHashPrefix).set(true);
        Status _status = new Status();
        _status.waitingProgressReports.add(reportHash);
        swapBNUsdToken();
        ProgressReportSubmitted(caller, progressReport.progress_report_title +
                " --> Progress Report Submitted Successfully.");
    }

    public boolean isAllElementPresent(MilestoneVoteAttributes[] vote, ArrayDB<Integer> submitted) {
        for (int i = 0; i < submitted.size(); i++) {
            boolean found = false;
            for (MilestoneVoteAttributes v : vote) {
                if (v.id == submitted.get(i)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }


    @Override
    @External
    public void voteProgressReport(String reportKey, String voteReason, MilestoneVoteAttributes[] votes,
                                   @Optional String budgetAdjustmentVote, @Optional boolean voteChange) {
        if (budgetAdjustmentVote == null) {
            budgetAdjustmentVote = "";
        }

        checkMaintenance();
        updatePeriod();
        PeriodController period = new PeriodController();
        Context.require(period.periodName.get().equals(VOTING_PERIOD),
                TAG + ": Progress Reports can be voted only on Voting Period.");
        Address caller = Context.getCaller();
        PReps pReps = new PReps();
        Context.require(ArrayDBUtils.containsInArrayDb(caller, pReps.validPreps),
                TAG + ": Voting can only be done by registered P-Reps.");
        String progressReportPrefix = progressReportPrefix(reportKey);
        ArrayDB<Integer> submittedMilestones = milestoneSubmitted.at(progressReportPrefix);// CAN TAKE FROM READONLY METHOD
        for (MilestoneVoteAttributes milestoneVote : votes) {
            Context.require(List.of(APPROVE, REJECT).contains(milestoneVote.vote),
                    TAG + ": Vote should be either _approve or _reject");
            Context.require(ArrayDBUtils.containsInArrayDb(milestoneVote.id, submittedMilestones),
                    TAG + ": Voting can only be done for milestone submitted in this progress report");
        }
        if (!voteChange) {
            if (votes.length == submittedMilestones.size()) {
                Context.require(isAllElementPresent(votes, submittedMilestones),
                        "You should submit votes for all milestones of the progress report");

            } else {
                Context.revert(TAG + ":: You should submit votes for all milestones of the progress report");
            }
        }
        Map<String, Object> progressReportDetails = getProgressReportDetails(reportKey);
        String status = (String) progressReportDetails.get(STATUS);

        if (!status.equals(WAITING)) {
            Context.revert(TAG + ": Voting can be only be done in waiting progress reports.");
        }

        BigInteger voterStake = getDelegation(caller);
        String proposalKey = ProgressReportDataDb.ipfsHash.at(progressReportPrefix).get();

        BigInteger totalVotes = ProgressReportDataDb.totalVotes.at(progressReportPrefix).getOrDefault(BigInteger.ZERO);
        Integer totalVoter = ProgressReportDataDb.totalVoters.at(progressReportPrefix).getOrDefault(0);

        if (totalVoter == 0 || totalVotes.equals(BigInteger.ZERO)) {
            ProgressReportDataDb.totalVoters.at(progressReportPrefix).set(pReps.validPreps.size());
            ProgressReportDataDb.totalVotes.at(progressReportPrefix).set(this.totalDelegationSnapshot.getOrDefault(BigInteger.ZERO));
        }

        DictDB<Address, Integer> voteChanged = ProgressReportDataDb.voteChange.at(progressReportPrefix);

        if (voteChanged.getOrDefault(caller, NOT_VOTED) == 1) {
            Context.revert(TAG + ":: Vote change can be done only once.");
        }
        for (MilestoneVoteAttributes milestoneVote : votes) {
            String milestonePrefix = mileStonePrefix(proposalKey, milestoneVote.id);
            ArrayDB<Address> voterList = MilestoneDb.votersList.at(milestonePrefix);
            if (!voteChange && (ArrayDBUtils.containsInArrayDb(caller, voterList))) {
                Context.revert(TAG + ":: Already Voted");

            }

            Map<String, Object> milestoneDetails = getDataFromMilestoneDB(milestonePrefix);
            BigInteger approvedVotes = (BigInteger) milestoneDetails.get(APPROVED_VOTES);
            BigInteger rejectedVotes = (BigInteger) milestoneDetails.get(REJECTED_VOTES);

            DictDB<String, Integer> votersIndexDb = MilestoneDb.votersListIndices.at(milestonePrefix).at(caller);
            if (!voteChange) {
                MilestoneDb.votersList.at(milestonePrefix).add(caller);
                votersIndexDb.set(INDEX, MilestoneDb.votersList.at(milestonePrefix).size());
                ProgressReportDataDb.votersReasons.at(progressReportPrefix).add(voteReason);
            } else {
                voteChanged.set(caller, VOTED);
                int index = votersIndexDb.getOrDefault(INDEX, 0);
                int voteIndex = votersIndexDb.getOrDefault(VOTE, 0);
                ProgressReportDataDb.votersReasons.at(progressReportPrefix).set(index - 1, voteReason);
                if (voteIndex == APPROVE_) {
                    if (milestoneVote.vote.equals(APPROVE)) {
                        Context.revert(TAG + ": Cannot cast same vote. Change your vote");
                    }
                    ArrayDBUtils.removeArrayItem(MilestoneDb.approveVoters.at(milestonePrefix), caller);
                    MilestoneDb.approvedVotes.at(milestonePrefix).set(approvedVotes.subtract(voterStake));
                } else {
                    if (milestoneVote.vote.equals(REJECT)) {
                        Context.revert(TAG + ": Cannot cast same vote. Change your vote");
                    }
                    ArrayDBUtils.removeArrayItem(MilestoneDb.rejectVoters.at(milestonePrefix), caller);
                    MilestoneDb.rejectedVotes.at(milestonePrefix).set(rejectedVotes.subtract(voterStake));
                }
            }

            boolean budgetAdjustment = (boolean) progressReportDetails.get(BUDGET_ADJUSTMENT);
            if (budgetAdjustment && getBudgetAdjustmentFeature()) { //
                budgetAdjustment(reportKey, budgetAdjustmentVote, voteChange);
            }
            approvedVotes = MilestoneDb.approvedVotes.at(milestonePrefix).getOrDefault(BigInteger.ZERO);
            rejectedVotes = MilestoneDb.rejectedVotes.at(milestonePrefix).getOrDefault(BigInteger.ZERO);
            if (milestoneVote.vote.equals(APPROVE)) {
                MilestoneDb.approveVoters.at(milestonePrefix).add(caller);
                votersIndexDb.set(VOTE, APPROVE_);
                MilestoneDb.approvedVotes.at(milestonePrefix).set(approvedVotes.add(voterStake));

            } else {
                MilestoneDb.rejectVoters.at(milestonePrefix).add(caller);
                votersIndexDb.set(VOTE, REJECT_);
                MilestoneDb.rejectedVotes.at(milestonePrefix).set(rejectedVotes.add(voterStake));

            }
            if (budgetAdjustment && getBudgetAdjustmentFeature()) {
                budgetAdjustment(reportKey, budgetAdjustmentVote, voteChange);

            }
            VotedSuccessfully(caller, "Progress Report Vote for " + progressReportDetails.get(PROGRESS_REPORT_TITLE) + " Successful.");
            swapBNUsdToken();
        }
    }

    private void budgetAdjustment(String reportKey, String budgetAdjustmentVote, boolean voteChange) {
        String progressReportPrefix = progressReportPrefix(reportKey);
        Address caller = Context.getCaller();
        BigInteger voterStake = getDelegation(caller);
        if (ArrayDBUtils.containsInArrayDb(reportKey, budgetApprovalsList)) {
            BigInteger budgetApprovedVotes = ProgressReportDataDb.budgetApprovedVotes.at(progressReportPrefix).getOrDefault(BigInteger.ZERO);
            BigInteger budgetRejectedVotes = ProgressReportDataDb.budgetRejectedVotes.at(progressReportPrefix).getOrDefault(BigInteger.ZERO);

            if (voteChange) {
                int budgetVoteIndex = budgetVotersListIndices.at(progressReportPrefix).at(caller).getOrDefault(VOTE, 0);
                if (budgetVoteIndex == APPROVE_) {
                    ArrayDBUtils.removeArrayItem(budgetApproveVoters.at(progressReportPrefix), caller);
                    ProgressReportDataDb.budgetApprovedVotes.at(progressReportPrefix).set(budgetApprovedVotes.subtract(voterStake));
                } else if (budgetVoteIndex == REJECT_) {
                    ArrayDBUtils.removeArrayItem(budgetRejectVoters.at(progressReportPrefix), caller);
                    ProgressReportDataDb.budgetRejectedVotes.at(progressReportPrefix).set(budgetRejectedVotes.subtract(voterStake));
                } else {
                    Context.revert(TAG + ": Choose option " + APPROVE + " or " + REJECT + " for budget adjustment");
                }
            } else {
                DictDB<String, Integer> budgetVoteIndex = budgetVotersListIndices.at(progressReportPrefix).at(caller);
                if (budgetAdjustmentVote.equals(APPROVE)) {
                    ProgressReportDataDb.budgetApproveVoters.at(progressReportPrefix).add(caller);
                    ProgressReportDataDb.budgetApprovedVotes.at(progressReportPrefix).set(budgetApprovedVotes.add(voterStake));
                    budgetVoteIndex.set(VOTE, APPROVE_);
                } else if (budgetAdjustmentVote.equals(REJECT)) {
                    ProgressReportDataDb.budgetRejectVoters.at(progressReportPrefix).add(caller);
                    ProgressReportDataDb.budgetRejectedVotes.at(progressReportPrefix).set(budgetRejectedVotes.add(voterStake));
                    budgetVoteIndex.set(VOTE, REJECT_);
                } else {
                    Context.revert(TAG + ": Choose option " + APPROVE + " or " + REJECT + " for budget adjustment");
                }
            }
        }
    }

    @Override
    @External(readonly = true)
    public List<String> getProposalsKeysByStatus(String status) {
        Status _status = new Status();
        Context.require(STATUS_TYPE.contains(status), TAG + ": Not a valid status");
        ArrayDB<String> proposalStatus = _status.proposalStatus.get(status);
        return ArrayDBUtils.arrayDBtoList(proposalStatus);
    }

    @Override
    @External(readonly = true)
    public int checkChangeVote(Address address, String ipfsHash, String proposalType) {
        if (proposalType.equals(PROPOSAL)) {
            String proposalPrefix = proposalPrefix(ipfsHash);
            return ProposalDataDb.votersListIndex.at(proposalPrefix).at(address).getOrDefault(CHANGE_VOTE, NOT_VOTED);
        } else if (proposalType.equals(PROGRESS_REPORTS)) {
            String progressReportPrefix = progressReportPrefix(ipfsHash);
            return ProgressReportDataDb.voteChange.at(progressReportPrefix).getOrDefault(address, NOT_VOTED);
        } else {
            return 0;
        }
    }

    @Override
    @External
    public void updateContributor(String _ipfs_hash, Address _new_contributor) {
        Context.require(!_new_contributor.isContract(), TAG + ": Contract Address not supported.");

        Map<String, Object> _proposal_details = getProposalDetails(_ipfs_hash);
        String _proposal_status = (String) _proposal_details.get(STATUS);
        Context.require(_proposal_status.equals(ACTIVE), TAG + ": Proposal must be active");
        // check sender must be current contributor
        Address _contributor_address = (Address) _proposal_details.get(CONTRIBUTOR_ADDRESS);
        Context.require(Context.getCaller().equals(_contributor_address), TAG + ": Caller must be current contributor");
        removeContributor(_contributor_address, _ipfs_hash);
        // update contributor's address
        contributors.add(_new_contributor);
        contributorProjects.at(_new_contributor).add(_ipfs_hash);

        // request update contributor address to cps treasury
        callScore(getCpsTreasuryScore(), "update_contributor_address", _ipfs_hash, _new_contributor);

        // emit event
        UpdateContributor(_contributor_address, _new_contributor);
    }

    @Override
    @Deprecated(since = "JAVA translation", forRemoval = true)
    @External(readonly = true)
    public int check_change_vote(Address _address, String _ipfs_hash, String _proposal_type) {
        return checkChangeVote(_address, _ipfs_hash, _proposal_type);
    }


    @External(readonly = true)
    public Map<String, Object> getProjectAmounts() {
        List<String> statusList = List.of(PENDING, ACTIVE, PAUSED, COMPLETED, DISQUALIFIED);
        BigInteger pendingAmountIcx = BigInteger.ZERO;
        BigInteger activeAmountIcx = BigInteger.ZERO;
        BigInteger pausedAmountIcx = BigInteger.ZERO;
        BigInteger completedAmountIcx = BigInteger.ZERO;
        BigInteger disqualifiedAmountIcx = BigInteger.ZERO;

        BigInteger pendingAmountBnusd = BigInteger.ZERO;
        BigInteger activeAmountBnusd = BigInteger.ZERO;
        BigInteger pausedAmountBnusd = BigInteger.ZERO;
        BigInteger completedAmountBnusd = BigInteger.ZERO;
        BigInteger disqualifiedAmountBnusd = BigInteger.ZERO;

        for (int statusId = 0; statusId < statusList.size(); statusId++) {
            BigInteger amountICX = BigInteger.ZERO;
            BigInteger amountBnusd = BigInteger.ZERO;

            List<String> proposalsKeysByStatus = this.getProposalsKeysByStatus(statusList.get(statusId));
            for (String proposalKey : proposalsKeysByStatus) {
                String proposalPrefix = proposalPrefix(proposalKey);
                BigInteger projectBudget = ProposalDataDb.totalBudget.at(proposalPrefix).getOrDefault(BigInteger.ZERO);
                String token = ProposalDataDb.token.at(proposalPrefix).getOrDefault("");
                if (token.equals(ICX)) {
                    amountICX = amountICX.add(projectBudget);
                } else {
                    amountBnusd = amountBnusd.add(projectBudget);
                }
            }
            if (statusId == 0) {
                pendingAmountIcx = amountICX;
                pendingAmountBnusd = amountBnusd;
            } else if (statusId == 1) {
                activeAmountIcx = amountICX;
                activeAmountBnusd = amountBnusd;
            } else if (statusId == 2) {
                pausedAmountIcx = amountICX;
                pausedAmountBnusd = amountBnusd;
            } else if (statusId == 3) {
                completedAmountIcx = amountICX;
                completedAmountBnusd = amountBnusd;
            } else {
                disqualifiedAmountIcx = amountICX;
                disqualifiedAmountBnusd = amountBnusd;
            }


        }
        Status status = new Status();
        return Map.of(statusList.get(0), Map.of(AMOUNT, Map.of(ICX, pendingAmountIcx, bnUSD, pendingAmountBnusd), COUNT, status.proposalStatus.get(statusList.get(0)).size()),
                statusList.get(1), Map.of(AMOUNT, Map.of(ICX, activeAmountIcx, bnUSD, activeAmountBnusd), COUNT, status.proposalStatus.get(statusList.get(1)).size()),
                statusList.get(2), Map.of(AMOUNT, Map.of(ICX, pausedAmountIcx, bnUSD, pausedAmountBnusd), COUNT, status.proposalStatus.get(statusList.get(2)).size()),
                statusList.get(3), Map.of(AMOUNT, Map.of(ICX, completedAmountIcx, bnUSD, completedAmountBnusd), COUNT, status.proposalStatus.get(statusList.get(3)).size()),
                statusList.get(4), Map.of(AMOUNT, Map.of(ICX, disqualifiedAmountIcx, bnUSD, disqualifiedAmountBnusd), COUNT, status.proposalStatus.get(statusList.get(4)).size()));


    }

    @Override
    @External(readonly = true)
    public Map<String, Integer> getSponsorsRecord() {
        Map<String, Integer> sponsorsDict = new HashMap<>();
        int size = sponsors.size();
        for (int i = 0; i < size; i++) {
            Address sponsorAddress = sponsors.get(i);
            sponsorsDict.put(sponsorAddress.toString(), sponsorProjects.at(sponsorAddress).size());
        }
        return sponsorsDict;
    }


    @Override
    @External
    public void updatePeriod() {
        checkMaintenance();
        BigInteger currentBlock = BigInteger.valueOf(Context.getBlockHeight());
        PeriodController period = new PeriodController();
        BigInteger nextBlock = period.nextBlock.get();
        PReps pReps = new PReps();
        Status status = new Status();
        if (currentBlock.compareTo(nextBlock) >= 0) {
            if (period.periodName.get().equals(APPLICATION_PERIOD)) {
                period.periodName.set(VOTING_PERIOD);
                period.previousPeriodName.set(APPLICATION_PERIOD);
                period.nextBlock.set(nextBlock.add(BLOCKS_DAY_COUNT.multiply(getVotingPeriod())));
                updateApplicationResult();

                period.updatePeriodIndex.set(0);
                setPreps();
                snapshotDelegation();

                int activeProposalCount = status.pending.size() + status.waitingProgressReports.size();
                swapCount.set(activeProposalCount + pReps.validPreps.size());

            } else {
                Integer updateIndex = period.updatePeriodIndex.get();
                if (updateIndex == 0) {
                    period.periodName.set(TRANSITION_PERIOD);
                    period.previousPeriodName.set(APPLICATION_PERIOD);
                    period.updatePeriodIndex.set(updateIndex + 1);
                    updateProposalsResult();

                    PeriodUpdate("Period Update State 1/4. Period Updated to Transition Period. " +
                            "After all the calculations are completed, " +
                            "Period will change to " + APPLICATION_PERIOD);
                } else if (updateIndex == 1) {
                    checkProgressReportSubmission();
                    period.updatePeriodIndex.set(updateIndex + 1);
                    PeriodUpdate("Period Update State 2/4. Progress Reports Checks Completed.");
                } else if (updateIndex == 2) {
                    updateProgressReportResult();
                    period.updatePeriodIndex.set(updateIndex + 1);
                    PeriodUpdate("Period Update State 3/4. Progress Reports Calculations Completed.");
                } else {
                    SetterGetter setterGetter = new SetterGetter();
                    updateDenylistPreps();
                    period.nextBlock.set(nextBlock.add(BLOCKS_DAY_COUNT.multiply(getApplicationPeriod())));
                    period.periodName.set(APPLICATION_PERIOD);
                    period.previousPeriodName.set(VOTING_PERIOD);
                    PeriodUpdate("Period Update State 4/4. Period Successfully Updated to Application Period.");
                    setPreps();

                    int activeProposalCount = status.active.size() + status.paused.size();
                    swapCount.set(activeProposalCount + activeProposalCount * pReps.validPreps.size());
                    callScore(setterGetter.cpfScore.get(), "resetSwapState");

                    ArrayDBUtils.clearArrayDb(budgetApprovalsList);
                    ArrayDBUtils.clearArrayDb(priorityVotedPreps);

                    period.periodCount.set(period.periodCount.getOrDefault(0) + 1);
                    burn(proposalFees.get(), null);
                    proposalFees.set(BigInteger.ZERO);
                }

            }
        }
    }


    private void updateDenylistPreps() {
        PReps pReps = new PReps();
        for (int i = 0; i < pReps.inactivePreps.size(); i++) {
            Address prep = pReps.inactivePreps.get(i);
            ArrayDBUtils.removeArrayItem(pReps.registeredPreps, prep);
            pReps.denylist.add(prep);
            int count = pReps.prepsDenylistStatus.getOrDefault(prep.toString(), 0) + 1;
            pReps.prepsDenylistStatus.set(prep.toString(), Math.min(count, 3));
            PRepPenalty(prep, "P-Rep added to Denylist.");

        }
        ArrayDBUtils.clearArrayDb(pReps.inactivePreps);
    }


    private void updateMilestoneDB(String milestonePrefix) {
        MilestoneDb.approvedVotes.at(milestonePrefix).set(BigInteger.ZERO);
        clearArrayDb(MilestoneDb.approveVoters.at(milestonePrefix));

        MilestoneDb.rejectedVotes.at(milestonePrefix).set(BigInteger.ZERO);
        clearArrayDb(MilestoneDb.rejectVoters.at(milestonePrefix));

        ArrayDB<Address> voters = MilestoneDb.votersList.at(milestonePrefix);
        for (int i = 0; i < voters.size(); i++) {
            Address prep = voters.get(i);
            DictDB<String, Integer> prepVote = MilestoneDb.votersListIndices.at(milestonePrefix).at(prep);
            prepVote.set(INDEX, 0);
            prepVote.set(VOTE, 0);
        }
        clearArrayDb(MilestoneDb.votersList.at(milestonePrefix));
    }

    /***
     Calculate votes for the progress reports and update the status and get the Installment and Sponsor
     Reward is the progress report is accepted.
     :return:
     ***/

    private void updateProgressReportResult() {
        Status status = new Status();
        List<String> waiting_progress_reports = arrayDBtoList(status.waitingProgressReports);
        PReps pReps = new PReps();

        for (String _reports : waiting_progress_reports) {
            Map<String, Object> _report_result = getProgressReportDetails(_reports);

            String _ipfs_hash = (String) _report_result.get(IPFS_HASH);
            String proposal_prefix = proposalPrefix(_ipfs_hash);
            String progressPrefix = progressReportPrefix(_reports);

            submitProgressReport.at(proposal_prefix).set(Boolean.FALSE);

            boolean _budget_adjustment = (boolean) _report_result.get(BUDGET_ADJUSTMENT);


//          If a progress report have any budget_adjustment, then it checks the budget adjustment first
            if (_budget_adjustment && getBudgetAdjustmentFeature()) {
                updateBudgetAdjustments(_reports);
            }
            Map<String, Object> _proposal_details = getProposalDetails(_ipfs_hash);

            int milestoneCount = (int) _proposal_details.get(MILESTONE_COUNT);
            String _proposal_status = (String) _proposal_details.get(STATUS);
            int _approved_reports_count = (int) _proposal_details.get(APPROVED_REPORTS);
            Address _sponsor_address = (Address) _proposal_details.get(SPONSOR_ADDRESS);
            BigInteger _sponsor_deposit_amount = (BigInteger) _proposal_details.get(SPONSOR_DEPOSIT_AMOUNT);
            String flag = (String) _proposal_details.get(TOKEN);

            List<Address> _main_preps_list = arrayDBtoList(pReps.validPreps);

            ArrayDB<Integer> milestoneSubmitted = ProgressReportDataDb.milestoneSubmitted.at(progressPrefix);

            int milestonePassed = 0;
            BigInteger milestoneBudget = BigInteger.ZERO;
            int milestoneSubmittedSize = milestoneSubmitted.size();

            for (int i = 0; i < milestoneSubmittedSize; i++) {
                String milestonePrefix = mileStonePrefix(_ipfs_hash, milestoneSubmitted.get(i));

                // checking which prep(s) did not vote the progress report
                checkInactivePreps(MilestoneDb.votersList.at(milestonePrefix));

                Map<String, Object> _milestone_details = getDataFromMilestoneDB(milestonePrefix);

                int milestoneStatus = (int) _milestone_details.get(STATUS);
                int _approve_voters = (int) _milestone_details.get(APPROVE_VOTERS);
                BigInteger _approved_votes = (BigInteger) _milestone_details.get(APPROVED_VOTES);
                BigInteger _total_votes = ProgressReportDataDb.totalVotes.at(progressPrefix).get();
                int _total_voters = ProgressReportDataDb.totalVoters.at(progressPrefix).getOrDefault(0);

                if (_total_voters == 0 || _total_votes.equals(BigInteger.ZERO) || _main_preps_list.size() < MINIMUM_PREPS) {
                    MilestoneDb.status.at(milestonePrefix).set(MILESTONE_REPORT_REJECTED);
                    updateMilestoneDB(milestonePrefix);
                } else {
                    double votersRatio = (double) _approve_voters / _total_voters;
                    double votesRatio = _approved_votes.doubleValue() / _total_votes.doubleValue();
                    if (votersRatio >= MAJORITY && votesRatio >= MAJORITY) {
                        _approved_reports_count += 1;

                        if (milestoneStatus == MILESTONE_REPORT_COMPLETED) {
                            milestonePassed += 1;
                            milestoneBudget = milestoneBudget.add(MilestoneDb.budget.at(milestonePrefix).getOrDefault(BigInteger.ZERO));
                            MilestoneDb.status.at(milestonePrefix).set(MILESTONE_REPORT_APPROVED);
                            int percentageCompleted = (_approved_reports_count * 100) / milestoneCount;
                            ProposalDataDb.updatePercentageCompleted(proposal_prefix, percentageCompleted);

                            ProposalDataDb.approvedReports.at(proposal_prefix).set(_approved_reports_count);

                            if (_approved_reports_count == milestoneCount) {
                                updateProposalStatus(_ipfs_hash, COMPLETED);
                                ProposalDataDb.updatePercentageCompleted(proposal_prefix, 100);

                                // Transfer the Sponsor - Bond back to the Sponsor P - Rep after the project is completed.
                                this.sponsorBondReturn.at(_sponsor_address.toString()).set(flag,
                                        this.sponsorBondReturn.at(_sponsor_address.toString()).getOrDefault(flag, BigInteger.ZERO).
                                                add(_sponsor_deposit_amount));
                                sponsorDepositStatus.at(proposal_prefix).set(BOND_RETURNED);
                                SponsorBondReturned(_sponsor_address,
                                        _sponsor_deposit_amount + " " + flag + " returned to sponsor address.");


                            } else if (_proposal_status.equals(PAUSED)) {
                                updateProposalStatus(_ipfs_hash, ACTIVE);
                            }

                        } else if (milestoneStatus == MILESTONE_REPORT_NOT_COMPLETED) {
                            int completionPeriod = MilestoneDb.completionPeriod.at(milestonePrefix).getOrDefault(0);
                            int proposalPeriod = ProposalDataDb.proposalPeriod.at(proposal_prefix).getOrDefault(0);
                            boolean extended = MilestoneDb.extensionFlag.at(milestonePrefix).getOrDefault(false);

                            if (getPeriodCount() == (proposalPeriod + completionPeriod)) {
                                if (extended) {
                                    updateProposalStatus(_ipfs_hash, _proposal_details);
                                } else {
                                    milestonePassed += 1;
                                    String proposalPrefix = proposalPrefix(_ipfs_hash);
                                    int project_duration = (int) _proposal_details.get(PROJECT_DURATION);
                                    MilestoneDb.extensionFlag.at(milestonePrefix).set(true);
                                    ProposalDataDb.projectDuration.at(proposalPrefix).set(project_duration + 1);
                                    MilestoneDb.completionPeriod.at(milestonePrefix).set(completionPeriod + 1);
                                }
                            }
                            updateMilestoneDB(milestonePrefix);

                        }

                    } else {
                        MilestoneDb.status.at(milestonePrefix).set(MILESTONE_REPORT_REJECTED);
                        updateMilestoneDB(milestonePrefix);
                    }
                }

            }
            if (milestonePassed > 0) {
                updateProgressReportStatus(_reports, APPROVED);
            } else {
                updateProgressReportStatus(_reports, PROGRESS_REPORT_REJECTED);
                updateProposalStatus(_ipfs_hash, _proposal_details);
            }


            int projectDuration = ProposalDataDb.projectDuration.at(proposal_prefix).getOrDefault(0);
            int proposalPeriod = ProposalDataDb.proposalPeriod.at(proposal_prefix).getOrDefault(0);
            boolean lastProgressReport = proposalPeriod + projectDuration - getPeriodCount() == 0;
            if (milestoneBudget.compareTo(BigInteger.ZERO) > 0) {
                // Request CPS Treasury to add some installments amount to the contributor address
                Address cpsTreasuryScore = getCpsTreasuryScore();
                callScore(cpsTreasuryScore, "sendInstallmentToContributor", _ipfs_hash, milestoneBudget);
                //Request CPS Treasury to add some sponsor reward amount to the sponsor address
                callScore(cpsTreasuryScore, "sendRewardToSponsor", _ipfs_hash, milestonePassed);
                if (lastProgressReport && milestonePassed != milestoneSubmittedSize) {
                    updateProposalStatus(_ipfs_hash, _proposal_details);
                }
            }
        }

    }


    private void updateProposalStatus(String _ipfs_hash, Map<String, Object> _proposal_details) {
        String _proposal_status = (String) _proposal_details.get(STATUS);
        Address _sponsor_address = (Address) _proposal_details.get(SPONSOR_ADDRESS);
        Address _contributor_address = (Address) _proposal_details.get(CONTRIBUTOR_ADDRESS);
        BigInteger _sponsor_deposit_amount = (BigInteger) _proposal_details.get(SPONSOR_DEPOSIT_AMOUNT);
        String flag = (String) _proposal_details.get(TOKEN);

        String proposalPrefix = proposalPrefix(_ipfs_hash);
        if (_proposal_status.equals(ACTIVE)) {
            int project_duration = (int) _proposal_details.get(PROJECT_DURATION);
            projectDuration.at(proposalPrefix).set(project_duration + 1);
            updateProposalStatus(_ipfs_hash, PAUSED);
        } else if (_proposal_status.equals(PAUSED)) {
            updateProposalStatus(_ipfs_hash, DISQUALIFIED);
            callScore(getCpsTreasuryScore(), "disqualifyProject", _ipfs_hash);

            removeContributor(_contributor_address, _ipfs_hash);
            removeSponsor(_sponsor_address, _ipfs_hash);

            sponsorDepositStatus.at(proposalPrefix).set(BOND_CANCELLED);

//          Transferring the sponsor bond deposit to CPF after the project being disqualified
            disqualifyProject(_sponsor_address, _sponsor_deposit_amount, flag);
        }
    }

    /***
     Check if all active and paused proposals submits the progress report
     :return:
     ***/
    private void checkProgressReportSubmission() {
        for (int i = 0; i < activeProposals.size(); i++) {
            String _ipfs_hash = activeProposals.get(i);
            String proposalPrefix = proposalPrefix(_ipfs_hash);

            Map<String, Object> _proposal_details = getProposalDetails(_ipfs_hash);
            String _proposal_status = (String) _proposal_details.get(STATUS);
            Address _sponsor_address = (Address) _proposal_details.get(SPONSOR_ADDRESS);
            Address _contributor_address = (Address) _proposal_details.get(CONTRIBUTOR_ADDRESS);
            String flag = (String) _proposal_details.get(TOKEN);

            if (!ProposalDataDb.submitProgressReport.at(proposalPrefix).getOrDefault(Boolean.FALSE)) {
                if (_proposal_status.equals(ACTIVE)) {
                    updateProposalStatus(_ipfs_hash, PAUSED);
                } else if (_proposal_status.equals(PAUSED)) {
                    updateProposalStatus(_ipfs_hash, DISQUALIFIED);
                    callScore(getCpsTreasuryScore(), "disqualifyProject", _ipfs_hash);


                    removeContributor(_contributor_address, _ipfs_hash);
                    removeSponsor(_sponsor_address, _ipfs_hash);

                    sponsorDepositStatus.at(proposalPrefix).set(BOND_CANCELLED);
                    BigInteger _sponsor_deposit_amount = (BigInteger) _proposal_details.get(SPONSOR_DEPOSIT_AMOUNT);

//              Transferring the sponsor bond deposit to CPF after the project being disqualified
                    disqualifyProject(_sponsor_address, _sponsor_deposit_amount, flag);
                }
            }
        }
    }

    /***
     Update the budget amount and added month time if the budget adjustment application is approved by majority.
     :param _budget_key: report hash of the progress report
     :type _budget_key: str
     :return:
     ***/
    private void updateBudgetAdjustments(String _budget_key) {
        Map<String, Object> _report_result = getProgressReportDetails(_budget_key);
        String _prefix = progressReportPrefix(_budget_key);

        Map<String, Object> _vote_result = getBudgetAdjustmentVoteResult(_budget_key);
        int _approve_voters = (int) _vote_result.get(APPROVE_VOTERS);
        int _total_voters = (int) _vote_result.get(TOTAL_VOTERS);
        BigInteger _approved_votes = (BigInteger) _vote_result.get(APPROVED_VOTES);
        BigInteger _total_votes = (BigInteger) _vote_result.get(TOTAL_VOTES);
        PReps pReps = new PReps();
        double votersRatio = (double) _approve_voters / _total_voters;
        if (_total_voters == 0 || _total_votes.equals(BigInteger.ZERO) || pReps.validPreps.size() < MINIMUM_PREPS) {
            budgetAdjustmentStatus.at(_prefix).set(REJECTED);
        } else if (votersRatio >= MAJORITY && (_approved_votes.doubleValue() / _total_votes.doubleValue()) >= MAJORITY) {
            String _ipfs_hash = (String) _report_result.get(IPFS_HASH);
            String proposal_prefix = proposalPrefix(_ipfs_hash);
            String token_flag = token.at(proposal_prefix).getOrDefault("");

            int _period_count = projectDuration.at(proposal_prefix).getOrDefault(0);
            BigInteger _total_budget = totalBudget.at(proposal_prefix).getOrDefault(BigInteger.ZERO);
            int _additional_duration = (int) _report_result.get(ADDITIONAL_DURATION);
            BigInteger _additional_budget = (BigInteger) _report_result.get(ADDITIONAL_BUDGET);

            projectDuration.at(proposal_prefix).set(_period_count + _additional_duration);
            totalBudget.at(proposal_prefix).set(_total_budget.add(_additional_budget));
            budgetAdjustmentStatus.at(_prefix).set(APPROVED);


//          After the budget adjustment is approved, Request new added fund to CPF
            callScore(getCpfTreasuryScore(), "updateProposalFund", _ipfs_hash, token_flag, _additional_budget, _additional_duration);
        } else {
            budgetAdjustmentStatus.at(_prefix).set(REJECTED);
        }
    }

    private void updateProposalsResult() {
        BigInteger distributionAmount = getRemainingFund().get(bnUSD);
        List<String> proposals = sortPriorityProposals(); // priority proposal mean?
        PReps pReps = new PReps();

        for (String proposal : proposals) {
            Map<String, Object> proposalDetails = getProposalDetails(proposal);
            Map<String, Object> proposalVoteDetails = getVoteResult(proposal);
            Address sponsorAddress = (Address) proposalDetails.get(SPONSOR_ADDRESS);
            Address contributorAddress = (Address) proposalDetails.get(CONTRIBUTOR_ADDRESS);
            BigInteger totalBudget = (BigInteger) proposalDetails.get(TOTAL_BUDGET);
            int projectDuration = (int) proposalDetails.get(MILESTONE_COUNT);
            BigInteger sponsorDepositAmount = (BigInteger) proposalDetails.get(SPONSOR_DEPOSIT_AMOUNT);
            int approvedVoters = (int) proposalVoteDetails.get(APPROVE_VOTERS);
            BigInteger approvedVotes = (BigInteger) proposalVoteDetails.get(APPROVED_VOTES);
            BigInteger totalVotes = (BigInteger) proposalVoteDetails.get(TOTAL_VOTES);
            int totalVoters = (int) proposalVoteDetails.get(TOTAL_VOTERS);
            String flag = (String) proposalDetails.get(TOKEN);
            String updatedStatus;
            String proposalPrefix = proposalPrefix(proposal);

            checkInactivePreps(ProposalDataDb.votersList.at(proposalPrefix));

            double voters_ratio = 0;
            if (totalVoters != 0) {
                voters_ratio = (double) approvedVoters / totalVoters;
            }
            if (totalVoters == 0 || totalVotes.equals(BigInteger.ZERO) || pReps.validPreps.size() < MINIMUM_PREPS) {
                updateProposalStatus(proposal, REJECTED);
                updatedStatus = REJECTED;
            } else if ((voters_ratio) >= MAJORITY &&
                    (approvedVotes.doubleValue() / totalVotes.doubleValue()) >= MAJORITY) {
                if (totalBudget.multiply(BigInteger.valueOf(102)).divide(BigInteger.valueOf(100)). // total budget + 2% of sponsor
                        compareTo(distributionAmount) < 0) {
                    updateProposalStatus(proposal, ACTIVE);
                    updatedStatus = ACTIVE;
                    sponsors.add(sponsorAddress);
                    sponsorProjects.at(sponsorAddress).add(proposal);
                    ProposalDataDb.sponsorDepositStatus.at(proposalPrefix).set(BOND_APPROVED);
                    callScore(getCpfTreasuryScore(), "transferProposalFundToCpsTreasury",
                            proposal, projectDuration, sponsorAddress, contributorAddress, flag, totalBudget);
                    distributionAmount = distributionAmount.subtract(totalBudget);

                } else {
                    updatedStatus = PENDING;
                    ProposalDataDb.totalVoters.at(proposalPrefix).set(0);
                    ProposalDataDb.totalVotes.at(proposalPrefix).set(BigInteger.ZERO);
                    ProposalDataDb.approvedVotes.at(proposalPrefix).set(BigInteger.ZERO);
                    ProposalDataDb.rejectedVotes.at(proposalPrefix).set(BigInteger.ZERO);

                    ArrayDBUtils.clearArrayDb(ProposalDataDb.rejectVoters.at(proposalPrefix));
                    ArrayDBUtils.clearArrayDb(ProposalDataDb.approveVoters.at(proposalPrefix));
                    ArrayDBUtils.clearArrayDb(ProposalDataDb.votersList.at(proposalPrefix));
                    ArrayDBUtils.clearArrayDb(ProposalDataDb.votersReasons.at(proposalPrefix));

                    for (int i = 0; i < pReps.validPreps.size(); i++) {
                        Address prep = pReps.validPreps.get(i);
                        BranchDB<Address, DictDB<String, Integer>> prepVoteChange = ProposalDataDb.votersListIndex.at(proposalPrefix);
                        prepVoteChange.at(prep).set(VOTE, 0);
                        prepVoteChange.at(prep).set(INDEX, 0);
                        prepVoteChange.at(prep).set(CHANGE_VOTE, 0);
                    }

                }
            } else {
                updateProposalStatus(proposal, REJECTED);
                updatedStatus = REJECTED;
            }

            proposalRank.set(proposal, null);
            if (updatedStatus.equals(REJECTED)) {
                removeContributor(contributorAddress, proposal);
                sponsorDepositStatus.at(proposalPrefix).set(BOND_RETURNED);
                BigInteger halfSubmissionFee = BigInteger.valueOf(APPLICATION_FEE / 2).multiply(EXA);
                Context.transfer(contributorAddress, halfSubmissionFee);
                proposalFees.set(proposalFees.get().subtract(halfSubmissionFee));

                BigInteger sponsorBondAmount = sponsorBondReturn.at(sponsorAddress.toString()).getOrDefault(flag, BigInteger.ZERO);
                sponsorBondReturn.at(sponsorAddress.toString()).set(flag, sponsorBondAmount.add(sponsorDepositAmount));
                SponsorBondReturned(sponsorAddress,
                        sponsorDepositAmount + " returned to sponsor address.");
            }
            if (proposals.size() > 0) {
                checkInactivePreps(priorityVotedPreps);
            }
        }
    }

    private void updateProposalStatus(String proposalHash, String propStatus) {
        Status status = new Status();
        String proposalPrefix = proposalPrefix(proposalHash);
        String currentStatus = ProposalDataDb.status.at(proposalPrefix).get();
        ProposalDataDb.timestamp.at(proposalPrefix).set(BigInteger.valueOf(Context.getBlockTimestamp()));
        ProposalDataDb.status.at(proposalPrefix).set(propStatus);

        ArrayDB<String> proposalStatus = status.proposalStatus.get(currentStatus);
        ArrayDBUtils.removeArrayItem(proposalStatus, proposalHash);
        status.proposalStatus.get(propStatus).add(proposalHash);
    }

    private void updateProgressReportStatus(String progressHash, String progressStatus) {
        String progressPrefix = progressReportPrefix(progressHash);
        String currentStatus = ProgressReportDataDb.status.at(progressPrefix).get();
        ProgressReportDataDb.timestamp.at(progressPrefix).set(BigInteger.valueOf(Context.getBlockTimestamp()));
        ProgressReportDataDb.status.at(progressPrefix).set(progressStatus);

        Status status = new Status();
        ArrayDB<String> progressReportStatus = status.progressReportStatus.get(currentStatus);
        ArrayDBUtils.removeArrayItem(progressReportStatus, progressHash);
        status.progressReportStatus.get(progressStatus).add(progressHash);
    }


    private void checkInactivePreps(ArrayDB<Address> prepList) {
        PReps pReps = new PReps();
        for (int i = 0; i < pReps.validPreps.size(); i++) {
            Address prep = pReps.validPreps.get(i);
            if (!containsInArrayDb(prep, prepList) && !containsInArrayDb(prep, pReps.inactivePreps)) {
                pReps.inactivePreps.add(prep);
            }
        }
    }

    private Map<String, Object> getProposalDetails(String proposal) {
        if (proposalKeyExists(proposal)) {
            return getDataFromProposalDB(proposalPrefix(proposal));
        }
        return Map.of();

    }

    private void addNewProgressReportKey(String ipfsHash, String reportHash) {
        String prefix = proposalPrefix(ipfsHash);
        if (!containsInArrayDb(reportHash, progressReports.at(prefix))) {
            progressReports.at(prefix).add(reportHash);
        }
    }

    @Override
    @External(readonly = true)
    public Map<String, ?> getProposalDetails(String status, @Optional Address walletAddress, @Optional int startIndex) {
        if (!STATUS_TYPE.contains(status)) {
            return Map.of(MESSAGE, "Not a valid _status.");
        }
        List<Object> proposalsList = new ArrayList<>();
        List<String> proposalKeys = getProposalsKeysByStatus(status);

        if (startIndex < 0) {
            startIndex = 0;
        }
        int endIndex = startIndex + 10;

        int count = proposalKeys.size();
        if (endIndex > count) {
            endIndex = count;
        }

        for (int i = startIndex; i < endIndex; i++) {
            String proposalKey = proposalKeys.get(i);
            Map<String, Object> proposalDetails = new HashMap<>();
            if (proposalKeyExists(proposalKey)) {
                String proposalPrefix = proposalPrefix(proposalKey);
                proposalDetails.put(IPFS_HASH, ProposalDataDb.ipfsHash.at(proposalPrefix).getOrDefault(""));
                proposalDetails.put(PROJECT_TITLE, ProposalDataDb.projectTitle.at(proposalPrefix).getOrDefault(""));
                proposalDetails.put(TOTAL_BUDGET, totalBudget.at(proposalPrefix).getOrDefault(BigInteger.ZERO));
                proposalDetails.put(TOKEN, token.at(proposalPrefix).getOrDefault(""));
                proposalDetails.put(PERCENTAGE_COMPLETED, percentageCompleted.at(proposalPrefix).getOrDefault(0));

                proposalDetails.put(TOTAL_VOTES, ProposalDataDb.totalVotes.at(proposalPrefix).getOrDefault(BigInteger.ZERO));
                proposalDetails.put(APPROVED_VOTES, ProposalDataDb.approvedVotes.at(proposalPrefix).getOrDefault(BigInteger.ZERO));
                proposalDetails.put(REJECTED_VOTES, ProposalDataDb.rejectedVotes.at(proposalPrefix).getOrDefault(BigInteger.ZERO));
                proposalDetails.put(ABSTAINED_VOTES, ProposalDataDb.abstainedVotes.at(proposalPrefix).getOrDefault(BigInteger.ZERO));

                proposalDetails.put(APPROVE_VOTERS, ProposalDataDb.approveVoters.at(proposalPrefix).size());
                proposalDetails.put(REJECT_VOTERS, ProposalDataDb.rejectVoters.at(proposalPrefix).size());
                proposalDetails.put(ABSTAIN_VOTERS, abstainVoters.at(proposalPrefix).size());
                proposalDetails.put(TOTAL_VOTERS, ProposalDataDb.totalVoters.at(proposalPrefix).getOrDefault(0));

                String propStatus = ProposalDataDb.status.at(proposalPrefix).getOrDefault("");
                proposalDetails.put(STATUS, propStatus);

                if (status.equals(SPONSOR_PENDING)) {
                    Address wallet = contributorAddress.at(proposalPrefix).get();
                    if (wallet.equals(walletAddress)) {
                        proposalsList.add(proposalDetails);
                    }
                } else if (propStatus.equals(status)) {
                    proposalsList.add(proposalDetails);
                }

            }
        }
        return Map.of(DATA, proposalsList, COUNT, count);

    }


    @Override
    @External(readonly = true)
    public Map<String, Object> getProposalDetailsByHash(String ipfsKey) {
        Map<String, Object> proposalDetails = new HashMap<>();
        String prefix = proposalPrefix(ipfsKey);
        proposalDetails.putAll(getProposalDetails(ipfsKey));
        proposalDetails.put(SPONSOR_VOTE_REASON, ProposalDataDb.sponsorVoteReason.at(prefix).getOrDefault(""));
        return proposalDetails;
    }


    private Map<String, Object> getProgressReportDetails(String progressKey) {
        return getDataFromProgressReportDB(progressReportPrefix(progressKey));
    }

    @External(readonly = true)
    public List<Integer> getMilestoneCountOfProgressReport(String progressKey) {
        return getMilestoneSubmittedFromProgressReportDB(progressReportPrefix(progressKey));
    }

    @External(readonly = true)
    public Map<String, Object> getProgressReportVoteDetails(String progressKey) {
        return getVoteResultsFromProgressReportDB(progressReportPrefix(progressKey));
    }

    @External(readonly = true)
    public Map<String, Object> getBudgetAdjustmentDetails(String progressKey) {
        return getBudgetAdjustmentDetailsFromDB(progressReportPrefix(progressKey));
    }

    @Override
    @External(readonly = true)
    public Map<String, ?> getProgressReports(String status, @Optional int startIndex) {
        if (!PROGRESS_REPORT_STATUS_TYPE.contains(status)) {
            return Map.of(MESSAGE, "Not a valid _status.");
        }

        List<Object> progressReportList = new ArrayList<>();
        Status _status = new Status();
        ArrayDB<String> progressReportKeys = _status.progressReportStatus.get(status);

        if (startIndex < 0) {
            startIndex = 0;
        }
        int endIndex = startIndex + 10;

        int count = progressReportKeys.size();
        if (endIndex > count) {
            endIndex = count;
        }

        for (int i = startIndex; i < endIndex; i++) {
            String progressReportKey = progressReportKeys.get(i);
            Map<String, Object> progressReportDetails = this.getProgressReportDetails(progressReportKey);
            String progressStatus = (String) progressReportDetails.get(STATUS);

            if (progressStatus.equals(status)) {
                progressReportList.add(progressReportDetails);
            }
        }
        return Map.of(DATA, progressReportList, COUNT, progressReportList.size());
    }

    /***
     Returns all the progress reports for a specific project
     :param _report_key : project key i.e. progress report ipfs hash
     :type _report_key : str
     :return : List of all progress report with status
     :rtype : dict
     ***/
    @Override
    @External(readonly = true)
    public Map<String, Object> getProgressReportsByHash(String reportKey) {
        if (progressKeyExists(reportKey)) {
            Map<String, Object> finalDetail = new HashMap<>();
            Map<String, Object> progressReportDetails = getProgressReportDetails(reportKey);
            finalDetail.putAll(progressReportDetails);
            boolean hasMilestone = false;
            int milestoneSubmittedSize = (int) progressReportDetails.get(MILESTONE_SUBMITTED_COUNT);
            if (milestoneSubmittedSize > 0) {
                hasMilestone = true;
            }
            finalDetail.put("isMilestone", hasMilestone);
            if (hasMilestone) {
                List<Integer> milestoneSubmittedList = getMilestoneCountOfProgressReport(reportKey);
                finalDetail.put("milestoneId", milestoneSubmittedList);
            } else {
                finalDetail.putAll(getProgressReportVoteDetails(reportKey));
            }

            return finalDetail;
        }
        return Map.of();
    }

    private Map<String, Object> getMilestoneReport(String reportKey, String ipfsHash) {
        Map<String, Object> milestoneReport = new HashMap<>();
        ArrayDB<Integer> mileSubmitted = milestoneSubmitted.at(progressReportPrefix(reportKey));
        for (int i = 0; i < mileSubmitted.size(); i++) {
            int count = mileSubmitted.get(i);
            milestoneReport.put("milestone_" + count, getMilestonesReport(ipfsHash, count));
        }
        return milestoneReport;
    }


    @Override
    @External(readonly = true)
    public Map<String, Object> getProgressReportsByProposal(String ipfsKey) {
        String proposalPrefix = proposalPrefix(ipfsKey);
        ArrayDB<String> reportKeys = ProposalDataDb.progressReports.at(proposalPrefix);

        List<Map<String, Object>> progressReportList = new ArrayList<>();

        int reportCount = reportKeys.size();
        for (int i = 0; i < reportCount; i++) {
            Map<String, Object> progressReportDetails = this.getProgressReportsByHash(reportKeys.get(i));
            progressReportList.add(progressReportDetails);
        }
        return Map.of(DATA, progressReportList, COUNT, reportCount);
    }


    @Override
    @External(readonly = true)
    public Map<String, Object> getSponsorsRequests(String status, Address sponsorAddress, @Optional int startIndex) {
        if (startIndex < 0) {
            startIndex = 0;
        }
        int endIndex = startIndex + 10;
        if (!ArrayDBUtils.containsInList(status, List.of(APPROVED, SPONSOR_PENDING, REJECTED, DISQUALIFIED))) {
            return Map.of(MESSAGE, "Not a valid _status.");
        }
        List<String> proposalKeys = new ArrayList<>();
        List<Object> sponsorRequests = new ArrayList<>();
        String prefix;

        if (status.equals(APPROVED)) {
            proposalKeys = arrayDBtoList(this.sponsorProjects.at(sponsorAddress));
            List<String> pendingProposals = getProposalsKeysByStatus(PENDING);
            for (String ipfsKey : pendingProposals) {
                prefix = proposalPrefix(ipfsKey);
                if (ProposalDataDb.sponsorAddress.at(prefix).get().equals(sponsorAddress)) {
                    proposalKeys.add(ipfsKey);
                }
            }
        } else {
            List<String> proposalKeysList = getProposalsKeysByStatus(status);
            for (String ipfsKey : proposalKeysList) {
                prefix = proposalPrefix(ipfsKey);
                if (ProposalDataDb.sponsorAddress.at(prefix).get().equals(sponsorAddress)) {
                    proposalKeys.add(ipfsKey);
                }
            }
        }
        int count = proposalKeys.size();
        if (endIndex > count) {
            endIndex = count;
        }

        BigInteger sponsorAmountICX = BigInteger.ZERO;
        BigInteger sponsorAmountBnusd = BigInteger.ZERO;

        for (int i = startIndex; i < endIndex; i++) {
            String proposalKey = proposalKeys.get(i);
            String proposalPrefix = proposalPrefix(proposalKey);
            String sponsorDepositStatus = ProposalDataDb.sponsorDepositStatus.at(proposalPrefix).getOrDefault("");
            Map<String, Object> proposalDetails = getProposalDetails(proposalKey);
            sponsorRequests.add(proposalDetails);
            if (sponsorDepositStatus.equals(BOND_APPROVED)) {
                String token = (String) proposalDetails.get(TOKEN);
                BigInteger sponsorDepositAmount = (BigInteger) proposalDetails.get(SPONSOR_DEPOSIT_AMOUNT);
                if (token.equals(ICX)) {
                    sponsorAmountICX = sponsorAmountICX.add(sponsorDepositAmount);
                } else if (token.equals(bnUSD)) {
                    sponsorAmountBnusd = sponsorAmountBnusd.add(sponsorDepositAmount);
                }
            }
        }

        return Map.of(DATA, sponsorRequests, COUNT, proposalKeys.size(),
                SPONSOR_DEPOSIT_AMOUNT, Map.of(ICX, sponsorAmountICX, bnUSD, sponsorAmountBnusd));
    }


    /***
     Returns remaining projects and progress reports to vote on the current voting period
     :param projectType: "proposal" or "progress_report" which type, remaining votes need to be checked
     :type projectType: str
     :param walletAddress: Wallet Address of the P-Rep
     :type walletAddress: str
     :return: list of details of proposal or progress report what they need to vote on the same voting period
     ***/
    @External(readonly = true)
    public List<Map<String, Object>> getRemainingProject(String projectType, Address walletAddress) {
        List<Map<String, Object>> _remaining_proposals = new ArrayList<>();
        List<Map<String, Object>> _remaining_progress_report = new ArrayList<>();
        if (projectType.equals(PROPOSAL)) {
            List<String> _proposal_keys = getProposalsKeysByStatus(PENDING);
            for (String _ipfs_key : _proposal_keys) {
                String prefix = proposalPrefix(_ipfs_key);

                if (!containsInArrayDb(walletAddress, ProposalDataDb.votersList.at(prefix))) {
                    Map<String, Object> _proposal_details = getProposalDetails(_ipfs_key);
                    _remaining_proposals.add(_proposal_details);
                }
            }
            return _remaining_proposals;
        }

        Status status = new Status();
        if (projectType.equals(PROGRESS_REPORTS)) {
            for (int i = 0; i < status.waitingProgressReports.size(); i++) {
                String reportHash = status.waitingProgressReports.get(i);
                String prefix = progressReportPrefix(reportHash);

                String ipfsHash = ProgressReportDataDb.ipfsHash.at(prefix).get();
                int milestoneCount = getMilestoneCount(ipfsHash);

                if (milestoneCount > 0) {

                    ArrayDB<Integer> milestoneSubmittedOf = milestoneSubmitted.at(prefix);

                    for (int j = 0; j < milestoneSubmittedOf.size(); j++) {
                        int milestoneID = milestoneSubmittedOf.get(j);
                        String milestonePrefix = mileStonePrefix(ipfsHash, milestoneID);

                        ArrayDB<Address> voterList = MilestoneDb.votersList.at(milestonePrefix);
                        if (!containsInArrayDb(walletAddress, voterList)) {
                            Map<String, Object> progressReportDetails = new HashMap<>();
                            progressReportDetails.putAll(getProgressReportDetails(reportHash));
                            progressReportDetails.putAll(getMilestoneReport(reportHash, ipfsHash));
                            _remaining_progress_report.add(progressReportDetails);
                        }
                    }

                } else {
                    if (!containsInArrayDb(walletAddress, ProgressReportDataDb.votersList.at(prefix))) {
                        Map<String, Object> progressReportDetails = new HashMap<>();
                        progressReportDetails.putAll(getProgressReportDetails(reportHash));
                        progressReportDetails.putAll(getVoteResultsFromProgressReportDB(prefix));
                        _remaining_progress_report.add(progressReportDetails);
                    }
                }
            }
            return _remaining_progress_report;
        }
        return List.of(Map.of("", ""));
    }


    /***
     Get vote results by proposal
     :param _ipfs_key : proposal ipfs key
     :type _ipfs_key : str
     :return: Vote status of given _ipfs_key
     :rtype : dict
     ***/
    @Override
    @External(readonly = true)
    public Map<String, Object> getVoteResult(String ipfsKey) {
        String prefix = proposalPrefix(ipfsKey);

        ArrayDB<Address> _voters_list = ProposalDataDb.votersList.at(prefix);
        ArrayDB<Address> approve_voters = ProposalDataDb.approveVoters.at(prefix);
        ArrayDB<Address> reject_voters = ProposalDataDb.rejectVoters.at(prefix);
        ArrayDB<Address> abstain_voters = ProposalDataDb.abstainVoters.at(prefix);
        List<Map<String, Object>> _vote_status = new ArrayList<>();
        String vote;
        for (int i = 0; i < _voters_list.size(); i++) {
            Address voter = _voters_list.get(i);
            if (containsInArrayDb(voter, approve_voters)) {
                vote = APPROVE;
            } else if (containsInArrayDb(voter, reject_voters)) {
                vote = REJECT;
            } else {
                vote = ABSTAIN;
            }

            String reason = ProposalDataDb.votersReasons.at(prefix).get(i);
            if (reason == null) {
                reason = "";
            }


            Map<String, Object> _voters = Map.of(ADDRESS, voter,
                    PREP_NAME, getPrepName(voter),
                    VOTE_REASON, reason,
                    VOTE, vote);
            _vote_status.add(_voters);
        }

        return Map.of(DATA, _vote_status, APPROVE_VOTERS, approve_voters.size(),
                REJECT_VOTERS, reject_voters.size(),
                ABSTAIN_VOTERS, abstain_voters.size(),
                TOTAL_VOTERS, ProposalDataDb.totalVoters.at(prefix).getOrDefault(0),
                APPROVED_VOTES, ProposalDataDb.approvedVotes.at(prefix).getOrDefault(BigInteger.ZERO),
                REJECTED_VOTES, ProposalDataDb.rejectedVotes.at(prefix).getOrDefault(BigInteger.ZERO),
                ABSTAINED_VOTES, ProposalDataDb.abstainedVotes.at(prefix).getOrDefault(BigInteger.ZERO),
                TOTAL_VOTES, ProposalDataDb.totalVotes.at(prefix).getOrDefault(BigInteger.ZERO));
    }

    // a different method for get milestone report
    @External(readonly = true)
    public Map<String, Object> getMilestoneVoteResult(String reportKey, int milestoneId) {
        String prefix = progressReportPrefix(reportKey);
        String ipfshHash = ProgressReportDataDb.ipfsHash.at(prefix).get();
        List<Integer> milestoneSubmitted = getMilestoneCountOfProgressReport(reportKey);
        if (milestoneSubmitted.contains(milestoneId)) {

            String milestonePrefix = mileStonePrefix(ipfshHash, milestoneId);
            ArrayDB<Address> _voters_list = MilestoneDb.votersList.at(milestonePrefix);
            ArrayDB<Address> _approved_voters_list = MilestoneDb.approveVoters.at(milestonePrefix);
            ArrayDB<Address> _rejected_voters_list = MilestoneDb.rejectVoters.at(milestonePrefix);

            List<Map<String, Object>> _vote_status = new ArrayList<>();
            String vote;
//      Differentiating the P-Rep(s) votes according to their votes
            for (int i = 0; i < _voters_list.size(); i++) {
                Address voter = _voters_list.get(i);
                if (containsInArrayDb(voter, _approved_voters_list)) {
                    vote = APPROVE;
                } else if (containsInArrayDb(voter, _rejected_voters_list)) {
                    vote = REJECT;
                } else {
                    vote = "not voted";
                }
                String reason = ProgressReportDataDb.votersReasons.at(prefix).get(i);
                if (reason == null) {
                    reason = "";
                }
                Map<String, Object> _voters = Map.of(ADDRESS, voter,
                        PREP_NAME, getPrepName(voter),
                        VOTE_REASON, reason,
                        VOTE, vote);
                _vote_status.add(_voters);
            }

            return Map.of(DATA, _vote_status, APPROVE_VOTERS, _approved_voters_list.size(),
                    REJECT_VOTERS, _rejected_voters_list.size(),
                    TOTAL_VOTERS, ProgressReportDataDb.totalVoters.at(prefix).getOrDefault(0),
                    APPROVED_VOTES, MilestoneDb.approvedVotes.at(milestonePrefix).getOrDefault(BigInteger.ZERO),
                    REJECTED_VOTES, MilestoneDb.rejectedVotes.at(milestonePrefix).getOrDefault(BigInteger.ZERO),
                    TOTAL_VOTES, ProgressReportDataDb.totalVotes.at(prefix).getOrDefault(BigInteger.ZERO));
        }
        return Map.of();
    }


    @External(readonly = true)
    public Map<String, Object> getProgressReportVoters(String reportKey) {
        String prefix = progressReportPrefix(reportKey);
        String ipfsHash = ProgressReportDataDb.ipfsHash.at(prefix).get();
        int milestoneCount = getMilestoneCount(ipfsHash);
        List<Map<String, Object>> voteStatus = new ArrayList<>();
        String vote;
        if (milestoneCount > 0) {

            ArrayDB<Integer> milestoneSubmittedOf = milestoneSubmitted.at(prefix);

            for (int i = 0; i < milestoneSubmittedOf.size(); i++) {
                int milestoneID = milestoneSubmittedOf.get(i);
                String milestonePrefix = mileStonePrefix(ipfsHash, milestoneID);

                ArrayDB<Address> voterList = MilestoneDb.votersList.at(milestonePrefix);
                ArrayDB<Address> approvedVoterList = MilestoneDb.approveVoters.at(milestonePrefix);
                ArrayDB<Address> rejectedVoterList = MilestoneDb.rejectVoters.at(milestonePrefix);

                for (int j = 0; j < voterList.size(); j++) {
                    Address voter = voterList.get(j);

                    if (containsInArrayDb(voter, approvedVoterList)) {
                        vote = APPROVE;
                    } else if (containsInArrayDb(voter, rejectedVoterList)) {
                        vote = REJECT;
                    } else {
                        vote = "not voted";
                    }
                    String reason = ProgressReportDataDb.votersReasons.at(prefix).get(j);
                    if (reason == null) {
                        reason = "";
                    }
                    Map<String, Object> voters = Map.of(ADDRESS, voter,
                            PREP_NAME, getPrepName(voter),
                            VOTE_REASON, reason,
                            VOTE, vote,
                            MILESTONE_ID, milestoneID);
                    voteStatus.add(voters);
                }
            }
        } else {
            ArrayDB<Address> voterList = ProgressReportDataDb.votersList.at(prefix);
            ArrayDB<Address> approvedVoterList = ProgressReportDataDb.approveVoters.at(prefix);
            ArrayDB<Address> rejectedVoterList = ProgressReportDataDb.rejectVoters.at(prefix);
            for (int i = 0; i < voterList.size(); i++) {
                Address voter = voterList.get(i);
                if (containsInArrayDb(voter, approvedVoterList)) {
                    vote = APPROVE;
                } else if (containsInArrayDb(voter, rejectedVoterList)) {
                    vote = REJECT;
                } else {
                    vote = "not voted";
                }
                String reason = ProgressReportDataDb.votersReasons.at(prefix).get(i);
                if (reason == null) {
                    reason = "";
                }
                Map<String, Object> voters = Map.of(ADDRESS, voter,
                        PREP_NAME, getPrepName(voter),
                        VOTE_REASON, reason,
                        VOTE, vote);
                voteStatus.add(voters);
            }
        }
        return Map.of(DATA, voteStatus);
    }


    /***
     Get vote results by progress report
     :param reportKey : progress report ipfs key
     :type reportKey : str
     :return: Vote status of given reportKey
     :rtype : dict
     ***/
    @Override
    @External(readonly = true)
    public Map<String, Object> getProgressReportResult(String reportKey) {

        String prefix = progressReportPrefix(reportKey);
        String ipfsHash = ProgressReportDataDb.ipfsHash.at(prefix).get();
        boolean hasMilestone = isMilestone.at(proposalPrefix(ipfsHash)).getOrDefault(false);
        if (hasMilestone) {

            List<Integer> milestoneSubmittedList = getMilestoneCountOfProgressReport(reportKey);

            return Map.of(
                    TOTAL_VOTERS, ProgressReportDataDb.totalVoters.at(prefix).getOrDefault(0),
                    TOTAL_VOTES, ProgressReportDataDb.totalVotes.at(prefix).getOrDefault(BigInteger.ZERO),
                    MILESTONE_ID, milestoneSubmittedList,
                    IS_MILESTONE, hasMilestone);
        }
        return Map.of(APPROVE_VOTERS, ProgressReportDataDb.approveVoters.at(prefix).size(),
                REJECT_VOTERS, ProgressReportDataDb.rejectVoters.at(prefix).size(),
                TOTAL_VOTERS, ProgressReportDataDb.totalVoters.at(prefix).getOrDefault(0),
                APPROVED_VOTES, ProgressReportDataDb.approvedVotes.at(prefix).getOrDefault(BigInteger.ZERO),
                REJECTED_VOTES, ProgressReportDataDb.rejectedVotes.at(prefix).getOrDefault(BigInteger.ZERO),
                TOTAL_VOTES, ProgressReportDataDb.totalVotes.at(prefix).getOrDefault(BigInteger.ZERO),
                IS_MILESTONE, hasMilestone);
    }

    @External(readonly = true)
    public Map<String, Object> getMilestonesReport(String ipfsKey, int milestoneId) {
        String milestonePrefix = mileStonePrefix(ipfsKey, milestoneId);
        return getDataFromMilestoneDB(milestonePrefix);
    }

    @External(readonly = true)
    public int getMilestoneCount(String ipfsHash) {
        if (proposalKeyExists(ipfsHash)) {
            return ProposalDataDb.getMilestoneCount(proposalPrefix(ipfsHash));
        }
        return 0;
    }

    @External(readonly = true)
    public int getMileststoneStatusOf(String proposalKey, int milestoneId) {
        String milestonePrefix = mileStonePrefix(proposalKey, milestoneId);
        return MilestoneDb.status.at(milestonePrefix).getOrDefault(0);
    }


    /***
     Get budget adjustment vote results
     :param _report_key : progress report ipfs key
     :type _report_key : str
     :return: Vote status of given _report_key
     :rtype : dict
     ***/

    @Override
    @External(readonly = true)
    public Map<String, Object> getBudgetAdjustmentVoteResult(String reportKey) {
        String prefix = progressReportPrefix(reportKey);

        ArrayDB<Address> _voters_list = ProgressReportDataDb.votersList.at(prefix);
        ArrayDB<Address> _approved_voters_list = budgetApproveVoters.at(prefix);
        ArrayDB<Address> _rejected_voters_list = budgetRejectVoters.at(prefix);
        List<Map<String, Object>> _vote_status = new ArrayList<>();
        String vote;
        for (int i = 0; i < _voters_list.size(); i++) {
            Address voter = _voters_list.get(i);
            if (containsInArrayDb(voter, _approved_voters_list)) {
                vote = APPROVE;
            } else if (containsInArrayDb(voter, _rejected_voters_list)) {
                vote = REJECT;
            } else {
                vote = "not voted";
            }
            Map<String, Object> _voters = Map.of(
                    ADDRESS, voter,
                    PREP_NAME, getPrepName(voter),
                    VOTE, vote);
            _vote_status.add(_voters);
        }

        return Map.of(DATA, _vote_status, APPROVE_VOTERS, _approved_voters_list.size(),
                REJECT_VOTERS, _rejected_voters_list.size(),
                TOTAL_VOTERS, ProgressReportDataDb.totalVoters.at(prefix).getOrDefault(0),
                APPROVED_VOTES, budgetApprovedVotes.at(prefix).getOrDefault(BigInteger.ZERO),
                REJECTED_VOTES, budgetRejectedVotes.at(prefix).getOrDefault(BigInteger.ZERO),
                TOTAL_VOTES, ProgressReportDataDb.totalVotes.at(prefix).getOrDefault(BigInteger.ZERO));
    }


    private void snapshotDelegation() {
        BigInteger maxDelegation = BigInteger.ZERO;
        PReps pReps = new PReps();
        BigInteger totalStake = BigInteger.ZERO;
        for (int i = 0; i < pReps.validPreps.size(); i++) {
            Address prep = pReps.validPreps.get(i);
            BigInteger stake = getStake(prep);
            delegationSnapshot.set(prep, stake);
            totalStake = totalStake.add(stake);
            if (stake.compareTo(maxDelegation) > 0) {
                maxDelegation = stake;
            }
        }
        this.maxDelegation.set(maxDelegation);
        this.totalDelegationSnapshot.set(totalStake);
    }

    private void updateApplicationResult() {
        PReps pReps = new PReps();
        Status status = new Status();
        PeriodController period = new PeriodController();
        if (pReps.validPreps.size() < MINIMUM_PREPS) {
            period.periodName.set(APPLICATION_PERIOD);
            PeriodUpdate("Period Updated back to Application Period due to less Registered P-Reps Count");

        } else if (getProposalsKeysByStatus(PENDING).size() == 0 &&
                status.progressReportStatus.get(WAITING).size() == 0 &&
                activeProposals.size() + status.paused.size() >= 0) {
            createActiveProposalDb();
            checkProgressReportSubmission();
            period.periodName.set(APPLICATION_PERIOD);
            PeriodUpdate("Period Updated back to Application Period due not enough " +
                    "Voting Proposals or Progress Reports.");
        } else {
            createActiveProposalDb();
            PeriodUpdate("Period Updated to Voting Period");
        }
    }

    private void createActiveProposalDb() {
        clearArrayDb(activeProposals);
        Status status = new Status();
        for (int i = 0; i < status.active.size(); i++) {
            String proposal = status.active.get(i);
            if (!ArrayDBUtils.containsInArrayDb(proposal, activeProposals)) {
                activeProposals.add(proposal);
            }
        }
        for (int i = 0; i < status.paused.size(); i++) {
            String proposal = status.paused.get(i);
            if (!ArrayDBUtils.containsInArrayDb(proposal, activeProposals)) {
                activeProposals.add(proposal);
            }
        }
    }

    private void payPrepPenalty(Address from, BigInteger _value) {
        checkMaintenance();
        updatePeriod();
        PReps pReps = new PReps();
        PeriodController period = new PeriodController();
        Context.require(period.periodName.get().equals(APPLICATION_PERIOD),
                TAG + " Penalty can only be paid on Application Period");
        Context.require(ArrayDBUtils.containsInArrayDb(from, pReps.denylist),
                TAG + " " + from + " not in denylist.");

        BigInteger penaltyAmount = getPenaltyAmount(from);
        Context.require(penaltyAmount.equals(_value),
                "Please pay Penalty amount of" + penaltyAmount + " to register as a P-Rep.");
        SetterGetter setterGetter = new SetterGetter();
        ArrayDBUtils.removeArrayItem(pReps.denylist, from);
        pReps.registeredPreps.add(from);
        pReps.validPreps.add(from);
        burn(_value, setterGetter.balancedDollar.get());
        PRepPenalty(from, _value + " bnUSD Penalty Received. P-Rep removed from Denylist.");

    }

    @Override
    @External
    public void tokenFallback(Address from, BigInteger value, byte[] data) {
        Context.require(Context.getCaller().equals(getBnusdScore()), TAG + " Only bnUSD token accepted.");

        String unpacked_data = new String(data);
        JsonObject transferData = Json.parse(unpacked_data).asObject();
        String methodName = transferData.get(METHOD).asString();
        JsonObject paramsName = transferData.get("params").asObject();

        if (methodName.equals("sponsorVote")) {
            String ipfsKey = paramsName.get(IPFS_HASH).asString();
            String vote = paramsName.get(VOTE).asString();
            String voteReason = paramsName.get(VOTE_REASON).asString();
            sponsorVote(ipfsKey, vote, voteReason, from, value);
        } else if (methodName.equals("payPrepPenalty")) {
            payPrepPenalty(from, value);

        } else {
            Context.revert(TAG + " Not supported method. Token transfer fails.");
        }


    }

    private void sponsorVote(String ipfsKey, String vote, String voteReason, Address from, BigInteger value) {
        checkMaintenance();
        updatePeriod();
        PeriodController period = new PeriodController();
        Context.require(period.periodName.get().equals(APPLICATION_PERIOD),
                TAG + " Sponsor Vote can only be done on Application Period");
        PReps pReps = new PReps();
        Context.require(ArrayDBUtils.containsInArrayDb(from, pReps.validPreps), TAG + ": Not a P-Rep");

        swapBNUsdToken();
        Map<String, Object> proposalDetails = getProposalDetails(ipfsKey);
        String status = (String) proposalDetails.get(STATUS);
        Address sponsorAddress = (Address) proposalDetails.get(SPONSOR_ADDRESS);
        Address contributorAddress = (Address) proposalDetails.get(CONTRIBUTOR_ADDRESS);
        String token = (String) proposalDetails.get(TOKEN);

        Context.require(from.equals(sponsorAddress), TAG + ": Not a valid sponsor");
        Context.require(List.of(ACCEPT, REJECT).contains(vote), TAG + ": Not valid vote");

        if (vote.equals(ACCEPT)) {
            Context.require(status.equals(SPONSOR_PENDING),
                    TAG + ": Sponsor can be only approve sponsorship for Pending proposals.");
            Context.require(token.equals(bnUSD), TAG + ": " + token + " Not a supported token.");

            BigInteger projectBudget = (BigInteger) proposalDetails.get(TOTAL_BUDGET);

            Context.require(value.equals(projectBudget.multiply(getSponsorBondPercentage()).divide(HUNDRED)),
                    TAG + ": Deposit " + getSponsorBondPercentage() + "% of the total budget of the project.");

            updateProposalStatus(ipfsKey, PENDING);
            String proposalPrefix = proposalPrefix(ipfsKey);
            sponsorDepositAmount.at(proposalPrefix).set(value);
            sponsoredTimestamp.at(proposalPrefix).set(BigInteger.valueOf(Context.getBlockTimestamp()));
            sponsorDepositStatus.at(proposalPrefix).set(BOND_RECEIVED);
            sponsorVoteReason.at(proposalPrefix).set(voteReason);
            proposalPeriod.at(proposalPrefix).set(getPeriodCount());

            SponsorBondReceived(from, "Sponsor Bond " + value + " " + token + " Received.");
        } else {
            removeContributor(contributorAddress, ipfsKey);
            updateProposalStatus(ipfsKey, REJECTED);
            BigInteger halfSubmissionFee = BigInteger.valueOf(APPLICATION_FEE / 2).multiply(EXA);
            Context.transfer(contributorAddress, halfSubmissionFee);
            proposalFees.set(proposalFees.get().subtract(halfSubmissionFee));
            SponsorBondRejected(from,
                    "Sponsor Bond Rejected for project " + proposalDetails.get(PROJECT_TITLE));

        }
    }

    @Override
    @External
    public void removeDenylistPreps() {
        validateAdmins();
        PReps pReps = new PReps();
        int size = pReps.denylist.size();
        for (int i = 0; i < size; i++) {
            Address prep = pReps.denylist.pop();
            pReps.prepsDenylistStatus.set(prep.toString(), 0);
        }
    }


    @Override
    @External
    public void claimSponsorBond() {
        Address caller = Context.getCaller();
        DictDB<String, BigInteger> userAmounts = sponsorBondReturn.at(caller.toString());
        BigInteger amountIcx = userAmounts.getOrDefault(ICX, BigInteger.ZERO);
        BigInteger amountBNUsd = userAmounts.getOrDefault(bnUSD, BigInteger.ZERO);

        if (amountIcx.compareTo(BigInteger.ZERO) > 0) {
            userAmounts.set(ICX, BigInteger.ZERO);
            Context.transfer(caller, amountIcx);
            SponsorBondClaimed(caller, amountIcx, amountIcx + " " + ICX + " withdrawn to " + caller);

        } else if (amountBNUsd.compareTo(BigInteger.ZERO) > 0) {
            userAmounts.set(bnUSD, BigInteger.ZERO);
            callScore(getBnusdScore(), TRANSFER, caller, amountBNUsd);
            SponsorBondClaimed(caller, amountIcx, amountBNUsd + " " + bnUSD + " withdrawn to " + caller);
        } else {
            Context.revert(TAG + " Claim Reward Fails. Available Amounts are " + amountIcx + " " + ICX + " and" + amountBNUsd + " " + bnUSD);
        }
    }


    private void swapBNUsdToken() {
        BigInteger sbh = swapBlockHeight.getOrDefault(BigInteger.ZERO);
        BigInteger currentBlock = BigInteger.valueOf(Context.getBlockHeight());
        if (sbh.compareTo(currentBlock) < 0) {
            swapBlockHeight.set(currentBlock.add(SWAP_BLOCK_DIFF));
            callScore(getCpfTreasuryScore(), "swapTokens", swapCount.getOrDefault(0));
        }
    }

    @Override
    @External
    public void setSwapCount(int value) {
        validateAdmins();
        if (value > 0) {
            swapCount.set(value);
        } else {
            Context.revert(value + " must be greater than 0");
        }
    }


    @External(readonly = true)
    public int getSwapCount() {
        return swapCount.getOrDefault(0);
    }

    @Override
    @External
    public void updateNextBlock(int blockCount) {
        validateAdmins();
        PeriodController period = new PeriodController();
        period.nextBlock.set(BigInteger.valueOf(Context.getBlockHeight() + blockCount));
    }

    private void disqualifyProject(Address sponsorAddress, BigInteger sponsorDepositAmount, String flag) {
        Context.require(flag.equals(bnUSD), TAG + " Not supported Token");
        JsonObject disqualifyProject = new JsonObject();
        disqualifyProject.add(METHOD, "returnFundAmount");
        JsonObject params = new JsonObject();
        params.add(SPONSOR_ADDRESS, sponsorAddress.toString());
        disqualifyProject.add("params", params);
        Address cpfScore = getCpfTreasuryScore();
        callScore(getBnusdScore(), TRANSFER, cpfScore,
                sponsorDepositAmount, disqualifyProject.toString().getBytes());
        SponsorBondReturned(cpfScore, "Project Disqualified. " + sponsorDepositAmount + " " + flag +
                " returned to CPF Treasury Address.");
    }

    @External(readonly = true)
    public Map<String, Object> getActiveProposalsList(@Optional int startIndex) {
        List<String> proposalKeys = new ArrayList<>();
        List<Map<String, Object>> activeProposalsList = new ArrayList<>();

        List<String> activeProposals = getProposalsKeysByStatus(ACTIVE);
        proposalKeys.addAll(activeProposals);
        List<String> pausedProposals = getProposalsKeysByStatus(PAUSED);
        proposalKeys.addAll(pausedProposals);

        int endIndex = startIndex + 10;
        int size = proposalKeys.size();
        if (endIndex > size) {
            endIndex = size;
        }

        for (int i = startIndex; i < endIndex; i++) {
            String proposalHash = proposalKeys.get(i);
            String proposalPrefix = proposalPrefix(proposalHash);
            Address contributorAddr = contributorAddress.at(proposalPrefix).get();
            Map<String, Object> proposalDetails = Map.of(PROJECT_TITLE, projectTitle.at(proposalPrefix).getOrDefault(""),
                    IPFS_HASH, proposalHash,
                    CONTRIBUTOR_ADDRESS, contributorAddr);
            activeProposalsList.add(proposalDetails);
        }
        return Map.of(DATA, activeProposalsList, COUNT, size);
    }

    /***
     Returns the list of all active or paused proposal from that address
     :param walletAddress : wallet address of the contributor
     :type walletAddress: Address
     :return: list of active proposals of a contributor
     ***/
    @External(readonly = true)
    public List<Map<String, Object>> getActiveProposals(Address walletAddress) {
        List<Map<String, Object>> _proposal_titles = new ArrayList<>();

        ArrayDB<String> contributorsAddress = contributorProjects.at(walletAddress);
        for (int i = 0; i < contributorsAddress.size(); i++) {
            String proposals = contributorsAddress.get(i);
            String prefix = proposalPrefix(proposals);
            String status = ProposalDataDb.status.at(prefix).getOrDefault("");
            if (ArrayDBUtils.containsInList(status, List.of(ACTIVE, PAUSED))) {
                int projectDuration = ProposalDataDb.projectDuration.at(prefix).getOrDefault(0);
                int proposalPeriod = ProposalDataDb.proposalPeriod.at(prefix).getOrDefault(0);
                boolean lastProgressReport = proposalPeriod + projectDuration - getPeriodCount() == 0;
                Map<String, Object> _proposals_details = Map.of(PROJECT_TITLE, projectTitle.at(prefix).getOrDefault(""),
                        IPFS_HASH, proposals,
                        NEW_PROGRESS_REPORT, submitProgressReport.at(prefix).getOrDefault(false),
                        "last_progress_report", lastProgressReport,
                        "milestoneDeadlines", getMilestoneDeadline(proposals));
                _proposal_titles.add(_proposals_details);
            }
        }

        return _proposal_titles;
    }


    /***
     Returns a dict of proposals of provided status
     :param walletAddress : user Signing in
     :type walletAddress : "iconservice.base.address"
     :return: List of all proposals_details
     ***/
    @Override
    @External(readonly = true)
    public Map<String, Object> getProposalDetailByWallet(Address walletAddress, @Optional int startIndex) {
        List<Map<String, Object>> _proposals_list = new ArrayList<>();
        ArrayDB<String> projects = contributorProjects.at(walletAddress);
        int endIndex = startIndex + 5;
        int size = projects.size();
        if (endIndex > size) {
            endIndex = size;
        }
        for (int i = startIndex; i < endIndex; i++) {
            Map<String, Object> _proposal_details = getProposalDetails(projects.get(i));
            _proposals_list.add(_proposal_details);
        }
        return Map.of(DATA, _proposals_list, COUNT, size);
    }


    @Override
    @External(readonly = true)
    public Map<String, Object> getProposalsHistory(@Optional int startIndex) {
        List<String> proposalKeys = new ArrayList<>();
        List<Map<String, Object>> proposalHistory = new ArrayList<>();

        List<String> completedProjects = getProposalsKeysByStatus(COMPLETED);
        proposalKeys.addAll(completedProjects);
        List<String> rejectedProposals = getProposalsKeysByStatus(REJECTED);
        proposalKeys.addAll(rejectedProposals);
        List<String> disqualifiedProjects = getProposalsKeysByStatus(DISQUALIFIED);
        proposalKeys.addAll(disqualifiedProjects);

        int endIndex = startIndex + 10;
        int size = proposalKeys.size();
        if (endIndex > size) {
            endIndex = size;
        }

        for (int i = startIndex; i < endIndex; i++) {
            Map<String, Object> proposalDetails = getProposalDetails(proposalKeys.get(i));
            proposalHistory.add(proposalDetails);
        }
        return Map.of(DATA, proposalHistory, COUNT, size);
    }

    //    =====================================TEMPORARY MIGRATIONS METHODS===============================================

    @External
    public void updateProposalPeriodCount(String newHash, int count) {
        validateAdmins();
        String newIpfsHashPrefix = proposalPrefix(newHash);
        proposalPeriod.at(newIpfsHashPrefix).set(count);
    }

    @External
    public void updateMilestoneDb(String progressHash, int MilestoneId) {
        validateAdmins();
        ArrayDB<Integer> milestoneSize = milestoneSubmitted.at(progressReportPrefix(progressHash));
        ArrayDBUtils.removeArrayItem(milestoneSize, MilestoneId);
    }


    @External
    public void milestoneDbMigration(String ipfsHash) {
        validateAdmins();
        String proposalPrefix = proposalPrefix(ipfsHash);
        ArrayDB<Integer> milestoneId = milestoneIds.at(proposalPrefix);
        int size = milestoneId.size();
        for (int i = 0; i < size; i++) {
            String migration_milestonePrefix = mileStonePrefix(proposalPrefix, milestoneId.get(i));
            String actual_milestonePrefix = mileStonePrefix(ipfsHash, milestoneId.get(i));

            MilestonesAttributes milestonesAttributes = new MilestonesAttributes();
            milestonesAttributes.id = MilestoneDb.id.at(migration_milestonePrefix).get();
            milestonesAttributes.budget = MilestoneDb.budget.at(migration_milestonePrefix).get();
            milestonesAttributes.completionPeriod = MilestoneDb.completionPeriod.at(migration_milestonePrefix).get();
            addDataToMilestoneDb(milestonesAttributes, actual_milestonePrefix);

        }
    }


    //    =====================================TEMPORARY MIGRATIONS METHODS===============================================

    //    =====================================EventLogs===============================================
    @Override
    @EventLog(indexed = 1)
    public void ProposalSubmitted(Address _sender_address, String note) {
    }

    @Override
    @EventLog(indexed = 1)
    public void ProgressReportSubmitted(Address _sender_address, String _project_title) {
    }

    @Override
    @EventLog(indexed = 1)
    public void SponsorBondReceived(Address _sender_address, String _notes) {
    }

    @Override
    @EventLog(indexed = 1)
    public void SponsorBondRejected(Address _sender_address, String _notes) {
    }

    @Override
    @EventLog(indexed = 1)
    public void VotedSuccessfully(Address _sender_address, String _notes) {
    }

    @Override
    @EventLog(indexed = 1)
    public void PRepPenalty(Address _prep_address, String _notes) {
    }

    @Override
    @EventLog(indexed = 1)
    public void UnRegisterPRep(Address _sender_address, String _notes) {
    }

    @Override
    @EventLog(indexed = 1)
    public void RegisterPRep(Address _sender_address, String _notes) {
    }

    @Override
    @EventLog(indexed = 1)
    public void SponsorBondReturned(Address _sender_address, String _notes) {
    }

    @Override
    @EventLog(indexed = 1)
    public void PeriodUpdate(String _notes) {
    }

    @Override
    @EventLog(indexed = 1)
    public void SponsorBondClaimed(Address _receiver_address, BigInteger _fund, String note) {
    }

    @Override
    @EventLog(indexed = 1)
    public void PriorityVote(Address _address, String note) {
    }


    @Override
    @EventLog(indexed = 1)
    public void UpdateContributor(Address _old, Address _new) {
    }

    //    =====================================EventLogs===============================================

    //    =====================================CallScore Methods===============================================

    public <T> T callScore(Class<T> t, Address address, String method, Object... params) {
        return Context.call(t, address, method, params);
    }

    public void callScore(Address address, String method, Object... params) {
        Context.call(address, method, params);
    }

    public void callScore(BigInteger amount, Address address, String method, Object... params) {
        Context.call(amount, address, method, params);
    }

    //    =====================================CallScore Methods===============================================


    //    =====================================Checkers===============================================

    public void validateAdmins() {
        Context.require(isAdmin(Context.getCaller()),
                TAG + ": Only Admins can call this method");

    }

    public void validateAdminScore(Address scoreAddress) {
        validateAdmins();
        Context.require(scoreAddress.isContract(), scoreAddress + " is not a SCORE Address");

    }

    public void onlyCPFTreasury() {
        Context.require(Context.getCaller().equals(getCpfTreasuryScore()), TAG + ": Only CPF treasury can call this method");
    }

    //    =====================================Checkers===============================================
}