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

package org.axonframework.intellij.ide.plugin.markers

import com.intellij.ide.util.PsiElementListCellRenderer
import com.intellij.openapi.util.Iconable.ICON_FLAG_VISIBILITY
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.util.toContainerText
import org.axonframework.intellij.ide.plugin.util.toElementText
import org.axonframework.intellij.ide.plugin.util.toIcon
import javax.swing.Icon

/**
 * Renders the listed items in line markers. The texts and icons are provided by the implementations of the `Handler`
 * and `MessageCreator` classes.
 *
 * Singleton instance. Please call `AxonCellRenderer.getInstance()` to get the instance.
 *
 * @see org.axonframework.intellij.ide.plugin.api.Handler
 * @see org.axonframework.intellij.ide.plugin.api.MessageCreator
 */
class AxonCellRenderer private constructor() : PsiElementListCellRenderer<PsiElement>() {
    companion object {
        private val renderer = lazy { AxonCellRenderer() }
        fun getInstance() = renderer.value
    }

    /**
     * Renders the text in the line marker popup. Contains the name of the item
     *
     * @return PSI element text
     */
    override fun getElementText(element: PsiElement): String = element.toElementText()

    /**
     * Renders the container text in the line marker popup. Contains additional contextual information.
     *
     * @return PSI element container text
     */
    override fun getContainerText(element: PsiElement, name: String?): String? = element.toContainerText()

    override fun getIconFlags(): Int {
        return ICON_FLAG_VISIBILITY
    }

    /**
     * Instructs the list which icon to show
     *
     * @return PSI element icon
     */
    override fun getIcon(element: PsiElement): Icon = element.toIcon()
}
