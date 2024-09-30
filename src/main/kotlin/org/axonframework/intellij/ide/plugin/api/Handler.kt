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
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.util.toShortName
import javax.swing.Icon

/**
 * Parent interface of any Handler, providing methods to describe the handler in interface elements.
 *
 * @see org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
 * @see org.axonframework.intellij.ide.plugin.resolving.handlers.types.AbstractHandlerSearcher
 */
interface Handler : PsiElementWrapper {
    /**
     * The PsiElement of the handler.
     */
    override val element: PsiElement

    /**
     * The type of the handler, used for filtering based on the handler type.
     * @see MessageHandlerType
     */
    val handlerType: MessageHandlerType

    /**
     * Fully qualified name of the payload being created.
     */
    val payload: String

    /**
     * Renders the main text in line marker popups. By default, it just shows the name of the payload's class,
     * but can be overridden by specific handlers.
     */
    override fun renderText(): String = payload.toShortName()

    override fun getIcon(): Icon {
        return AxonIcons.Handler
    }
}
