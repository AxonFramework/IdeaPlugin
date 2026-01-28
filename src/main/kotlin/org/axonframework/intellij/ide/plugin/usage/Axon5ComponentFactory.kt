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

package org.axonframework.intellij.ide.plugin.usage

import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.api.AxonVersion
import org.axonframework.intellij.ide.plugin.api.VersionedComponentFactory
import org.axonframework.intellij.ide.plugin.resolving.handlers.searchers.common.AbstractHandlerSearcher
import org.axonframework.intellij.ide.plugin.resolving.handlers.searchers.common.CommandHandlerSearcher
import org.axonframework.intellij.ide.plugin.resolving.handlers.searchers.common.EventHandlerSearcher
import org.axonframework.intellij.ide.plugin.resolving.handlers.searchers.common.EventSourcingHandlerSearcher
import org.axonframework.intellij.ide.plugin.resolving.handlers.searchers.common.QueryHandlerSearcher
import org.axonframework.intellij.ide.plugin.resolving.handlers.searchers.axon5.EntityCreatorSearcher

/**
 * Component factory for Axon Framework 5.x projects.
 *
 * Provides v5-specific components including:
 * - @EventSourced and @EventSourcedEntity annotation support
 * - @EntityCreator for event-based entity construction
 * - Entity-based terminology (not "Aggregate")
 * - Updated package names for modeling annotations
 */
class Axon5ComponentFactory : VersionedComponentFactory {
    override fun getVersion(): AxonVersion = AxonVersion.V5

    override fun createHandlerSearchers(): List<AbstractHandlerSearcher> {
        return listOf(
            CommandHandlerSearcher(),
            EventHandlerSearcher(),
            EventSourcingHandlerSearcher(),
            QueryHandlerSearcher(),
            EntityCreatorSearcher(),
        )
    }

    override fun getEntityAnnotations(): List<String> {
        return listOfNotNull(AxonAnnotation.EVENT_SOURCED_ENTITY.getAnnotationNameForVersion(AxonVersion.V5))
    }

    override fun getEntityTerminology(): String {
        return "Entity"
    }
}
