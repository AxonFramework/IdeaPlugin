package org.axonframework.intellij.ide.plugin.util

data class PerformanceMeasurement(
        val name: PerformanceSubject,
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
