package de.tuberlin.mcc.faas4fogsim

import java.io.File
import java.util.*
import kotlin.math.roundToInt


fun main(args: Array<String>) {
    if (args.isNotEmpty() && args[0] == "sim1") {
        print("Running Simulation 1")
        varyRequestLoad()
    } else if (args.isNotEmpty() && args[0] == "sim2") {
        print("Running Simulation 2")
        varyNoOfExecutables()
    } else {
        print("Please describe whether you want to start sim1 (varyRequestLoad) or sim2 (varyNumberOfExecutables)")
    }
}

// Simulation 1: used to study the effect of an increasing request load on the processing prices
fun varyRequestLoad() {
    val writer = File("summary.csv").printWriter()
    val writerPerNode = File("noderesults.csv").printWriter()
    val writerPerReqs = File("reqresults.csv").printWriter()
    writerPerReqs.println("reqPerEdgePerSecond;min_latency;avg_latency;max_latency")
    writerPerNode.println("reqPerEdgePerSecond;name;type;processing_earnings;storage_earnings;processed_reqs;delegated_reqs;no_executables")

    for (reqsPerSec in 100..10000 step 100) {
        val config = Configuration(storageCapacityEdge = 200, storageCapacityIntermediary = 1000,
            parallelRequestCapacityEdge = 5, parallelRequestCapacityIntermediary = 20, numberOfExecutables = 10,
            avgExecutionPrice = 100.0, averageStoragePrice = 10.0, requestsPerEdgePerSecond = reqsPerSec)

        val sim = Simulator(config)
        val result = sim.runSimulation()
        writer.println(result.toCsvString())
        writerPerReqs.println("${config.requestsPerEdgePerSecond};${result.minLatency};${result.avgLatency.asDecimal()};${result.maxLatency}")
        result.nodeResults.forEach { writerPerNode.println("${config.requestsPerEdgePerSecond};${it.toCsvString()}") }
    }

    writer.close()
    writerPerNode.close()
    writerPerReqs.close()
}

// Simulation 2: used to study the effect of an increasing number of executeables on storage prices
fun varyNoOfExecutables() {
    val writer = File("summary.csv").printWriter()
    val writerPerNode = File("noderesults.csv").printWriter()
    val writerPerReqs = File("reqresults.csv").printWriter()
    writerPerReqs.println("no_executables;min_latency;avg_latency;max_latency")
    writerPerNode.println("no_executables;name;type;processing_earnings;storage_earnings;processed_reqs;delegated_reqs;no_executables")


    for (noOfExec in 5..100 step 5) {
        val config = Configuration(numberOfExecutables = noOfExec)

        val sim = Simulator(config)
        val result = sim.runSimulation()
        writer.println(result.toCsvString())
        writerPerReqs.println("${config.numberOfExecutables};${result.minLatency};${result.avgLatency.asDecimal()};${result.maxLatency}")
        result.nodeResults.forEach { writerPerNode.println("${config.numberOfExecutables};${it.toCsvString()}") }
    }


    writer.close()
    writerPerNode.close()
    writerPerReqs.close()
}

data class Configuration(
    var randomSeed: Long = 0L,
    var maxDevInPercent: Int = 50, // must be in [0;100]
    val simulationDuration: Int = 120000,

    // node parameters
    var storageCapacityEdge: Int = 100,
    var storageCapacityIntermediary: Int = 500,
    var parallelRequestCapacityEdge: Int = 10000,
    var parallelRequestCapacityIntermediary: Int = 10000,
    var avgExecLatency: Int = 30,

    // connection parameters
    var avgEdge2IntermediaryLatency: Int = 20,
    var avgIntermediary2CloudLatency: Int = 40,

    // executable parameters
    var numberOfExecutables: Int = 5,
    var averageStoragePrice: Double = 100.0,
    val averageExecutableSize: Int = 10,

    // request parameters
    var requestsPerEdgePerSecond: Int = 10000,
    var avgExecutionPrice: Double = 1.0

    // output files

) {
    val random = Random(randomSeed)
    val requestsPerEdgeNode = (requestsPerEdgePerSecond * simulationDuration / 1000.0).roundToInt()

    fun withVariance(value: Int): Int {
        val temp = (random.nextDouble() * maxDevInPercent * value / 100.0).roundToInt()
        return if (random.nextBoolean()) value + temp else value - temp
    }

    fun withVariance(value: Double): Double {
        val temp = random.nextDouble() * value * maxDevInPercent / 100.0
        return if (random.nextBoolean()) value + temp else value - temp
    }

}