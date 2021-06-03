package de.tuberlin.mcc.faas4fogsim

import org.apache.logging.log4j.LogManager
import java.io.File

import java.util.Random
import kotlin.math.roundToInt
import kotlin.reflect.KProperty1
import kotlin.system.exitProcess
import kotlin.text.StringBuilder

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
    fun defineTopology() {
        //TODO edit here and insert network topology for simulation. EXAMPLE:
        val cNode = buildCloudNode()
        val iNode = buildIntermediaryNode("intermediary")
        val eNode = buildEdgeNode("edge")
        iNode.connectIntermediaryToCloud(cNode)
        eNode.connectEdgeToIntermediary(iNode)
    }

    /**
     * defines the set of executables for functions that shall be deployed on the topology
     */
    fun defineExecutables() {
        //define executables and their parameter settings here manually or use config
        //for (x in 1..5) defineExecutable(1, "executable$x", x.toDouble(), 2)

        for (x in 1..config.numberOfExecutables) defineExecutable(
            config.withVariance(config.averageExecutableSize),
            "executable$x",
            config.withVariance(config.averageStoragePrice),
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
                val price = config.withVariance(config.avgExecutionPrice)
                reqs.add(Triple(timestamp, node, ExecRequest(executable, price, timestamp)))
                //  counter++
            }
            //println("defined $counter requests for node ${node.name}")
        }
        reqs.sortedBy { it.first }.forEach { addRequestToSimulation(it.third, it.first, it.second) }


    }

    fun offerRequests() {
        for ((timestamp, nodeToReqMap) in requests.toSortedMap()) {
            for ((node, reqList) in nodeToReqMap) {
                node.offerRequests(timestamp, reqList)
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

    private fun defineExecutable(size: Int, name: String, storePrice: Double, execLatency: Int) {
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

    fun runSimulation(): SimulationResult {
        defineTopology()
        println("Added the following nodes:")
        edge.union(intermediary).union(cloud).forEach { println(it) }
        defineExecutables()
        println("Added ${executables.size} executables")
        defineRequests()
        println("Defined ${requests.flatMap { it.value.flatMap { it.value } }.count()} requests")
        offerRequests()

        val nodeResults = mutableListOf<NodeResult>()
        for (node in edge.union(intermediary.union(cloud))) {
            val (processed, delegated) = node.getRequestStats()
            val (procEarning, storeEarning) = node.getEarningStats()
            val noOfExec = node.executables.size
            nodeResults.add(
                NodeResult(
                    node.name,
                    node.nodeType,
                    procEarning,
                    storeEarning,
                    processed,
                    delegated,
                    noOfExec
                )
            )
        }
        var min = Integer.MAX_VALUE
        var max = Integer.MIN_VALUE
        var sum = 0
        var counter = 0
        requests.flatMap { it.value.flatMap { it.value } }.forEach {
            counter++
            sum += it.totalLatency
            if (it.totalLatency < min) min = it.totalLatency
            if (it.totalLatency > max) max = it.totalLatency
        }
        val requestResult = RequestResult(min, sum.div(counter.toDouble()), max)
        return SimulationResult(config, nodeResults, requestResult)
    }

}

