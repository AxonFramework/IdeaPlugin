/*
 *  Copyright (c) 2022-2026. Axon Framework
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.axonframework.intellij.ide.plugin.inspections.aggregate

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import org.axonframework.intellij.ide.plugin.util.aggregateResolver
import org.axonframework.intellij.ide.plugin.util.isAggregate
import org.axonframework.intellij.ide.plugin.util.isAxon4Project
import org.jetbrains.kotlin.idea.codeinsight.api.classic.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.uast.UClass
import org.jetbrains.uast.toUElementOfType


/**
 * Inspects aggregate classes on whether they have an EntityId defined. If not, we show a warning.
 *
 * The warning can be suppressed via the normal IntelliJ actions
 */
class KotlinAggregateIdInspection : AbstractKotlinInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element !is KtClass) {
                    return
                }
                // Only run this inspection on Axon 4 projects
                if (!element.project.isAxon4Project()) {
                    return
                }
                val uClass = element.toUElementOfType<UClass>() ?: return
                if (!uClass.isAggregate()) {
                    return
                }
                val entity = uClass.qualifiedName?.let { uClass.aggregateResolver().getEntityByName(it) } ?: return
                if (entity.routingKey == null) {
                    holder.registerProblem(
                        element,
                        aggregateIdDescription,
                        ProblemHighlightType.WARNING,
                        element.identifyingElement!!.textRangeInParent,
                    )
                }

                if (entity.routingKeyType == "void") {
                    holder.registerProblem(
                        element,
                        aggregateIdVoidDescription,
                        ProblemHighlightType.WARNING,
                        element.identifyingElement!!.textRangeInParent,
                    )
                }
            }
        }
    }

    override fun getStaticDescription(): String {
        return aggregateIdStaticDescription
    }
}
