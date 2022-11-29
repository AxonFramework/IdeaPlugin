/*
 *  Copyright (c) (2010-2022). Axon Framework
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

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.util.toViewText
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.toUElement
import javax.swing.Icon

/**
 * Parent interface of any creator or handler. Represents a wrapped PsiElement.
 *
 * @see Handler
 * @see MessageCreator
 */
interface PsiElementWrapper {
    val element: PsiElement


    /**
     * Renders the main text in line marker popups.
     */
    fun renderText(): String {
        val methodParent = element.toUElement()?.getParentOfType<UMethod>()
        if (methodParent != null) {
            return methodParent.toViewText()
        }

        if (element is PsiClass) {
            return (element as PsiClass).name ?: element.containingFile.name
        }

        return element.containingFile.name
    }

    /**
     * Renders the grey text next to the initial identifier. Is optional, and by default empty, but can be overridden
     * by specific implementations
     *
     * @return Container text used in a line marker popup.
     */
    fun renderContainerText(): String? = null

    /**
     * Returns the correct icon for the handler, should be implemented by each implementor of Handler.
     *
     * @return The correct icon to be used in a line marker popup
     */
    fun getIcon(): Icon

    /**
     * Retrieve the key to sort the list with. By default takes icon and text into account to show it nicely categorized.
     * Override this method for customized behavior.
     */
    fun getSortKey() = getIcon().toString() + renderText()
}
