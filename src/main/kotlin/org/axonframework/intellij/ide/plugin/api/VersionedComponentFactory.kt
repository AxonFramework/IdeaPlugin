/*
 *  Copyright (c) 2022-2026. Axon Framework
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.axonframework.intellij.ide.plugin.api

import org.axonframework.intellij.ide.plugin.resolving.handlers.searchers.common.AbstractHandlerSearcher

/**
 * Factory interface for creating version-specific components.
 *
 * This factory pattern allows the plugin to support multiple Axon Framework versions by providing
 * different implementations for each version. Components are instantiated based on the detected
 * Axon Framework version in the project.
 *
 * @see AxonVersion
 * @see org.axonframework.intellij.ide.plugin.usage.AxonVersionService
 */
interface VersionedComponentFactory {
    /**
     * Returns the Axon version this factory supports.
     */
    fun getVersion(): AxonVersion

    /**
     * Creates the list of handler searchers appropriate for this Axon version.
     * Different versions may have different handler types or annotations.
     *
     * @return List of handler searchers for this version
     */
    fun createHandlerSearchers(): List<AbstractHandlerSearcher>

    /**
     * Returns the fully qualified names of aggregate/entity root annotations for this version.
     * - Axon 4: @AggregateRoot
     * - Axon 5: @EventSourced, @EventSourcedEntity
     *
     * @return List of FQNs for entity annotations
     */
    fun getEntityAnnotations(): List<String>

    /**
     * Returns the terminology used for aggregates in this version.
     * - Axon 4: "Aggregate"
     * - Axon 5: "Entity"
     *
     * @return The terminology string
     */
    fun getEntityTerminology(): String
}
