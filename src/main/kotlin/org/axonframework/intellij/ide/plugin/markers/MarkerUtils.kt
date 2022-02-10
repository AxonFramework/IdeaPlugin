package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.util.handlerResolver
import org.axonframework.intellij.ide.plugin.util.sortingByDisplayName

/**
 * Creates a line marker containing the handlers for a given payload.
 *
 * @param payload payload represented by qualified name
 * @return An Axon publisher line marker
 */
fun PsiElement.markerForQualifiedName(payload: String): RelatedItemLineMarkerInfo<PsiElement>? {
    val handlers = handlerResolver().findHandlersForType(payload)
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
