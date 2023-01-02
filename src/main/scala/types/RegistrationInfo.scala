package types

import org.ergoplatform.appkit._

case class RegistrationInfo(
    mintTransactionId: String,
    spentTransactionId: String,
    ergonameRegistered: ErgoNameHash,
    ergonameTokenId: ErgoId,
)