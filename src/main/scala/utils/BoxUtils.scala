package utils

import org.ergoplatform.restapi.client.{Asset, ErgoTransactionOutput, Registers}
import org.ergoplatform.explorer.client.{ExplorerApiClient, DefaultApi}
import org.ergoplatform.explorer.client.model.OutputInfo
import org.ergoplatform.appkit.impl.ScalaBridge
import java.util
import scala.collection.JavaConversions._
import org.ergoplatform.ErgoBox

object BoxUtils {

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