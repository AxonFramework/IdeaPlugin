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
import org.axonframework.intellij.ide.plugin.util.axonScope
import org.axonframework.intellij.ide.plugin.util.createCachedValue
import org.axonframework.intellij.ide.plugin.util.javaFacade
import org.axonframework.intellij.ide.plugin.util.toQualifiedName
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.toUElement

class DeadlineMessageCreatorSearcher(val project: Project) : MessageCreatorSearcher {
    private val cache = project.createCachedValue {
        findAllCreators()
    }

    override fun findByPayload(payload: String): List<CreatorSearchResult> {
        val byName = findAll().filter { it.name == payload }
        if (byName.isNotEmpty()) {
            return byName
        }
        // Fallback, find the deadlines by payload.
        // The implementor has made it's own abstraction which we cannot resolve.
        return findAll().filter { it.payload == payload }
    }

    override fun findAll(): List<CreatorSearchResult> {
        return cache.value
    }

    private fun findAllCreators(): List<CreatorSearchResult> {
        val deadlineManager = project.javaFacade().findClass("org.axonframework.deadline.DeadlineManager", project.allScope())
                ?: return emptyList()
        return deadlineManager.methods
                .filter { it.name == "schedule" }
                .flatMap {
                    MethodReferencesSearch.search(it, project.axonScope(), false)
                }
                .distinct()
                .mapNotNull {
                    val parentCallExpression = it.element.toUElement()?.getParentOfType(UCallExpression::class.java)
                            ?: return@mapNotNull null

                    val deadlineName = parentCallExpression.valueArguments[1].evaluate() as String
                    val payloadType = parentCallExpression.valueArguments.getOrNull(2)?.getExpressionType().toQualifiedName()
                    CreatorSearchResult(payloadType, deadlineName, it.element)
                }
    }
}
