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

package org.axonframework.intellij.ide.plugin.util

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.PsiElementWrapper
import org.axonframework.intellij.ide.plugin.resolving.MessageCreationResolver
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.toUElement
import javax.swing.Icon

fun sortingByDisplayName() = { elementA: PsiElementWrapper, elementB: PsiElementWrapper ->
    comparePsiElementsBasedOnDisplayName(elementA.element, elementB.element)
}

/**
 * Sorts the provided PSI elements. It's sorted by:
 * - Whether it's in a test source file
 * - Its name
 */
fun comparePsiElementsBasedOnDisplayName(elementA: PsiElement, elementB: PsiElement): Int {
    return elementA.toElementText().compareTo(elementB.toElementText())
}

fun String.toShortName() = split(".").last()

/**
 * Creates a description of the element as used in lists.
 *
 * @see org.axonframework.intellij.ide.plugin.markers.AxonCellRenderer
 */
fun PsiElement.toElementText(): String {
    val handlerResolver = project.getService(MessageHandlerResolver::class.java)
    val handler = handlerResolver.findHandlerByElement(this)
    if (handler != null) {
        return handler.renderText()
    }

    val creatorResolver = project.getService(MessageCreationResolver::class.java)
    val creator = creatorResolver.findCreatorByElement(this)
    if (creator?.parentHandler != null) {
        return creator.parentHandler!!.renderText()
    }

    val methodParent = toUElement()?.getParentOfType<UMethod>()
    if (methodParent != null) {
        return methodParent.containingClassname() + "." + methodParent.name
    }

    if (this is PsiClass) {
        return this.name ?: this.containingFile.name
    }

    return this.containingFile.name
}

/**
 * Creates a description of the element's container as used in lists.
 *
 * @see org.axonframework.intellij.ide.plugin.markers.AxonCellRenderer
 */
fun PsiElement.toContainerText(): String? {
    val handlerResolver = project.getService(MessageHandlerResolver::class.java)

    val handler = handlerResolver.findHandlerByElement(this)
    if (handler != null) {
        return handler.renderContainerText()
    }
    return null
}

/**
 * Determines the icon that should be shown for this PsiElement
 *
 * @see org.axonframework.intellij.ide.plugin.markers.AxonCellRenderer
 */
fun PsiElement.toIcon(): Icon {
    val handler = project.getService(MessageHandlerResolver::class.java).findHandlerByElement(this)
    if (handler != null) {
        return handler.getIcon()
    }
    val creator = project.getService(MessageCreationResolver::class.java).findCreatorByElement(this)
    if (creator != null) {
        return creator.parentHandler?.getIcon() ?: creator.icon
    }
    return AxonIcons.Axon
}
