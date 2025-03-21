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

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.util.hasAccessor
import org.axonframework.intellij.ide.plugin.util.hasAnnotation
import org.axonframework.intellij.ide.plugin.util.resolveAnnotation
import org.axonframework.intellij.ide.plugin.util.resolveAnnotationStringValue
import org.axonframework.intellij.ide.plugin.util.resolveAnnotationValue
import org.axonframework.intellij.ide.plugin.util.resolvePayloadType
import org.axonframework.intellij.ide.plugin.util.toClass

/**
 * Checks whether the associationProperty defined in the annotation exists on the message.
 * Shows a warning otherwise.
 */
class JavaSagaAssociationPropertyInspection : AbstractBaseJavaLocalInspectionTool() {
    override fun checkMethod(method: PsiMethod, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (!method.hasAnnotation(AxonAnnotation.SAGA_EVENT_HANDLER)) {
            return null
        }

        val associationResolver = method.resolveAnnotationValue(AxonAnnotation.SAGA_EVENT_HANDLER, "associationResolver")
        if (associationResolver != null) {
            // Customized by user. Leave it be.
            return null
        }
        val attribute = method.resolveAnnotationStringValue(AxonAnnotation.SAGA_EVENT_HANDLER, "associationProperty")
            ?: // IDE will show a warning here on its own, since it's a required property of the annotation. Leave it be.
            return null
        val payload = method.resolvePayloadType() ?: return null
        val payloadClass = method.project.toClass(payload) ?: return null
        if (payloadClass.hasAccessor(attribute, true)) {
            return null
        }
        val annotation = method.resolveAnnotation(AxonAnnotation.SAGA_EVENT_HANDLER) ?: return null
        val property = annotation.findAttributeValue("associationProperty") ?: return null
        return arrayOf(
            manager.createProblemDescriptor(
                property,
                null,
                associationPropertyDescription,
                ProblemHighlightType.WARNING,
                isOnTheFly,
            )
        )
    }

    override fun getStaticDescription(): String {
        return associationPropertyStaticDescription
    }
}
