/*
 *  Copyright (c) 2022. Axon Framework
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.axonframework.intellij.ide.plugin.actions

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.logger
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType.EVENT
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType.EVENT_SOURCING
import org.axonframework.intellij.ide.plugin.resolving.handlers.types.CommandHandler
import org.axonframework.intellij.ide.plugin.resolving.handlers.types.EventHandler
import org.axonframework.intellij.ide.plugin.resolving.handlers.types.SagaEventHandler
import org.axonframework.intellij.ide.plugin.util.creatorResolver
import org.axonframework.intellij.ide.plugin.util.handlerResolver
import org.axonframework.intellij.ide.plugin.util.toShortName
import java.nio.charset.Charset
import java.util.Base64
import java.util.LinkedList

class GenerateEventModelAction : AnAction(AxonIcons.Axon) {

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }

    data class Command(val commandName: String, val aggregateName: String, val events: MutableList<Event> = mutableListOf())

    data class Event(val name: String, val views: List<String> = mutableListOf())

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val handlers = project.handlerResolver().findAllHandlers()
        val eventPayloads = handlers
            .filter { it.handlerType == EVENT_SOURCING || it.handlerType == EVENT }
            .map { it.payload }
            .distinct()
        val commandList = LinkedList<Command>()
        fun getOrCreate(commandName: String, aggregateName: String): Command =
            commandList.firstOrNull { it.aggregateName == aggregateName && it.commandName == commandName } ?: run {
                val command = Command(commandName, aggregateName)
                commandList.add(command)
                command
            }

        eventPayloads.forEach { eventPayload ->
            project.creatorResolver().getCreatorsForPayload(eventPayload)
                .filter { c -> c.parentHandler != null }
                .filter { c -> c.parentHandler is CommandHandler }
                .forEach { c ->
                    val commandHandler = c.parentHandler as CommandHandler
                    val command = getOrCreate(commandHandler.payload, commandHandler.componentName)
                    if (command.events.none { it.name == eventPayload }) {
                        val processingGroups =
                            handlers.filterIsInstance<EventHandler>().filter { it.payload == eventPayload }.map { it.processingGroup }
                                .distinct()
                        command.events.add(Event(eventPayload, processingGroups))
                    }
                }
        }
        val objectMapper = ObjectMapper().findAndRegisterModules()

        val logger = logger<GenerateEventModelAction>()
        logger.info("Eventmodel: " + objectMapper.writeValueAsString(commandList))

        val tempCommandNameMap = commandList.map { it.aggregateName }.distinct().groupBy { it.toShortName() }.toMutableMap()
        // Correct for double names
        tempCommandNameMap.filter { it.value.size > 1 }.forEach {
            // Find the first difference in package
            val packages = it.value.map { v -> v.split(".") }
            var firstDifferentIndex = 2

            var items = packages.map { p -> p[p.size - firstDifferentIndex] }
            while (items.distinct().size < it.value.size) {
                firstDifferentIndex += 1
                items = packages.map { p -> p[p.size - firstDifferentIndex] }
            }

            it.value.forEach { name ->
                val parts = name.split(".")
                val newName = parts[parts.size - firstDifferentIndex] + parts.last()
                tempCommandNameMap[newName] = listOf(name)
            }
            tempCommandNameMap.remove(it.key)
        }
        // Reverse them to a single mapping
        val commandNameToShortNameMap = mutableMapOf<String, String>()
        tempCommandNameMap.forEach { (shortName, fullName) -> commandNameToShortNameMap[fullName[0]] = shortName }
        // Determine context name for an aggregate. If another aggregate is already

        var text = commandList.groupBy { it.aggregateName }.map { (aggName, items) ->
            val actualAggName = commandNameToShortNameMap[aggName]
            var aggText = "context $actualAggName\n"
            val viewMap = mutableMapOf<String, MutableList<String>>()
            items.forEach {
                aggText += "a ${aggName.toShortName()} :: ${it.commandName.toShortName()} -> ${
                    it.events.joinToString(separator = ",") { event -> event.name.toShortName() }
                } \n"
                it.events.forEach { event ->
                    event.views.forEach { v ->
                        viewMap.computeIfAbsent(v) { LinkedList() }.add(event.name.toShortName())
                    }
                }
            }
            // Makes it go over the limit for some larger diagrams. Might re-enable later
//            viewMap.forEach { view, events ->
//                aggText += "view $view :: " + events.joinToString(separator = ",") + "\n"
//            }
            aggText
        }.joinToString(separator = "\n\n")

        // Now define the Saga's
        // We can find saga's by finding producers of commands that have a parentHandler of type Saga or Event processing.
        // We can find the origin context by finding the command handler invocation that produced the command
        val commandHandlers = handlers.filterIsInstance<CommandHandler>()
        commandHandlers.forEach { command ->
            project.creatorResolver().getCreatorsForPayload(command.payload).forEach { c ->
                if (c.parentHandler is EventHandler || c.parentHandler is SagaEventHandler) {
                    // We have a saga!
                    val destinationContext = commandNameToShortNameMap[command.componentName]
                    val destinationCommand = command.payload.toShortName()
                    val originEvent = c.parentHandler!!.payload.toShortName()
                    // Resolve origin context now, by finding the producing command handler
                    val originAgg = project.creatorResolver().getCreatorsForPayload(c.parentHandler!!.payload)
                        .mapNotNull {(it.parentHandler as? CommandHandler)?.componentName }.firstOrNull()
                    if (originAgg != null) {
                        val originContext = commandNameToShortNameMap[originAgg]
                        val sagaName = when (val ph = c.parentHandler) {
                            is EventHandler -> ph.processingGroup
                            is SagaEventHandler -> ph.processingGroup
                            else -> "Unknown"
                        }
                        text += "saga $sagaName :: $originContext $originEvent -> $destinationContext $destinationCommand\n"
                    }
                }
            }
        }

        // Output
        logger.info("Eventmodeltext: \n$text")

        val encode = String(Base64.getUrlEncoder().encode(text.toByteArray(Charset.defaultCharset())), Charset.forName("UTF-8"))
        BrowserUtil.open("https://morlack.github.io/eventmodeler/#$encode")
    }
}
