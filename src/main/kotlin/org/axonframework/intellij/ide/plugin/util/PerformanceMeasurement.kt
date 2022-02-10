package org.axonframework.intellij.ide.plugin.util

/**
 * Represents a measurement of the PerformanceRegistry.
 * Keeps track of the total time spent on something, its total executions and min/max values.
 *
 * @see PerformanceRegistry
 */
data class PerformanceMeasurement(
        val name: String,
) {
    var min: Long = Long.MAX_VALUE
    var max: Long = 0
    var total: Long = 0

    var count: Int = 0

    fun addValue(value: Long) {
        if (value < min) {
            min = value
        }
        if (value > max) {
            max = value
        }
        count += 1
        total += value
    }

    val avg: Double
        get() = total.toDouble() / count
}
