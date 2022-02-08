package org.axonframework.intellij.ide.plugin.api

import org.jetbrains.uast.UField

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

    AGGREGATE("org.axonframework.spring.stereotype.Aggregate"),
    AGGREGATE_IDENTIFIER("org.axonframework.modelling.command.AggregateIdentifier"),
    TARGET_AGGREGATE_IDENTIFIER("org.axonframework.modelling.command.TargetAggregateIdentifier"),
    ENTITY_ID("org.axonframework.modelling.command.EntityId"),
    PROCESSING_GROUP("org.axonframework.config.ProcessingGroup")
    ;

    fun fieldIsAnnotated(field: UField): Boolean {
        return field.uAnnotations.any { it.qualifiedName == annotationName }
    }
}
