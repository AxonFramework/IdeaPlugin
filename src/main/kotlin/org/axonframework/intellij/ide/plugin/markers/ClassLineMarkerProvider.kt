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
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.util.aggregateResolver
import org.axonframework.intellij.ide.plugin.util.axonScope
import org.axonframework.intellij.ide.plugin.util.creatorResolver
import org.axonframework.intellij.ide.plugin.util.handlerResolver
import org.axonframework.intellij.ide.plugin.util.isAggregate
import org.axonframework.intellij.ide.plugin.util.javaFacade
import org.axonframework.intellij.ide.plugin.util.sortingByDisplayName
import org.jetbrains.uast.UClass
import org.jetbrains.uast.getUParentForIdentifier

/**
 * Provides a gutter icon on class declarations of types which are used in handlers.
 *
 * Alternatively, if the class is part of an aggregate(-member) hierarchy, show an icon to navigate to all handlers in the hierarcy.
 */
class ClassLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val uElement = getUParentForIdentifier(element) ?: return null
        if (uElement !is UClass) {
            return null
        }
        val qualifiedName = uElement.qualifiedName ?: return null
        if (!uElement.isAggregate()) {
            val handlers = element.handlerResolver().findHandlersForType(qualifiedName)
            if (handlers.isNotEmpty()) {
                return AxonGutterIconBuilder(AxonIcons.Axon)
                    .setPopupTitle("Axon References To This Class")
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

        val parents = element.aggregateResolver()
            .getAllModelsRelatedToName(qualifiedName)
            .mapNotNull {
                element.javaFacade().findClass(it.name, element.project.axonScope())
            }
        if (parents.isNotEmpty()) {
            return AxonGutterIconBuilder(AxonIcons.Axon)
                .setPopupTitle("Related Models")
                .setTooltipText("Navigate to members in the same model hierarchy")
                .setCellRenderer(AxonCellRenderer.getInstance())
                .setTargets(NotNullLazyValue.createValue { parents })
                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                .createLineMarkerInfo(element)

        }

        return null
    }
}
