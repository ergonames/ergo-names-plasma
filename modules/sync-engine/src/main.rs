use anyhow::{Result};
use postgres::{Client, NoTls};
use reqwest::blocking::Response;
use serde_json::Value;

const DATABASE_PATH: &str = "postgresql://ergonames:ergonames@localhost:5432/ergonames";
const INITIAL_AVL_TREE_CREATION_ID: &str = "e271e7cb9b9c7932546e8a5746c91cb1c0f1114ff173a90e1fe979170f71c579";
const EXPLORER_API_BASE_URL: &str = "https://api-testnet.ergoplatform.com/api";

#[derive(Clone)]
struct MintInformation {
    ergoname_registered: String,
    mint_transaction_id: String,
    spent_transaction_id: Option<String>,
    ergoname_token_id: String
}

#[derive(Clone)]
struct InitialTransactionInformation {
    inital_transaction_id: String,
    spent_transaction_id: Option<String>,
}

fn main() {
    create_registration_information_schema();
    let initial_transaction_info: InitialTransactionInformation = get_inital_transaction_information(INITIAL_AVL_TREE_CREATION_ID);
    let initial_transaction_info: MintInformation = convert_inital_transaction_information_to_mint_transaction_information(initial_transaction_info);
    let mut last_spent_transaction_id: Option<String> = initial_transaction_info.clone().spent_transaction_id.clone();
    write_to_confirmed_registry_insertions_table(initial_transaction_info);
    let mut sync: bool = true;
    while sync {
        let mint_information: Option<MintInformation> = get_mint_information(last_spent_transaction_id.clone());
        if mint_information.is_some() {
            let mint_information: MintInformation = mint_information.unwrap();
            write_to_confirmed_registry_insertions_table(mint_information.clone());
            last_spent_transaction_id = mint_information.spent_transaction_id.clone();
        } else {
            sync = false;
        }
    }
}

fn write_to_confirmed_registry_insertions_table(mint_information: MintInformation) {
    let mut database_client: Client = connect_to_database().unwrap();
    let query: &str = "INSERT INTO confirmed_registry_insertions (ergoname_registered, mint_transaction_id, spent_transaction_id, ergoname_token_id) VALUES ($1, $2, $3, $4)";
    database_client.execute(query, &[ &mint_information.ergoname_registered, &mint_information.mint_transaction_id, &mint_information.spent_transaction_id, &mint_information.ergoname_token_id]).unwrap();
}

fn get_mint_information(last_spent_transaction_id: Option<String>) -> Option<MintInformation> {
    if last_spent_transaction_id.is_none() {
        return None
    }
    let last_spent_transaction_id: String = last_spent_transaction_id.unwrap();
    let url: String = format!("{}/v1/transactions/{}", EXPLORER_API_BASE_URL, last_spent_transaction_id);
    let response: Response = reqwest::blocking::get(&url).unwrap();
    let body: String = response.text().unwrap();
    let body: Value = serde_json::from_str(&body).unwrap();
    let mint_transaction_id: String = last_spent_transaction_id;
    let spent_transaction_id: Option<&str> = body["outputs"][1]["spentTransactionId"].as_str();
    let ergoname_registered: String = body["outputs"][0]["assets"][0]["name"].as_str().unwrap().to_string();
    let ergoname_token_id: String = body["outputs"][0]["assets"][0]["tokenId"].as_str().unwrap().to_string();
    let mint_information: MintInformation = MintInformation {
        mint_transaction_id: mint_transaction_id,
        spent_transaction_id: spent_transaction_id.map(|s| s.to_string()),
        ergoname_registered: ergoname_registered,
        ergoname_token_id: ergoname_token_id
    };
    Some(mint_information)
}

fn get_inital_transaction_information(inital_transaction_id: &str) -> InitialTransactionInformation {
    let url: String = format!("{}/v1/transactions/{}", EXPLORER_API_BASE_URL, inital_transaction_id);
    let response: Response = reqwest::blocking::get(&url).unwrap();
    let body: String = response.text().unwrap();
    let body: Value = serde_json::from_str(&body).unwrap();
    let spent_transaction_id: Option<&str> = body["outputs"][0]["spentTransactionId"].as_str();
    let spent_transaction_id: Option<String> = match spent_transaction_id {
        Some(id) => Some(id.to_string()),
        None => None
    };
    let initial_transaction_information: InitialTransactionInformation = InitialTransactionInformation {
        inital_transaction_id: inital_transaction_id.to_string(),
        spent_transaction_id: spent_transaction_id
    };
    initial_transaction_information
}

fn convert_inital_transaction_information_to_mint_transaction_information(init: InitialTransactionInformation) -> MintInformation {
    let mint_transaction_id: String = init.inital_transaction_id;
    let spent_transaction_id: Option<String> = init.spent_transaction_id;
    let ergoname_registered: String = "".to_string();
    let ergoname_token_id: String = "".to_string();
    let mint_information: MintInformation = MintInformation {
        ergoname_registered: ergoname_registered,
        mint_transaction_id: mint_transaction_id,
        spent_transaction_id: spent_transaction_id,
        ergoname_token_id: ergoname_token_id
    };
    mint_information
}

fn connect_to_database() -> Result<Client> {
    let client: Client = Client::connect(DATABASE_PATH, NoTls)?;
    Ok(client)
}

fn create_registration_information_schema() {
    let mut database_client: Client = connect_to_database().unwrap();
    let query: &str = "CREATE TABLE IF NOT EXISTS confirmed_registry_insertions (
        ergoname_registered VARCHAR(64) NOT NULL PRIMARY KEY,
        mint_transaction_id VARCHAR(64) NOT NULL,
        spent_transaction_id VARCHAR(64),
        ergoname_token_id VARCHAR(64) NOT NULL
    );";
    database_client.execute(query, &[]).unwrap();
}

fn create_database_schema() {
    create_registration_information_schema();
}