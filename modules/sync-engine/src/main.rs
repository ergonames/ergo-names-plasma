use anyhow::{Result};
use reqwest::blocking::Response;
use rocksdb::{DB};
use serde_json::Value;
use sigma_util::hash::blake2b256_hash;


#[derive(Clone, Debug)]
struct MintInformation {
    mint_transaction_id: String,
    spent_transaction_id: String,
    ergoname_registered: String,
    ergoname_token_id: String,
}

impl MintInformation {

    fn to_bytes(&self) -> Vec<u8> {
        let mut bytes: Vec<u8> = Vec::new();
        bytes.extend_from_slice(self.mint_transaction_id.as_bytes());
        bytes.extend_from_slice(self.spent_transaction_id.as_bytes());
        bytes.extend_from_slice(self.ergoname_registered.as_bytes());
        bytes.extend_from_slice(self.ergoname_token_id.as_bytes());
        bytes
    }

    fn from_bytes(bytes: &[u8]) -> MintInformation {
        let mint_transaction_id: String = String::from_utf8(bytes[0..64].to_vec()).unwrap();
        let spent_transaction_id: String = String::from_utf8(bytes[64..128].to_vec()).unwrap();
        let ergoname_registered: String = String::from_utf8(bytes[128..192].to_vec()).unwrap();
        let ergoname_token_id: String = String::from_utf8(bytes[192..256].to_vec()).unwrap();
        MintInformation {
            mint_transaction_id,
            spent_transaction_id,
            ergoname_registered,
            ergoname_token_id,
        }
    }
}

fn main() {
    let initial_transaction_id: &str = "d55409dc8823b8c2a69196f6fb8715e2ed7ab637f4fc8b668624a8a92e5550a9";
    let first_insertion_transaction: Result<String> = get_first_insertion_transaction(initial_transaction_id);
    if first_insertion_transaction.is_err() {
        panic!("Error: {:?}", first_insertion_transaction.err());
    }
    let first_insertion_transaction: String = first_insertion_transaction.unwrap();
    write_inital(initial_transaction_id, &first_insertion_transaction);
    let mut spend_transaction_id: String = first_insertion_transaction.clone();
    while spend_transaction_id != "0000000000000000000000000000000000000000000000000000000000000000" {
        let mint_information: MintInformation = parse_transaction_data(spend_transaction_id.clone());
        write_to_database(mint_information.clone());
        spend_transaction_id = mint_information.spent_transaction_id.clone();
    }


    // let db = DB::open_default("db").unwrap();
    // let first_insertion_transaction_bytes = db.get(initial_transaction_id.as_bytes()).unwrap().unwrap();
    // let first_insertion_transaction = String::from_utf8(first_insertion_transaction_bytes).unwrap();
    // println!("{}", first_insertion_transaction);
}

fn get_first_insertion_transaction(initial_transaction_id: &str) -> Result<String> {
    let initial_transaction: Result<String> = get_transaction_by_id(initial_transaction_id);
    let initial_transaction_json: Value = convert_to_json(initial_transaction.unwrap());
    let spent_transaction_id: String = initial_transaction_json["outputs"][0]["spentTransactionId"].as_str().unwrap().to_owned();
    return Ok(spent_transaction_id);
}

fn get_transaction_by_id(transaction_id: &str) -> Result<String> {
    let url: String = format!("https://api-testnet.ergoplatform.com/api/v1/transactions/{}", transaction_id);
    let response: Response = reqwest::blocking::get(&url).unwrap();
    let body: String = response.text().unwrap();
    return Ok(body);
}

fn convert_to_json(transaction_data: String) -> Value {
    let transaction: Value = serde_json::from_str(&transaction_data).unwrap();
    return transaction;
}

fn parse_transaction_data(transaction_id: String) -> MintInformation {
    let transaction_data: String = get_transaction_by_id(&transaction_id).unwrap();
    let transaction: Value = serde_json::from_str(&transaction_data).unwrap();
    let mint_transaction_id: String = transaction["id"].as_str().unwrap().to_owned();
    let spent_transaction_id: Option<&str> = transaction["outputs"][1]["spentTransactionId"].as_str();
    let spent_transaction_id: String = match spent_transaction_id {
        Some(spent_transaction_id) => spent_transaction_id.to_owned(),
        None => "0000000000000000000000000000000000000000000000000000000000000000".to_owned(),
    };
    let ergoname_registered: Option<&str> = transaction["outputs"][0]["assets"][0]["name"].as_str();
    let ergoname_registered: String = match ergoname_registered {
        Some(ergoname_registered) => ergoname_registered.to_owned(),
        None => "0000000000000000000000000000000000000000000000000000000000000000".to_owned(),
    };
    let ergoname_token_id: Option<&str> = transaction["outputs"][0]["assets"][0]["tokenId"].as_str();
    let ergoname_token_id: String = match ergoname_token_id {
        Some(ergoname_token_id) => ergoname_token_id.to_owned(),
        None => "0000000000000000000000000000000000000000000000000000000000000000".to_owned(),
    };

    let ergoname_registered_hash: Box<[u8; 32]> = blake2b256_hash(ergoname_registered.as_bytes());
    let ergoname_registered_hash: String = hex::encode(ergoname_registered_hash.to_vec());

    let mint_information: MintInformation = MintInformation {
        mint_transaction_id,
        spent_transaction_id,
        ergoname_registered: ergoname_registered_hash,
        ergoname_token_id,
    };
    return mint_information;
}

fn write_inital(initial_transaction_id: &str, first_insertion_transaction: &str) {
    let db = DB::open_default("db").unwrap();
    db.put(initial_transaction_id.as_bytes(), first_insertion_transaction.as_bytes()).unwrap();
}

fn write_to_database(mint_information: MintInformation) {
    let db = DB::open_default("db").unwrap();
    db.put(mint_information.mint_transaction_id.as_bytes(), mint_information.to_bytes()).unwrap();
    match db.get(mint_information.mint_transaction_id.as_bytes()) {
        Ok(Some(value)) => {
            let mint_information: MintInformation = MintInformation::from_bytes(&value);
            println!("Mint Information: {:?}", mint_information);
        }
        Ok(None) => println!("No value found"),
        Err(e) => println!("Operational problem encountered: {}", e),
    }
    db.delete(mint_information.mint_transaction_id.as_bytes()).unwrap();
}