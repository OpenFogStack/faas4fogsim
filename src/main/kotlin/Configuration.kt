package de.tuberlin.mcc.faas4fogsim

import java.io.File
import java.util.*
import kotlin.math.roundToInt

/**
 * timeU = ms
 * spaceU = size of an average functions
 * moneyU = random currency
 */
data class Configuration(
    val experimentName: String,

    val randomSeed: Long = 0L,
    val maxDevInPercent: Int = 50, // must be in [0;100]
    val maxPriceDevInPercent: Int = maxDevInPercent, // we can also just deviate the storage/processing prices
    val simulationDuration: Int = 2 * 60 * 1000, // timeU

    // node parameters
    val storageCapacityEdge: Int = 10, // spaceU
    val storageCapacityIntermediary: Int = 50, // spaceU
    val parallelRequestCapacityEdge: Int = 5, // requests
    val parallelRequestCapacityIntermediary: Int = 20, // requests
    val avgExecLatency: Int = 30, // timeU
    val nodeDisloyalProbability: Int = 100, // chance for a node to replace the cheapest executable if a better one is found

    // connection parameters
    val avgEdge2IntermediaryLatency: Int = 20, // timeU
    val avgIntermediary2CloudLatency: Int = 40, // timeU

    // executable parameters
    val numberOfExecutables: Int = 10, // executables
    val averageStoragePrice: Double = 100.0, // moneyU / executable for complete simulation duration

    // request parameters
    val individualEdgeLoad: Int = 1000, // requests / 1000 timeU
    val avgExecutionPrice: Double = 100.0, // moneyU / request for complete execution time

    // output files
    val outputDir: String = ".",
    val suffixNodeResult: String = "-nodes",
    val suffixRequestResult: String = "-requests",
    val suffixConfigInfo: String = "-config"
) {
    val random = Random(randomSeed)
    val disloyalRandom = Random(randomSeed) // unique random since loyalty is not always checked

    val requestsPerEdgeNode = (individualEdgeLoad * simulationDuration / 1000.0).roundToInt()

    val nodesFile = File("$outputDir/$experimentName$suffixNodeResult.csv")
    val requestsFile = File("$outputDir/$experimentName$suffixRequestResult.csv")
    val infoFile = File("$outputDir/$experimentName$suffixConfigInfo.txt")

    fun withVariance(value: Int): Int {
        val temp = (random.nextDouble() * maxDevInPercent * value / 100.0).roundToInt()
        return if (random.nextBoolean()) value + temp else value - temp
    }

    fun withVariance(value: Double): Double {
        val temp = random.nextDouble() * value * maxDevInPercent / 100.0
        return if (random.nextBoolean()) value + temp else value - temp
    }

    fun withVariancePrice(value: Double): Double {
        val temp = random.nextDouble() * value * maxPriceDevInPercent / 100.0
        return if (random.nextBoolean()) value + temp else value - temp
    }

}