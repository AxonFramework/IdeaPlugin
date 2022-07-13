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

package org.axonframework.intellij.ide.plugin.markers.handlers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.markers.AxonNavigationGutterIconRenderer
import org.axonframework.intellij.ide.plugin.util.deadlineReferenceResolver
import org.axonframework.intellij.ide.plugin.util.resolveAnnotationStringValue
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.getContainingUMethod
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.toUElement

/**
 * Provides a gutter icon on methods annotated with the @DeadlineHandler annotations.
 * When clicked on the icon, shows all places the deadline is scheduled based on the deadline name.
 */
class DeadlineHandlerMethodLineMarkerProvider : AbstractHandlerLineMarkerProvider() {
    override fun createLineMarker(
        element: PsiElement,
        handlerType: MessageHandlerType,
        payload: String?
    ): LineMarkerInfo<*>? {
        if (handlerType != MessageHandlerType.DEADLINE) {
            return null
        }
        val method = element.toUElement()?.getParentOfType<UAnnotation>()?.getContainingUMethod() ?: return null
        val deadlineName = method.resolveAnnotationStringValue(AxonAnnotation.DEADLINE_HANDLER, "deadlineName")
            ?: payload
            ?: return null

        return AxonNavigationGutterIconRenderer(
            icon = AxonIcons.Handler,
            popupTitle = "Deadline Schedulers",
            tooltipText = "Navigate to schedule invocation of this deadline",
            emptyText = "No deadline schedule invocations could be found",
            elements = ValidatingLazyValue(element) {
                element.deadlineReferenceResolver().findByDeadlineName(deadlineName)
                    .distinctBy { it.parentHandler }
            }).createLineMarkerInfo(element)
    }
}
