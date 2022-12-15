package utils

import scenarios.UpdateRegistry.{ErgoNameHash, ErgoName}

import io.getblok.getblok_plasma.{PlasmaParameters, ByteConversion}
import io.getblok.getblok_plasma.collections.{OpResult, PlasmaMap, Proof, ProvenResult}
import sigmastate.AvlTreeFlags
import scorex.crypto.hash.Blake2b256
import org.ergoplatform.appkit._
import org.ergoplatform.explorer.client.{ExplorerApiClient, DefaultApi}

object RegistrySync {

    def syncRegistry(transactionId: String, explorerClient: DefaultApi): PlasmaMap[ErgoNameHash, ErgoId] = {
        val registryMap = new PlasmaMap[ErgoNameHash, ErgoId](AvlTreeFlags.AllOperationsAllowed, PlasmaParameters.default)
        val transactionInfo = explorerClient.getApiV1TransactionsP1(transactionId).execute().body()
        val transactionOutputs = transactionInfo.getOutputs()
        val outputZero = transactionOutputs.get(0)
        if (outputZero.getSpentTransactionId() != null) {
            syncRegistry(outputZero.getSpentTransactionId(), explorerClient)
        }
        registryMap
    }

}