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

package org.axonframework.intellij.ide.plugin.creators.searchers

import com.intellij.openapi.project.Project
import com.intellij.psi.search.searches.MethodReferencesSearch
import org.axonframework.intellij.ide.plugin.util.axonScope
import org.axonframework.intellij.ide.plugin.util.createCachedValue
import org.axonframework.intellij.ide.plugin.util.deadlineResolver
import org.axonframework.intellij.ide.plugin.util.toQualifiedName
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.evaluateString
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.sourcePsiElement
import org.jetbrains.uast.toUElement

/**
 * Finds all references of the DeadlineManager's schedule method and reports them as MessageCreators.
 *
 * The first String argument of the call is recorded as the deadline's name.
 */
class DeadlineMessageCreatorSearcher(val project: Project) : MessageCreatorSearcher {
    private val cache = project.createCachedValue {
        findAllCreators()
    }

    override fun findByPayload(payload: String): List<CreatorSearchResult> {
        return findAll().filter { it.payload == payload }
    }

    override fun findAll(): List<CreatorSearchResult> {
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
    private fun findAllCreators(): List<CreatorSearchResult> {
        val methods = project.deadlineResolver().getAllScheduleMethods()
        val references = methods
            .flatMap { method ->
                MethodReferencesSearch.search(method, project.axonScope(), true)
                    .findAll()
            }
        return references
            .mapNotNull { it?.element?.toUElement()?.getParentOfType(UCallExpression::class.java) }
            .flatMap { parentCallExpression ->
                listOfNotNull(
                    createCreatorBasedOnDeadlineName(parentCallExpression),
                    createCreatorBasedOnPayloadType(parentCallExpression)
                )
            }.distinct()
    }

    /**
     * Searches for the deadline name arguments. This is the first String argument.
     */
    private fun createCreatorBasedOnDeadlineName(callExpression: UCallExpression): CreatorSearchResult? {
        val argument =
            callExpression.valueArguments.firstOrNull { it.getExpressionType().toQualifiedName() == "java.lang.String" }
        val deadlineName = argument?.evaluateString() ?: return null
        return CreatorSearchResult(deadlineName, callExpression.sourcePsiElement!!)
    }

    /**
     * Searches for the payload arguments. This is the first argument that is not belonging to java (such as Instant or String).
     *
     */
    private fun createCreatorBasedOnPayloadType(callExpression: UCallExpression): CreatorSearchResult? {
        val argument = callExpression.valueArguments.firstOrNull {
            it.getExpressionType().toQualifiedName()?.startsWith("java") != true
        } ?: return null
        val deadlineType = argument.getExpressionType()?.toQualifiedName() ?: return null
        return CreatorSearchResult(deadlineType, callExpression.sourcePsiElement!!)
    }
}
