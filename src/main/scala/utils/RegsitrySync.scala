package utils

import scenarios.UpdateRegistry.{ErgoNameHash, ErgoName}

import io.getblok.getblok_plasma.{PlasmaParameters, ByteConversion}
import io.getblok.getblok_plasma.collections.{OpResult, PlasmaMap, Proof, ProvenResult}
import sigmastate.AvlTreeFlags
import scorex.crypto.hash.Blake2b256
import org.ergoplatform.appkit._
import org.ergoplatform.explorer.client.{ExplorerApiClient, DefaultApi}
import org.ergoplatform.explorer.client.model.OutputInfo
import org.ergoplatform.ErgoBox
import org.ergoplatform.restapi.client.Registers
import special.collection.CollOverArray
import org.ergoplatform.explorer.client.model.TransactionInfo

object RegistrySync {

    def syncRegistry(initialTransactionId: String, explorerClient: DefaultApi): PlasmaMap[ErgoNameHash, ErgoId] = {
        val firstInsertionTransactionId = getFirstSpentTransactionId(initialTransactionId, explorerClient)
        val registry = syncLoop(firstInsertionTransactionId, explorerClient)
        registry
    }

    def syncInitial(): PlasmaMap[ErgoNameHash, ErgoId] = {
        val registry = new PlasmaMap[ErgoNameHash, ErgoId](AvlTreeFlags.AllOperationsAllowed, PlasmaParameters.default)
        return registry
    }

    def getFirstSpentTransactionId(initialTransactionId: String, explorerClient: DefaultApi): String = {
        val transactionInfo = explorerClient.getApiV1TransactionsP1(initialTransactionId).execute().body()
        val transactionOutputs = transactionInfo.getOutputs()
        val registryUpdateOutput = transactionOutputs.get(0)
        val spentTransactionId = registryUpdateOutput.getSpentTransactionId()
        spentTransactionId
    }

    def syncLoop(transactionId: String, explorerClient: DefaultApi): PlasmaMap[ErgoNameHash, ErgoId] = {
        val registry = new PlasmaMap[ErgoNameHash, ErgoId](AvlTreeFlags.AllOperationsAllowed, PlasmaParameters.default)
        var spentTransactionId = transactionId
        var lastSpentTransactionId = ""
        while (spentTransactionId != null) {
            val transactionInfo = getTransactionInfo(spentTransactionId, explorerClient)
            val ergoNameHashRegistered = getErgoNameHashRegistered(transactionInfo, explorerClient)
            val tokenIdRegistered = getTokenIdRegistered(transactionInfo, explorerClient)
            val ergonameData: Seq[(ErgoNameHash, ErgoId)] = Seq(ergoNameHashRegistered -> tokenIdRegistered)
            val result: ProvenResult[ErgoId] = registry.insert(ergonameData: _*)

            lastSpentTransactionId = spentTransactionId
            spentTransactionId = getSpentTransactionId(transactionInfo)
            if (lastSpentTransactionId == spentTransactionId) {
                spentTransactionId = null
            }
        }
        registry
    }

    def getTransactionInfo(transactionId: String, explorerClient: DefaultApi): TransactionInfo = {
        val transactionInfo = explorerClient.getApiV1TransactionsP1(transactionId).execute().body()
        transactionInfo
    }

    def getBoxInfo(boxId: String, explorerClient: DefaultApi): OutputInfo = {
        val boxInfo = explorerClient.getApiV1BoxesP1(boxId).execute().body()
        boxInfo
    }

    def getSpentTransactionId(transactionInfo: TransactionInfo): String = {
        val outputZero = transactionInfo.getOutputs().get(0)
        val spentTransactionId = outputZero.getSpentTransactionId()
        spentTransactionId
    }

    def getErgoNameHashRegistered(transactionInfo: TransactionInfo, explorerClient: DefaultApi): ErgoNameHash = {
        val transactionOutputs = transactionInfo.getOutputs()
        val registryBoxId = transactionOutputs.get(0).getBoxId()
        val registryBox: OutputInfo = getBoxInfo(registryBoxId, explorerClient)
        val registryBoxRegisters = registryBox.getAdditionalRegisters()
        val R5_ergoname = registryBoxRegisters.get("R5").renderedValue
        val R5_ergonameBytes = R5_ergoname.getBytes()
        val ergoname: ErgoNameHash = ErgoNameHash(R5_ergonameBytes)
        ergoname
    }

    def getTokenIdRegistered(transactionInfo: TransactionInfo, explorerClient: DefaultApi): ErgoId = {
        val transactionOutputs = transactionInfo.getOutputs()
        val registryBoxId = transactionOutputs.get(0).getBoxId()
        val registryBox: OutputInfo = getBoxInfo(registryBoxId, explorerClient)
        val registryBoxRegisters = registryBox.getAdditionalRegisters()
        val R6_tokenId = registryBoxRegisters.get("R6").renderedValue
        val tokenId = ErgoId.create(R6_tokenId)
        tokenId
    }

}