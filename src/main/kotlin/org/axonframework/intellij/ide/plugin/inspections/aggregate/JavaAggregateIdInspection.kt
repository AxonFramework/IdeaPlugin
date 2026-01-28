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

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiClass
import org.axonframework.intellij.ide.plugin.util.aggregateResolver
import org.axonframework.intellij.ide.plugin.util.isAggregate
import org.axonframework.intellij.ide.plugin.util.isAxon4Project

/**
 * Inspects aggregate classes on whether they have an EntityId defined. If not, we show a warning.
 *
 * The warning can be suppressed via the normal IntelliJ actions
 */
class JavaAggregateIdInspection : AbstractBaseJavaLocalInspectionTool() {
    override fun checkClass(
        aClass: PsiClass,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        // Only run this inspection on Axon 4 projects, as Aggregates no longer exist in Axon 5
        if (!aClass.project.isAxon4Project()) {
            return null
        }
        if (!aClass.isAggregate()) {
            return null
        }
        val entity = aClass.qualifiedName?.let { aClass.aggregateResolver().getEntityByName(it) } ?: return emptyArray()
        if (entity.routingKey == null) {
            return arrayOf(
                manager.createProblemDescriptor(
                    aClass,
                    aClass.identifyingElement!!.textRangeInParent,
                    aggregateIdDescription,
                    ProblemHighlightType.WARNING,
                    isOnTheFly,
                )
            )
        }
        // Show error when it's a void method
        if (entity.routingKeyType == "void") {
            return arrayOf(
                manager.createProblemDescriptor(
                    aClass,
                    aClass.identifyingElement!!.textRangeInParent,
                    aggregateIdVoidDescription,
                    ProblemHighlightType.WARNING,
                    isOnTheFly,
                )
            )
        }
        return null
    }

    override fun getStaticDescription(): String {
        return aggregateIdStaticDescription
    }
}
