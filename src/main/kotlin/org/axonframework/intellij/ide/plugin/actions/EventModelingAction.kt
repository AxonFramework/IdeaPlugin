package org.axonframework.intellij.ide.plugin.actions

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.axonframework.intellij.ide.plugin.creators.DefaultMessageCreator
import org.axonframework.intellij.ide.plugin.handlers.types.AggregateConstructor
import org.axonframework.intellij.ide.plugin.handlers.types.CommandHandler
import org.axonframework.intellij.ide.plugin.handlers.types.EventProcessorHandler
import org.axonframework.intellij.ide.plugin.handlers.types.SagaEventHandler
import org.axonframework.intellij.ide.plugin.resolving.MessageCreationResolver
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.axonframework.intellij.ide.plugin.util.areAssignable
import org.axonframework.intellij.ide.plugin.util.axonScope
import java.io.File
import java.nio.charset.Charset

val log = logger<EventModelingAction>()

class EventModelingAction : AnAction() {
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    enum class EventModelEntityType {
        AGGREGATE,
        ENTITY,
        OTHER
    }

    data class EventModelEventItem(
            val event: String,
            val entityType: EventModelEntityType,
            val entityName: String,
            val views: List<String>,
            val triggersSagas: List<String>,
    )

    data class EventModelItem(
            val command: String,
            val events: List<EventModelEventItem>,
            val sagaTriggers: List<SagaTriggers>,
    )

    data class SagaTriggers(
            val saga: String,
            val command: String
    )

    override fun actionPerformed(e: AnActionEvent) {
        log.info("EventModelingAction performed!")
        val project = e.project!!
        val handlerResolver = project.getService(MessageHandlerResolver::class.java)
        val handlers = handlerResolver.findAllHandlers()

        val creatorResolver = project.getService(MessageCreationResolver::class.java)
        val creators = creatorResolver.resolveAllCreators()

        val commandHandlers = handlers.filterIsInstance<CommandHandler>()
        val commandPayloads = commandHandlers.map { it.payloadFullyQualifiedName }.distinct()
        val commands = commandPayloads.map { payload ->
            val handlersForPayload = commandHandlers.filter { areAssignable(project, it.payloadFullyQualifiedName, payload) }

            // Now find events that are produced by these commands
            val eventsCreatedDuringCommand = creators.filter { it.parentHandler != null && handlersForPayload.contains(it.parentHandler) }
                    .flatMap { creator ->
                        // Special case. An aggregate could have been constructed, not directly building an event.
                        // Find the events created in the constructor, and execute some replace magic
                        // so they are matched
                        val constructorHandlers = handlers.filterIsInstance<AggregateConstructor>()
                                .filter { it.payloadFullyQualifiedName == creator.payloadFullyQualifiedName }
                        if (constructorHandlers.isNotEmpty()) {
                            constructorHandlers.flatMap {
                                creators.filter { c -> c.parentHandler == it }
                                        .filterIsInstance<DefaultMessageCreator>()
                                        .map { c -> c.copy(parentHandler = creator.parentHandler) }
                            }
                        } else listOf(creator)
                    }

            val events = (eventsCreatedDuringCommand)
                    .filter { it.parentHandler is CommandHandler }
                    .map { creator ->
                        val parentHandler = creator.parentHandler as CommandHandler
                        val views = handlers.filterIsInstance<EventProcessorHandler>()
                                .filter { it.payloadFullyQualifiedName == creator.payloadFullyQualifiedName }
                                .map { handler -> handler.processingGroup }
                                .distinct()

                        val sagaHandlers = handlers.filterIsInstance<SagaEventHandler>()
                                .filter { saga -> saga.payloadFullyQualifiedName == creator.payloadFullyQualifiedName }
                                .map { saga -> saga.processingGroup }
                                .distinct()

                        EventModelEventItem(
                                creator.payloadFullyQualifiedName,
                                resolveModelType(project, parentHandler.modelFqn),
                                parentHandler.modelFqn,
                                views,
                                sagaHandlers
                        )
                    }

            val triggeredBySagas = creators
                    .filter { it.parentHandler is SagaEventHandler }
                    .filter { it.payloadFullyQualifiedName == payload }
                    .map { SagaTriggers((it.parentHandler as SagaEventHandler).processingGroup, it.payloadFullyQualifiedName) }
                    .distinct()
            EventModelItem(payload.toShortName(), events, triggeredBySagas)
        }

        val json = ObjectMapper().findAndRegisterModules().writeValueAsString(commands)
        log.info("Result: $commands \n$json")

        val content = IOUtils.readLines(javaClass.classLoader.getResourceAsStream("index.html"), Charset.defaultCharset()).joinToString(separator = "\n").replace("__EVENT_MODEL__", json)
        FileUtils.writeStringToFile(File(project.basePath + "/.axon/index.html"), content, Charset.defaultCharset())
        BrowserUtil.browse(project.basePath + "/.axon/index.html", project)
    }

    private fun resolveModelType(project: Project, modelFqn: String): EventModelEntityType {
        val clazz = JavaPsiFacade.getInstance(project).findClass(modelFqn, project.axonScope())
                ?: return EventModelEntityType.OTHER
        if (clazz.hasAnnotation("org.axonframework.spring.stereotype.Aggregate")) {
            return EventModelEntityType.AGGREGATE
        }
        if (clazz.allFields.any { it.hasAnnotation("org.axonframework.modelling.command.EntityId") }) {
            return EventModelEntityType.ENTITY
        }
        return EventModelEntityType.OTHER
    }

    private fun String.toShortName() = this.split(".").last()
}
