package types

import org.ergoplatform.appkit._

case class RegistrationInfo(
    mintTransactionId: String,
    spentTransactionId: String,
    ergonameRegistered: ErgoNameHash,
    ergonameTokenId: ErgoId,
)

object RegistrationInfo {

    def fromBytes(bytes: Array[Byte]): RegistrationInfo = {
        val mintTransactionId = bytes.slice(0, 32).map("%02x".format(_)).mkString
        val spentTransactionId = bytes.slice(32, 64).map("%02x".format(_)).mkString
        val ergonameRegistered = ErgoNameHash(bytes.slice(64, 96))
        val ergonameTokenIdStr = bytes.slice(96, 128).map("%02x".format(_)).mkString
        val ergonameTokenId = ErgoId.create(ergonameTokenIdStr)
        RegistrationInfo(mintTransactionId, spentTransactionId, ergonameRegistered, ergonameTokenId)
    }

    def toBytes(info: RegistrationInfo): Array[Byte] = {
        val mintTransactionIdBytes = info.mintTransactionId.getBytes("UTF-8")
        val spentTransactionIdBytes = info.spentTransactionId.getBytes("UTF-8")
        val ergonameRegisteredBytes = info.ergonameRegistered.hashedName
        val ergonameTokenIdBytes = info.ergonameTokenId.getBytes
        mintTransactionIdBytes ++ spentTransactionIdBytes ++ ergonameRegisteredBytes ++ ergonameTokenIdBytes
    }

}