package de.tuberlin.mcc.faas4fogsim

import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

fun main(args: Array<String>) {
    if (args.isNotEmpty() && args[0] == "sim1") {
        logger.info("Running Simulation 1 - varyRequestLoad")
        varyRequestLoad()
    } else if (args.isNotEmpty() && args[0] == "sim2") {
        logger.info("Running Simulation 2 - varyNoOfExecutables")
        varyNoOfExecutables()
    } else if (args.isNotEmpty() && args[0] == "sim3") {
        logger.info("Running Simulation 3 - varyLoyalty")
        varyLoyalty()
    } else {
        logger.error("Please describe whether you want to start sim1 (varyRequestLoad) or sim2 (varyNumberOfExecutables)")
    }
}

// Simulation 1: used to study the effect of an increasing request load on the processing prices
fun varyRequestLoad() {
    val additionalFields = listOf("individualEdgeLoad")

    for (individualEdgeLoad in 100..10000 step 100) {
        val config = Configuration(
            "varyRequestLoad",
            individualEdgeLoad = individualEdgeLoad,
            storageCapacityEdge = Int.MAX_VALUE,
            storageCapacityIntermediary = Int.MAX_VALUE
        )

        val sim = Simulator(config)
        val result = sim.runSimulation()

        if (individualEdgeLoad == 100) {
            result.prepareResultFiles(additionalFields)
        }

        result.storeResults(additionalFields)
    }
}

// Simulation 2: used to study the effect of an increasing number of executables on storage prices
fun varyNoOfExecutables() {
    val additionalFields = listOf("numberOfExecutables")

    for (noOfExec in 5..100 step 5) {
        val config = Configuration(
            "varyNoOfExecutables",
            numberOfExecutables = noOfExec,
            parallelRequestCapacityEdge = Int.MAX_VALUE,
            parallelRequestCapacityIntermediary = Int.MAX_VALUE
        )

        val sim = Simulator(config)
        val result = sim.runSimulation()

        if (noOfExec == 5) {
            result.prepareResultFiles(additionalFields)
        }

        result.storeResults(additionalFields)
    }
}

fun varyLoyalty() {
    val additionalFields = listOf("nodeDisloyalProbability")

    for (nodeDisloyalProbability in listOf(0, 20, 40, 60, 80, 100)) {
        val config = Configuration(
            "varyLoyalty100L",
            nodeDisloyalProbability = nodeDisloyalProbability,
            numberOfExecutables = 100, // 10% on Edge, 50% on Intermediary
            randomSeed = 100L
        )

        val sim = Simulator(config)
        val result = sim.runSimulation(simpleTopology = false)

        if (nodeDisloyalProbability == 0) {
            result.prepareResultFiles(additionalFields)
        }

        result.storeResults(additionalFields)
    }

}

