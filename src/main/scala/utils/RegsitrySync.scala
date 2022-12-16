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
import org.ergoplatform.restapi.client.{Asset, ErgoTransactionOutput, Registers}
import special.collection.CollOverArray
import org.ergoplatform.explorer.client.model.TransactionInfo

import org.ergoplatform.appkit.impl.ScalaBridge
import java.util
import scala.collection.JavaConversions._

object RegistrySync {

    def syncRegistry(initialTransactionId: String, explorerClient: DefaultApi): PlasmaMap[ErgoNameHash, ErgoId] = {
        val initialRegistry = syncInitial()
        val firstInsertionTransactionid = getFirstSpentTransactionId(initialTransactionId, explorerClient)
        val registryMap = syncUpdates(firstInsertionTransactionid, explorerClient, initialRegistry)
        val secMap = syncUpdates("b049092a6dbe55aa1c943e0fc9121a889e7446cfd0b187b6d4a0f6a13fc991da", explorerClient, registryMap)
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

    def convertOutputInfoToErgoBox(box: OutputInfo): ErgoBox = {
        val tokens = new util.ArrayList[Asset](box.getAssets.size)
        for (asset <- box.getAssets) {
        tokens.add(new Asset().tokenId(asset.getTokenId).amount(asset.getAmount))
        }
        val registers = new Registers
        for (registerEntry <- box.getAdditionalRegisters.entrySet) {
        registers.put(registerEntry.getKey, registerEntry.getValue.serializedValue)
        }
        val boxConversion: ErgoTransactionOutput = new ErgoTransactionOutput()
            .ergoTree(box.getErgoTree)
            .boxId(box.getBoxId)
            .index(box.getIndex)
            .value(box.getValue)
            .transactionId(box.getTransactionId)
            .creationHeight(box.getCreationHeight)
            .assets(tokens)
            .additionalRegisters(registers)
        ScalaBridge.isoErgoTransactionOutput.to(boxConversion)
    }

}