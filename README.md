# ErgoNames Plasma

### Current Status (Subject to change)

- **Initial Transaction Id:** `5850648970f3da6358ce26e9bbe1223e58b4afc395ade5a180e70158a2fb75e3`
- **Mint Contract Address:** `tUZbxGJzvgicP5iADDWd4fGTfhQKGvPjZEEtwSuQFDXLxbaoC1JeGjuQLsPvq2urV8Pmcp5RNhGr2xPGbFSWCeEgVJuSTkf7JgC2eW5iRoEpsZZQBGz83JG9Xfzi2cHKPZYgx1FotLYHpP9oqVevT5GKE3UcKedNDLzgJY`
- **Proxy Contract Address:** `Ms7smJwLGbUAjuWQ`

Disclaimer: Current proxy contract is completely unsafe. Current code is `sigmaProp(1==1)`

### Configuration File Parameters

- **defaultExplorerApiUrl:** The default explorer API URL to use for the Ergo Explorer API.
- **initialTxId:** The initial transaction ID that created the on chain AvlTree registry.
- **ergoNameToRegister:** The ErgoName to register is regular text.
- **liveMode:** The service mode to run the ErgoNames Plasma service in live mode. Options are `true` or `false`.

### Roadmap

- [ ] Minting Contract
  - [X] Allow for updating registry
  - [X] Mint new token
  - [ ] Price checking
  - [ ] Correct payment destination
  - [ ] Refunds allows
- [ ] Proxy Contract
  - [ ] Ensure equal miner fee is used across all transactions
- [ ] Sync Engine
  - [X] Sync using explorer API
  - [ ] Sync using Ergo node API
  - [X] Store data in local database
- [ ] Minting Information
  - [X] Mint new ErgoName NFT
  - [ ] Correct price paid
- [ ] System
  - [ ] Run as daemon
  - [ ] Dockerize service