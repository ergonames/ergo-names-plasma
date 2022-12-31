package utils

import org.rocksdb._
import java.io.File
import types.RegistrationInfo

object DatabaseUtils {

    def readFromDatabase(mintTransactionId: String): RegistrationInfo = {
        var options = new Options().setCreateIfMissing(true)
        val db = RocksDB.open(options, getDatabasePath())
        val mintTransactionIdBytes = mintTransactionId.getBytes
        val registrationInfoBytes = db.get(mintTransactionIdBytes)
        val registrationInfo = RegistrationInfo.fromBytes(registrationInfoBytes)
        db.close()
        registrationInfo
    }

    def getDatabaseOptions(): Options = {
        val options = new Options().setCreateIfMissing(true)
        options
    }

    def getDatabasePath(): String = {
        val path = "db"
        path
    }

}