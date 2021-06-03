package de.tuberlin.mcc.faas4fogsim

import java.io.File


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
    val additionalFields = listOf("requestsPerEdgePerSecond")

    for (requestsPerEdgePerSecond in 100..10000 step 100) {
        val config = Configuration("varyRequestLoad", storageCapacityEdge = 200, storageCapacityIntermediary = 1000,
            parallelRequestCapacityEdge = 5, parallelRequestCapacityIntermediary = 20, numberOfExecutables = 10,
            avgExecutionPrice = 100.0, averageStoragePrice = 10.0, requestsPerEdgePerSecond = requestsPerEdgePerSecond)

        val sim = Simulator(config)
        val result = sim.runSimulation()

        if (requestsPerEdgePerSecond == 100) {
            result.prepareResultFiles(additionalFields)
        }

        result.storeResults(additionalFields)
    }
}

// Simulation 2: used to study the effect of an increasing number of executables on storage prices
fun varyNoOfExecutables() {
    val additionalFields = listOf("numberOfExecutables")

    for (noOfExec in 5..100 step 5) {
        val config = Configuration("varyNoOfExecutables", numberOfExecutables = noOfExec)

        val sim = Simulator(config)
        val result = sim.runSimulation()

        if (noOfExec == 5) {
            result.prepareResultFiles(additionalFields)
        }

        result.storeResults(additionalFields)
    }

}

