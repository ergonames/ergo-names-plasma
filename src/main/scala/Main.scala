import io.getblok.getblok_plasma.{PlasmaParameters, ByteConversion}
import io.getblok.getblok_plasma.collections.{OpResult, PlasmaMap, Proof, ProvenResult}
import org.ergoplatform.appkit.ErgoId
import sigmastate.AvlTreeFlags
import scorex.crypto.hash.Blake2b256

object Main {

  case class ErgoName(name: String) {
    def toErgoNameHash: ErgoNameHash = ErgoNameHash(Blake2b256.hash(name.getBytes("UTF-8")))
  }

  case class ErgoNameHash(hashedName: Array[Byte])

  implicit val nameConversion: ByteConversion[ErgoNameHash] = new ByteConversion[ErgoNameHash] {
    override def convertToBytes(t: ErgoNameHash): Array[Byte] = t.hashedName

    override def convertFromBytes(bytes: Array[Byte]): ErgoNameHash = ErgoNameHash(bytes)
  }

  def main(args: Array[String]): Unit = {
    val tokenMap = new PlasmaMap[ErgoNameHash, ErgoId](AvlTreeFlags.AllOperationsAllowed, PlasmaParameters.default)

    val ergoname: ErgoNameHash = ErgoName("balb").toErgoNameHash
    val tokenId: ErgoId = ErgoId.create("0cd8c9f416e5b1ca9f986a7f10a84191dfb85941619e49e53c0dc30ebf83324b")

    val ergonameData: Seq[(ErgoNameHash, ErgoId)] = Seq(ergoname -> tokenId)
    val result: ProvenResult[ErgoId] = tokenMap.insert(ergonameData: _*)
    val opResults: Seq[OpResult[ErgoId]] = result.response
    val proof: Proof = result.proof
    println(tokenMap.lookUp(ergoname))
  }

}