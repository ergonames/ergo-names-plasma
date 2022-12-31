package scenarios

import org.rocksdb._
import utils.DatabaseUtils

object TestDatabase {

    def main(args: Array[String]): Unit = {
        val initialTransactionId = "d55409dc8823b8c2a69196f6fb8715e2ed7ab637f4fc8b668624a8a92e5550a9"
        val db = RocksDB.open(DatabaseUtils.getDatabaseOptions(), DatabaseUtils.getDatabasePath())
        val info = DatabaseUtils.readFromDatabase(initialTransactionId)
        println(info)
    }
}