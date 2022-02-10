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
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.api.MessageType
import org.axonframework.intellij.ide.plugin.handlers.types.CommandHandler
import org.axonframework.intellij.ide.plugin.resolving.MessageCreationResolver
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.axonframework.intellij.ide.plugin.util.annotationResolver
import org.axonframework.intellij.ide.plugin.util.handlerResolver
import org.axonframework.intellij.ide.plugin.util.resolvePayloadType
import org.axonframework.intellij.ide.plugin.util.sortingByDisplayName
import org.axonframework.intellij.ide.plugin.util.toQualifiedName
import org.jetbrains.kotlin.j2k.getContainingClass
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UIdentifier
import org.jetbrains.uast.UNamedExpression
import org.jetbrains.uast.getContainingUMethod
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.toUElementOfType

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
        val uElement = element.toUElementOfType<UIdentifier>() ?: return null
        val uAnnotation = uElement.getParentOfType<UAnnotation>() ?: return null
        if (uElement.getParentOfType<UNamedExpression>() != null) {
            // If we don't return here, all identifiers in an annotation will trigger this marker.
            // E.q. `@CommandHandler(payloadType = BookRoomCommand.class)` will make 3
            return null
        }
        val annotationName = uAnnotation.qualifiedName ?: return null
        val method = uAnnotation.getContainingUMethod() ?: return null


        // Resolve what the handling type is of the annotation
        val handlerType = element.annotationResolver().getMessageTypeForAnnotation(annotationName) ?: return null
        val qualifiedName = method.javaPsi.resolvePayloadType()?.toQualifiedName() ?: return null

        if (handlerType == MessageHandlerType.COMMAND_INTERCEPTOR) {
            // Special case, show all command handlers that it intercepts
            return createCommandInterceptorLineMarker(element, qualifiedName)
        }

        return createPayloadCreatorLineMarker(element, qualifiedName)
    }

    private fun createPayloadCreatorLineMarker(element: PsiElement, qualifiedName: String): RelatedItemLineMarkerInfo<PsiElement> {
        return NavigationGutterIconBuilder.create(AxonIcons.Handler)
                .setPopupTitle("Payload Creators")
                .setTooltipText("Navigate to creation of message payload")
                .setCellRenderer(AxonCellRenderer.getInstance())
                .setTargets(NotNullLazyValue.createValue {
                    val publisherResolver = element.project.getService(MessageCreationResolver::class.java)
                    publisherResolver.getCreatorsForPayload(qualifiedName)
                            .sortedWith(sortingByDisplayName())
                            .map { it.element }
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
