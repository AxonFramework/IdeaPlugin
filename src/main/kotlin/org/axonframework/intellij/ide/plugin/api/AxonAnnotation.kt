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

package org.axonframework.intellij.ide.plugin.api

/**
 * Contains used Axon annotations during analysis of the source code with their fully qualified name.
 */
enum class AxonAnnotation(val annotationName: String) {
    COMMAND_HANDLER("org.axonframework.commandhandling.CommandHandler"),
    EVENT_HANDLER("org.axonframework.eventhandling.EventHandler"),
    EVENT_SOURCING_HANDLER("org.axonframework.eventsourcing.EventSourcingHandler"),
    QUERY_HANDLER("org.axonframework.queryhandling.QueryHandler"),
    COMMAND_HANDLER_INTERCEPTOR("org.axonframework.modelling.command.CommandHandlerInterceptor"),
    SAGA_EVENT_HANDLER("org.axonframework.modelling.saga.SagaEventHandler"),
    DEADLINE_HANDLER("org.axonframework.deadline.annotation.DeadlineHandler"),
    RESET_HANDLER("org.axonframework.eventhandling.ResetHandler"),

    AGGREGATE_ROOT("org.axonframework.modelling.command.AggregateRoot"),
    AGGREGATE_MEMBER("org.axonframework.modelling.command.AggregateMember"),
    ROUTING_KEY("org.axonframework.commandhandling.RoutingKey"),
    ENTITY_ID("org.axonframework.modelling.command.EntityId"),
    PROCESSING_GROUP("org.axonframework.config.ProcessingGroup")
    ;
}
