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

package org.axonframework.intellij.ide.plugin.util

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation

/**
 * Resolves the name of the projection an event handler is in. Looks for the @ProcessingGroup annotation and
 * gets its value. Otherwise, looks at the package name
 */
fun PsiMethod.findProcessingGroup(): String {
    val containingClass = this.containingClass ?: return ""
    return containingClass.resolveAnnotationStringValue(AxonAnnotation.PROCESSING_GROUP, "value")
        ?: if (this.isAnnotated(AxonAnnotation.SAGA_EVENT_HANDLER)) {
            containingClassFqn()
        } else toPackageName()
}

fun PsiMethod.toViewText(): String {
    return buildString {
        append(containingClassname())
        if(!isConstructor) {
            append(".")
            append(name)
        }
        append("(")
        append(parameterList.parameters.joinToString(separator = ", ") { it.type.toQualifiedName()?.toShortName() ?: "Unknown" })
        append(")")
    }
}

fun PsiMethod.containingClassname() = containingClass?.name ?: ""
fun PsiMethod.containingClassFqn() = containingClass?.qualifiedName ?: ""

private fun PsiMethod.toPackageName() = containingClass?.qualifiedName?.split(".")?.dropLast(1)?.joinToString(".") ?: ""
