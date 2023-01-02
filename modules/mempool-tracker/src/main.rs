use anyhow::{Result};
use postgres::{Client, NoTls};
use reqwest::blocking::Response;
use serde_json::Value;
use std::{time::Duration, thread::sleep};

const DATABASE_PATH: &str = "postgresql://ergonames:ergonames@localhost:5432/ergonames";
const PROXY_CONTRACT_ERGO_TREE: &str = "1003040001010400d801d601b2a5730000d1eded7301e6c67201040e93c17201c1b2a4730200";
const NODE_BASE_URL: &str = "http://104.237.139.78:9052";

struct MempoolTransaction {
    transaction_id: String,
    box_id: String
}

fn main() {
    create_database_schema();
    loop {
        let transactions: Vec<Value> = get_all_transactions().unwrap();
        let mempool_transactions: Vec<MempoolTransaction> = convert_to_mempool_transaction(transactions);
        write_to_database(mempool_transactions);
        sleep(Duration::from_secs(10));
    }
}

fn write_to_database(mempool_transactions: Vec<MempoolTransaction>) {
    let mut database_client: Client = connect_to_database().unwrap();
    for mempool_transaction in mempool_transactions {
        let query: &str = "INSERT INTO mempool_information (transaction_id, box_id) VALUES ($1, $2)";
        database_client.execute(query, &[&mempool_transaction.transaction_id, &mempool_transaction.box_id]).unwrap();
    }
}

fn convert_to_mempool_transaction(transactions: Vec<Value>) -> Vec<MempoolTransaction> {
    let mut mempool_transactions: Vec<MempoolTransaction> = Vec::new();
    for transaction in transactions {
        let transaction_id: String = transaction["transactionId"].as_str().unwrap().to_owned();
        let box_id: String = transaction["boxId"].as_str().unwrap().to_owned();
        let mempool_transaction: MempoolTransaction = MempoolTransaction {
            transaction_id,
            box_id
        };
        mempool_transactions.push(mempool_transaction);
    }
    return mempool_transactions;
}

fn connect_to_database() -> Result<Client> {
    let client: Client = Client::connect(DATABASE_PATH, NoTls)?;
    Ok(client)
}

fn get_all_transactions() -> Result<Vec<Value>> {
    let url: String = format!("{}/transactions/unconfirmed/outputs/byErgoTree", NODE_BASE_URL);
    let response: Response = reqwest::blocking::Client::new()
        .post(url)
        .header("Content-Type", "application/json")
        .header("accept", "application/json")
        .body(format!("\"{}\"", PROXY_CONTRACT_ERGO_TREE))
        .send()?;
    let body_string: String = response.text()?;
    let response_body: Value = serde_json::from_str(&body_string).unwrap();
    let transactions: Vec<Value> = response_body.as_array().unwrap().to_owned();
    return Ok(transactions);
}

fn create_database_schema() {
    let mut database_client: Client = connect_to_database().unwrap();
    let query: &str = "CREATE TABLE IF NOT EXISTS pending_registrations (
        transaction_id VARCHAR(64) PRIMARY KEY,
        box_id VARCHAR(64) NOT NULL
    );";
    database_client.execute(query, &[]).unwrap();
}