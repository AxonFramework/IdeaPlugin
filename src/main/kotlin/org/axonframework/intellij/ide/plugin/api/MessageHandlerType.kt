package org.axonframework.intellij.ide.plugin.api


enum class MessageType {
    COMMAND,
    EVENT,
    QUERY,
}

enum class MessageHandlerType(
        val annotationName: String,
        val messageType: MessageType,
) {
    COMMAND("org.axonframework.commandhandling.CommandHandler", MessageType.COMMAND),
    EVENT("org.axonframework.eventhandling.EventHandler", MessageType.EVENT),
    EVENT_SOURCING("org.axonframework.eventsourcing.EventSourcingHandler", MessageType.EVENT),
    QUERY("org.axonframework.queryhandling.QueryHandler", MessageType.QUERY),
    COMMAND_INTERCEPTOR("org.axonframework.modelling.command.CommandHandlerInterceptor", MessageType.COMMAND),
    SAGA("org.axonframework.modelling.saga.SagaEventHandler", MessageType.EVENT),
    ;

    companion object {
        fun exists(annotationName: String?): Boolean {
            if (annotationName == null) {
                return false
            }
            return values().any { type -> type.annotationName == annotationName }
        }
    }
}

