package scenarios

import utils.ErgoScriptContract

import org.ergoplatform.appkit._
import org.ergoplatform.appkit.config.ErgoToolConfig

object GetProxyContractAddress {

    def main(args: Array[String]): Unit = {
        val address = getProxyContractAddressScenario("config.json")
        println("Proxy Contract Address: " + address)
    }

    def getProxyContractAddressScenario(configFilePath: String): Address = {
        val proxyContract = ErgoScriptContract("src/main/resources/ProxyContract.ergoscript").loadContract()
        val mintContract = ErgoScriptContract("src/main/resources/MintingContract.ergoscript").loadContract()

        val toolConfig = ErgoToolConfig.load(configFilePath)
        val nodeConfig = toolConfig.getNode()
        val ergoClient = RestApiErgoClient.create(nodeConfig, RestApiErgoClient.defaultMainnetExplorerUrl)
        val contractAddress = ergoClient.execute((ctx: BlockchainContext) => {
            val compiledMintContract = ctx.compileContract(
                ConstantsBuilder.empty(),
                mintContract
            )
            val mintContractAddress = Address.fromErgoTree(compiledMintContract.getErgoTree, ctx.getNetworkType)
            val proxyContractConstats = ConstantsBuilder.create()
                .item("mintContractScript", mintContractAddress.toString())
                .build()
            
            val compiledProxyContract = ctx.compileContract(
                proxyContractConstats,
                proxyContract
            )
            val contractAddress = Address.fromErgoTree(compiledProxyContract.getErgoTree, ctx.getNetworkType)
            contractAddress
        })
        contractAddress
    }
}