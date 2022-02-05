package org.axonframework.intellij.ide.plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.logger
import org.axonframework.intellij.ide.plugin.handlers.types.AggregateConstructor
import org.axonframework.intellij.ide.plugin.handlers.types.CommandHandler
import org.axonframework.intellij.ide.plugin.resolving.MessageCreationResolver
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.axonframework.intellij.ide.plugin.util.areAssignable

val log = logger<EventModelingAction>()

class EventModelingAction : AnAction() {
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        log.info("EventModelingAction performed!")
        val handlerResolver = e.project!!.getService(MessageHandlerResolver::class.java)
        val handlers = handlerResolver.findAllHandlers()

        val creatorResolver = e.project!!.getService(MessageCreationResolver::class.java)
        val creators = creatorResolver.resolveAllCreators()

        val commandHandlers = handlers.filter { it is CommandHandler || it is AggregateConstructor }
        val commandPayloads = commandHandlers.map { it.payloadFullyQualifiedName }.distinct()
        commandPayloads.forEach { payload ->
            val handlersForPayload = commandHandlers.filter { areAssignable(e.project!!, it.payloadFullyQualifiedName, payload) }

            // Now find events that are produced by these commands
            val events = creators.filter { it.parentHandler != null && handlersForPayload.contains(it.parentHandler) }
            log.info("$payload -> ${events.map { it.payloadFullyQualifiedName.split(".").last() }}")

        }
    }
}
