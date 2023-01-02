package scenarios

import java.sql.{Connection, DriverManager, ResultSet}
import utils.DatabaseUtils

object TestDatabase {

    def main(args: Array[String]): Unit = {
        val initialTransactionId = "e271e7cb9b9c7932546e8a5746c91cb1c0f1114ff173a90e1fe979170f71c579"
        val registrationInfo = DatabaseUtils.readFromDatabase(initialTransactionId)
        val registrationInfo2 = DatabaseUtils.readFromDatabase(registrationInfo.spentTransactionId)
        println(registrationInfo)
        println(registrationInfo2)
    }
}