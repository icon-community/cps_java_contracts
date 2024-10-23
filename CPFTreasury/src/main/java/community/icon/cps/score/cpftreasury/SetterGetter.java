package community.icon.cps.score.cpftreasury;

import score.Address;
import score.ArrayDB;
import score.Context;
import score.annotation.External;
import scorex.util.ArrayList;

import java.math.BigInteger;
import java.util.List;

import static community.icon.cps.score.cpftreasury.CPFTreasury.councilFlag;
import static community.icon.cps.score.cpftreasury.CPFTreasury.councilManagers;
import static community.icon.cps.score.cpftreasury.Constants.TAG;
import static community.icon.cps.score.cpftreasury.Validations.validateAdmins;
import static community.icon.cps.score.cpftreasury.Validations.validateGovernanceContract;

public class SetterGetter {
    /**
     * Sets the cps score address. Only owner can set the method
     *
     * @param score: Score address of cps score
     */
    @External
    public void setCpsScore(Address score) {
        Validations.validateAdminScore(score);
        CPFTreasury.cpsScore.set(score);
    }

    /**
     * Returns the cps score address
     *
     * @return cps score address
     */
    @External(readonly = true)
    public Address getCpsScore() {
        return CPFTreasury.cpsScore.get();
    }

    /**
     * Sets the cps treasury score address. Only cps admins can set the method
     *
     * @param score: Score address of cps treasury score
     */
    @External
    public void setCpsTreasuryScore(Address score) {
        Validations.validateAdminScore(score);
        CPFTreasury.cpsTreasuryScore.set(score);
    }

    /**
     * Returns the cps treasury score address
     *
     * @return cps treasury score address
     */
    @External(readonly = true)
    public Address getCpsTreasuryScore() {
        return CPFTreasury.cpsTreasuryScore.get();
    }

    /**
     * Sets the bnUSD score address. Only cps admins can set the method
     *
     * @param score: Score address of bnUSD score
     */
    @External
    public void setBnUSDScore(Address score) {
        Validations.validateAdminScore(score);
        CPFTreasury.balancedDollar.set(score);
    }

    /**
     * Returns the bnUSD score address
     *
     * @return cps bnUSD address
     */
    @External(readonly = true)
    public Address getBnUSDScore() {
        return CPFTreasury.balancedDollar.get();
    }

    /**
     * Sets the sicx score address. Only cps admins can set the method
     *
     * @param score: Score address of sicx score
     */
    @External
    public void setSicxScore(Address score) {
        Validations.validateAdminScore(score);
        CPFTreasury.sICXScore.set(score);
    }

    /**
     * Reruns the sicx score address
     *
     * @return sicx score address
     */
    @External(readonly = true)
    public Address getSicxScore() {
        return CPFTreasury.sICXScore.get();
    }

    /**
     * Sets the dex score address. Only owner can set the method
     *
     * @param score: Score address of dex score
     */
    @External
    public void setDexScore(Address score) {
        Validations.validateAdminScore(score);
        CPFTreasury.dexScore.set(score);
    }

    /**
     * Returns the dex score address
     *
     * @return dex score address
     */
    @External(readonly = true)
    public Address getDexScore() {
        return CPFTreasury.dexScore.get();
    }

    /**
     * Sets the router score address. Only owner can set the method
     *
     * @param score: Score address of router score
     */
    @External
    public void setRouterScore(Address score) {
        Validations.validateAdminScore(score);
        CPFTreasury.routerScore.set(score);
    }

    /**
     * Returns the router score address
     *
     * @return router score address
     */
    @External(readonly = true)
    public Address getRouterScore() {
        return CPFTreasury.routerScore.get();
    }

    @External
    public void setOracleAddress(Address score) {
        Validations.validateAdminScore(score);
        CPFTreasury.oracleAddress.set(score);
    }

    /**
     * Returns the router score address
     *
     * @return router score address
     */
    @External(readonly = true)
    public Address getOracleAddress() {
        return CPFTreasury.oracleAddress.get();
    }

    @External
    public void setSponsorBondPercentage(BigInteger bondValue) {
        validateAdmins();
        Context.call(getCpsScore(), "setSponsorBondPercentage", bondValue);
    }

    @External
    public void setPeriod(BigInteger applicationPeriod) {
        validateGovernanceContract();
        Context.call(getCpsScore(), "setPeriod", applicationPeriod);
    }

    @External
    public void setOnsetPayment(BigInteger paymentPercentage) {
        validateGovernanceContract();
        Context.call(getCpsTreasuryScore(), "setOnsetPayment", paymentPercentage);
    }

    @External
    public void toggleCouncilFlag() {
        validateGovernanceContract();
        councilFlag.set(!councilFlag.getOrDefault(false));
    }

    @External(readonly = true)
    public boolean getCouncilFlag() {
        return councilFlag.getOrDefault(false);
    }

    @External
    public void setCouncilManagers(Address[] newCouncilManagers) {
        validateGovernanceContract();
        int sizeOfCouncilManagers = newCouncilManagers.length;
        Context.require(sizeOfCouncilManagers >= 3, TAG + ":: council managers should be greater than 3");
        Context.require(sizeOfCouncilManagers % 2 == 1, TAG + ":: council managers should be an odd number");
        if (councilManagers.size() > 0) {
            clearArrayDb(councilManagers);
        }
        for (Address newCouncilManager : newCouncilManagers) {
            councilManagers.add(newCouncilManager);
        }
    }

    @External(readonly = true)
    public List<Address> getCouncilManagers() {
        return arrayDBtoList(councilManagers);
    }


    <T> List<T> arrayDBtoList(ArrayDB<T> arraydb) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < arraydb.size(); i++) {
            list.add(arraydb.get(i));
        }
        return list;
    }

    void clearArrayDb(ArrayDB<?> array_db) {
        int size = array_db.size();
        for (int i = 0; i < size; i++) {
            array_db.pop();
        }

    }

}
