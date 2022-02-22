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

package org.axonframework.intellij.ide.plugin.inspections.aggregate

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import org.axonframework.intellij.ide.plugin.util.aggregateResolver
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.idea.refactoring.memberInfo.qualifiedClassNameForRendering
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtProperty


/**
 * Inspects aggregate classes on whether they have an EntityId defined. If not, we show a warning.
 *
 * The warning can be suppressed via the normal IntelliJ actions
 */
class KotlinAggregateMemberRoutingKeyInspection : AbstractKotlinInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element !is KtClass) {
                    return
                }
                val member = element.aggregateResolver().getMemberForName(element.qualifiedClassNameForRendering())
                    ?: return
                member.children.filter { it.isCollection && !it.member.entityIdPresent }.map { child ->
                    val ktField =
                        element.declarations.filterIsInstance<KtProperty>().first { it.name == child.fieldName }
                    holder.registerProblem(
                        ktField,
                        missingEntityIdDescription,
                        ProblemHighlightType.WARNING,
                        ktField.identifyingElement!!.textRangeInParent,
                    )
                }
            }
        }
    }

    override fun getStaticDescription(): String {
        return aggregateIdStaticDescription
    }
}
