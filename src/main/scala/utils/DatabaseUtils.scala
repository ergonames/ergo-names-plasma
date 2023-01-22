package utils

import java.sql.{Connection, DriverManager, ResultSet}
import java.io.File
import types.RegistrationInfo
import types.ErgoNameHash
import org.ergoplatform.appkit.ErgoId
import types.ErgoName
import types.PendingRegistration

object DatabaseUtils {

    def readFromDatabase(mintTransactionId: String): RegistrationInfo = {
        val connection = DriverManager.getConnection(getDatabasePath())
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery(s"SELECT * FROM confirmed_registry_insertions WHERE mint_transaction_id = '$mintTransactionId'")
        if (resultSet.next()) {
            val registrationInfo = getRegistrationInfo(resultSet)
            connection.close()
            registrationInfo
        } else {
            connection.close()
            null
        }
    }

    def getPendingRegistrations(): Array[PendingRegistration] = {
        val connection = DriverManager.getConnection(getDatabasePath())
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery(s"SELECT * FROM pending_registrations")
        if (resultSet.next()) {
            val pendingRegistrations = getPendingRegistrationInfo(resultSet)
            connection.close()
            pendingRegistrations
        } else {
            connection.close()
            null
        }
    }

    def removePendingRegistration(transactionId: String, boxId: String): Unit = {
        val connection = DriverManager.getConnection(getDatabasePath())
        val statement = connection.createStatement()
        statement.executeUpdate(s"DELETE FROM pending_registrations WHERE transaction_id = '$transactionId' AND box_id = '$boxId'")
        connection.close()
    }

    def getMostRecentMintTransactionId(): String = {
        val connection = DriverManager.getConnection(getDatabasePath())
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery(s"SELECT * FROM confirmed_registry_insertions WHERE spent_transaction_id IS NULL")
        if (resultSet.next()) {
            val registrationInfo = getRegistrationInfo(resultSet)
            connection.close()
            registrationInfo.mintTransactionId
        } else {
            connection.close()
            null
        }
    }

    def getRegistrationInfo(resultSet: ResultSet): RegistrationInfo = {
        val mintTransactionId = resultSet.getString("mint_transaction_id")
        val spentTransactionId = resultSet.getString("spent_transaction_id")
        val ergonameRegistered = ErgoName(resultSet.getString("ergoname_registered")).toErgoNameHash
        val ergonameTokenId = ErgoId.create(resultSet.getString("ergoname_token_id"))
        val registrationInfo = RegistrationInfo(mintTransactionId, spentTransactionId, ergonameRegistered, ergonameTokenId)
        registrationInfo
    }

    def getPendingRegistrationInfo(resultSet: ResultSet): Array[PendingRegistration] = {
        var pendingRegistrations = Array[PendingRegistration]()
        for (i <- 0 to resultSet.getFetchSize()) {
            val transactionId = resultSet.getString("transaction_id")
            val boxId = resultSet.getString("box_id")
            val pendingRegistration = PendingRegistration(transactionId, boxId)
            pendingRegistrations = pendingRegistrations :+ pendingRegistration
        }
        pendingRegistrations
    }

    def getDatabasePath(): String = {
        val path = "jdbc:postgresql://localhost:5432/ergonames?user=ergonames&password=ergonames"
        path
    }

}