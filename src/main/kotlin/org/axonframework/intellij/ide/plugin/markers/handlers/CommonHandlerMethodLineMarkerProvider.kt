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
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.markers.AxonNavigationGutterIconRenderer
import org.axonframework.intellij.ide.plugin.util.creatorResolver

/**
 * Provides a gutter icon on all generic handler methods
 */
class CommonHandlerMethodLineMarkerProvider : AbstractHandlerLineMarkerProvider() {
    private val blacklistedTypes = listOf(MessageHandlerType.DEADLINE, MessageHandlerType.COMMAND, MessageHandlerType.COMMAND_INTERCEPTOR)
    override fun createLineMarker(
        element: PsiElement,
        handlerType: MessageHandlerType,
        payload: String?,
    ): LineMarkerInfo<*>? {
        if (blacklistedTypes.contains(handlerType) || payload == null) {
            return null
        }

        return AxonNavigationGutterIconRenderer(
            icon = AxonIcons.Handler,
            popupTitle = "Payload Creators",
            tooltipText = "Navigate to creators of $payload",
            emptyText = "No creators of this message payload were found",
            elements = NotNullLazyValue.createValue {
                element.creatorResolver().getCreatorsForPayload(payload)
                    .distinctBy { it.parentHandler }
            }).createLineMarkerInfo(element)
    }
}
