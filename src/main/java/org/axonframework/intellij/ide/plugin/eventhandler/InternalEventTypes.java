package org.axonframework.intellij.ide.plugin.eventhandler;

public enum InternalEventTypes {
    ABSTRACT_ANNOTATED_AGGREGATE_ROOT("org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot"),
    ABSTRACT_ANNOTATED_ENTITY("org.axonframework.eventsourcing.annotation.AbstractAnnotatedEntity");

    private final String fullyQualifiedName;

    InternalEventTypes(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }
}
