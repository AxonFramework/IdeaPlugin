package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClassLiteralExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.UTypeReferenceExpression
import org.jetbrains.uast.UastCallKind
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.getQualifiedName
import org.jetbrains.uast.getUParentForIdentifier
import org.jetbrains.uast.toUElement

/**
 * Provides a gutter icon on constructor invocations when that type is also known as a message payload.
 * It is known as a message payload when a handler can be found for it.
 *
 * This one is only for Java files. Kotlin and Java apparently have different UAST trees as well.
 *
 * @see MessageHandlerResolver
 */
class JavaPublishMethodLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val referenceExpression = getUParentForIdentifier(element) as? USimpleNameReferenceExpression ?: return null
        val uElementParent = element.parent.parent.toUElement()
        val isConstructor = uElementParent is UCallExpression && uElementParent.kind == UastCallKind.CONSTRUCTOR_CALL
        val isClassReference = uElementParent is UTypeReferenceExpression && uElementParent.uastParent is UClassLiteralExpression && uElementParent.getParentOfType<UAnnotation>() == null
        if (!isConstructor && !isClassReference) {
            return null
        }
        val qualifiedName = referenceExpression.getQualifiedName() ?: return null
        return element.markerForQualifiedName(qualifiedName)
    }
}
