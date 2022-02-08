package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.axonframework.intellij.ide.plugin.util.toQualifiedName
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClassLiteralExpression
import org.jetbrains.uast.UIdentifier
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.UastCallKind
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.getQualifiedName
import org.jetbrains.uast.toUElementOfType

/**
 * Provides a gutter icon on constructor invocations when that type is also known as a message payload.
 * It is known as a message payload when a handler can be found for it.
 *
 * This one is only for Kotlin files. Kotlin and Java apparently have different UAST trees as well.
 *
 * @see MessageHandlerResolver
 */
class KotlinPublishMethodLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val uElement = element.toUElementOfType<UIdentifier>() ?: return null
        val callExpression = uElement.getParentOfType(UCallExpression::class.java, false, USimpleNameReferenceExpression::class.java)
        if (callExpression != null && callExpression.kind == UastCallKind.CONSTRUCTOR_CALL) {
            val qualifiedName = callExpression.classReference.getQualifiedName() ?: return null
            return element.markerForQualifiedName(qualifiedName)
        }
        val qualifiedReference = uElement.getParentOfType(UClassLiteralExpression::class.java, true, UIdentifier::class.java)
        if (qualifiedReference != null) {
            val qualifiedName = qualifiedReference.type.toQualifiedName() ?: return null
            return element.markerForQualifiedName(qualifiedName)
        }
        return null
    }
}
