package de.tuberlin.mcc.faas4fogsim

import org.apache.logging.log4j.LogManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.KProperty1
import kotlin.system.exitProcess

private val logger = LogManager.getLogger()

class SimulationResult(
    private val config: Configuration,
    private val nodeResults: Collection<NodeResult>,
    private val requestResult: RequestResult
) {

    /**
     * Prepares the result files.
     *
     * For node results and request results, one can supply [additionalFields] to define which additional
     * columns based on configured values should be added.
     */
    fun prepareResultFiles(additionalFields: List<String> = emptyList()) {
        config.nodesFile.writeText(NodeResult.csvHeader + ";" + additionalFields.joinToString(";") + "\n")
        config.requestsFile.writeText(RequestResult.csvHeader + ";" + additionalFields.joinToString(";") + "\n")
        config.infoFile.writeText("Experiments began on ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date())}\n")
    }

    /**
     * Writes the results to file.
     *
     * For node results and request results, one can supply [additionalFields] to define which additional
     * columns based on configured values should be added.
     */
    fun storeResults(additionalFields: List<String> = emptyList()) {
        // get config value csv
        val configFields = additionalFields.map { getConfigValue((it)) }.joinToString(";")

        // write node results
        nodeResults.forEach {
            config.nodesFile.appendText(it.csv + ";" + configFields + "\n")
        }

        // write request results
        config.requestsFile.appendText(requestResult.csv + ";" + configFields + "\n")

        // write info text
        config.infoFile.appendText(config.toString() + "\n")
    }

    private fun getConfigValue(fieldName: String): String {
        try {
            val property = config::class.members
                .first { it.name == fieldName } as KProperty1<Any, *>

            return property.get(config).toString()
        } catch (e: Exception) {
            logger.fatal("There is no field $fieldName in configuration $config")
        }

        exitProcess(1)
    }

}

data class NodeResult(
    val nodeName: String,
    val nodeType: NodeType,
    val processingEarnings: Double,
    val storageEarnings: Double,
    val processedRequests: Int,
    val delegatedRequests: Int,
    val numberOfStoredExecutables: Int
) {
    companion object {
        const val csvHeader =
            "name;type;processing_earnings;storage_earnings;processed_reqs;delegated_reqs;stored_executables";
    }

    val csv =
        "$nodeName;$nodeType;${processingEarnings.asDecimal()};${storageEarnings.asDecimal()};$processedRequests;$delegatedRequests;$numberOfStoredExecutables"

}

data class RequestResult(
    val minLatency: Int,
    val avgLatency: Double,
    val maxLatency: Int
) {
    companion object {
        const val csvHeader = "min_latency;max_latency;avg_latency"
    }

    val csv = "$minLatency;${avgLatency.asDecimal()};$maxLatency"
}