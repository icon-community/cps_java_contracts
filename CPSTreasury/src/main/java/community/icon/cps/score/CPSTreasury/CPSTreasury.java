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

package community.icon.cps.score.CPSTreasury;

import score.*;
import score.annotation.EventLog;
import score.annotation.External;

import java.math.BigInteger;

public class CPSTreasury {
    private final String name;
    private final String symbol;

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


    private static final VarDB<String> id = Context.newVarDB(ID, String.class);
    private static final ArrayDB<String> proposalsKeys = Context.newArrayDB(PROPOSALS_KEYS, String.class);
    private static final DictDB<String, Integer> proposalsKeyListIndex = Context.newDictDB(PROPOSALS_KEY_LIST_INDEX, Integer.class);
    private static final DictDB<String, BigInteger> fundRecord = Context.newDictDB(FUND_RECORD, BigInteger.class);
    private static final BranchDB<String, DictDB<String, BigInteger>> installmentFundRecord = Context.newBranchDB(INSTALLMENT_FUND_RECORD, BigInteger.class);

    private static final VarDB<Address> cpfTreasuryScore = Context.newVarDB(CPF_TREASURY_SCORE, Address.class);
    private static final VarDB<Address> cpsScore = Context.newVarDB(CPS_SCORE, Address.class);
    private static final VarDB<Address> balancedDollar = Context.newVarDB(BALANCED_DOLLAR, Address.class);

    public  CPSTreasury(String name, String symbol){
        this.name=name;
        this.symbol=symbol;
    }

    @External(readonly = true)
    public String name(){
        return this.name;
    }

    @External(readonly = true)
    public String symbol(){
        return this.symbol;
    }



    @EventLog(indexed = 1)
    public void FundReturned(Address _sponsor_address, String note){}

    @EventLog(indexed = 1)
    public void ProposalFundTransferred(String _ipfs_key, String note){}

    @EventLog(indexed = 1)
    public void ProposalDisqualified(String _ipfs_key, String note){}

    @EventLog(indexed = 1)
    public void FundReceived(Address _sponsor_address, String note){}


}
