package org.axonframework.intellij.ide.plugin.api

enum class MessageHandlerType(
        val annotationName: String,
) {
    COMMAND("org.axonframework.commandhandling.CommandHandler"),
    EVENT("org.axonframework.eventhandling.EventHandler"),
    EVENT_SOURCING("org.axonframework.eventsourcing.EventSourcingHandler"),
    QUERY("org.axonframework.queryhandling.QueryHandler"),
    COMMAND_INTERCEPTOR("org.axonframework.modelling.command.CommandHandlerInterceptor"),
    ;

    companion object {
        fun exists(annotationName: String?): Boolean {
            if(annotationName == null) {
                return false
            }
            return values().any { type -> type.annotationName == annotationName }
        }
    }
}
