package de.tuberlin.mcc.faas4fogsim

import java.io.File
import java.util.*
import kotlin.math.roundToInt

data class Configuration(
    val experimentName: String,

    val randomSeed: Long = 0L,
    val maxDevInPercent: Int = 50, // must be in [0;100]
    val simulationDuration: Int = 120000,

    // node parameters
    val storageCapacityEdge: Int = 100,
    val storageCapacityIntermediary: Int = 500,
    val parallelRequestCapacityEdge: Int = 10000,
    val parallelRequestCapacityIntermediary: Int = 10000,
    val avgExecLatency: Int = 30,

    // connection parameters
    val avgEdge2IntermediaryLatency: Int = 20,
    val avgIntermediary2CloudLatency: Int = 40,

    // executable parameters
    val numberOfExecutables: Int = 5,
    val averageStoragePrice: Double = 100.0,
    val averageExecutableSize: Int = 10,

    // request parameters
    val requestsPerEdgePerSecond: Int = 10000,
    val avgExecutionPrice: Double = 1.0,

    // output files
    val outputDir: String = ".",
    val suffixNodeResult: String = "-nodes",
    val suffixRequestResult: String = "-requests",
    val suffixConfigInfo: String = "-config"
) {
    val random = Random(randomSeed)
    val requestsPerEdgeNode = (requestsPerEdgePerSecond * simulationDuration / 1000.0).roundToInt()

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

}