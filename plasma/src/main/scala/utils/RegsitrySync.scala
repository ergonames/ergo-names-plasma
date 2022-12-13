package utils

import scenarios.UpdateRegistry.ErgoNameHash

import io.getblok.getblok_plasma.{PlasmaParameters, ByteConversion}
import io.getblok.getblok_plasma.collections.{OpResult, PlasmaMap, Proof, ProvenResult}
import sigmastate.AvlTreeFlags
import scorex.crypto.hash.Blake2b256
import org.ergoplatform.appkit.ErgoId

object RegistrySync {

    def syncRegistry(initialTxId: String, defaultTestnetExplorerUrl: String): PlasmaMap[ErgoNameHash, ErgoId] = {
        val registryMap = new PlasmaMap[ErgoNameHash, ErgoId](AvlTreeFlags.AllOperationsAllowed, PlasmaParameters.default)
        registryMap
    }

}