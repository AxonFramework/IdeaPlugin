/*
 *  Copyright (c) 2022-(2010-2023). Axon Framework
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
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.util.aggregateResolver
import org.axonframework.intellij.ide.plugin.util.containingClassFqn
import org.axonframework.intellij.ide.plugin.util.hasAccessor
import org.axonframework.intellij.ide.plugin.util.hasAnnotation
import org.axonframework.intellij.ide.plugin.util.resolvePayloadType
import org.axonframework.intellij.ide.plugin.util.toClass
import org.jetbrains.kotlin.idea.codeinsight.api.classic.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElementOfType


/**
 * Inspects that the routingKey specified by the EntityId annotation is present in all commands and events
 * being handled by that entity.
 *
 * The warning can be suppressed via the normal IntelliJ actions
 */
class KotlinMissingRoutingKeyOnAggregateMemberInspection : AbstractKotlinInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element !is KtNamedFunction) {
                    return
                }
                val method = element.toUElementOfType<UMethod>() ?: return
                val containingClassName = method.containingClassFqn()
                val entity = element.aggregateResolver().getEntityByName(containingClassName) ?: return
                val entityMember = element.aggregateResolver().getEntityMembersByName(containingClassName).firstOrNull() ?: return
                if (!entityMember.isCollection) {
                    return
                }

                val routingKey = entityMember.routingKey
                    ?: entity.routingKey
                    ?: return
                if (method.hasAnnotation(AxonAnnotation.EVENT_SOURCING_HANDLER) &&
                    entityMember.eventForwardingMode != "org.axonframework.modelling.command.ForwardMatchingInstances"
                ) {
                    return
                }
                val payload = method.resolvePayloadType() ?: return
                val payloadClass = method.project.toClass(payload) ?: return
                if (payloadClass.hasAccessor(routingKey)) {
                    return
                }

                holder.registerProblem(
                    element,
                    missingRoutingKeyDescription.replace("__ATT__", routingKey),
                    ProblemHighlightType.WARNING,
                    element.identifyingElement!!.textRangeInParent,
                )
            }
        }
    }

    override fun getStaticDescription(): String {
        return missingRoutingKeyStaticDescription
    }
}
