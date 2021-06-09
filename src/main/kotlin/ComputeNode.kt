package de.tuberlin.mcc.faas4fogsim

import org.apache.logging.log4j.LogManager
import kotlin.math.min

private val logger = LogManager.getLogger()

data class ComputeNode(
    val nodeType: NodeType,
    val storageCapacity: Int,
    val parallelRequestCapacity: Int,
    val name: String,
    val config: Configuration
) {
    var uplinkLatency: Int = 0
    var parentNode: ComputeNode? = null

    val executables: MutableSet<Executable> = mutableSetOf()
    private var usedStorageCapacity: Double = 0.0

    val utilization: IntArray = IntArray(config.simulationDuration) { 0 }
    private var earnings: Double = 0.0

    private var requestsProcessed = 0
    private var requestsPushedUp = 0

    /**
     * places an executable locally if sufficient space is left or if bid is high enough to evict existing executable(s)
     * @param newOne Executable that shall be stored
     */
    fun offerExecutable(newOne: Executable) {

        //find lowest paying entries and drop them until the new executable fits in
        while (usedStorageCapacity + newOne.size > storageCapacity) {
            val exec = executables.minByOrNull { it.storePrice }
                ?: throw RuntimeException("Insufficient capacity but min price element was null: $this")
            usedStorageCapacity -= exec.size
            executables.remove(exec)
           // println("$nodeType node $name dropped executable ${exec.name}. Capacity: $usedStorageCapacity out of $storageCapacity")
        }
        executables.add(newOne)
        usedStorageCapacity += newOne.size
       // println("$nodeType node $name stored executable ${newOne.name}. Capacity: $usedStorageCapacity out of $storageCapacity")
    }

    fun offerRequests(timestamp: Int, requests: Collection<ExecRequest>): List<Pair<ComputeNode, List<ExecRequest>>> {
        return requests.sortedByDescending { it.execPrice }.mapNotNull { offerRequest(timestamp, it) }
    }

    /**
     * Executes a request on this node or pushes it to the next node towards the cloud if not sufficient capacity or missing executable.
     * Requests will not be executed if a cloud node would push them up.
     * @param timestamp start timestamp for the request execution
     * @param request request that shall be executed
     *
     * Returns the requests that should be pushed up to the parent node (one field stores actualStart field which equals
     * the timestamp of next planned execution).
     */
    private fun offerRequest(timestamp: Int, request: ExecRequest): Pair<ComputeNode, List<ExecRequest>>? {
        val pushedUp = mutableListOf<ExecRequest>()

        val isCloud = nodeType == NodeType.CLOUD
        val hasCpuCapacityLeft = checkUtilization(
            timestamp,
            request.executable.execLatency
        )
        val executableExists = executables.contains(request.executable)

        if (isCloud || (executableExists && hasCpuCapacityLeft)) processRequest(timestamp, request)
        else {
           // println("pushing up from $name to ${parentNode?.name}: $request")
            val uplinkDelay = config.withVariance(uplinkLatency)
            request.pushTowardsCloud(uplinkDelay)
            requestsPushedUp++
            pushedUp.add(request)
        }

        return if (parentNode != null) {
            Pair(parentNode!!, pushedUp)
        } else {
            if (pushedUp.isNotEmpty()) {
                logger.warn("Must push request to a parent, but none exist -> will not be executed")
            }
            null
        }
    }

    /**
     * checks whether there is compute capacity left in the specified time interval
     */
    private fun checkUtilization(timestamp: Int, duration: Int): Boolean {
        if (timestamp + duration >= config.simulationDuration) return false
        return utilization[timestamp] < parallelRequestCapacity
    }

    /**
     * processes request, i.e., updates utilization stats and creates log entries
     */
    private fun processRequest(timestamp: Int, request: ExecRequest) {
        val latency = config.withVariance(request.executable.execLatency)
        val end = min(timestamp + latency, config.simulationDuration)
        for (x in timestamp until end) utilization[x]++
        request.execute(name, nodeType,latency)
        earnings += request.execPrice
        requestsProcessed++
        //println("Processing request for price ${request.execPrice} on node $name ($nodeType) at t=$timestamp")
    }

    fun getRequestStats() = Pair(requestsProcessed,requestsPushedUp)
    fun getEarningStats() = Pair(earnings,executables.sumByDouble { it.storePrice })
    
    fun avgConcurrentRequests(): Double {
        return utilization.average()
    }

    fun connectTo(other: ComputeNode, uplinkLatency:Int) {
        parentNode = other
        this.uplinkLatency = uplinkLatency
    }

}

enum class NodeType { CLOUD, EDGE, INTERMEDIARY }



