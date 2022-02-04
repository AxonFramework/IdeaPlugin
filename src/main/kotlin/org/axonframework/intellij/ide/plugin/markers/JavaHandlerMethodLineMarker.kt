package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.psi.PsiClassObjectAccessExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil

/**
 * Shows a line marker at a method defition if it is also a handler.
 * Navigates to creators of the payload.
 */
class JavaHandlerMethodLineMarker : AbstractHandlerLineMarker() {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element is PsiIdentifier && element.parent is PsiMethod) {
            val method = element.parent as PsiMethod
            return createLineMarker(element, method)
        }

        // Check if a .class reference is there. If so, show line marker (might be for queryEmitter for example)
        if (element is PsiIdentifier && element.parent is PsiJavaCodeReferenceElement && PsiTreeUtil.findFirstContext(element, true) { it is PsiClassObjectAccessExpression } != null) {
            val qualifiedName = (element.parent as PsiJavaCodeReferenceElement).qualifiedName ?: return null
            return createLineMarker(element, qualifiedName)
        }

        return null
    }
}
