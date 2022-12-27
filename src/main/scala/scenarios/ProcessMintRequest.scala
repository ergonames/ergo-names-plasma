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
import special.collection.Coll

object ProcessMintRequest {

  def main(args: Array[String]): Unit = {
      val proxyBoxToSpendId = "e2f4589b352110b883a7decdef25a9906a8b9d7635c1d5b387332eb4ad008c96"
      val txInfo = processMintRequestScenario("config.json", proxyBoxToSpendId)
      println(txInfo)
  }

  def processMintRequestScenario(configFilePath: String, proxyBoxToSpendId: String): String = {
    val contract = ErgoScriptContract("src/main/resources/MintingContract.ergoscript").loadContract()

    val toolConfig = ErgoToolConfig.load("config.json")
    val nodeConfig = toolConfig.getNode()

    val configParameters = toolConfig.getParameters()
    val defaultTestnetExplorerUrl = configParameters.get("defaultTestnetExplorerUrl")
    val initialTxId = configParameters.get("initialTxId")
    val liveModeRaw = configParameters.get("liveMode")
    var liveMode = false
    if (liveModeRaw == "true") {
      liveMode = true
    }

    val ergoClient = RestApiErgoClient.create(nodeConfig, RestApiErgoClient.defaultTestnetExplorerUrl)
    val explorerClient = new ExplorerApiClient(RestApiErgoClient.defaultTestnetExplorerUrl).createService(classOf[DefaultApi])
    val txId = ergoClient.execute((ctx: BlockchainContext) => {
      val prover = ctx.newProverBuilder
        .withMnemonic(
          SecretString.create(nodeConfig.getWallet.getMnemonic),
          SecretString.create(nodeConfig.getWallet.getPassword)
        )
        .withEip3Secret(0)
        .build()
      
      val senderAddress = prover.getEip3Addresses().get(0)

      val compiledContract = ctx.compileContract(
        ConstantsBuilder.empty(),
        contract
      )
      val contractAddress = Address.fromErgoTree(compiledContract.getErgoTree, ctx.getNetworkType)

      val mostRecentTransactionId = RegistrySyncEngine.getMostRecentTransactionId(initialTxId, explorerClient)
      val registryEmpty = RegistrySyncEngine.checkIfRegistryIsEmpty(initialTxId, explorerClient)
      var mostRecentBoxId = ""
      if (registryEmpty) {
        mostRecentBoxId = RegistrySyncEngine.getOutputZeroBoxIdFromTransactionId(mostRecentTransactionId, explorerClient)
      } else {
        mostRecentBoxId = RegistrySyncEngine.getOutputOneBoxIdFromTransactionId(mostRecentTransactionId, explorerClient)
      }

      val contractBoxes = ctx.getBoxesById(mostRecentBoxId)
      val contractBox = contractBoxes(0)
      val contractBoxRegisters = contractBox.getRegisters()
      val registry = contractBoxRegisters.get(0)

      val proxyBoxes = ctx.getBoxesById(proxyBoxToSpendId)
      val proxyBox = proxyBoxes(0)
      val proxyBoxRegisters = proxyBox.getRegisters()
      val proxyBoxErgoNameRaw = proxyBoxRegisters.get(0)
      val proxyBoxReceiverAddressRaw = proxyBoxRegisters.get(1)

      val ergonameToRegisterBytes = proxyBoxErgoNameRaw.getValue.asInstanceOf[Coll[Byte]].toArray
      val ergoNameToRegister = new String(ergonameToRegisterBytes)

      val proxyBoxReceiverAddressBytes = proxyBoxReceiverAddressRaw.getValue.asInstanceOf[Coll[Byte]].toArray
      val proxyBoxReceiverAddress = Address.fromPropositionBytes(ctx.getNetworkType(), proxyBoxReceiverAddressBytes)

      val tokenMap: PlasmaMap[ErgoNameHash, ErgoId] = RegistrySyncEngine.syncFromLocal()
      val ergoname: ErgoNameHash = ErgoName(ergoNameToRegister).toErgoNameHash
      val tokenId: ErgoId = contractBox.getId()
      val ergonameData: Seq[(ErgoNameHash, ErgoId)] = Seq(ergoname -> tokenId)
      val result: ProvenResult[ErgoId] = tokenMap.insert(ergonameData: _*)
      val opResults: Seq[OpResult[ErgoId]] = result.response
      val proof: Proof = result.proof

      val contractBoxWithContextVars = contractBox.withContextVars(
        ContextVar.of(0.toByte, ErgoValue.of(ergoname.hashedName)),
        ContextVar.of(1.toByte, proof.ergoValue)
      )

      val boxesToSpend: java.util.List[InputBox] = new java.util.ArrayList[InputBox]()

      val walletBoxes = ctx.getUnspentBoxesFor(senderAddress, 0, 20)
      boxesToSpend.add(contractBoxWithContextVars)
      boxesToSpend.add(proxyBox)
      boxesToSpend.addAll(walletBoxes)
      
      val tokenToMint = new Eip4Token(tokenId.toString(), 1L, ergoNameToRegister, "test ergoname token", 0)

      val mintOutputBox = ctx.newTxBuilder.outBoxBuilder
        .value(Parameters.MinChangeValue)
        .contract(proxyBoxReceiverAddress.toErgoContract())
        .mintToken(tokenToMint)
        .build()

      val registryOutputBox = ctx.newTxBuilder().outBoxBuilder
        .value(Parameters.MinChangeValue)
        .contract(compiledContract)
        .registers(
          tokenMap.ergoValue
        )
        .build()
      
      val tx = ctx.newTxBuilder
        .boxesToSpend(boxesToSpend)
        .outputs(mintOutputBox, registryOutputBox)
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