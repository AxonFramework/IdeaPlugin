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

package org.axonframework.intellij.ide.plugin.inspections.saga

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.util.resolveAnnotationStringValue
import org.axonframework.intellij.ide.plugin.util.resolveAnnotationValue
import org.axonframework.intellij.ide.plugin.util.resolvePayloadType
import org.axonframework.intellij.ide.plugin.util.toClass
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElementOfType


/**
 * Checks whether the associationProperty defined in the annotation exists on the message.
 * Shows a warning otherwise.
 */
class KotlinSagaAssociationPropertyInspection : AbstractKotlinInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element !is KtNamedFunction) {
                    return
                }

                val method = element.toUElementOfType<UMethod>() ?: return

                val associationResolver = method.resolveAnnotationValue(AxonAnnotation.SAGA_EVENT_HANDLER, "associationResolver")
                if (associationResolver != null) {
                    // Customized by user. Leave it be.
                    return
                }
                val attribute = method.resolveAnnotationStringValue(AxonAnnotation.SAGA_EVENT_HANDLER, "associationProperty")
                    ?: // IDE will show a warning here on its own, since it's a required property of the annotation. Leave it be.
                    return
                val payload = method.resolvePayloadType() ?: return
                val payloadClass = method.project.toClass(payload) ?: return
                val hasField = payloadClass.fields.any { it.name == attribute }
                if (hasField) {
                    return
                }
                holder.registerProblem(
                    element,
                    associationPropertyDescription,
                    ProblemHighlightType.WARNING,
                    element.identifyingElement!!.textRangeInParent,
                )
            }
        }
    }

    override fun getStaticDescription(): String {
        return associationPropertyStaticDescription
    }
}
