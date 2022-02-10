/*
 *  Copyright (c) 2022. Axon Framework
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
