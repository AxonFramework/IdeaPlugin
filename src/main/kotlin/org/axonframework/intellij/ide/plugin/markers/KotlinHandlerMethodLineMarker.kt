package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.axonframework.intellij.ide.plugin.resolving.MessagePublisherResolver
import org.jetbrains.kotlin.asJava.toLightMethods
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.psi.KtNamedFunction

class KotlinHandlerMethodLineMarker : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element.parent !is KtNamedFunction || element !is LeafPsiElement || element.elementType !is KtModifierKeywordToken) {
            return null
        }
        val method = element.parent.toLightMethods().getOrNull(0) ?: return null;
        val repository = element.project.getService(MessageHandlerResolver::class.java)
        val handler = repository.findHandlerByElement(method) ?: return null

        val publisherResolver = element.project.getService(MessagePublisherResolver::class.java)
        val resolvers = publisherResolver.getPublishersForType(handler.payloadType)
        return NavigationGutterIconBuilder.create(AxonIcons.Handler)
                .setTooltipText("Navigate to ${handler.messageType.displayName()} constructor")
                .setCellRenderer(AxonCellRenderer.getInstance())
                .setTargets(resolvers)
                .createLineMarkerInfo(element)

    }
}
