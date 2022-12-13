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
    initialBoxId = parameters['initialBoxId']
    return [defaultExplorerApiUrl, initialTxId, initialBoxId]
    

def sync(defaultExplorerApiUrl, initialTxId, initialBoxId):
    print(defaultExplorerApiUrl)
    print(initialTxId)
    print(initialBoxId)

def main():
    setup()
    config = getConfig()
    defaultExplorerApiUrl = config[0]
    initialTxId = config[1]
    initialBoxId = config[2]
    sync(defaultExplorerApiUrl, initialTxId, initialBoxId)

if __name__ == '__main__':
    main()