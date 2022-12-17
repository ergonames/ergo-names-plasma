# ErgoNames Plasma
###### Proof of Concept Plasma Implementation

### Current Status

- **Initial Transaction Id:** `4e8c311cecd1c977ee5aa836f292e2f9c211b906947973d4aaeb50b8999a71dc`
- **Initial Box Id:** `c667259b608c1454ccff152ce06208b3637c07b20c285ae5342d5b0ce3ea70dd`
- **Most Recent Box Id:** `38dfd616d54e75d1662d6c930d70d10841899a9c7593356763e75b5f617444c5`

### Configuration File Parameters

- **defaultExplorerApiUrl:** The default explorer API URL to use for the Ergo Explorer API.
- **initialTxId:** The initial transaction ID that created the on chain AvlTree registry.
- **initialBoxId:** The initial box ID that created the on chain AvlTree registry.
- **mostRecentBoxId:** The most recent box ID that updated the on chain AvlTree registry.
- **ergoNameToRegister:** The ErgoName to register is regular text.
- **tokenIdToRegister:** The token ID to register (same ID that can be found on chain).
- **serviceMode:** The service mode to run the ErgoNames Plasma service in. Options are `live` or `dry`.