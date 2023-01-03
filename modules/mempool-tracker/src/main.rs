use anyhow::{Result};
use postgres::{Client, NoTls};
use reqwest::blocking::Response;
use serde_json::Value;
use std::{time::Duration, thread::sleep};

const DATABASE_PATH: &str = "postgresql://ergonames:ergonames@localhost:5432/ergonames";
const PROXY_CONTRACT_ERGO_TREE: &str = "1003040001010400d801d601b2a5730000d1eded7301e6c67201040e93c17201c1b2a4730200";
const MINTING_CONTRACT_ERGO_TREE: &str = "100504000400040204020502d805d601b2a5730000d602b2db63087201730100d603c5a7d604b2a4730200d605b2a5730300d1edededed938c7202017203938c720202730493e4c67201040ee4c67204040e93c27201e4c67204050eed93db6401e4c672050464db6401e4dc640ce4c6a704640283013c0e0e8602e4e3000e7203e4e3010e93c27205c2a7";
const NODE_BASE_URL: &str = "http://104.237.139.78:9052";

#[derive(Clone, Debug)]
struct MempoolTransaction {
    transaction_id: String,
    box_id: String
}

fn main() {
    create_database_schema();
    loop {
        delete_all_pending_registrations();
        let proxy_transactions: Vec<Value> = get_pending_transactions_at_proxy_contract().unwrap();
        let mempool_transactions: Vec<MempoolTransaction> = convert_to_mempool_transaction(proxy_transactions);
        let all_transactions: Vec<Value> = get_all_pending_transactions_at_minting_contract().unwrap();
        let all_mempool_transactions: Vec<MempoolTransaction> = convert_to_mempool_transaction(all_transactions);
        let transactions_to_add: Vec<MempoolTransaction> = remove_spent(mempool_transactions, all_mempool_transactions.clone());
        let transactions_to_remove: Vec<MempoolTransaction> = transactions_to_remove(transactions_to_add.clone(), all_mempool_transactions.clone());
        remove_from_database(transactions_to_remove);
        write_to_database(transactions_to_add);
        sleep(Duration::from_secs(10));
    }
}

fn delete_all_pending_registrations() {
    let mut database_client: Client = connect_to_database().unwrap();
    let query: &str = "DELETE FROM pending_registrations";
    database_client.execute(query, &[]).unwrap();
}

fn remove_from_database(transactions: Vec<MempoolTransaction>) {
    let mut database_client: Client = connect_to_database().unwrap();
    for transaction in transactions {
        let query: &str = "DELETE FROM pending_registrations WHERE transaction_id = $1 AND box_id = $2";
        database_client.execute(query, &[&transaction.transaction_id, &transaction.box_id]).unwrap();
    }
}

fn transactions_to_remove(transactions: Vec<MempoolTransaction>, all_transactions: Vec<MempoolTransaction>) -> Vec<MempoolTransaction> {
    let mut transactions_to_remove: Vec<MempoolTransaction> = Vec::new();
    for transaction in all_transactions {
        for mempool_transaction in transactions.clone() {
            if mempool_transaction.box_id == transaction.box_id {
                transactions_to_remove.push(mempool_transaction);
            }
        }
    }
    return transactions_to_remove;
}

fn remove_spent(mut proxy_transactions: Vec<MempoolTransaction>, all_transactions: Vec<MempoolTransaction>) -> Vec<MempoolTransaction> {
    for transaction in all_transactions {
        proxy_transactions.retain(|x| x.box_id != transaction.box_id);
    }
    return proxy_transactions;
}

fn write_to_database(mempool_transactions: Vec<MempoolTransaction>) {
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

fn get_all_pending_transactions_at_minting_contract() -> Result<Vec<Value>> {
    let url: String = format!("{}/transactions/unconfirmed/outputs/byErgoTree", NODE_BASE_URL);
    let response: Response = reqwest::blocking::Client::new()
        .post(url)
        .header("Content-Type", "application/json")
        .header("accept", "application/json")
        .body(format!("\"{}\"", MINTING_CONTRACT_ERGO_TREE))
        .send()?;
    let body_string: String = response.text()?;
    let response_body: Value = serde_json::from_str(&body_string).unwrap();
    let transactions: Vec<Value> = response_body.as_array().unwrap().to_owned();
    return Ok(transactions);
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

fn create_database_schema() {
    let mut database_client: Client = connect_to_database().unwrap();
    let query: &str = "CREATE TABLE IF NOT EXISTS pending_registrations (
        transaction_id VARCHAR(64) PRIMARY KEY,
        box_id VARCHAR(64) NOT NULL
    );";
    database_client.execute(query, &[]).unwrap();
}