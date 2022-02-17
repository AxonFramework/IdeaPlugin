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

package org.axonframework.intellij.ide.plugin.markers.handlers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.api.MessageType
import org.axonframework.intellij.ide.plugin.handlers.types.CommandHandlerInterceptor
import org.axonframework.intellij.ide.plugin.markers.AxonCellRenderer
import org.axonframework.intellij.ide.plugin.util.creatorResolver
import org.axonframework.intellij.ide.plugin.util.handlerResolver
import org.axonframework.intellij.ide.plugin.util.sortingByDisplayName

/**
 * Provides a gutter icon on all generic handler methods
 */
class CommonHandlerMethodLineMarkerProvider : AbstractHandlerLineMarkerProvider() {
    override fun createLineMarker(
        element: PsiElement,
        handlerType: MessageHandlerType,
        payload: String?
    ): LineMarkerInfo<*>? {
        if (handlerType == MessageHandlerType.DEADLINE || handlerType == MessageHandlerType.COMMAND_INTERCEPTOR || payload == null) {
            return null
        }

        val interceptingElements = if (handlerType == MessageHandlerType.COMMAND) {
            element.handlerResolver().findHandlersForType(payload, MessageType.COMMAND, true)
                .filterIsInstance<CommandHandlerInterceptor>()
                .sortedWith(sortingByDisplayName())
                .map { it.element }
        } else emptyList()

        val icon = if (interceptingElements.isEmpty()) AxonIcons.Handler else AxonIcons.HandlerIntercepted
        return NavigationGutterIconBuilder.create(icon)
            .setPopupTitle("Payload Creators")
            .setTooltipText("Navigate to creation of message payload")
            .setCellRenderer(AxonCellRenderer.getInstance())
            .setTargets(NotNullLazyValue.createValue {
                val creatingElements = element.creatorResolver().getCreatorsForPayload(payload)
                    .distinctBy { it.parentHandler }
                    .sortedWith(sortingByDisplayName())
                    .map { it.element }
                interceptingElements + creatingElements
            })
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .setEmptyPopupText("No creators of this message payload were found")
            .createLineMarkerInfo(element)
    }
}
