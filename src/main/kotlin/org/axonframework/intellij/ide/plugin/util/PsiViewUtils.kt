package org.axonframework.intellij.ide.plugin.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.TestSourcesFilter
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.containingClass

fun comparePsiElementsBasedOnDisplayName(project: Project, elementA: PsiElement, elementB: PsiElement): Int {
    val testSourceCompare = TestSourcesFilter.isTestSources(elementA.containingFile.virtualFile, project).compareTo(
            TestSourcesFilter.isTestSources(elementB.containingFile.virtualFile, project)
    )
    return if (testSourceCompare != 0) {
        testSourceCompare
    } else {
        renderElementText(elementA).compareTo(renderElementText(elementB))
    }
}

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

fun renderElementContainerText(element: PsiElement): String? {
    val methodParent = PsiTreeUtil.findFirstParent(element) { it is PsiMethod } as PsiMethod? ?: return null
    val handler = element.project.getService(MessageHandlerResolver::class.java).findHandlerByElement(methodParent)
            ?: return null
    if (handler.handlerType == MessageHandlerType.COMMAND_INTERCEPTOR) {
        return "Interceptor of ${methodParent.containingClass?.name}"
    }
    if (handler.handlerType == MessageHandlerType.SAGA) {
        return "Saga: " + handler.processingGroup
    }
    if (handler.handlerType == MessageHandlerType.EVENT_SOURCING || handler.handlerType == MessageHandlerType.COMMAND) {
        return "Aggregate: ${methodParent.containingClass?.name}"
    }
    return handler.processingGroup?.let { "ProcessingGroup: $it" }
}
