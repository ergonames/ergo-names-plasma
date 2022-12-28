# ErgoNames Plasma

### Current Status (Subject to change)

- **Initial Transaction Id:** `237ec829703605ba7383b8aa6d77d66e375bee0bca58889be17f09d820cb4b70`
- **Mint Contract Address:** `24fZfBxxFZYG6ErBfvit6VhzwagencNc2mVwnHugTA5yA78tLvfTPsEz8AucKAEwJarwT9N1mtfxLnWD8WchdQdxr3su3MszYXSniUpUf3T13rXRBRTdx4qUimoGJhGPYASjrqaR4V6SnkpkkHkp62onP5fPbNfxdQbuo3uCVmmBQd6Xt3t8NEEVHFqNrCVT1pp4q`
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