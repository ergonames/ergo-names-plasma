package scenarios

import utils.RegistrySyncEngine
import utils.DatabaseUtils

import org.ergoplatform.appkit._
import org.ergoplatform.explorer.client.{ExplorerApiClient, DefaultApi}
import org.ergoplatform.appkit.config.ErgoToolConfig
import io.getblok.getblok_plasma.collections.PlasmaMap
import types.ErgoNameHash
import types.RegistrationInfo

object SyncRegistry {

    def main(args: Array[String]): Unit = {
        syncRegistryScenario("config.json")
    }

    def syncRegistryScenario(configFilePath: String) {
        val toolConfig = ErgoToolConfig.load("config.json")
        val configParameters = toolConfig.getParameters()
        val initialTransactionId = configParameters.get("initialTxId")

        val explorerClient = new ExplorerApiClient(RestApiErgoClient.defaultTestnetExplorerUrl).createService(classOf[DefaultApi])
        
        val mostRecentTransactionId = RegistrySyncEngine.getMostRecentTransactionId(initialTransactionId, explorerClient)
        val registryEmpty = RegistrySyncEngine.checkIfRegistryIsEmpty(initialTransactionId, explorerClient)

        var spentTransactionId = RegistrySyncEngine.getBoxSpentTransactionId(initialTransactionId, explorerClient)
        var registrationNumber = 1
        while (spentTransactionId != null) {
            val ergonameHash = RegistrySyncEngine.getErgoNameHash(spentTransactionId, explorerClient)
            val tokenId = RegistrySyncEngine.getTokenId(spentTransactionId, explorerClient)
            val registrationInfo = RegistrationInfo(registrationNumber, ergonameHash, tokenId, spentTransactionId)
            DatabaseUtils.writeToDatabase(registrationInfo)
            spentTransactionId = RegistrySyncEngine.getBoxSpentTransactionId(spentTransactionId, explorerClient)
            registrationNumber += 1
        }
    }

}