package utils

import scenarios.UpdateRegistry.{ErgoNameHash, ErgoName}
import utils.BoxUtils.convertOutputInfoToErgoBox

import io.getblok.getblok_plasma.{PlasmaParameters, ByteConversion}
import io.getblok.getblok_plasma.collections.{OpResult, PlasmaMap, Proof, ProvenResult}
import sigmastate.AvlTreeFlags
import scorex.crypto.hash.Blake2b256
import org.ergoplatform.appkit._
import org.ergoplatform.explorer.client.{ExplorerApiClient, DefaultApi}
import org.ergoplatform.explorer.client.model.OutputInfo
import org.ergoplatform.ErgoBox
import special.collection.CollOverArray
import org.ergoplatform.explorer.client.model.TransactionInfo

object RegistrySync {

    def syncRegistry(initialTransactionId: String, explorerClient: DefaultApi): PlasmaMap[ErgoNameHash, ErgoId] = {
        val registry = syncEmptyRegistry()
        val isEmpty = checkIfRegistryIsEmpty(initialTransactionId, explorerClient)
        if (isEmpty) {
            return registry
        }
        var spentTransactionId = getBoxSpentTransactionId(initialTransactionId, explorerClient)
        while (spentTransactionId != null) {
            syncUpdates(spentTransactionId, explorerClient, registry)
            spentTransactionId = getBoxSpentTransactionId(spentTransactionId, explorerClient)
        }
        registry
    }

    def syncEmptyRegistry(): PlasmaMap[ErgoNameHash, ErgoId] = {
        val registry = new PlasmaMap[ErgoNameHash, ErgoId](AvlTreeFlags.AllOperationsAllowed, PlasmaParameters.default)
        registry
    }

    def checkIfRegistryIsEmpty(initialTransactionId: String, explorerClient: DefaultApi): Boolean = {
        val spentTransactionId = getBoxSpentTransactionId(initialTransactionId, explorerClient)
        if (spentTransactionId == null) {
            true
        } else {
            false
        }
    }

    def getBoxSpentTransactionId(transactionId: String, explorerClient: DefaultApi): String = {
        val transactionInfo = explorerClient.getApiV1TransactionsP1(transactionId).execute().body()
        val transactionOutputs = transactionInfo.getOutputs()
        val registryUpdateOutput = transactionOutputs.get(0)
        val spentTransactionId = registryUpdateOutput.getSpentTransactionId()
        spentTransactionId
    }

    def syncUpdates(spentTransactionId: String, explorerClient: DefaultApi, registry: PlasmaMap[ErgoNameHash, ErgoId]): PlasmaMap[ErgoNameHash, ErgoId] = { 
        val transactionInfo = explorerClient.getApiV1TransactionsP1(spentTransactionId).execute().body()
        val transactionOutputs = transactionInfo.getOutputs()
        val registryBoxId = transactionOutputs.get(0).getBoxId()
        val registryBox = explorerClient.getApiV1BoxesP1(registryBoxId).execute().body()
        val registryErgoBoxType = convertOutputInfoToErgoBox(registryBox)
        val R5_ergoname = registryErgoBoxType.additionalRegisters(ErgoBox.R5).value.asInstanceOf[CollOverArray[Byte]].toArray
        val ergoname: ErgoNameHash = ErgoNameHash(R5_ergoname)
        val R6_tokenIdBytes = registryErgoBoxType.additionalRegisters(ErgoBox.R6).value.asInstanceOf[CollOverArray[Byte]].toArray
        val R6_tokenId = R6_tokenIdBytes.map(_.toByte).map("%02x" format _).mkString
        val tokenId = ErgoId.create(R6_tokenId)
        val ergonameData: Seq[(ErgoNameHash, ErgoId)] = Seq(ergoname -> tokenId)
        val result: ProvenResult[ErgoId] = registry.insert(ergonameData: _*)
        registry
    }

    def getMostRecentTransactionId(initialTransactionId: String, explorerClient: DefaultApi): String = {
        var spentTransactionId = getBoxSpentTransactionId(initialTransactionId, explorerClient)
        if (spentTransactionId == null) {
            return initialTransactionId
        }
        var spentIdToReturn = spentTransactionId
        while (spentTransactionId != null) {
            spentTransactionId = getBoxSpentTransactionId(spentTransactionId, explorerClient)
            if (spentTransactionId != null) {
                spentIdToReturn = spentTransactionId
            }
        }
        spentIdToReturn
    }

    def getOutputZeroBoxIdFromTransactionId(transactionId: String, explorerClient: DefaultApi): String = {
        if (transactionId == null) {
            return null
        }
        val transactionInfo = explorerClient.getApiV1TransactionsP1(transactionId).execute().body()
        val transactionOutputs = transactionInfo.getOutputs()
        val registryUpdateOutput = transactionOutputs.get(0)
        val registryBoxId = registryUpdateOutput.getBoxId()
        registryBoxId
    }

}