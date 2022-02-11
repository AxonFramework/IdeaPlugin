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

package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.api.MessageType
import org.axonframework.intellij.ide.plugin.handlers.types.CommandHandler
import org.axonframework.intellij.ide.plugin.resolving.MessageCreationResolver
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.axonframework.intellij.ide.plugin.util.annotationResolver
import org.axonframework.intellij.ide.plugin.util.handlerResolver
import org.axonframework.intellij.ide.plugin.util.isAggregate
import org.axonframework.intellij.ide.plugin.util.resolveAnnotationStringValue
import org.axonframework.intellij.ide.plugin.util.resolvePayloadType
import org.axonframework.intellij.ide.plugin.util.sortingByDisplayName
import org.axonframework.intellij.ide.plugin.util.toQualifiedName
import org.jetbrains.kotlin.j2k.getContainingClass
import org.jetbrains.kotlin.utils.addToStdlib.firstNotNullResult
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UIdentifier
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UNamedExpression
import org.jetbrains.uast.getContainingUClass
import org.jetbrains.uast.getContainingUMethod
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.toUElement
import org.jetbrains.uast.toUElementOfType
import javax.swing.Icon

/**
 * Provides a gutter icon on handler annotations. These contain the places where the message payload was created,
 * for quick navigation.
 *
 * Also provides an Interceptor icon on CommandHandlerInterceptors specifically, containing all command handlers
 * that are intercepted by that interceptor.
 *
 * @see MessageHandlerResolver
 * @see MessageCreationResolver
 */
class HandlerMethodLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val (handlerType, qualifiedName) = getInfoForAnnotatedFunction(element)
                ?: getInfoForNonAnnotatedFunction(element) ?: return null

        if (handlerType == MessageHandlerType.DEADLINE) {
            // Special case, deadline name can be specified in annotation, but payload type is also valid. Match on both
            val method = element.toUElement()?.getParentOfType<UAnnotation>()?.getContainingUMethod()
            return createPayloadCreatorLineMarker(element, listOfNotNull(
                    method?.resolveAnnotationStringValue(AxonAnnotation.DEADLINE_HANDLER, "deadlineName"),
                    qualifiedName
            ), AxonIcons.DeadlinePublisher)
        }

        if (handlerType == MessageHandlerType.COMMAND_INTERCEPTOR) {
            // Special case, show all command handlers that it intercepts
            return createCommandInterceptorLineMarker(element, qualifiedName)
        }

        // Generic, shows all constructors of the payload type
        return createPayloadCreatorLineMarker(element, listOf(qualifiedName), AxonIcons.Handler)
    }

    private fun getInfoForAnnotatedFunction(element: PsiElement): Pair<MessageHandlerType, String>? {
        val uElement = element.toUElementOfType<UIdentifier>() ?: return null
        val uAnnotation = uElement.getParentOfType<UAnnotation>() ?: return null
        if (uElement.getParentOfType<UNamedExpression>() != null) {
            // If we don't return here, all identifiers in an annotation will trigger this marker.
            // E.q. `@CommandHandler(payloadType = BookRoomCommand.class)` will make 3
            return null
        }
        val annotationName = uAnnotation.qualifiedName ?: return null
        val method = uAnnotation.getContainingUMethod() ?: return null
        val handlerType = element.annotationResolver().getMessageTypeForAnnotation(annotationName) ?: return null
        val qualifiedName = method.javaPsi.resolvePayloadType()?.toQualifiedName() ?: return null
        return Pair(handlerType, qualifiedName)
    }

    private fun getInfoForNonAnnotatedFunction(element: PsiElement): Pair<MessageHandlerType, String>? {
        val uElement = element.toUElementOfType<UIdentifier>() ?: return null
        val method = uElement.uastParent as? UMethod ?: return null
        if (method.isConstructor && method.uastParameters.isNotEmpty() && method.getContainingUClass().isAggregate()) {
            val qualifiedName = method.getContainingUClass()?.qualifiedName ?: return null
            return Pair(MessageHandlerType.COMMAND, qualifiedName)
        }
        return null
    }

    private fun createPayloadCreatorLineMarker(element: PsiElement, payloads: List<String>, icon: Icon): RelatedItemLineMarkerInfo<PsiElement> {
        return NavigationGutterIconBuilder.create(icon)
                .setPopupTitle("Payload Creators")
                .setTooltipText("Navigate to creation of message payload")
                .setCellRenderer(AxonCellRenderer.getInstance())
                .setTargets(NotNullLazyValue.createValue {
                    val publisherResolver = element.project.getService(MessageCreationResolver::class.java)
                    payloads
                            .firstNotNullResult { payload ->
                                // Resolve the first hit. Useful with deadlines, first match on name, if not
                                // found, it matches by payload type.
                                val result = publisherResolver.getCreatorsForPayload(payload)
                                result.ifEmpty { null }
                            }
                            ?.distinctBy { it.parentHandler }
                            ?.sortedWith(sortingByDisplayName())
                            ?.map { it.element }
                            ?: emptyList()

                })
                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                .setEmptyPopupText("No creators of this message payload were found")
                .createLineMarkerInfo(element)
    }

    /**
     * Creates an interceptor icon in the gutter, containing all handler methods that handle relevant commands.
     *
     * Note: It will currently only match the aggregate/entity fully qualified name. This is different from Axon,
     * since if a CommandHandlerInterceptor is defined in the Aggregate, commands of entities can also be intercepted,
     */
    private fun createCommandInterceptorLineMarker(element: PsiElement, qualifiedName: String): RelatedItemLineMarkerInfo<PsiElement> {
        val handlers = element.handlerResolver()
                .findHandlersForType(qualifiedName, MessageType.COMMAND)
                .filterIsInstance<CommandHandler>()
                .filter { it.componentName == element.getContainingClass()?.getQualifiedName() }
                .sortedWith(sortingByDisplayName())
        return NavigationGutterIconBuilder.create(AxonIcons.Interceptor)
                .setPopupTitle("Commands Intercepted")
                .setTooltipText("Navigate to command handlers that are intercepted")
                .setCellRenderer(AxonCellRenderer.getInstance())
                .setTargets(handlers.map { it.element })
                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                .setEmptyPopupText("No intercepted command handlers were found")
                .createLineMarkerInfo(element)
    }
}
