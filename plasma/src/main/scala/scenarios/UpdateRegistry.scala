package scenarios

import utils.{ErgoScriptContract, RegistrySync}

import io.getblok.getblok_plasma.{PlasmaParameters, ByteConversion}
import io.getblok.getblok_plasma.collections.{OpResult, PlasmaMap, Proof, ProvenResult}
import org.ergoplatform.appkit._
import org.ergoplatform.appkit.config.ErgoToolConfig
import sigmastate.AvlTreeFlags
import scorex.crypto.hash.Blake2b256

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

    val ergoClient = RestApiErgoClient.create(nodeConfig, RestApiErgoClient.defaultTestnetExplorerUrl)
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

      val rawBoxes = ctx.getBoxesById(mostRecentBoxId)
      var boxesToSpend: java.util.List[InputBox] = new java.util.ArrayList[InputBox]()
      for (box <- rawBoxes) {
        boxesToSpend.add(box)
      }
      val spendBox = boxesToSpend.get(0)
      val registers = spendBox.getRegisters()
      val registry = registers.get(0)
      println(registry.getType())

      val tokenMap = RegistrySync.syncRegistry(initialTxId, defaultTestnetExplorerUrl)
      val ergoname: ErgoNameHash = ErgoName("test").toErgoNameHash
      val tokenId: ErgoId = ErgoId.create("0cd8c9f416e5b1ca9f986a7f10a84191dfb85941619e49e53c0dc30ebf83324b")
      val ergonameData: Seq[(ErgoNameHash, ErgoId)] = Seq(ergoname -> tokenId)
      val result: ProvenResult[ErgoId] = tokenMap.insert(ergonameData: _*)
      val opResults: Seq[OpResult[ErgoId]] = result.response
      val proof: Proof = result.proof
      
      val outBox = ctx.newTxBuilder.outBoxBuilder
        .value(Parameters.MinChangeValue)
        .contract(compiledContract)
        .registers(
          ErgoValue.of(ergoname.hashedName),
          ErgoValue.of(tokenId.getBytes),
          proof.ergoValue
        )
        .build()

      val tx = ctx.newTxBuilder
        .boxesToSpend(boxesToSpend)
        .outputs(outBox)
        .fee(Parameters.MinFee)
        .sendChangeTo(contractAddress.getErgoAddress())
        .build()

      val signed = prover.sign(tx)
      val txId = signed.toJson(true)
      txId
      // val txId = ctx.sendTransaction(signed)
      // txId
    })
    println(txId)
  }

}