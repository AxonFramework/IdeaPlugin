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
import org.axonframework.intellij.ide.plugin.util.creatorResolver
import org.axonframework.intellij.ide.plugin.util.handlerResolver
import org.axonframework.intellij.ide.plugin.util.isAggregate
import org.axonframework.intellij.ide.plugin.util.sortingByDisplayName
import org.jetbrains.uast.UClass
import org.jetbrains.uast.getUParentForIdentifier

/**
 * Provides a gutter icon on class declarations of types which are used in handlers.
 */
class ClassLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val uElement = getUParentForIdentifier(element) ?: return null
        if (uElement !is UClass || uElement.isAggregate()) {
            return null
        }

        val qualifiedName = uElement.qualifiedName ?: return null
        val handlers = element.handlerResolver().findHandlersForType(qualifiedName)
        if (handlers.isEmpty()) {
            return null
        }

        return NavigationGutterIconBuilder.create(AxonIcons.Publisher)
                .setTooltipText("Navigate to message handlers and creations")
                .setCellRenderer(AxonCellRenderer.getInstance())
                .setTargets(NotNullLazyValue.createValue {
                    val publishers = element.creatorResolver().getCreatorsForPayload(qualifiedName)
                    val allItems = handlers + publishers
                    allItems.sortedWith(sortingByDisplayName()).map { it.element }
                })
                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                .createLineMarkerInfo(element)
    }
}
