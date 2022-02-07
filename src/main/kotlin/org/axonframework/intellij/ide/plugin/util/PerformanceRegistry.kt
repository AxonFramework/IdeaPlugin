package org.axonframework.intellij.ide.plugin.util

import org.jetbrains.kotlin.utils.addToStdlib.measureTimeMillisWithResult
import java.util.concurrent.ConcurrentHashMap

enum class PerformanceSubject {
    AnnotationResolverCompute,
    MessageCreationResolverResolve,
    MessageCreationResolverFindParents,
    MessageCreationResolverFindParentsRecursive,
    MessageHandlerResolverResolve,
    UtilCheckAssignableInheritors,
    UtilResolvePayloadType,
}

/**
 * In order to support the plugin as well as possible we need to know what parts of the plugin eat performance.
 */
object PerformanceRegistry {
    private val measurements = ConcurrentHashMap<PerformanceSubject, PerformanceMeasurement>()

    fun <T> measure(subject: PerformanceSubject, block: () -> T): T {
        val result = measureTimeMillisWithResult(block)
        measurements.computeIfAbsent(subject) { PerformanceMeasurement(subject) }.addValue(result.first)
        return result.second
    }

    fun getMeasurements() = measurements.values
}
