package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.axonframework.intellij.ide.plugin.util.sortingByDisplayName

fun PsiElement.markerForQualifiedName(qualifiedName: String): RelatedItemLineMarkerInfo<PsiElement>? {
    val repository = project.getService(MessageHandlerResolver::class.java)
    val handlers = repository.findHandlersForType(qualifiedName)
            .sortedWith(sortingByDisplayName())
    if (handlers.isEmpty()) {
        return null
    }
    return NavigationGutterIconBuilder.create(AxonIcons.Publisher)
            .setPopupTitle("Axon Message Handlers")
            .setTooltipText("Navigate to Axon message handlers")
            .setCellRenderer(AxonCellRenderer.getInstance())
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .setTargets(NotNullLazyValue.createValue { handlers.map { it.element } })
            .createLineMarkerInfo(this)

}
