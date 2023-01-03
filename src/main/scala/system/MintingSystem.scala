package system

import scenarios.ProcessMintRequest.processMintRequestScenario
import utils.DatabaseUtils

object MintingSystem {

    def main(args: Array[String]): Unit = {
        systemLoop()
    }

    def systemLoop() {
        var systemRunning = true
        while (systemRunning) {
            val pendingRegistrations = DatabaseUtils.getPendingRegistrations()
            if (pendingRegistrations != null) {
                for (pendingRegistration <- pendingRegistrations) {
                    val txInfo = processMintRequestScenario("config.json", pendingRegistration.boxId)
                    DatabaseUtils.removePendingRegistration(pendingRegistration.transactionId, pendingRegistration.boxId)
                    println(txInfo)
                }
            }
        }
    }
}