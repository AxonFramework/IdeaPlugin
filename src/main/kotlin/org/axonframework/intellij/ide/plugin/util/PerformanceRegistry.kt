package org.axonframework.intellij.ide.plugin.util

import org.jetbrains.kotlin.utils.addToStdlib.measureTimeMillisWithResult
import java.util.concurrent.ConcurrentHashMap

/**
 * In order to support the plugin as well as possible we need to know what parts of the plugin eat performance.
 *
 * You can wrap the execution of a function in the `measure` function with an identifier you want to track the
 * performance under. Then, you can dump the information using the `DumpAxonPerformanceAction`
 *
 * @see org.axonframework.intellij.ide.plugin.actions.DumpAxonPerformanceAction
 */
object PerformanceRegistry {
    private val measurements = ConcurrentHashMap<String, PerformanceMeasurement>()

    /**
     * Measures the execution of a code block in milliseconds, registers the result and then returns the invoked
     * block's result.
     *
     * @param subject The subject key for logging the performance
     * @param block The code block to execute
     */
    fun <T> measure(subject: String, block: () -> T): T {
        val result = measureTimeMillisWithResult(block)
        measurements.computeIfAbsent(subject) { PerformanceMeasurement(subject) }.addValue(result.first)
        return result.second
    }

    /**
     * Gets all measurements for display purposes.
     */
    fun getMeasurements() = measurements.values
}
