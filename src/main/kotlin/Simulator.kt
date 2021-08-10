package de.tuberlin.mcc.faas4fogsim

import org.apache.logging.log4j.LogManager
import kotlin.contracts.contract

private val logger = LogManager.getLogger()

class Simulator(val config: Configuration) {

    val cloud = mutableSetOf<ComputeNode>()
    val intermediary = mutableSetOf<ComputeNode>()
    val edge = mutableSetOf<ComputeNode>()
    val executables = mutableMapOf<String, Executable>()
    val requests: MutableMap<Int, MutableMap<ComputeNode, MutableList<ExecRequest>>> = mutableMapOf()


    /**
     * defines the network topology of cloud, intermediary and edge nodes
     */
    fun defineTopology(simple: Boolean = true) {
        //TODO edit here and insert network topology for simulation. EXAMPLE:
        if (simple) {
            logger.info("Building simple topology")
            val cNode = buildCloudNode()
            val iNode = buildIntermediaryNode("intermediary")
            val eNode = buildEdgeNode("edge")
            iNode.connectIntermediaryToCloud(cNode)
            eNode.connectEdgeToIntermediary(iNode)
        } else {
            logger.info("Building complex topology")
            val cNode = buildCloudNode()
            for (i in 1..3) {
                val iNode = buildIntermediaryNode("intermediary-$i")
                for (e in 1..5) {
                    val eNode = buildEdgeNode("edge-$i-$e")
                    eNode.connectEdgeToIntermediary(iNode)
                }
                iNode.connectEdgeToIntermediary(cNode)
            }
        }
    }

    /**
     * defines the set of executables for functions that shall be deployed on the topology
     */
    fun defineExecutables() {
        //define executables and their parameter settings here manually or use config
        //for (x in 1..5) defineExecutable(1, "executable$x", x.toDouble(), 2)

        for (x in 1..config.numberOfExecutables) defineExecutable(
            config.withVariance(1.0), // an executable has an average size of 1.0
            "executable$x",
            config.withVariancePrice(config.averageStoragePrice),
            config.avgExecLatency
        )
    }

    /**
     * defines request arrival rates and characteristics
     */
    fun defineRequests() {
        // define request arrival rates and parameter settings here or use config
//        for (exec in executables.values) {
//            defineRequest(exec, 1.0, 0, edge.first())
//        }

        // timestamp, computenode, execrequest
        val reqs = mutableListOf<Triple<Int, ComputeNode, ExecRequest>>()
        for (node in edge) {
            //var counter = 0
            for (reqNo in 1..config.requestsPerEdgeNode) {
                val timestamp = config.random.nextInt(config.simulationDuration)
                val executable = executables.values.elementAt(config.random.nextInt(config.numberOfExecutables))
                val price = config.withVariancePrice(config.avgExecutionPrice)
                reqs.add(Triple(timestamp, node, ExecRequest(executable, price, timestamp)))
                //  counter++
            }
            //println("defined $counter requests for node ${node.name}")
        }
        reqs.sortedBy { it.first }.forEach { addRequestToSimulation(it.third, it.first, it.second) }


    }

    fun offerRequests() {
        for (timestamp in 0..config.simulationDuration - 1) {
            val nodeToReqMap = requests.get(timestamp) ?: continue

            for ((node, reqList) in nodeToReqMap) {
                val pushedUp = node.offerRequests(timestamp, reqList)
                // stored pushed up requests in requests-data-structure
                for ((pushTarget, requestList) in pushedUp) {
                    for (request in requestList) {
                        val innerMap = requests.getOrPut(request.actualStart) { mutableMapOf() }
                        val listForNode = innerMap.getOrPut(pushTarget) { mutableListOf() }
                        listForNode.add(request)
                    }
                }
            }
        }
    }

    private fun addRequestToSimulation(request: ExecRequest, timestamp: Int, node: ComputeNode) {
        var nodeToReqList = requests[timestamp]
        if (nodeToReqList == null) {
            nodeToReqList = mutableMapOf()
            requests[timestamp] = nodeToReqList
        }
        var reqList = nodeToReqList[node]
        if (reqList == null) {
            reqList = mutableListOf()
            nodeToReqList[node] = reqList
        }
        reqList.add(request)
    }

    private fun defineExecutable(size: Double, name: String, storePrice: Double, execLatency: Int) {
        val exec = Executable(size, name, storePrice, execLatency)
        cloud.union(intermediary).union(edge).forEach { it.offerExecutable(exec) }
        executables[exec.name] = exec
    }

    private fun buildCloudNode(): ComputeNode = buildNode(NodeType.CLOUD, Int.MAX_VALUE, Int.MAX_VALUE, "Cloud")
    private fun buildIntermediaryNode(name: String): ComputeNode = buildNode(
        NodeType.INTERMEDIARY,
        config.storageCapacityIntermediary,
        config.parallelRequestCapacityIntermediary,
        name
    )

    private fun buildEdgeNode(name: String): ComputeNode = buildNode(
        NodeType.EDGE,
        config.storageCapacityEdge,
        config.parallelRequestCapacityEdge,
        name
    )

    private fun buildNode(
        nodeType: NodeType,
        storageCapacity: Int,
        parallelRequestCapacity: Int,
        name: String
    ): ComputeNode {
        val node = ComputeNode(
            nodeType,
            storageCapacity,
            parallelRequestCapacity,
            name,
            config
        )
        when (nodeType) {
            NodeType.EDGE -> edge.add(node)
            NodeType.INTERMEDIARY -> intermediary.add(node)
            NodeType.CLOUD -> cloud.add(node)
        }
        return node
    }

    fun ComputeNode.connectEdgeToIntermediary(intermediary: ComputeNode) =
        this.connectTo(intermediary, config.avgEdge2IntermediaryLatency)

    fun ComputeNode.connectIntermediaryToCloud(cloud: ComputeNode) =
        this.connectTo(cloud, config.avgIntermediary2CloudLatency)

    fun runSimulation(simpleTopology: Boolean = true): SimulationResult {
        logger.info("Simulation configuration: $config")
        defineTopology(simpleTopology)
        edge.union(intermediary).union(cloud).forEach { logger.debug(it) }
        defineExecutables()
        logger.info("Added ${executables.size} executables")
        defineRequests()
        logger.info("Calculated timestamps for ${requests.flatMap { it.value.flatMap { it.value } }.count()} requests")
        offerRequests()

        val nodeResults = mutableListOf<NodeResult>()
        for (node in edge.union(intermediary.union(cloud))) {
            val (processed, delegated) = node.getRequestStats()
            val (procEarning, storeEarning) = node.getEarningStats()
            val avgConcurrentRequests = node.avgConcurrentRequests()
            val noOfExec = node.executables.size
            nodeResults.add(
                NodeResult(
                    node.name,
                    node.nodeType,
                    procEarning,
                    storeEarning,
                    processed,
                    delegated,
                    noOfExec,
                    avgConcurrentRequests
                )
            )
        }
        var min = Integer.MAX_VALUE
        var max = Integer.MIN_VALUE
        var sum = 0
        var counter = 0
        requests.flatMap { it.value.flatMap { it.value } }
            .filter { it.executionCompleted } // only count requests that have been completly executed
            .forEach {
                counter++
                sum += it.totalLatency
                if (it.totalLatency < min) min = it.totalLatency
                if (it.totalLatency > max) max = it.totalLatency
            }
        val requestResult = RequestResult(min, sum.div(counter.toDouble()), max)
        logger.info("Simulation completed")
        return SimulationResult(config, nodeResults, requestResult)
    }

}

