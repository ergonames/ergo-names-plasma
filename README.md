# ErgoNames Plasma

### Current Status

- **Initial Transaction Id:** `d55409dc8823b8c2a69196f6fb8715e2ed7ab637f4fc8b668624a8a92e5550a9`
- **Mint Contract Address:** `24fZfBxxFZYG6ErBfvit6VhzwagencNc2mVwnHugTA5yA78tLvfTPsEz8AucKAEwJarwT9N1mtfxMWWcq3s71sUSK1HGteZkHbYdCATFHYLDr9LJw9p4ZdGH8xUBp7NHLeP2ZyNSEpANvqt8rzLayKYrS74pFnbxm3mRDFk6oGAbCXirGEaZGhHTSk9mzHgFxwVjC`
- **Proxy Contract Address:** `94v87P3ayTU44tnP43ZQq54u8p7idZEc5scUeRYhmXvXQCJCga5sWRiH8dFyidiQeHqRkxcTx6se1KdnHCd4rPCGJqapCrZL5i82XdHNM6GziYsTkzz2xrTV3aTbFjqhD4DibhCfxsPJZs67J1CVW61V1fMLQqkvcTvbPHdEtm5sXfW8eZ67URLff2YXsz52XEx3W9Y2MtLWLwJZb32jVyjJo24huKL7VNZo7dT8ETvvCatqnNivA3AajGTFEHwXMrtLoggiAbES5vjH81MhJViyFyPhQmi4`

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