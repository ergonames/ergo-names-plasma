package utils

import scenarios.UpdateRegistry.{ErgoNameHash, ErgoName}

import io.getblok.getblok_plasma.{PlasmaParameters, ByteConversion}
import io.getblok.getblok_plasma.collections.{OpResult, PlasmaMap, Proof, ProvenResult}
import sigmastate.AvlTreeFlags
import scorex.crypto.hash.Blake2b256
import org.ergoplatform.appkit._
import org.ergoplatform.explorer.client.{ExplorerApiClient, DefaultApi}

object RegistrySync {

    def syncRegistry(initialTransactionId: String, explorerClient: DefaultApi): PlasmaMap[ErgoNameHash, ErgoId] = {
        val initialRegistry = syncInitial()
        val registryMap = syncUpdates(initialTransactionId, explorerClient, initialRegistry)
        registryMap
    }

    def syncInitial(): PlasmaMap[ErgoNameHash, ErgoId] = {
        val registryMap = new PlasmaMap[ErgoNameHash, ErgoId](AvlTreeFlags.AllOperationsAllowed, PlasmaParameters.default)
        return registryMap
    }

    def syncUpdates(spentTransactionId: String, explorerClient: DefaultApi, registry: PlasmaMap[ErgoNameHash, ErgoId]): PlasmaMap[ErgoNameHash, ErgoId] = {
        val transactionInfo = explorerClient.getApiV1TransactionsP1(spentTransactionId).execute().body()
        val transactionOutputs = transactionInfo.getOutputs()
        val registryUpdateOutput = transactionOutputs.get(0)
        val registryUpdateRegisters = registryUpdateOutput.getAdditionalRegisters()
        val ergonameRegistered = registryUpdateRegisters.get(5)
        val tokenIdRegistered = registryUpdateRegisters.get(6)
        println(ergonameRegistered)
        println(tokenIdRegistered)
        registry
    }

}