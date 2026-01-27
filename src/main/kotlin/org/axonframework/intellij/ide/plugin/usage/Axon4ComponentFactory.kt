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
import org.axonframework.intellij.ide.plugin.resolving.handlers.searchers.axon4.AggregateConstructorSearcher
import org.axonframework.intellij.ide.plugin.resolving.handlers.searchers.axon4.DeadlineHandlerSearcher
import org.axonframework.intellij.ide.plugin.resolving.handlers.searchers.axon4.SagaEventHandlerSearcher

/**
 * Component factory for Axon Framework 4.x projects.
 *
 * Provides v4-specific components including:
 * - @AggregateRoot annotation support
 * - Aggregate-based constructor searcher
 * - Traditional aggregate terminology
 */
class Axon4ComponentFactory : VersionedComponentFactory {
    override fun getVersion(): AxonVersion = AxonVersion.V4

    override fun createHandlerSearchers(): List<AbstractHandlerSearcher> {
        return listOf(
            CommandHandlerSearcher(),
            EventHandlerSearcher(),
            EventSourcingHandlerSearcher(),
            QueryHandlerSearcher(),
            SagaEventHandlerSearcher(),
            AggregateConstructorSearcher(),
            DeadlineHandlerSearcher(),
        )
    }

    override fun getEntityAnnotations(): List<String> {
        return listOfNotNull(AxonAnnotation.AGGREGATE_ROOT.getAnnotationNameForVersion(AxonVersion.V4))
    }

    override fun getEntityTerminology(): String {
        return "Aggregate"
    }
}
