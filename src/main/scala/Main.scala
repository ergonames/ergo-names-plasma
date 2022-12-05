import io.getblok.getblok_plasma.{PlasmaParameters, ByteConversion}
import io.getblok.getblok_plasma.collections.{OpResult, PlasmaMap, Proof, ProvenResult}
import io.getblok.getblok_plasma.ByteConversion.convertsLongVal
import org.ergoplatform.appkit.ErgoId
import sigmastate.AvlTreeFlags
import io.getblok.getblok_plasma.PlasmaKey
import scorex.crypto.hash.{Blake2b256, Digest32}

object Main {

  case class ErgonameHash(base16str: String) {
    def toBytes: Array[Byte] = {
      val digest: Digest32 = Blake2b256.hash(base16str)
      val bytes: Array[Byte] = digest.toArray
      bytes
    }
  }

  object ErgonameHash {
    implicit val ergonameHashConversion: ByteConversion[ErgonameHash] = new ByteConversion[ErgonameHash] {
      override def convertToBytes(t: ErgonameHash): Array[Byte] = t.toBytes

      override def convertFromBytes(bytes: Array[Byte]): ErgonameHash = ErgonameHash(new String(bytes))
    }
  }

  def main(args: Array[String]): Unit = {
    val tokenMap = new PlasmaMap[ErgonameHash, ErgoId](AvlTreeFlags.AllOperationsAllowed, PlasmaParameters.default)

    val ergoname: String = convertToBase16("balb")
    val ergonameHash: ErgonameHash = ErgonameHash(ergoname)
    val tokenId: ErgoId = ErgoId.create("0cd8c9f416e5b1ca9f986a7f10a84191dfb85941619e49e53c0dc30ebf83324b")

    val ergonameData: Seq[(ErgonameHash, ErgoId)] = Seq(ergonameHash -> tokenId)
    val result: ProvenResult[ErgoId] = tokenMap.insert(ergonameData: _*)
    val opResults: Seq[OpResult[ErgoId]] = result.response
    val proof: Proof = result.proof
    println(tokenMap.lookUp(ergonameHash))
  }

  def convertToBase16(str: String): String = {
    str.getBytes.map("%02x".format(_)).mkString
  }

}