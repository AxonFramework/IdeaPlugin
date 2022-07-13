/*
 *  Copyright (c) (2010-2022). Axon Framework
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
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconRenderer
import com.intellij.codeInsight.navigation.NavigationUtil
import com.intellij.navigation.GotoRelatedItem
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.util.PsiUtilCore
import com.intellij.ui.awt.RelativePoint
import org.axonframework.intellij.ide.plugin.api.PsiElementWrapper
import java.awt.event.MouseEvent
import javax.swing.Icon


/**
 * Gutter renderer that is modified to always show a popup list, even when there is only one option.
 *
 */
class AxonNavigationGutterIconRenderer(
    private val icon: Icon,
    popupTitle: String,
    private val tooltipText: String?,
    emptyText: String?,
    private val elements: NotNullLazyValue<List<PsiElementWrapper>>,
) : NavigationGutterIconRenderer(popupTitle, emptyText, { AxonCellRenderer(elements) }, NotNullLazyValue.createValue {
    elements.value.sortedBy { it.getSortKey() }.mapNotNull { p ->
        if(p.element.isValid) {
            val spm = SmartPointerManager.getInstance(p.element.project)
            spm.createSmartPsiElementPointer(p.element)
        } else null
    }
}) {

    override fun getIcon(): Icon {
        return icon
    }

    override fun getTooltipText(): String? {
        return tooltipText
    }

    override fun getAlignment(): Alignment {
        return Alignment.LEFT
    }

    override fun navigateToItems(event: MouseEvent?) {
        if (event != null) {
            val elements = PsiUtilCore.toPsiElementArray(targetElements.filter { it.isValid })
            val popup = NavigationUtil.getPsiElementPopup(elements, myCellRenderer.compute(), myPopupTitle)
            popup.show(RelativePoint(event))
        }
    }

    fun createLineMarkerInfo(element: PsiElement): LineMarkerInfo<*> {
        return RelatedItemLineMarkerInfo(
            element,
            element.textRange,
            icon,
            { tooltipText },
            this,
            alignment,
            {
                elements.value
                    .filter { it.element.isValid }
                    .map { WrappedGoToRelatedItem(it) }
            }
        )
    }
}


class WrappedGoToRelatedItem(val wrapper: PsiElementWrapper) : GotoRelatedItem(wrapper.element)
