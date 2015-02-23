package org.axonframework.intellij.ide.plugin.handler;

public enum AnnotationTypes {
    EVENT_HANDLER("org.axonframework.eventhandling.annotation.EventHandler", "@EventHandler"),
    EVENT_SOURCING_HANDLER("org.axonframework.eventsourcing.annotation.EventSourcingHandler", "@EventSourcingHandler"),
    SAGA_EVENT_HANDLER("org.axonframework.saga.annotation.SagaEventHandler", "@SagaEventHandler", "associationProperty"),
    COMMAND_EVENT_HANDLER("org.axonframework.commandhandling.annotation.CommandHandler", "@CommandHandler");

    private final String fullyQualifiedName;
    private final String annotation;
    private final String requiredProperty;

    AnnotationTypes(String fullyQualifiedName, String annotation) {
        this(fullyQualifiedName, annotation, null);
    }

    AnnotationTypes(String fullyQualifiedName, String annotation, String requiredProperty) {
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

    public boolean isCommand() {
        return this.annotation.equals("@CommandHandler");
    }
}
