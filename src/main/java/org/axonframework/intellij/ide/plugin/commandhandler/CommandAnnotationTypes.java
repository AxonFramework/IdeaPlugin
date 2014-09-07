package org.axonframework.intellij.ide.plugin.commandhandler;

public enum CommandAnnotationTypes {
    COMMAND_HANDLER("org.axonframework.eventhandling.annotation.CommandHandler", "@CommandHandler");

    private final String fullyQualifiedName;
    private final String annotation;

    CommandAnnotationTypes(String fullyQualifiedName, String annotation) {
        this.fullyQualifiedName = fullyQualifiedName;
        this.annotation = annotation;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public String getAnnotation() {
        return annotation;
    }

}
