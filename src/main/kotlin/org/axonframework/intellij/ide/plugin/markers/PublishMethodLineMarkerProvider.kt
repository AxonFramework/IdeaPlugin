package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.axonframework.intellij.ide.plugin.util.sortingByDisplayName
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClassLiteralExpression
import org.jetbrains.uast.UIdentifier
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.UTypeReferenceExpression
import org.jetbrains.uast.UastCallKind
import org.jetbrains.uast.getQualifiedName
import org.jetbrains.uast.toUElement

/**
 * Provides a gutter icon on constructor invocations when that type is also known as a message paylaod.
 */
class PublishMethodLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element.toUElement() !is UIdentifier) {
            return null
        }
        val uElement = element.parent.toUElement()
        val uElementParent = element.parent.parent.toUElement()
        val isConstructor = uElementParent is UCallExpression && uElementParent.kind == UastCallKind.CONSTRUCTOR_CALL
        val isClassReference = uElementParent is UTypeReferenceExpression && uElementParent.uastParent is UClassLiteralExpression
        if (uElement !is USimpleNameReferenceExpression || (!isConstructor && !isClassReference)) {
            return null
        }

        val qualifiedName = uElement.getQualifiedName() ?: return null
        val repository = element.project.getService(MessageHandlerResolver::class.java)
        val handlers = repository.findHandlersForType(qualifiedName)
                .sortedWith(element.project.sortingByDisplayName())
        if (handlers.isEmpty()) {
            return null
        }
        return NavigationGutterIconBuilder.create(AxonIcons.Publisher)
                .setTooltipText("Navigate to Axon handlers")
                .setCellRenderer(AxonCellRenderer.getInstance())
                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                .setTargets(NotNullLazyValue.createValue { handlers.map { it.element } })
                .createLineMarkerInfo(element)
    }
}
