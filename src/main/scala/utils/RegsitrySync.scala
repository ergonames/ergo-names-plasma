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

object RegistrySync {

    def syncRegistry(initialTransactionId: String, explorerClient: DefaultApi, ctx: BlockchainContext): PlasmaMap[ErgoNameHash, ErgoId] = {
        val initialRegistry = syncInitial()
        val firstInsertionTransactionid = getFirstSpentTransactionId(initialTransactionId, explorerClient)
        val registryMap = syncUpdates(firstInsertionTransactionid, explorerClient, initialRegistry, ctx)
        registryMap
    }

    def syncInitial(): PlasmaMap[ErgoNameHash, ErgoId] = {
        val registryMap = new PlasmaMap[ErgoNameHash, ErgoId](AvlTreeFlags.AllOperationsAllowed, PlasmaParameters.default)
        return registryMap
    }

    def getFirstSpentTransactionId(initialTransactionId: String, explorerClient: DefaultApi): String = {
        val transactionInfo = explorerClient.getApiV1TransactionsP1(initialTransactionId).execute().body()
        val transactionOutputs = transactionInfo.getOutputs()
        val registryUpdateOutput = transactionOutputs.get(0)
        val spentTransactionId = registryUpdateOutput.getSpentTransactionId()
        spentTransactionId
    }

    def syncUpdates(spentTransactionId: String, explorerClient: DefaultApi, registry: PlasmaMap[ErgoNameHash, ErgoId], ctx: BlockchainContext): PlasmaMap[ErgoNameHash, ErgoId] = { 
        val transactionInfo = explorerClient.getApiV1TransactionsP1(spentTransactionId).execute().body()
        val transactionOutputs = transactionInfo.getOutputs()
        val registryBoxId = transactionOutputs.get(0).getBoxId()
        val registryBox = ctx.getBoxesById(registryBoxId)(0)
        val registryBoxRegisters = registryBox.getRegisters()
        val R5_ergonameBytes = registryBoxRegisters.get(1).getValue().asInstanceOf[CollOverArray[Byte]].toArray
        val ergoname: ErgoNameHash = ErgoNameHash(R5_ergonameBytes)
        val R6_tokenId = registryBoxRegisters.get(2).getValue().asInstanceOf[CollOverArray[Byte]].toArray
        val R6_deserialized = R6_tokenId.map("%02x".format(_)).mkString
        val tokenId = ErgoId.create(R6_deserialized)
        val ergonameData: Seq[(ErgoNameHash, ErgoId)] = Seq(ergoname -> tokenId)
        val result: ProvenResult[ErgoId] = registry.insert(ergonameData: _*)
        registry
    }

}