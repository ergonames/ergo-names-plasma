use anyhow::{Result};
use postgres::{Client, NoTls};
use reqwest::blocking::Response;
use serde_json::Value;
use std::{time::Duration, thread::sleep};

const DATABASE_PATH: &str = "postgresql://ergonames:ergonames@localhost:5432/ergonames";
const PROXY_CONTRACT_ERGO_TREE: &str = "1003040001010400d801d601b2a5730000d1eded7301e6c67201040e93c17201c1b2a4730200";
const NODE_BASE_URL: &str = "http://104.237.139.78:9052";

#[derive(Clone, Debug)]
struct MempoolTransaction {
    transaction_id: String,
    box_id: String
}

fn main() {
    create_pending_registrations_schema();
    create_pending_insertions_schema();
    loop {
        let proxy_transactions: Vec<Value> = get_pending_transactions_at_proxy_contract().unwrap();
        let mempool_transactions: Vec<MempoolTransaction> = convert_to_mempool_transaction(proxy_transactions);
        write_to_pending_regisrations_table(mempool_transactions);
        sleep(Duration::from_secs(10));
    }
}

fn write_to_pending_regisrations_table(mempool_transactions: Vec<MempoolTransaction>) {
    let mut database_client: Client = connect_to_database().unwrap();
    for mempool_transaction in mempool_transactions {
        let query: &str = "INSERT INTO pending_registrations (transaction_id, box_id) VALUES ($1, $2) ON CONFLICT DO NOTHING";
        database_client.execute(query, &[&mempool_transaction.transaction_id, &mempool_transaction.box_id]).unwrap();
    }
}

fn convert_to_mempool_transaction(transactions: Vec<Value>) -> Vec<MempoolTransaction> {
    let mut mempool_transactions: Vec<MempoolTransaction> = Vec::new();
    if transactions.len() == 0 {
        return mempool_transactions;
    }
    for transaction in transactions {
        let transaction_id: String = transaction["transactionId"].as_str().unwrap().to_owned();
        let box_id: String = match transaction["boxId"].as_str() {
            Some(box_id) => box_id.to_owned(),
            None => transaction["inputs"][1]["boxId"].as_str().unwrap().to_owned()
        };
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

fn get_pending_transactions_at_proxy_contract() -> Result<Vec<Value>> {
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

fn create_pending_registrations_schema() {
    let mut database_client: Client = connect_to_database().unwrap();
    let query: &str = "CREATE TABLE IF NOT EXISTS pending_registrations (
        transaction_id VARCHAR(64) PRIMARY KEY,
        box_id VARCHAR(64) NOT NULL
    );";
    database_client.execute(query, &[]).unwrap();
}

fn create_pending_insertions_schema() {
    let mut database_client: Client = connect_to_database().unwrap();
    let query: &str = "CREATE TABLE IF NOT EXISTS pending_insertions (
        mint_transaction_id VARCHAR(64) PRIMARY KEY,
        spent_transaction_id VARCHAR(64),
        ergoname_registered VARCHAR(64) NOT NULL,
        ergoname_token_id VARCHAR(64) NOT NULL
    );";
    database_client.execute(query, &[]).unwrap();
}