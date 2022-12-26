package scenarios

import utils.ErgoScriptContract

import org.ergoplatform.appkit._
import org.ergoplatform.appkit.config.ErgoToolConfig

object GetProxyContractAddress {

    def main(args: Array[String]): Unit = {
        getProxyContractAddressScenario("config.json")
    }

    def getProxyContractAddressScenario(configFilePath: String) {
        val proxyContract = ErgoScriptContract("src/main/resources/ProxyContract.ergoscript").loadContract()
        val mintContract = ErgoScriptContract("src/main/resources/MintingContract.ergoscript").loadContract()

        val toolConfig = ErgoToolConfig.load(configFilePath)
        val nodeConfig = toolConfig.getNode()
        val ergoClient = RestApiErgoClient.create(nodeConfig, RestApiErgoClient.defaultMainnetExplorerUrl)
        val addr = ergoClient.execute((ctx: BlockchainContext) => {
            val compiledMintContract = ctx.compileContract(
                ConstantsBuilder.empty(),
                mintContract
            )
            val mintContractAddress = Address.fromErgoTree(compiledMintContract.getErgoTree, ctx.getNetworkType)
            val mintContractPropositionBytes = mintContractAddress.toPropositionBytes()
            val proxyContractConstats = ConstantsBuilder.create()
                .item("mintContractPropositionBytes", mintContractPropositionBytes)
                .build()
            
            val compiledProxyContract = ctx.compileContract(
                proxyContractConstats,
                proxyContract
            )
            val contractAddress = Address.fromErgoTree(compiledProxyContract.getErgoTree, ctx.getNetworkType)
            contractAddress
        })
        println("Contract Address: " + addr)
    }
}