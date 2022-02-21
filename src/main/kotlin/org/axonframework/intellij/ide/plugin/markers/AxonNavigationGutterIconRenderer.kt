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

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.codeInsight.navigation.NavigationGutterIconRenderer
import com.intellij.codeInsight.navigation.NavigationUtil
import com.intellij.ide.util.PsiElementListCellRenderer
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.util.PsiUtilCore
import com.intellij.ui.awt.RelativePoint
import java.awt.event.MouseEvent
import javax.swing.Icon


/**
 * Gutter renderer that is modified to always show a popup list, even when there is only one option.
 *
 * Used by the AxonGutterIconBuilder.
 *
 * @see AxonGutterIconBuilder
 */
class AxonNavigationGutterIconRenderer(
    popupTitle: String,
    emptyText: String?,
    private val alignment: Alignment,
    private val icon: Icon,
    private val tooltipText: String?,
    pointers: NotNullLazyValue<List<SmartPsiElementPointer<*>>>,
    cellRenderer: Computable<PsiElementListCellRenderer<*>>,
) : NavigationGutterIconRenderer(popupTitle, emptyText, cellRenderer, pointers) {
    override fun getIcon(): Icon {
        return icon
    }

    override fun getTooltipText(): String? {
        return tooltipText
    }

    override fun getAlignment(): Alignment {
        return alignment
    }

    override fun navigateToItems(event: MouseEvent?) {
        if (event != null) {
            val elements = PsiUtilCore.toPsiElementArray(targetElements)
            val popup = NavigationUtil.getPsiElementPopup(elements, myCellRenderer.compute(), myPopupTitle)
            popup.show(RelativePoint(event))
        }
    }
}

/**
 * The default gutter icon builder of IntelliJ extended to use the AxonNavigationGutterIconRenderer.
 *
 * @See AxonNavigationGutterIconRenderer
 */
class AxonGutterIconBuilder(icon: Icon) :
    NavigationGutterIconBuilder<PsiElement>(icon, DEFAULT_PSI_CONVERTOR, PSI_GOTO_RELATED_ITEM_PROVIDER) {
    override fun createGutterIconRenderer(
        pointers: NotNullLazyValue<List<SmartPsiElementPointer<*>>>,
        renderer: Computable<PsiElementListCellRenderer<*>>,
        empty: Boolean
    ): NavigationGutterIconRenderer {
        return AxonNavigationGutterIconRenderer(myPopupTitle, myEmptyText, myAlignment, myIcon, myTooltipText, pointers, renderer)
    }
}
