import requests
from os.path import exists
import os
import json


def setup():
    if not exists('../db/'):
        os.mkdir('../db/')
        setup()
    if not exists('../db/txs.json'):
        open('../db/txs.json', 'w')
        setup()


def getConfig():
    filePath = '../plasma/config.json'
    configJson = json.load(open(filePath))
    parameters = configJson['parameters']
    defaultExplorerApiUrl = parameters['defaultExplorerApiUrl']
    initialTxId = parameters['initialTxId']
    return [defaultExplorerApiUrl, initialTxId]
    

def sync(defaultExplorerApiUrl, initialTxId):
    url = defaultExplorerApiUrl + 'api/v1/transactions/' + initialTxId
    response = requests.get(url)
    tx = response.json()
    txId = tx['id']
    registryOutput = tx['outputs'][0]
    additionalRegisters = registryOutput['additionalRegisters']
    spentTransactionId = registryOutput['spentTransactionId']
    txData = { "transactionId": txId, "additionalRegisters": additionalRegisters, "boxId": registryOutput['boxId'], "spentTransactionId": spentTransactionId }
    txs = json.load(open('../db/txs.json'))
    txs.append(txData)
    json.dump(txs, open('../db/txs.json', 'w'))
    if spentTransactionId is not None:
        sync(defaultExplorerApiUrl, spentTransactionId)


def main():
    setup()
    config = getConfig()
    defaultExplorerApiUrl = config[0]
    initialTxId = config[1]
    sync(defaultExplorerApiUrl, initialTxId)

if __name__ == '__main__':
    main()