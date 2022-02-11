/*
 *  Copyright (c) 2022. Axon Framework
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.handlers.types.DeadlineHandler
import org.axonframework.intellij.ide.plugin.util.handlerResolver
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UIdentifier
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.toUElementOfType

/**
 * Shows a gutter icon whenever an element's string matches one of the deadline handlers.
 *
 * @see DeadlineHandler
 */
class DeadlineNameMethodLineMarker : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val uElement = element.toUElementOfType<UIdentifier>() ?: return null
        val nameReference = uElement.getParentOfType<USimpleNameReferenceExpression>() ?: return null
        if (uElement.getParentOfType<UAnnotation>() != null) {
            return null
        }
        val name = nameReference.evaluate() as String? ?: return null
        val handlers = element.project.handlerResolver().findAllHandlers()
                .filterIsInstance<DeadlineHandler>()
                .filter { it.deadlineName == name }
        if (handlers.isEmpty()) {
            return null
        }

        return NavigationGutterIconBuilder.create(AxonIcons.DeadlineHandler)
                .setPopupTitle("Axon Deadline Handlers")
                .setTooltipText("Navigate to Axon deadline handlers")
                .setCellRenderer(AxonCellRenderer.getInstance())
                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                .setTargets(NotNullLazyValue.createValue { handlers.map { it.element } })
                .createLineMarkerInfo(element)
    }
}
