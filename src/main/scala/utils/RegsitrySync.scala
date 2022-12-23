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
            val ergonameHash = getErgoNameHash(spentTransactionId, explorerClient)
            val tokenId = getTokenId(spentTransactionId, explorerClient)
            val ergonameData: Seq[(ErgoNameHash, ErgoId)] = Seq(ergonameHash -> tokenId)
            val result: ProvenResult[ErgoId] = registry.insert(ergonameData: _*)
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

    def getErgoNameHash(spentTransactionId: String, explorerClient: DefaultApi): ErgoNameHash = {
        val transactionInfo = explorerClient.getApiV1TransactionsP1(spentTransactionId).execute().body()
        val transactionOutputs = transactionInfo.getOutputs()
        val mintBoxId = transactionOutputs.get(0).getBoxId()
        val mintBox = explorerClient.getApiV1BoxesP1(mintBoxId).execute().body()
        val mintBoxType = convertOutputInfoToErgoBox(mintBox)
        val ergonameNameRaw = mintBoxType.additionalRegisters(ErgoBox.R4).value.asInstanceOf[CollOverArray[Byte]].toArray
        val ergonameNameBase16 = ergonameNameRaw.map(_.toByte).map("%02x" format _).mkString
        val ergonameName = new String(ergonameNameBase16.sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte))
        val ergoname: ErgoNameHash = ErgoName(ergonameName).toErgoNameHash
        ergoname
    }

    def getTokenId(spentTransactionId: String, explorerClient: DefaultApi): ErgoId = {
        val transactionInfo = explorerClient.getApiV1TransactionsP1(spentTransactionId).execute().body()
        val transactionOutputs = transactionInfo.getOutputs()
        val mintBoxId = transactionOutputs.get(0).getBoxId()
        val mintBox = explorerClient.getApiV1BoxesP1(mintBoxId).execute().body()
        val mintBoxType = convertOutputInfoToErgoBox(mintBox)
        val tokenIdRaw = mintBoxType.additionalTokens(0)._1.repr
        val tokenIdStr = tokenIdRaw.map(_.toByte).map("%02x" format _).mkString
        val tokenId = ErgoId.create(tokenIdStr)
        tokenId
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

    def getOutputOneBoxIdFromTransactionId(transactionId: String, explorerClient: DefaultApi): String = {
        if (transactionId == null) {
            return null
        }
        val transactionInfo = explorerClient.getApiV1TransactionsP1(transactionId).execute().body()
        val transactionOutputs = transactionInfo.getOutputs()
        val registryUpdateOutput = transactionOutputs.get(1)
        val registryBoxId = registryUpdateOutput.getBoxId()
        registryBoxId
    }

}