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

package org.axonframework.intellij.ide.plugin.resolving.creators

import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageCreator
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
data class DefaultMessageCreator(
    override val element: PsiElement,
    override val payload: String,
    override val parentHandler: Handler?,
) : MessageCreator {

    /**
     * Returns the correct icon for the creator
     */
    override fun getIcon(): Icon {
        return AxonIcons.Publisher
    }

    override fun renderText(): String {
        if (parentHandler != null) {
            return parentHandler.renderText()
        }
        return super.renderText()
    }
}
