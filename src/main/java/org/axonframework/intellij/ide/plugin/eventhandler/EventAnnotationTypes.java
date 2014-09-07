package org.axonframework.intellij.ide.plugin.eventhandler;

public enum EventAnnotationTypes {
    EVENT_HANDLER("org.axonframework.eventhandling.annotation.EventHandler", "@EventHandler"),
    EVENT_SOURCING_HANDLER("org.axonframework.eventsourcing.annotation.EventSourcingHandler", "@EventSourcingHandler"),
    SAGA_EVENT_HANDLER("org.axonframework.saga.annotation.SagaEventHandler", "@SagaEventHandler", "associationProperty");

    private final String fullyQualifiedName;
    private final String annotation;
    private final String requiredProperty;

    EventAnnotationTypes(String fullyQualifiedName, String annotation) {
        this(fullyQualifiedName, annotation, null);
    }

    EventAnnotationTypes(String fullyQualifiedName, String annotation, String requiredProperty) {
        this.fullyQualifiedName = fullyQualifiedName;
        this.annotation = annotation;
        this.requiredProperty = requiredProperty;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public String getAnnotation() {
        return annotation;
    }

    public String getRequiredProperty() {
        return requiredProperty;
    }
}
