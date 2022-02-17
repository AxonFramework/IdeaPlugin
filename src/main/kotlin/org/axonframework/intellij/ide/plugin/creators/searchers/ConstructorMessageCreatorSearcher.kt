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
import org.axonframework.intellij.ide.plugin.util.areAssignable
import org.axonframework.intellij.ide.plugin.util.axonScope
import org.axonframework.intellij.ide.plugin.util.handlerResolver
import org.axonframework.intellij.ide.plugin.util.javaFacade

class ConstructorMessageCreatorSearcher(val project: Project) : MessageCreatorSearcher {
    private val handlerResolver = project.handlerResolver()
    private val psiFacade = project.javaFacade()

    override fun findByPayload(payload: String): List<CreatorSearchResult> {
        val matchingHandlers = handlerResolver.findAllHandlers()
            .map { it.payload }
            .filter { areAssignable(project, payload, it) }
        val classesForQualifiedName = listOf(payload).plus(matchingHandlers)
            .distinct()
        return resolveCreatorsForFqns(classesForQualifiedName)
    }

    override fun findAll(): List<CreatorSearchResult> {
        val handlers = handlerResolver.findAllHandlers()
        val payloads = handlers.map { it.payload }.distinct()
        return payloads.flatMap { findByPayload(it) }
    }

    private fun resolveCreatorsForFqns(fqns: List<String>): List<CreatorSearchResult> {
        return fqns.flatMap { typeFqn ->
            psiFacade.findClasses(typeFqn, project.axonScope()).flatMap { clazz ->
                clazz.constructors
                    .flatMap { MethodReferencesSearch.search(it, project.axonScope(), true) }
                    .map { ref -> CreatorSearchResult(typeFqn, ref.element) }
                    .distinct()
            }
        }
    }
}
