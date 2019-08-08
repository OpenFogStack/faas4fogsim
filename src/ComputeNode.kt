package de.tuberlin.mcc.faas4fogsim


import kotlin.math.min


data class ComputeNode(
    val nodeType: NodeType,
    val storageCapacity: Int,
    val parallelRequestCapacity: Int,
    val name: String,

    val simDuration: Int
) {
    var uplinkLatency: Int = 0
    var parentNode: ComputeNode? = null

    val executables: MutableSet<Executable> = mutableSetOf()
    private var usedStorageCapacity: Int = 0

    private val utilization: IntArray = IntArray(simDuration) { 0 }
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
            val exec = executables.minBy { it.storePrice }
                ?: throw RuntimeException("Insufficient capacity but min price element was null: $this")
            usedStorageCapacity -= exec.size
            executables.remove(exec)
           // println("$nodeType node $name dropped executable ${exec.name}. Capacity: $usedStorageCapacity out of $storageCapacity")
        }
        executables.add(newOne)
        usedStorageCapacity += newOne.size
       // println("$nodeType node $name stored executable ${newOne.name}. Capacity: $usedStorageCapacity out of $storageCapacity")
    }

    fun offerRequests(timestamp: Int, requests: Collection<ExecRequest>) {
        requests.sortedByDescending { it.execPrice }.forEach { offerRequest(timestamp, it) }
    }

    /**
     * Executes a request on this node or pushes it to the next node towards the cloud if not sufficient capacity or missing executable.
     * Requests will not be executed if a cloud node would push them up.
     * @param timestamp start timestamp for the request execution
     * @param request request that shall be executed
     */
    private fun offerRequest(timestamp: Int, request: ExecRequest) {
        val isCloud = nodeType == NodeType.CLOUD
        val hasCpuCapacityLeft = checkUtilization(
            timestamp,
            request.executable.execLatency
        )
        val executableExists = executables.contains(request.executable)

        if (isCloud || (executableExists && hasCpuCapacityLeft)) processRequest(timestamp, request)
        else {
           // println("pushing up from $name to ${parentNode?.name}: $request")
            val uplinkDelay = uplinkLatency.withVariance()
            request.pushTowardsCloud(uplinkDelay)
            requestsPushedUp++
            parentNode?.offerRequest(timestamp + uplinkDelay, request)
        }

    }

    /**
     * checks whether there is compute capacity left in the specified time interval
     */
    private fun checkUtilization(timestamp: Int, duration: Int): Boolean {
        if (timestamp + duration >= simDuration) return false
        return utilization[timestamp] < parallelRequestCapacity
    }

    /**
     * processes request, i.e., updates utilization stats and creates log entries
     */
    private fun processRequest(timestamp: Int, request: ExecRequest) {
        val latency = request.executable.execLatency.withVariance()
        val end = min(timestamp + latency, Config.simulationDuration)
        for (x in timestamp until end) utilization[x]++
        request.execute(name, nodeType,latency)
        earnings += request.execPrice
        requestsProcessed++
        //println("Processing request for price ${request.execPrice} on node $name ($nodeType) at t=$timestamp")
    }


    fun getStatsString(): String {
        return "$name;$nodeType;${parentNode?.name};${(usedStorageCapacity.toDouble() / storageCapacity).asPercent()};${utilization.min()?.div(
            parallelRequestCapacity
        )?.asPercent()};${utilization.average().div(parallelRequestCapacity).asPercent()};${utilization.max()?.div(
            parallelRequestCapacity
        )?.asPercent()};${executables.sumByDouble { it.storePrice }.asDecimal()};${earnings.asDecimal()};$requestsProcessed;$requestsPushedUp"
    }

    fun getRequestStats() = Pair(requestsProcessed,requestsPushedUp)
    fun getEarningStats() = Pair(earnings,executables.sumByDouble { it.storePrice })


    fun getStatsStringHeader(): String =
        "name;node_type;parent;storage_util;min_cpu_util;avg_cpu_util;max_cpu_util;storage_earnings;processing_earnings;requests_processed;requests_pushed_up"

    fun connectTo(other: ComputeNode, uplinkLatency:Int) {
        parentNode = other
        this.uplinkLatency = uplinkLatency
    }

}

enum class NodeType { CLOUD, EDGE, INTERMEDIARY }



