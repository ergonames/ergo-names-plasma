# ErgoNames Plasma

### Current Status

- **Initial Transaction Id:** `d5dbcf3c96eac9257d8ab840a2c6cf1c2cf5d17543fe2f1fa89a6f98b4a76106`
- **Mint Contract Address:** `24fZfBxxFZYG6ErBfvit6VhzwagencNc2mVwnHugTA5yA78tLvfTPsEz8AucKAEwJarwT9N1mtfxMWWcq3s71sUSK1HGteZkHbYdCATFHYLDr9LJw9p4ZdGH8xUBp7NHLeP2ZyNSEpANvqt8rzLayKYrS74pFnbxm3mRDFk6oGAbCXirGEaZGhHTSk9mzHgFxwVjC`
- **Proxy Contract Address:** `fyUVBXH65Y9SsiXzwnmU7GHhzEZUht5inZ9ZJ4d6vq5Z16DVLxfzBv46mfQtZZ1ZWRxhjCMzk61V8EFwzVbqr6anx7s63NiotqHWkWxBaHJrs2RC42XPhPRfixkihSNwarjoJjVtCQUR3UxUCmR9oWCCZ9bDSyoU79V6ohnvh3XJKWsA2ZsrnoYsrH84m51fCDQUvCseFivV9WTdjKzSog9ZmHoWv3JrTxcL5XMHfUAraG4fPajd5VxCcbmXAWtDuNnB3h75ip143nweHDyqnCf6vSXJk8jZwW1QauBCnYMNy2GX`

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