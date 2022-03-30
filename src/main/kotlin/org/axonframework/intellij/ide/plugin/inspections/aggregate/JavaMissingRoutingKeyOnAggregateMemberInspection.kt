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

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.util.aggregateResolver
import org.axonframework.intellij.ide.plugin.util.containingClassFqn
import org.axonframework.intellij.ide.plugin.util.hasAccessor
import org.axonframework.intellij.ide.plugin.util.hasAnnotation
import org.axonframework.intellij.ide.plugin.util.resolvePayloadType
import org.axonframework.intellij.ide.plugin.util.toClass
import org.axonframework.intellij.ide.plugin.util.toGetterRepresentation

/**
 * Inspects that the routingKey specified by the EntityId annotation is present in all commands and events
 * being handled by that entity.
 *
 * The warning can be suppressed via the normal IntelliJ actions
 */
class JavaMissingRoutingKeyOnAggregateMemberInspection : AbstractBaseJavaLocalInspectionTool() {

    override fun checkMethod(method: PsiMethod, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        val model = method.aggregateResolver().getMemberForName(method.containingClassFqn()) ?: return null

        val parentModelChild = method.aggregateResolver().getParentOfModelChild(method.containingClassFqn()).firstOrNull() ?: return null
        if (!parentModelChild.isCollection) {
            return null
        }
        val routingKey = parentModelChild.routingKey
            ?: model.routingKey
            ?: return null

        if (method.hasAnnotation(AxonAnnotation.EVENT_SOURCING_HANDLER) &&
            parentModelChild.forwardingMode != "org.axonframework.modelling.command.ForwardMatchingInstances"
        ) {
            return null
        }
        val payload = method.resolvePayloadType() ?: return null
        val payloadClass = method.project.toClass(payload) ?: return null
        if (payloadClass.hasAccessor(routingKey)) {
            return null
        }

        return arrayOf(
            manager.createProblemDescriptor(
                method,
                method.identifyingElement!!.textRangeInParent,
                missingRoutingKeyDescription.replace("__ATT__", routingKey),
                ProblemHighlightType.WARNING,
                isOnTheFly,
            )
        )

    }

    override fun getStaticDescription(): String {
        return missingRoutingKeyStaticDescription
    }
}
