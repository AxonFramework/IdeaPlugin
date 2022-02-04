package org.axonframework.intellij.ide.plugin.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.TestSourcesFilter
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import org.axonframework.intellij.ide.plugin.api.PsiElementWrapper
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.containingClass

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
        renderElementText(elementA).compareTo(renderElementText(elementB))
    }
}

/**
 * Renders the name of the Psi Element. Currently shows `ContainingClass.methodName`.
 *
 * @return PSI element description text
 */
fun renderElementText(element: PsiElement): String {
    val ktMethodParent = PsiTreeUtil.findFirstParent(element) { it is KtNamedFunction } as KtNamedFunction?
    if (ktMethodParent != null) {
        return ktMethodParent.containingClass()?.name + "." + ktMethodParent.name
    }
    val javaMethodParent = PsiTreeUtil.findFirstParent(element) { it is PsiMethod } as PsiMethod?
    if (javaMethodParent != null) {
        return javaMethodParent.containingClass?.name + "." + javaMethodParent.name
    }

    return element.containingFile.name
}

/**
 * Renders the container text in the line marker popup. Contains additional contextual information such as the
 * containing aggregate or the processing group.
 *
 * @return PSI element container text
 */
fun renderElementContainerText(element: PsiElement): String? {
    val methodParent = PsiTreeUtil.findFirstParent(element) { it is PsiMethod } as PsiMethod? ?: return null
    val handler = element.project.getService(MessageHandlerResolver::class.java).findHandlerByElement(methodParent)
            ?: return null
    return handler.renderContainerText()
}
