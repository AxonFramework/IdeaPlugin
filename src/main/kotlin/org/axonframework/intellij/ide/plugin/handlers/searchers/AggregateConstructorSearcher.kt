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

package org.axonframework.intellij.ide.plugin.handlers.searchers

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.handlers.types.AggregateConstructor
import org.axonframework.intellij.ide.plugin.resolving.ResolvedAnnotation
import org.axonframework.intellij.ide.plugin.util.annotationResolver
import org.axonframework.intellij.ide.plugin.util.axonScope

/**
 * Searches for constructors of classes annotated with Aggregate and a constructor without CommandHandler annotation.
 * This is applicable in the case of invariants.
 *
 * @see AggregateConstructor
 */
class AggregateConstructorSearcher : AbstractHandlerSearcher(MessageHandlerType.COMMAND) {

    /**
     * The default search of AbstractSearchHandler is overridden heren since we don't want to search based on
     * annotations, but based on all constructor references of all aggregates.
     */
    override fun search(project: Project): List<Handler> {
        return project.annotationResolver()
            .getAnnotationClasses(AxonAnnotation.AGGREGATE_ROOT)
            .flatMap { annotation ->
                searchAggregateConstructorsForAnnotation(annotation, project)
            }
    }

    private fun searchAggregateConstructorsForAnnotation(
        annotation: ResolvedAnnotation,
        project: Project
    ): List<Handler> {
        return AnnotatedElementsSearch.searchPsiClasses(annotation.psiClass, project.axonScope()).findAll()
            .flatMap { it.constructors.toList() }
            .filter { !it.hasAnnotation(MessageHandlerType.COMMAND.annotationName) && it.hasParameters() }
            .mapNotNull { createMessageHandler(it, null) }
    }

    override fun createMessageHandler(method: PsiMethod, annotation: PsiClass?): Handler? {
        return AggregateConstructor(method, method.containingClass?.qualifiedName!!)
    }
}
