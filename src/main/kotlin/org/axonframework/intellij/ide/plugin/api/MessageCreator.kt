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

package org.axonframework.intellij.ide.plugin.api

import com.intellij.psi.PsiElement
import javax.swing.Icon

/**
 * Represents an element that creates a message (payload).
 *
 * @see org.axonframework.intellij.ide.plugin.resolving.MessageCreationResolver
 */
interface MessageCreator : PsiElementWrapper {
    /**
     * The PsiElement that is creating the payload.
     */
    override val element: PsiElement

    /**
     * Fully qualified name of the payload being created.
     */
    val payload: String

    /**
     * The parent handler that published the message. For example, if this MessageCreator represents an event
     * created by a CommandHandler, the parentHandler will be that CommandHandler.
     * The same applied for commands created by a SagaEventHandler, among others.
     */
    val parentHandler: Handler?

    /**
     * Renders the grey text next to the initial identifier.
     */
    val containerText: String?

    /**
     * Returns the correct icon for the creator, based on the parent handler type.
     */
    val icon: Icon
}

