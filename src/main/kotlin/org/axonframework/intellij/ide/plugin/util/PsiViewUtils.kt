package org.axonframework.intellij.ide.plugin.util

import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.api.PsiElementWrapper
import org.axonframework.intellij.ide.plugin.markers.AxonCellRenderer

fun sortingByDisplayName() = { elementA: PsiElementWrapper, elementB: PsiElementWrapper ->
    comparePsiElementsBasedOnDisplayName(elementA.element, elementB.element)
}

/**
 * Sorts the provided PSI elements. It's sorted by:
 * - Whether it's in a test source file
 * - Its name
 */
fun comparePsiElementsBasedOnDisplayName(elementA: PsiElement, elementB: PsiElement): Int {
    val renderer = AxonCellRenderer.getInstance()
    return renderer.getElementText(elementA).compareTo(renderer.getElementText(elementB))
}

fun String.toShortName() = split(".").last()
