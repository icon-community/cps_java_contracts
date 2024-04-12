# DEPLOYMENT SCRIPT

The script contains scripts to 
- deploy required contracts
- configure contracts
- add funds to the system

## Prerequisites

Install [go](https://go.dev/doc/install)

- ### goloop
```sh
go install github.com/icon-project/goloop/cmd/goloop@latest
```

- ### jq
Install jq
```sh
sudo apt install jq
brew install jq
sudo pacman -S jq
```

## Usage

The constants are defined in `const.sh`. Wallet and environment can be changed
from here.
`deploy.sh` contains all the deployment and configuration script.Invoke with `-a` flag to
complete all transaction. 
```sh
bash deploy.sh -a
```
