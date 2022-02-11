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

package org.axonframework.intellij.ide.plugin.creators

import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageCreator
import org.axonframework.intellij.ide.plugin.handlers.types.CommandHandler
import org.axonframework.intellij.ide.plugin.handlers.types.DeadlineHandler
import org.axonframework.intellij.ide.plugin.handlers.types.EventSourcingHandler
import org.axonframework.intellij.ide.plugin.handlers.types.SagaEventHandler
import org.axonframework.intellij.ide.plugin.util.toShortName
import javax.swing.Icon

/**
 * Default implementation of a `MessageCreator`, handling the container text and icons to be shown for it.
 *
 * If MessageCreators diverge in the future (e.g. require too different functionality), other implementations can be
 * made and constructed in the `MessageCreationResolver`
 *
 * @see MessageCreator
 * @see org.axonframework.intellij.ide.plugin.resolving.MessageCreationResolver
 */
data class DefaultMessageCreator(override val element: PsiElement,
                                 override val payload: String?,
                                 override val name: String?,
                                 override val parentHandler: Handler?) : MessageCreator {
    /**
     * Renders the grey text next to the initial identifier.
     *
     * If the parent handler is EventSourcingHandler, it means that the message is published from the aggregate while
     * an event is being applied to the source. We show a warning here that it's a side effect of it.
     *
     * If the parent handler is a Saga, add the Saga to qualify the event better.
     */
    override val containerText = when (parentHandler) {
        is EventSourcingHandler -> "Side effect of EventSourcingHandler"
        is SagaEventHandler -> "Saga ${parentHandler.processingGroup}"
        is DeadlineHandler -> "Deadline: ${parentHandler.deadlineName.toShortName()}"
        else -> null
    }

    /**
     * Returns the correct icon for the creator, based on the parent handler type.
     */
    override val icon: Icon = when (parentHandler) {
        is CommandHandler, is EventSourcingHandler -> AxonIcons.Model
        is SagaEventHandler -> AxonIcons.Saga
        is DeadlineHandler -> AxonIcons.DeadlineHandler
        else -> AxonIcons.Publisher
    }
}
