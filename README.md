# ErgoNames Plasma

### Current Status

- **Initial Transaction Id:** `d55409dc8823b8c2a69196f6fb8715e2ed7ab637f4fc8b668624a8a92e5550a9`
- **Contract Address:** `KvbWcSgBPVS6DNrcUMm2A8mS3swQb9oXz3e3bjqivn6ENTaASF5N6jut2KYrmaUDk21qryvWihSX5njfbFfs8ELxFawW5Y57tUtEEo1MoriZsHCgxBSURJCgJQfH7wzM1V8ssNYuuKxEtsGtLz5BN2HzcdbZqSxKefaZZ4iqCTtrpPD7dKBYatUm`

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
  - [ ] Store data in local database
- [ ] Minting Information
  - [X] Mint new ErgoName NFT
  - [ ] Correct price paid
- [ ] System
  - [ ] Run as daemon
  - [ ] Dockerize service