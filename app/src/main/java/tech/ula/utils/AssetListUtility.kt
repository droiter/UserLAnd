package tech.ula.utils

import java.io.BufferedReader
import java.io.InputStreamReader
import javax.net.ssl.SSLHandshakeException

class AssetListUtility(
    private val deviceArchitecture: String,
    private val distributionType: String,
    private val connectionUtility: ConnectionUtility
) {

    private val allAssetListTypes = listOf(
            "support" to "all",
            "support" to deviceArchitecture,
            distributionType to "all",
            distributionType to deviceArchitecture
    )

    fun retrieveAllAssetLists(): List<List<Asset>> {
        val allAssetLists = ArrayList<List<Asset>>()
        allAssetListTypes.forEach {
            (assetType, location) ->
            allAssetLists.add(retrieveAndParseAssetList(assetType, location))
        }
        return allAssetLists.toList()
    }

    private fun retrieveAndParseAssetList(
        assetType: String,
        location: String,
        protocol: String = "https",
        retries: Int = 0
    ): List<Asset> {
        val assetList = ArrayList<Asset>()

        val url = "$protocol://github.com/CypherpunkArmory/UserLAnd-Assets-" +
                "$distributionType/raw/master/assets/$deviceArchitecture/assets.txt"
        try {
            val reader = BufferedReader(InputStreamReader(connectionUtility.getUrlInputStream(url)))

            reader.forEachLine {
                val (filename, timestampAsString) = it.split(" ")
                if (filename == "assets.txt") return@forEachLine
                val remoteTimestamp = timestampAsString.toLong()
                assetList.add(Asset(filename, distributionType, remoteTimestamp))
            }

            reader.close()
            return assetList.toList()
        } catch (err: SSLHandshakeException) {
            if (retries >= 5) throw object : Exception("Error getting asset list") {}
            return retrieveAndParseAssetList(assetType, location,
                    "http", retries + 1)
        } catch (err: Exception) {
            throw object : Exception("Error getting asset list") {}
        }
    }
}