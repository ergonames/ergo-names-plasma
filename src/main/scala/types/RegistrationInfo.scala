package types

import org.ergoplatform.appkit._

case class RegistrationInfo(
    registrationNumber: Int,
    ergoNameHash: ErgoNameHash,
    tokenId: ErgoId,
    transactionId: String,
)

object RegistrationInfo {

    def fromBytes(bytes: Array[Byte]): RegistrationInfo = {
        val registrationNumberBytes = bytes.slice(0, 4)
        val registrationNumber = java.nio.ByteBuffer.wrap(registrationNumberBytes).getInt
        val ergoNameHashBytes = bytes.slice(4, 36)
        val ergoNameHash = ErgoNameHash(ergoNameHashBytes)
        val tokenIdBytes = bytes.slice(36, 68)
        val tokenIdStr = tokenIdBytes.map("%02x" format _).mkString
        val tokenId = ErgoId.create(tokenIdStr)
        val transactionIdBytes = bytes.slice(68, bytes.length)
        val transactionId = transactionIdBytes.map("%02x" format _).mkString
        RegistrationInfo(registrationNumber, ergoNameHash, tokenId, transactionId)
    }

    def toBytes(info: RegistrationInfo): Array[Byte] = {
        val registrationNumberBytes = java.nio.ByteBuffer.allocate(4).putInt(info.registrationNumber).array()
        val ergoNameHashBytes = info.ergoNameHash.hashedName
        val tokenIdBytes = info.tokenId.getBytes
        val transactionIdBytes = info.transactionId.getBytes("UTF-8")
        registrationNumberBytes ++ ergoNameHashBytes ++ tokenIdBytes ++ transactionIdBytes
    }

}