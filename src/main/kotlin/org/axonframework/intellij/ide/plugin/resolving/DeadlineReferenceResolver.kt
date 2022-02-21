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

package org.axonframework.intellij.ide.plugin.resolving

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.MethodReferencesSearch
import org.axonframework.intellij.ide.plugin.api.MessageCreator
import org.axonframework.intellij.ide.plugin.creators.DefaultMessageCreator
import org.axonframework.intellij.ide.plugin.util.PerformanceRegistry
import org.axonframework.intellij.ide.plugin.util.axonScope
import org.axonframework.intellij.ide.plugin.util.createCachedValue
import org.axonframework.intellij.ide.plugin.util.deadlineMethodResolver
import org.axonframework.intellij.ide.plugin.util.findParentHandlers
import org.axonframework.intellij.ide.plugin.util.toQualifiedName
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.evaluateString
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.sourcePsiElement
import org.jetbrains.uast.toUElement

/**
 * Finds all references of the DeadlineManager's schedule and cancel method and reports them as MessageCreators.
 *
 * The first String argument of the call is recorded as the deadline's name. In addition, if there's a payload, that is also
 * registered as a method name.
 */
class DeadlineReferenceResolver(val project: Project) {
    private val cache = project.createCachedValue {
        findAllReferences()
    }

    fun findByDeadlineName(deadlineName: String): List<MessageCreator> {
        return findAll().filter { it.payload == deadlineName }
    }

    fun findByElement(element: PsiElement): MessageCreator? {
        return findAll().firstOrNull { it.element == element }
    }

    fun findAll(): List<MessageCreator> {
        return cache.value
    }

    /**
     * Finds all method invocations of deadline creation.
     * Attempts to register two handlers per invocation:
     * - One based on deadlineName (specified in annotation)
     * - One based on payloadType (if any)
     *
     * This way, we can always match correctly.
     */
    private fun findAllReferences(): List<MessageCreator> {
        val scheduleReferences = project.deadlineMethodResolver().getAllScheduleMethods()
            .flatMap { method ->
                MethodReferencesSearch.search(method, project.axonScope(), true)
                    .findAll()
            }
        val schedulers = scheduleReferences
            .mapNotNull { it?.element?.toUElement()?.getParentOfType(UCallExpression::class.java) }
            .flatMap { parentCallExpression ->
                listOfNotNull(
                    createCreatorBasedOnDeadlineName(parentCallExpression),
                    createCreatorBasedOnPayloadType(parentCallExpression)
                )
            }.flatten().distinct()

        val cancelReferences = project.deadlineMethodResolver().getAllCancelMethods()
            .flatMap { method ->
                MethodReferencesSearch.search(method, project.axonScope(), true)
                    .findAll()
            }
        val cancelers = cancelReferences
            .mapNotNull { it?.element?.toUElement()?.getParentOfType(UCallExpression::class.java) }
            .flatMap { parentCallExpression ->
                listOfNotNull(
                    createCreatorBasedOnDeadlineName(parentCallExpression)
                )
            }.flatten().distinct()

        return schedulers + cancelers
    }

    /**
     * Searches for the deadline name arguments. This is the first String argument.
     */
    private fun createCreatorBasedOnDeadlineName(callExpression: UCallExpression): List<MessageCreator> {
        val argument =
            callExpression.valueArguments.firstOrNull { it.getExpressionType().toQualifiedName() == "java.lang.String" }
        val deadlineName = argument?.evaluateString() ?: return emptyList()
        return createCreators(deadlineName, callExpression.sourcePsiElement!!)
    }

    /**
     * Searches for the payload arguments. This is the first argument that is not belonging to java (such as Instant or String).
     *
     */
    private fun createCreatorBasedOnPayloadType(callExpression: UCallExpression): List<MessageCreator> {
        val argument = callExpression.valueArguments.firstOrNull {
            it.getExpressionType().toQualifiedName()?.startsWith("java") != true
        } ?: return emptyList()
        val deadlineType = argument.getExpressionType()?.toQualifiedName() ?: return emptyList()
        return createCreators(deadlineType, callExpression.sourcePsiElement!!)
    }

    private fun createCreators(payload: String, element: PsiElement): List<MessageCreator> {
        val parentHandlers = PerformanceRegistry.measure("DeadlineReferenceResolver.findParentHandlers") {
            element.findParentHandlers()
        }
        if (parentHandlers.isEmpty()) {
            return listOf(DefaultMessageCreator(element, payload, null))
        }
        return parentHandlers.map { DefaultMessageCreator(element, payload, it) }
    }
}
