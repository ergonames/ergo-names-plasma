package scenarios

import utils.ErgoScriptContract

import org.ergoplatform.appkit._
import org.ergoplatform.appkit.config.ErgoToolConfig

object GetContractAddress {

    def main(args: Array[String]): Unit = {
        val contract = ErgoScriptContract("src/main/resources/contract.ergoscript").loadContract()

        val toolConfig = ErgoToolConfig.load("config.json")
        val nodeConfig = toolConfig.getNode()
        val ergoClient = RestApiErgoClient.create(nodeConfig, RestApiErgoClient.defaultMainnetExplorerUrl)
        val addr = ergoClient.execute((ctx: BlockchainContext) => {
            val compiledContract = ctx.compileContract(
            ConstantsBuilder.empty(),
            contract
            )
            val contractAddress = Address.fromErgoTree(compiledContract.getErgoTree, ctx.getNetworkType)
            contractAddress
        })
        println("Contract Address: " + addr)
    }
}