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

package org.axonframework.intellij.ide.plugin.handlers.types

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.util.containingClassname

/**
 * Represents a method being able to handle an event. There are more specific event handlers (`EventSourcingHandler` and
 * `SagaEventHandler`) that are not included here. These have their own representation, despite being meta-annotated
 * by `@EventHandler`
 *
 * @param processingGroup The name of the component handling the event, based on package or ProcessingGroup annotation
 *
 * @See org.axonframework.intellij.ide.plugin.handlers.searchers.EventHandlerSearcher
 * @see SagaEventHandler
 * @see EventSourcingHandler
 */
data class EventHandler(
    override val element: PsiMethod,
    override val payload: String,
    val processingGroup: String,
) : Handler {
    override val handlerType: MessageHandlerType = MessageHandlerType.EVENT

    override fun renderText(): String {
        return element.containingClassname().ifEmpty { "Event Processor" }
    }

    override fun renderContainerText(): String {
        return processingGroup
    }
}
