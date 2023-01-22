# ErgoNames Plasma

### Current Status (Subject to change)

- **Initial Transaction Id:** `e271e7cb9b9c7932546e8a5746c91cb1c0f1114ff173a90e1fe979170f71c579`
- **Mint Contract Address:** `24fZfBxxFZYG6ErBfvit6VhzwagencNc2mVwnHugTA5yA78tLvfTPsEz8AucKAEwJarwT9N1mtfxLnWD8WchdQdxr3su3MszYXSniUpUf3T13rXRBRTdx4qUimoGJhGPYASjrqaR4V6SnkpkkHkp62onP5fPbNfxdQbuo3uCVmmBQd6Xt3t8NEEVHFqNrCVT1pp4q`
- **Proxy Contract Address:** `2QSobRecPvrMVpdNVdqiEcDjCoZYyMCXRwh8cA8M5qx3rwiuQA5bifAzghk`

Disclaimer: Current proxy contract is completely unsafe. The funds deposited into the contract can be sent to any address.

### Service Overview

##### Minting Service

The `minting-service` is used to mint new ErgoNames NFTs. The service is used to create a transaction by combining the on-chain AvlTree registry with the transactions in the mempool. The service then submits the transaction to the network.

##### Sync Engine

The `sync-engine` tracks on-chain transactions and stores the data in a local database. The information in the local database is used to rebuild the on-chain AvlTree registry.

##### Mempool Tracker

The `mempool-tracker` tracks transactions in the mempool at the proxy contract address and stores the data in a local database. The transactions in the mempool are used as an input in the minting transaction.

### Configuration File Parameters

- **explorerApiUrl:** The URL of the Ergo Explorer API.
- **initialTxId:** The initial transaction ID that created the on chain AvlTree registry.
- **receiverAddress:** Used in SubmitMintRequest scenario to set the receiver address of the minted NFT.
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
  - [X] Ensure equal miner fee is used across all transactions
  - [ ] Ensure funds are sent to mint contract
  - [X] Ensure registers are set correctly
- [ ] Sync Engine
  - [ ] Remove explorer calls
  - [ ] Sync using Ergo node API
  - [X] Store data in local database
- [ ] Mempool Tracker
  - [X] Track proxy contract transactions
  - [ ] Remove transactions once used in minting transaction
- [ ] Minting Information
  - [X] Mint new ErgoName NFT
  - [ ] Correct price paid
- [ ] System
  - [ ] Use chained transactions to allow for multiple mints per block
  - [ ] Run as daemon
  - [ ] Dockerize service

### Running Services

###### Local Database Setup

```
docker run --name ergonames-local-registry-db -e POSTGRES_USER=ergonames -e POSTGRES_PASSWORD=ergonames -p 5432:5432 -v /db:/var/lib/postgresql/data -d postgres
```

Where `/db` is the local directory to store the database data.