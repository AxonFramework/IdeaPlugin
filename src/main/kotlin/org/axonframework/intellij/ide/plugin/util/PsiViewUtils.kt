package org.axonframework.intellij.ide.plugin.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.TestSourcesFilter
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.api.PsiElementWrapper
import org.axonframework.intellij.ide.plugin.markers.AxonCellRenderer

fun Project.sortingByDisplayName() = { elementA: PsiElementWrapper, elementB: PsiElementWrapper ->
    comparePsiElementsBasedOnDisplayName(this, elementA.element, elementB.element)
}

/**
 * Sorts the provided PSI elements. It's sorted by:
 * - Whether it's in a test source file
 * - Its name
 */
fun comparePsiElementsBasedOnDisplayName(project: Project, elementA: PsiElement, elementB: PsiElement): Int {
    val testSourceCompare = TestSourcesFilter.isTestSources(elementA.containingFile.virtualFile, project)
            .compareTo(TestSourcesFilter.isTestSources(elementB.containingFile.virtualFile, project))

    return if (testSourceCompare != 0) {
        testSourceCompare
    } else {
        val renderer = AxonCellRenderer.getInstance()
        renderer.getElementText(elementA).compareTo(renderer.getElementText(elementB))
    }
}

fun String.toShortName() = split(".").last()
