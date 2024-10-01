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

package org.axonframework.intellij.ide.plugin.resolving.handlers.types

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.util.toShortName

/**
 * Represents a method being able to handle an event for sourcing an aggregate.
 *
 * @param entity The fully qualified name of the aggregate class sourced by the event
 * @See org.axonframework.intellij.ide.plugin.resolving.handlers.searchers.EventSourcingHandlerSearcher
 */
data class EventSourcingHandler(
    override val element: PsiMethod,
    override val payload: String,
    val entity: String,
) : Handler, PsiElement by element {
    override val handlerType: MessageHandlerType = MessageHandlerType.EVENT_SOURCING

    override fun renderText(): String {
        return "EventSourcingHandler " + entity.toShortName()
    }
}
