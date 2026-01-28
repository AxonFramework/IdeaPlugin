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

/**
 * Contains used Axon annotations during analysis of the source code with their fully qualified name.
 *
 * Some annotations have different package locations in different Axon versions (v4 vs v5).
 * Use [getAnnotationNameForVersion] to get the correct FQN for a specific version.
 */
enum class AxonAnnotation(
    val v4AnnotationName: String?,
    val v5AnnotationName: String?
) {
    // Handler annotations - same package in both versions
    COMMAND_HANDLER(
        "org.axonframework.commandhandling.CommandHandler",
        "org.axonframework.messaging.commandhandling.annotation.CommandHandler"
    ),
    EVENT_HANDLER(
        "org.axonframework.eventhandling.EventHandler",
        "org.axonframework.messaging.eventhandling.annotation.EventHandler"
    ),
    EVENT_SOURCING_HANDLER(
        "org.axonframework.eventsourcing.EventSourcingHandler",
        "org.axonframework.eventsourcing.annotation.EventSourcingHandler"
    ),
    QUERY_HANDLER(
        "org.axonframework.queryhandling.QueryHandler",
        "org.axonframework.messaging.queryhandling.annotation.QueryHandler"
    ),
    COMMAND_HANDLER_INTERCEPTOR(
        "org.axonframework.modelling.command.CommandHandlerInterceptor",
        null
    ),
    MESSAGE_HANDLER_INTERCEPTOR(
        "org.axonframework.messaging.interceptors.MessageHandlerInterceptor",
        null,
    ),
    SAGA_EVENT_HANDLER(
        "org.axonframework.modelling.saga.SagaEventHandler",
        null // Sagas removed in Axon 5
    ),
    DEADLINE_HANDLER(
        "org.axonframework.deadline.annotation.DeadlineHandler",
        null // Deadlines removed in Axon 5
    ),

    // Modeling annotations - package changed in v5 from .modelling.command to .modelling.entity
    AGGREGATE_ROOT(
        "org.axonframework.modelling.command.AggregateRoot",
        null // v5 uses @EventSourced instead
    ),
    AGGREGATE_MEMBER(
        "org.axonframework.modelling.command.AggregateMember", // v4
        "org.axonframework.modelling.entity.annotation.EntityMember" // v5: renamed to EntityMember
    ),
    ROUTING_KEY(
        "org.axonframework.commandhandling.RoutingKey",
        null
    ),
    ENTITY_ID(
        "org.axonframework.modelling.command.EntityId",
        null,
    ),
    PROCESSING_GROUP(
        "org.axonframework.config.ProcessingGroup",
        null
    ),

    // Axon Framework 5.x only annotations
    EVENT_SOURCED(
        null,
        "org.axonframework.extension.spring.stereotype.EventSourced" // Spring stereotype, meta-annotated with @EventSourcedEntity
    ),
    EVENT_SOURCED_ENTITY(
        null,
        "org.axonframework.eventsourcing.annotation.EventSourcedEntity" // Core annotation
    ),
    ENTITY_CREATOR(
        null,
        "org.axonframework.eventsourcing.annotation.reflection.EntityCreator"
    ),
    ;

    /**
     * Returns the annotation name for a specific Axon version.
     * @return The FQN for the annotation, or null if not supported in that version
     */
    fun getAnnotationNameForVersion(version: AxonVersion): String? {
        return when (version) {
            AxonVersion.V4 -> v4AnnotationName
            AxonVersion.V5 -> v5AnnotationName
            AxonVersion.UNKNOWN -> v4AnnotationName // Default to v4 for backward compatibility
        }
    }

    /**
     * Returns all annotation names (v4 and v5) for this annotation.
     * Useful for meta-annotation searches that should work across versions.
     */
    fun getAllAnnotationNames(): List<String> {
        return listOfNotNull(v4AnnotationName, v5AnnotationName).distinct()
    }

    /**
     * Legacy accessor for backward compatibility.
     * Defaults to v4 annotation name.
     */
    val annotationName: String
        get() = v4AnnotationName ?: v5AnnotationName ?: error("No annotation name defined for $this")
}
