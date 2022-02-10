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
