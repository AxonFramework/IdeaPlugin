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
import com.fasterxml.jackson.databind.SerializationFeature
import com.intellij.json.JsonLanguage
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.testFramework.LightVirtualFile
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.api.Model
import org.axonframework.intellij.ide.plugin.resolving.AnnotationResolver
import org.axonframework.intellij.ide.plugin.resolving.MessageCreationResolver
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.axonframework.intellij.ide.plugin.util.aggregateResolver
import org.jetbrains.kotlin.idea.debugger.getService

/**
 * Action that can be invoked using the "Dump Axon Plugin Information" in IntelliJ.
 * It will open a new window with a JSON file (virtual, not on disk) that can be sent with a bug report.
 *
 * The JSON contains all information about what the plugin detects in terms of annotations, message handlers and
 * creators, as well as the text it renders for them.
 *
 * Note: This will contain all commands and events of an application; might be sensitive.
 */
class DumpAxonInformationAction : AnAction() {
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val annotations = project.getService<AnnotationResolver>().getAllAnnotations().map {
            val children = it.value.map { ann ->
                AnnotationWithParent(
                    ann.psiClass.qualifiedName,
                    ann.parent?.psiClass?.qualifiedName
                )
            }
            AnnotationInfo(
                it.key,
                children
            )
        }
        val handlers = project.getService<MessageHandlerResolver>().findAllHandlers().map {
            it.toInfo()
        }
        val creators = project.getService<MessageCreationResolver>().resolveAllCreators().map {
            CreatorInfo(it.payload, it.parentHandler?.toInfo(), it.containerText)
        }

        val info = AxonInfo(annotations, handlers, creators, project.aggregateResolver().getModels())
        val mapper = ObjectMapper().findAndRegisterModules().enable(SerializationFeature.INDENT_OUTPUT)
        val dump = mapper.writeValueAsString(info)
        FileEditorManager.getInstance(project).openEditor(
            OpenFileDescriptor(project, LightVirtualFile("axon-dump.json", JsonLanguage.INSTANCE, dump)),
            true
        )
    }

    private fun Handler.toInfo() =
        HandlerInfo(handlerType, this::class.java.simpleName, payload, renderText(), renderContainerText())

    private data class AxonInfo(
        val annotations: List<AnnotationInfo>,
        val handlers: List<HandlerInfo>,
        val creators: List<CreatorInfo>,
        val models: List<Model>,
    )

    private data class CreatorInfo(
        val payload: String,
        val parentHandler: HandlerInfo?,
        val containerText: String?,
    )

    private data class HandlerInfo(
        val handlerType: MessageHandlerType,
        val handlerClass: String,
        val payload: String,
        val renderText: String,
        val containerText: String?,
    )

    private data class AnnotationInfo(
        val type: AxonAnnotation,
        val annotations: List<AnnotationWithParent>
    )

    private data class AnnotationWithParent(
        val annotation: String?,
        val annotationParent: String?,
    )
}
