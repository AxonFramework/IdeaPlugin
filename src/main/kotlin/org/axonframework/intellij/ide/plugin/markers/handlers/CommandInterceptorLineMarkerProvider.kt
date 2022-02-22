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
import org.axonframework.intellij.ide.plugin.api.MessageType
import org.axonframework.intellij.ide.plugin.markers.AxonGutterIconBuilder
import org.axonframework.intellij.ide.plugin.resolving.handlers.types.CommandHandler
import org.axonframework.intellij.ide.plugin.util.aggregateResolver
import org.axonframework.intellij.ide.plugin.util.handlerResolver
import org.axonframework.intellij.ide.plugin.util.sortingByDisplayName
import org.jetbrains.uast.getContainingUClass
import org.jetbrains.uast.toUElement

/**
 * Creates an interceptor icon in the gutter, containing all handler methods that are intercepted by this
 * interceptor. Includes aggregate members, using the `AggregateStructureResolver`
 *
 * @see org.axonframework.intellij.ide.plugin.resolving.AggregateStructureResolver
 */
class CommandInterceptorLineMarkerProvider : AbstractHandlerLineMarkerProvider() {

    override fun createLineMarker(
        element: PsiElement,
        handlerType: MessageHandlerType,
        payload: String?,
    ): LineMarkerInfo<*>? {
        if (handlerType != MessageHandlerType.COMMAND_INTERCEPTOR) {
            return null
        }
        val className = element.toUElement()?.getContainingUClass()?.qualifiedName ?: return null
        // An interceptor without payload is fine, default to Object to match all
        val actualPayload = payload ?: "java.lang.Object"

        return AxonGutterIconBuilder(AxonIcons.Interceptor)
            .setPopupTitle("Commands Intercepted")
            .setTooltipText("Navigate to command handlers that are intercepted")
            .setTargets(NotNullLazyValue.createValue {
                val members = element.aggregateResolver().getMemberWithSubEntities(className)
                element.handlerResolver().findHandlersForType(actualPayload, MessageType.COMMAND)
                    .filterIsInstance<CommandHandler>()
                    .filter {
                        val name = it.element.toUElement()?.getContainingUClass()?.qualifiedName
                        members.any { member -> member.name == name }
                    }
                    .sortedWith(sortingByDisplayName())
                    .map { it.element }
            })
            .setEmptyPopupText("No intercepted command handlers were found")
            .createLineMarkerInfo(element)
    }
}
