package scenarios

import utils.{ErgoScriptContract, RegistrySyncEngine}
import types.{ErgoName, ErgoNameHash}

import io.getblok.getblok_plasma.{PlasmaParameters, ByteConversion}
import io.getblok.getblok_plasma.collections.{OpResult, PlasmaMap, Proof, ProvenResult}
import org.ergoplatform.appkit._
import org.ergoplatform.appkit.config.ErgoToolConfig
import sigmastate.AvlTreeFlags
import scorex.crypto.hash.Blake2b256
import org.ergoplatform.explorer.client.{ExplorerApiClient, DefaultApi}
import scorex.util.encode.Base58
import sigmastate.serialization.ErgoTreeSerializer
import scorex.util.encode.Base64

object SubmitMintRequest {

    def main(args: Array[String]): Unit = {
        val txInfo = submitMintRequestScenario("config.json")
        println(txInfo)
    }

    def submitMintRequestScenario(configFilePath: String): String = {
        val proxyContract = ErgoScriptContract("src/main/resources/ProxyContract.ergoscript").loadContract()
        val mintContract = ErgoScriptContract("src/main/resources/MintingContract.ergoscript").loadContract()

        val toolConfig = ErgoToolConfig.load(configFilePath)
        val nodeConfig = toolConfig.getNode()

        val configParameters = toolConfig.getParameters()
        val explorerApiUrl = configParameters.get("explorerApiUrl")
        val initialTxId = configParameters.get("initialTxId")
        val ergoNameToRegister = configParameters.get("ergoNameToRegister")
        val liveModeRaw = configParameters.get("liveMode")
        var liveMode = false
        if (liveModeRaw == "true") {
            liveMode = true
        }

        val ergoClient = RestApiErgoClient.create(nodeConfig, explorerApiUrl)
        val explorerClient = new ExplorerApiClient(explorerApiUrl).createService(classOf[DefaultApi])
        val txId = ergoClient.execute((ctx: BlockchainContext) => {
            val prover = ctx.newProverBuilder
                .withMnemonic(
                    SecretString.create(nodeConfig.getWallet.getMnemonic),
                    SecretString.create(nodeConfig.getWallet.getPassword)
                )
                .withEip3Secret(0)
                .build()

            val senderAddress = prover.getEip3Addresses().get(0)

            val compiledProxyContract = ctx.compileContract(
                ConstantsBuilder.empty(),
                proxyContract
            )

            val contractAddress = Address.fromErgoTree(compiledProxyContract.getErgoTree, ctx.getNetworkType)

            val walletBoxes = BoxOperations.createForSender(senderAddress, ctx).withInputBoxesLoader(new ExplorerAndPoolUnspentBoxesLoader()).loadTop()
            
            val receiverAddress = Address.create(configParameters.get("receiverAddress"))

            val requestBox = ctx.newTxBuilder().outBoxBuilder()
                .value(Parameters.MinChangeValue + Parameters.MinFee)
                .contract(compiledProxyContract)
                .registers(
                    ErgoValue.of(ergoNameToRegister.getBytes()),
                    ErgoValue.of(receiverAddress.getErgoAddress().script.bytes)
                )
                .build()

            val tx = ctx.newTxBuilder()
                .boxesToSpend(walletBoxes)
                .outputs(requestBox)
                .fee(Parameters.MinFee)
                .sendChangeTo(senderAddress.getErgoAddress())
                .build()
            
            val signed = prover.sign(tx)
            val txId = signed.toJson(true)
            if (liveMode) {
                ctx.sendTransaction(signed)
            }
            txId
        })
        txId
    }
}