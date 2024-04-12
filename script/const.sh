#!/bin/bash

export SCRIPTS_DIR=$PWD
export CPS_JAVA_SCORE="$SCRIPTS_DIR/.."
export DUMMY_SCORE="$SCRIPTS_DIR/../dummy"
export CONTRACT_FILE_NAME="contracts_$(date +"%Y%m%d_%H%M%S").txt"
export CONTRACT_ADDRESS="$SCRIPTS_DIR/contracts/$CONTRACT_FILE_NAME"

#---------------------------------------------------------------------
#-----------------CHANGE THIS BY REQUIREMENT--------------------------
#---------------------------------------------------------------------

export ICON=icon
export ICON_NET=local            ## [local, testnet, mainnet ]

export SICX_ADMIN="hxb6b5791be0b5ef67063b3c10b840fb81514db2fd"
export DEPLOYER_ADDRESS="hxb6b5791be0b5ef67063b3c10b840fb81514db2fd"

export ICON_WALLET_NAME=godWallet
export ICON_WALLET=$CPS_JAVA_SCORE/cps-gochain-local/data/godWallet.json
export ICON_PASSWORD=gochain

case $ICON_NET in
	"local" )
		export ICON_NID=3
		export ICON_NODE=http://localhost:9082/api/v3/
		export ICON_DEBUG_NODE=http://localhost:9082/api/v3d
	;;
	"testnet" )
		export ICON_NID=2
		export ICON_NODE=https://lisbon.net.solidwallet.io/api/v3/
		export ICON_DEBUG_NODE=https://lisbon.net.solidwallet.io/api/v3d
	;;
esac

export ICON_COMMON_ARGS="--uri $ICON_NODE --nid $ICON_NID --step_limit 2500000000 --key_store $ICON_WALLET \
--key_password $ICON_PASSWORD "

export GOVERNANCE_SCORE=cx0000000000000000000000000000000000000000