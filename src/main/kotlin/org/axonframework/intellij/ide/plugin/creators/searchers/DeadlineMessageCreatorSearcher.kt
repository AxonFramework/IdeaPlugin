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
import org.axonframework.intellij.ide.plugin.util.allScope
import org.axonframework.intellij.ide.plugin.util.createCachedValue
import org.axonframework.intellij.ide.plugin.util.deadlineResolver
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.evaluateString
import org.jetbrains.uast.getParentOfType
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
        // Fallback, find the deadlines by payload.
        // The implementor has made its own abstraction which we cannot resolve.
        return findAll().filter { it.payload == payload }
    }

    override fun findAll(): List<CreatorSearchResult> {
        return cache.value
    }

    private fun findAllCreators(): List<CreatorSearchResult> {
        val methods = project.deadlineResolver().getAllScheduleMethods()
        val references = methods
                .flatMap { method ->
                    MethodReferencesSearch.search(method, project.allScope(), true)
                            .findAll()
                }
        return references.mapNotNull {
            val parentCallExpression = it?.element?.toUElement()?.getParentOfType(UCallExpression::class.java)
                    ?: return@mapNotNull null
            // Find the first string argument and register it as the deadline's name
            // We cannot look it up by argument index here, since Kotlin is being nasty with it. Revisit this later.
            val deadlineName = parentCallExpression.valueArguments
                    .firstOrNull { argument ->
                        argument.getExpressionType()?.canonicalText == "java.lang.String"
                    }?.evaluateString()
                    ?: return@mapNotNull null
            CreatorSearchResult(deadlineName, it.element)
        }.distinct()
    }
}
