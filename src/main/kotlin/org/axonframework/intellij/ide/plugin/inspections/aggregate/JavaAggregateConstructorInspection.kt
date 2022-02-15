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

import com.intellij.codeInsight.daemon.impl.actions.SuppressFix
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiClass
import org.axonframework.intellij.ide.plugin.util.isAggregate


/**
 * Inspects aggregate classes on whether they have a constructor without arguments. If not, we show a warning.
 *
 * The warning can be suppressed via the normal IntelliJ actions
 */
class JavaAggregateConstructorInspection : AbstractBaseJavaLocalInspectionTool() {
    override fun checkClass(aClass: PsiClass, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor> {
        if (!aClass.isAggregate()) {
            return emptyArray()
        }
        val isMissingEmptyConstructor = aClass.constructors.none { !it.hasParameters() }
        if (isMissingEmptyConstructor) {
            return arrayOf(
                    manager.createProblemDescriptor(
                            aClass,
                            aClass.identifyingElement!!.textRangeInParent,
                            emptyConstructorDescription,
                            ProblemHighlightType.ERROR,
                            isOnTheFly,
                            SuppressFix(shortName),
                    )
            )
        }
        return emptyArray()
    }

    override fun getStaticDescription(): String {
        return emptyConstructorStaticDescription
    }
}
