/*
 *  Copyright (c) (2010-2022). Axon Framework
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

package org.axonframework.intellij.ide.plugin.markers.handlers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.api.MessageType
import org.axonframework.intellij.ide.plugin.markers.AxonNavigationGutterIconRenderer
import org.axonframework.intellij.ide.plugin.resolving.handlers.types.CommandHandlerInterceptor
import org.axonframework.intellij.ide.plugin.util.creatorResolver
import org.axonframework.intellij.ide.plugin.util.handlerResolver

/**
 * Provides a gutter icon on command handlers, switching to an intercepted icon if necessary
 */
class CommandHandlerMethodLineMarkerProvider : AbstractHandlerLineMarkerProvider() {
    override fun createLineMarker(
        element: PsiElement,
        handlerType: MessageHandlerType,
        payload: String?,
    ): LineMarkerInfo<*>? {
        if (handlerType != MessageHandlerType.COMMAND || payload == null) {
            return null
        }

        val interceptingElements = element.handlerResolver().findHandlersForType(payload, MessageType.COMMAND, true)
            .filterIsInstance<CommandHandlerInterceptor>()

        val icon = if (interceptingElements.isNotEmpty()) AxonIcons.HandlerIntercepted else AxonIcons.Handler
        return AxonNavigationGutterIconRenderer(
            icon = icon,
            popupTitle = "Payload Creators",
            tooltipText = "Navigate to creators of $payload",
            emptyText = "No creators of this message payload were found",
            elements = ValidatingLazyValue(element) {
                val creatingElements = element.creatorResolver().getCreatorsForPayload(payload)
                interceptingElements + creatingElements
            })
            .createLineMarkerInfo(element)
    }
}
