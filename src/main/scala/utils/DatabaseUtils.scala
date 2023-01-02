package utils

import java.sql.{Connection, DriverManager, ResultSet}
import java.io.File
import types.RegistrationInfo
import types.ErgoNameHash
import org.ergoplatform.appkit.ErgoId
import types.ErgoName

object DatabaseUtils {

    def readFromDatabase(mintTransactionId: String): RegistrationInfo = {
        val connection = DriverManager.getConnection(getDatabasePath())
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery(s"SELECT * FROM registration_information WHERE mint_transaction_id = '$mintTransactionId'")
        if (resultSet.next()) {
            val registrationInfo = getRegistrationInfo(resultSet)
            connection.close()
            registrationInfo
        } else {
            connection.close()
            null
        }
    }

    def getMostRecentMintTransactionId(): String = {
        val connection = DriverManager.getConnection(getDatabasePath())
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery(s"SELECT * FROM registration_information WHERE spent_transaction_id IS NULL")
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

    def getDatabasePath(): String = {
        val path = "jdbc:postgresql://localhost:5432/ergonames?user=ergonames&password=ergonames"
        path
    }

}