package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.jetbrains.kotlin.idea.debugger.sequence.psi.resolveType
import org.jetbrains.kotlin.idea.refactoring.fqName.fqName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

class KotlinPublishMethodLineMarker : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element.parent !is KtNameReferenceExpression || element.parent.parent !is KtCallExpression) {
            return null
        }
        val resolvedType = (element.parent.parent as KtCallExpression?)?.resolveType() ?: return null
        val fq = resolvedType.fqName ?: return null
        val project = element.project
        val scope = GlobalSearchScope.allScope(project)
        val type = JavaPsiFacade.getInstance(project).elementFactory.createTypeByFQClassName(fq.asString(), scope)

        val repository = element.project.getService(MessageHandlerResolver::class.java)
        val handlers = repository.findHandlersForType(type)
        if (handlers.isEmpty()) {
            return null
        }
        return NavigationGutterIconBuilder.create(AxonIcons.Publisher)
                .setTooltipText("Navigate to Axon handlers")
                .setPopupTitle("Navigate To Axon Handlers")
                .setCellRenderer(AxonCellRenderer.getInstance())
                .setTargets(NotNullLazyValue.createValue { handlers.map { it.element } })
                .createLineMarkerInfo(element)

    }
}
