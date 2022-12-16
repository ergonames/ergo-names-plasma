package scenarios

import utils.{ErgoScriptContract, RegistrySync}

import io.getblok.getblok_plasma.{PlasmaParameters, ByteConversion}
import io.getblok.getblok_plasma.collections.{OpResult, PlasmaMap, Proof, ProvenResult}
import org.ergoplatform.appkit._
import org.ergoplatform.appkit.config.ErgoToolConfig
import sigmastate.AvlTreeFlags
import scorex.crypto.hash.Blake2b256
import org.ergoplatform.explorer.client.{ExplorerApiClient, DefaultApi}

object UpdateRegistry {

  case class ErgoName(name: String) {
    def toErgoNameHash: ErgoNameHash = ErgoNameHash(Blake2b256.hash(name.getBytes("UTF-8")))
  }

  case class ErgoNameHash(hashedName: Array[Byte])

  implicit val nameConversion: ByteConversion[ErgoNameHash] = new ByteConversion[ErgoNameHash] {    
    override def convertToBytes(t: ErgoNameHash): Array[Byte] = t.hashedName

    override def convertFromBytes(bytes: Array[Byte]): ErgoNameHash = ErgoNameHash(bytes)
  }

  def main(args: Array[String]): Unit = {
    val contract = ErgoScriptContract("src/main/resources/contract.ergoscript").loadContract()

    val toolConfig = ErgoToolConfig.load("config.json")
    val nodeConfig = toolConfig.getNode()

    val configParameters = toolConfig.getParameters()
    val defaultTestnetExplorerUrl = configParameters.get("defaultTestnetExplorerUrl")
    val initialTxId = configParameters.get("initialTxId")
    val initialBoxId = configParameters.get("initialBoxId")
    val mostRecentBoxId = configParameters.get("mostRecentBoxId")
    val ergoNameToRegister = configParameters.get("ergoNameToRegister")
    val tokenIdToRegister = configParameters.get("tokenIdToRegister")
    val serviceMode = configParameters.get("serviceMode")

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

      val contractBoxes = ctx.getBoxesById(mostRecentBoxId)
      val contractBox = contractBoxes(0)
      val registers = contractBox.getRegisters()
      val registry = registers.get(0)

      val tokenMap: PlasmaMap[ErgoNameHash, ErgoId] = RegistrySync.syncRegistry(initialTxId, explorerClient)
      val ergoname: ErgoNameHash = ErgoName(ergoNameToRegister).toErgoNameHash
      val tokenId: ErgoId = ErgoId.create(tokenIdToRegister)
      val ergonameData: Seq[(ErgoNameHash, ErgoId)] = Seq(ergoname -> tokenId)
      val result: ProvenResult[ErgoId] = tokenMap.insert(ergonameData: _*)
      val opResults: Seq[OpResult[ErgoId]] = result.response
      val proof: Proof = result.proof
      
      val outBox = ctx.newTxBuilder.outBoxBuilder
        .value(Parameters.MinChangeValue)
        .contract(compiledContract)
        .registers(
          tokenMap.ergoValue,
          ErgoValue.of(ergoname.hashedName),
          ErgoValue.of(tokenId.getBytes),
        )
        .build()

      val boxToSpend = contractBox.withContextVars(
        ContextVar.of(0.toByte, ErgoValue.of(ergoname.hashedName)),
        ContextVar.of(1.toByte, ErgoValue.of(tokenId.getBytes)),
        ContextVar.of(2.toByte, proof.ergoValue),
      )

      val boxesToSpend: java.util.List[InputBox] = new java.util.ArrayList[InputBox]()
      boxesToSpend.add(boxToSpend)

      val walletBoxes = ctx.getUnspentBoxesFor(senderAddress, 0, 20)
      boxesToSpend.addAll(walletBoxes)
      
      val tx = ctx.newTxBuilder
        .boxesToSpend(boxesToSpend)
        .outputs(outBox)
        .fee(Parameters.MinFee)
        .sendChangeTo(senderAddress.getErgoAddress())
        .build()

      val signed = prover.sign(tx)
      val txId = signed.toJson(true)
      println(txId)
      if (serviceMode == "live") {
        ctx.sendTransaction(signed)
      }
      txId
    })
    println(txId)
  }

}