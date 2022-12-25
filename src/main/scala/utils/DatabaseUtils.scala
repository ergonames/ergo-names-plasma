package utils

import org.iq80.leveldb.{DB, Options}
import java.io.File
import types.RegistrationInfo

object DatabaseUtils {

    def writeToDatabase(registrationInfo: RegistrationInfo) {
        val db = org.iq80.leveldb.impl.Iq80DBFactory.factory.open(new File(getDatabasePath()), getDatabaseOptions())
        val registrationNumber = registrationInfo.registrationNumber
        val registrationNumberBytes: Array[Byte] = registrationNumber.toString.getBytes
        db.put(registrationNumberBytes, RegistrationInfo.toBytes(registrationInfo))
        db.close()
    }

    def readFromDatabase(registrationNumber: Int): RegistrationInfo = {
        val db = org.iq80.leveldb.impl.Iq80DBFactory.factory.open(new File(getDatabasePath()), getDatabaseOptions())
        val registrationNumberBytes: Array[Byte] = registrationNumber.toString.getBytes
        val registrationInfoBytes = db.get(registrationNumberBytes)
        val registrationInfo = RegistrationInfo.fromBytes(registrationInfoBytes)
        db.close()
        registrationInfo
    }

    def getDatabaseOptions(): Options = {
        val options = new Options()
        options.createIfMissing(true)
        options
    }

    def getDatabasePath(): String = {
        val path = "db"
        path
    }

    def getTotalKeys(): Int = {
        val db = org.iq80.leveldb.impl.Iq80DBFactory.factory.open(new File(getDatabasePath()), getDatabaseOptions())
        val iterator = db.iterator()
        var totalKeys = 0
        iterator.seekToFirst()
        while (iterator.hasNext()) {
            totalKeys += 1
            iterator.next()
        }
        db.close()
        totalKeys
    }

}