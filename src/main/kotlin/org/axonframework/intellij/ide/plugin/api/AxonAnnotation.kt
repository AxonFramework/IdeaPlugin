package org.axonframework.intellij.ide.plugin.api

import org.jetbrains.uast.UField

enum class AxonAnnotation(val annotationName: String) {
    AGGREGATE("org.axonframework.spring.stereotype.Aggregate"),
    AGGREGATE_IDENTIFIER("org.axonframework.modelling.command.AggregateIdentifier"),
    ENTITY_ID("org.axonframework.modelling.command.EntityId"),
    PROCESSING_GROUP("org.axonframework.config.ProcessingGroup")
    ;

    fun fieldIsAnnotated(field: UField): Boolean {
        return field.uAnnotations.any { it.qualifiedName == annotationName }
    }
}
