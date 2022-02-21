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
fun PsiElement.toElementText(): String = cacheData("Axon_elementText") {
    val handler = handlerResolver().findHandlerByElement(this)
    if (handler != null) {
        return@cacheData handler.renderText()
    }

    val creator = creatorResolver().findCreatorByElement(this)
    if (creator?.parentHandler != null) {
        return@cacheData creator.parentHandler!!.renderText()
    }

    val deadlineResolver = deadlineReferenceResolver()
    val deadline = deadlineResolver.findByElement(this)
    if (deadline?.parentHandler != null) {
        return@cacheData deadline.parentHandler!!.renderText()
    }

    val methodParent = toUElement()?.getParentOfType<UMethod>()
    if (methodParent != null) {
        return@cacheData methodParent.containingClassname() + "." + methodParent.name
    }

    if (this is PsiClass) {
        return@cacheData this.name ?: this.containingFile.name
    }

    return@cacheData this.containingFile.name
}

/**
 * Creates a description of the element's container as used in lists.
 *
 * @see org.axonframework.intellij.ide.plugin.markers.AxonCellRenderer
 */
fun PsiElement.toContainerText(): String? = cacheData("Axon_containerText") {
    val handler = handlerResolver().findHandlerByElement(this)
    if (handler != null) {
        return@cacheData handler.renderContainerText()
    }
    return@cacheData null
}

/**
 * Determines the icon that should be shown for this PsiElement
 *
 * @see org.axonframework.intellij.ide.plugin.markers.AxonCellRenderer
 */
fun PsiElement.toIcon(): Icon = cacheData("Axon_icon") {
    val handler = handlerResolver().findHandlerByElement(this)
    if (handler != null) {
        return@cacheData handler.getIcon()
    }
    val creator = creatorResolver().findCreatorByElement(this)
    if (creator != null) {
        return@cacheData creator.parentHandler?.getIcon() ?: creator.icon
    }
    val deadline = deadlineReferenceResolver().findByElement(this)
    if (deadline?.parentHandler != null) {
        return@cacheData deadline.parentHandler?.getIcon() ?: deadline.icon
    }
    return@cacheData AxonIcons.Axon
}
