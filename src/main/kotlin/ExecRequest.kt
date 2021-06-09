package de.tuberlin.mcc.faas4fogsim

import java.text.DecimalFormat

data class ExecRequest(val executable: Executable, val execPrice: Double, val plannedStart: Int) {
    var totalLatency: Int = 0
    var executionNode: String = "not executed"
    var executionNodeType: NodeType? = null
    var actualStart = plannedStart
    var executionCompleted = false

    /**
     * invoke when executing this request
     * @param node name of the node executing this request
     * @param nodeType type (cloud/edge/intermediary) of the node executing the request
     */
    fun execute(node: String, nodeType: NodeType, latency: Int) {
        totalLatency += latency
        executionNode = node
        executionNodeType = nodeType
        executionCompleted = true
        //println("Executed ${executable.name} on node $node ($nodeType): price=$execPrice, totalLatency = $totalLatency, execLatency=${executable.execLatency}")
    }

    /**
     * invoke when pushing a request to another node
     * @param latency the latency for transmitting the request to the next node
     */
    fun pushTowardsCloud(latency: Int) {
        //  println("push2Cloud invoked: $latency")
        totalLatency += latency
        actualStart += latency
    }
}


data class Executable(val size: Double, val name: String, val storePrice: Double, val execLatency: Int)


fun Number.asPercent() = DecimalFormat("0.0000%").format(this)
fun Number.asDecimal() = DecimalFormat("0.0000").format(this)





