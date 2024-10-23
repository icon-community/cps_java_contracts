#!/bin/bash

source const.sh

function icon_wait_tx() {
    local ret=1
    local tx_hash=$1
     while :; do
            goloop rpc \
                --uri "$ICON_NODE" \
                txresult "$tx_hash" &>/dev/null && break || sleep 1
        done

    local res=$(goloop rpc \
        --uri $ICON_NODE \
        txresult "$tx_hash")
    local status=$(jq <<<"$res" -r .status)

    echo "status : $status"
    [ "$status" == 0x0 ] && echo $res && exit 0
    [ "$status" == 0x1 ] && ret=0

    return $ret
}

function save_address() {
    local ret=1
    local tx_hash=$1
    local addrLoc=$2
    local fileName=$3
    local res=$(goloop rpc \
            --uri $ICON_NODE \
            txresult "$tx_hash" 2>/dev/null)

    local score_address=$(jq <<<"$res" -r .scoreAddress)
    echo "contract address : $score_address"
    echo "$fileName: $score_address" >> $addrLoc
}


function deploy_contract() {
  local jarFile=$1
  local addrLoc=$2


  local contractName=$(basename "$jarFile" | cut -d'-' -f1)
  echo "deploying contract $contractName"

  local params=()
  for i in "${@:3}"; do params+=("--param $i"); done

  local tx_hash=$(
    goloop rpc sendtx deploy $jarFile \
    --content_type application/java \
    --to $GOVERNANCE_SCORE \
    $ICON_COMMON_ARGS \
    ${params[@]} | jq -r .
    )
  icon_wait_tx $tx_hash
  save_address $tx_hash $addrLoc $contractName

}

function icon_send_tx() {
    local address=$1
    local method=$2

    local params=()
    for i in "${@:3}"; do params+=("--param $i"); done
    local tx_hash=$(
        goloop rpc sendtx call \
            --to "$address" \
            --method "$method" \
            $ICON_COMMON_ARGS \
            ${params[@]} | jq -r .
    )
    icon_wait_tx "$tx_hash"
}

function setPrepPenaltyAmount() {
    local param="{\"params\":{\"penalty\":[\"2\",\"5\",\"10\"]}}"
    local address=$(grep 'CPSCore' "$CONTRACT_ADDRESS" | cut -d' ' -f2)
    local method="setPrepPenaltyAmount"
    local tx_hash=$(goloop rpc sendtx call \
              --to "$address" \
              --method "$method" \
              --raw $param\
              $ICON_COMMON_ARGS | jq -r .
      )
    icon_wait_tx $tx_hash
}

function icon_transfer_icx() {
    local address=$1
    local val=$2

    local params=()
    for i in "${@:3}"; do params+=("--param $i"); done

    local tx_hash=$(
        goloop rpc sendtx transfer \
            --to "$address" \
            --value "$val" \
            $ICON_COMMON_ARGS \
            ${params[@]} | jq -r .
    )
    icon_wait_tx "$tx_hash"
}


function deploy_cps_contracts() {

    echo "-------------------------------------------------------------------"
    echo "--------------------------DEPLOY CONTRACTS-------------------------"
    echo "-------------------------------------------------------------------"
    local CPS_CORE_FILE=$CPS_JAVA_SCORE/CPSCore/build/libs/CPSCore-[0-9].[0-9].[0-9]-optimized.jar
    deploy_contract $CPS_CORE_FILE $CONTRACT_ADDRESS bondValue="15" applicationPeriod="15"

    local cps_score_addr=$(grep 'CPSCore' "$CONTRACT_ADDRESS" | cut -d' ' -f2)
    local CPS_TREASURY_FILE=$CPS_JAVA_SCORE/CPSTreasury/build/libs/CPSTreasury-[0-9].[0-9].[0-9]-optimized.jar
    deploy_contract $CPS_TREASURY_FILE $CONTRACT_ADDRESS cpsScore=$cps_score_addr

    local CPF_TREASURY_FILE=$CPS_JAVA_SCORE/CPFTreasury/build/libs/CPFTreasury-[0-9].[0-9].[0-9]-optimized.jar
    deploy_contract $CPF_TREASURY_FILE $CONTRACT_ADDRESS cpsScore=$cps_score_addr

    local BNUSD_FILE=$DUMMY_SCORE/bnUSD/build/libs/bnUSD-[0-9].[0-9].[0-9]-optimized.jar
    deploy_contract $BNUSD_FILE $CONTRACT_ADDRESS _name="Dummy_bnUSD_Coin" _symbol="bnUSD" _decimals="18" _initialSupply="1000000000000000000000"

    local DEX_FILE=$DUMMY_SCORE/Dex/build/libs/Dex-[0-9].[0-9].[0-9]-optimized.jar
    deploy_contract $DEX_FILE $CONTRACT_ADDRESS

    local ORACLE_FILE=$DUMMY_SCORE/oracle/build/libs/oracle-[0-9].[0-9].[0-9]-optimized.jar
    deploy_contract $ORACLE_FILE $CONTRACT_ADDRESS

    local ROUTER_FILE=$DUMMY_SCORE/Router/build/libs/Router-[0-9].[0-9].[0-9]-optimized.jar
    deploy_contract $ROUTER_FILE $CONTRACT_ADDRESS

    local SICX_FILE=$DUMMY_SCORE/sICX/build/libs/sICX-[0-9].[0-9].[0-9]-optimized.jar
    deploy_contract $SICX_FILE $CONTRACT_ADDRESS _admin=$SICX_ADMIN

}

function setters_in_cps_score() {
    echo "-------------------------------------------------------------------"
    echo "--------------------SET CONTRACTS IN CPS SCORE---------------------"
    echo "-------------------------------------------------------------------"

    local cps_score_addr=$(grep 'CPSCore' "$CONTRACT_ADDRESS" | cut -d' ' -f2)

    echo "setting admin address"
    icon_send_tx $cps_score_addr "addAdmin" address=$SICX_ADMIN

    echo "setting cps treasury address"
    local cps_treasury_addr=$(grep 'CPSTreasury' "$CONTRACT_ADDRESS" | cut -d' ' -f2)
    icon_send_tx $cps_score_addr "setCpsTreasuryScore" score=$cps_treasury_addr

    echo "setting cpf treasury address"
    local cpf_treasury_addr=$(grep 'CPFTreasury' "$CONTRACT_ADDRESS" | cut -d' ' -f2)
    icon_send_tx $cps_score_addr "setCpfTreasuryScore" score=$cpf_treasury_addr

    echo "setting bnUSD address"
    local bnUSD_addr=$(grep 'bnUSD' "$CONTRACT_ADDRESS" | cut -d' ' -f2)
    icon_send_tx $cps_score_addr "setBnusdScore" score=$bnUSD_addr

}

function setter_in_cpf_treasury() {
    echo "-------------------------------------------------------------------"
    echo "-----------------SET CONTRACTS IN CPF TREASURY---------------------"
    echo "-------------------------------------------------------------------"

    local cpf_treasury_addr=$(grep 'CPFTreasury' "$CONTRACT_ADDRESS" | cut -d' ' -f2)

    echo "setting cps treasury address"
    local cps_treasury_addr=$(grep 'CPSTreasury' "$CONTRACT_ADDRESS" | cut -d' ' -f2)
    icon_send_tx $cpf_treasury_addr "setCpsTreasuryScore" score=$cps_treasury_addr

    echo "setting bnUSD address"
    local bnUSD_addr=$(grep 'bnUSD' "$CONTRACT_ADDRESS" | cut -d' ' -f2)
    icon_send_tx $cpf_treasury_addr "setBnUSDScore" score=$bnUSD_addr

    echo "setting dex address"
    local dex_addr=$(grep 'Dex' "$CONTRACT_ADDRESS" | cut -d' ' -f2)
    icon_send_tx $cpf_treasury_addr "setDexScore" score=$dex_addr

    echo "setting oracle address"
    local oracle_addr=$(grep 'oracle' "$CONTRACT_ADDRESS" | cut -d' ' -f2)
    icon_send_tx $cpf_treasury_addr "setOracleAddress" score=$oracle_addr

    echo "setting router address"
    local router_addr=$(grep 'Router' "$CONTRACT_ADDRESS" | cut -d' ' -f2)
    icon_send_tx $cpf_treasury_addr "setRouterScore" score=$router_addr

    echo "setting sICX address"
    local sICX_addr=$(grep 'sICX' "$CONTRACT_ADDRESS" | cut -d' ' -f2)
    icon_send_tx $cpf_treasury_addr "setSicxScore" score=$sICX_addr
}

function setters_in_cps_treasury() {
    echo "-------------------------------------------------------------------"
    echo "-----------------SET CONTRACTS IN CPS TREASURY---------------------"
    echo "-------------------------------------------------------------------"

    local cps_treasury_addr=$(grep 'CPSTreasury' "$CONTRACT_ADDRESS" | cut -d' ' -f2)

    echo "setting cpf treasury address"
    local cpf_treasury_addr=$(grep 'CPFTreasury' "$CONTRACT_ADDRESS" | cut -d' ' -f2)
    icon_send_tx $cps_treasury_addr "setCpfTreasuryScore" score=$cpf_treasury_addr

    echo "setting bnUSD address"
    local bnUSD_addr=$(grep 'bnUSD' "$CONTRACT_ADDRESS" | cut -d' ' -f2)
    icon_send_tx $cps_treasury_addr "setBnUSDScore" score=$bnUSD_addr

}

function setters_in_dex() {
    echo "-------------------------------------------------------------------"
    echo "-----------------SET CONTRACTS IN DEX------------------------------"
    echo "-------------------------------------------------------------------"

    local dex=$(grep 'Dex' "$CONTRACT_ADDRESS" | cut -d' ' -f2)

    local sICX_addr=$(grep 'sICX' "$CONTRACT_ADDRESS" | cut -d' ' -f2)
    icon_send_tx $dex "setSicxScore" _score=$sICX_addr

}

function add_funds() {
  echo "-------------------------------------------------------------------"
  echo "-----------------ADD FUND IN TREASURY------------------------------"
  echo "-------------------------------------------------------------------"

  local cpf_treasury_addr=$(grep 'CPFTreasury' "$CONTRACT_ADDRESS" | cut -d' ' -f2)

  echo "set treasury fund limit"
  icon_send_tx $cpf_treasury_addr "setMaximumTreasuryFundIcx" value="0xd3c21bcecceda0000000"
  icon_send_tx $cpf_treasury_addr "setMaximumTreasuryFundBnusd" value="0xd3c21bcecceda0000000"

  echo "send fund to treasury"
  local bnUSD_addr=$(grep 'bnUSD' "$CONTRACT_ADDRESS" | cut -d' ' -f2)
  icon_send_tx $bnUSD_addr "mintTo" to=$cpf_treasury_addr amount="50000000000000000000000"

  echo "send sicx to dex for swap"
  local sICX_addr=$(grep 'sICX' "$CONTRACT_ADDRESS" | cut -d' ' -f2)
  local dex_addr=$(grep 'Dex' "$CONTRACT_ADDRESS" | cut -d' ' -f2)
  icon_send_tx $sICX_addr "mintWithTokenFallBack" _to=$dex_addr _amount="10000000000000000000000" \
  _data="0x7b0a2020226d6574686f64223a20225f646578220a7d"

  echo "send ICX to dex"
  icon_transfer_icx $dex_addr "1000000000000000000000"


}

function configure_system() {
  echo "-------------------------------------------------------------------"
  echo "-----------------SET SYSTEM CONFIGURATION--------------------------"
  echo "-------------------------------------------------------------------"
  local cps_score_addr=$(grep 'CPSCore' "$CONTRACT_ADDRESS" | cut -d' ' -f2)

  echo "turn off maintainance mode"
  icon_send_tx $cps_score_addr "toggleMaintenance"

  echo "set initial block"
  icon_send_tx $cps_score_addr "setInitialBlock"

  echo "set prep's penalty amount"
  setPrepPenaltyAmount

  local cpf_treasury_addr=$(grep 'CPFTreasury' "$CONTRACT_ADDRESS" | cut -d' ' -f2)

  echo "toggle swap flag"
  icon_send_tx $cpf_treasury_addr "toggleSwapFlag"

  echo "set oracle slippage percentage"
  icon_send_tx $cpf_treasury_addr "setOraclePercentageDifference" value="3"
#  icon_send_tx $cps_score_addr "toggleBudgetAdjustmentFeature"

}



function set_contracts() {
    setters_in_cps_score
    setters_in_cps_treasury
    setter_in_cpf_treasury
    setters_in_dex

}

function configure_fund_and_system() {
    add_funds
    configure_system
}

SHORT=adsc
LONG=all,deploy-contracts,set-contracts,configure-system

options=$(getopt -o $SHORT --long $LONG -n 'deploy.sh' -- "$@")
if [ $? -ne 0 ]; then
    echo "Usage: $0 [-a] [-d] [-s] [-c]" >&2
    exit 1
fi

eval set -- "$options"

while true; do
    case "$1" in
        -a|--all) deploy_cps_contracts; set_contracts; configure_fund_and_system; shift ;;
        -d|--deploy-contracts) deploy_cps_contracts; shift ;;
        -s|--set-contracts) set_contracts; shift ;;
        -c|--configure-system) configure_fund_and_system; shift ;;
        --) shift; break ;;
        *) echo "Internal error!"; exit 1 ;;
    esac
done