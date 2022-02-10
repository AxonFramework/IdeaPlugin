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
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.util.annotationResolver
import org.axonframework.intellij.ide.plugin.util.axonScope

/**
 * HandlerSearchers are responsible for finding the handlers of their `MessageHandlerType`.
 * They can choose to add additional information in their `MessageHandlerType` so this can be used when the handler
 * is displayed.
 *
 * In order to add a new HandlerSearcher:
 * - Add the Type to `MessageHandlerType`
 * - Create a `Handler` implementation, responsible for containing all data related to the handler.
 * - Create a `HandlerSearcher` implementation, responsible for creating the `Handler` instance.
 * - Add your handler to `MessageHandlerResolver.searchers` list
 *
 * @see org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
 * @see Handler
 */
abstract class AbstractHandlerSearcher(private val handlerType: MessageHandlerType) {
    /**
     * Method that should be implemented by HandlerSearchers, creating their representation of a `Handler` with
     * possible additional information.
     *
     * @see Handler
     *
     * @return Handler implementation that represents the given method.
     */
    protected abstract fun createMessageHandler(method: PsiMethod): Handler?

    /**
     * Executes the actual search for handlers of a certain type. Does so based on the annotation provided and all
     * annotations that are annotated by it.
     *
     * Can be overridden if different search behavior is wanted.
     *
     * @return List of found handlers for the annotation in the `MessageHandlerType`
     */
    open fun search(project: Project): List<Handler> {
        val annotationClasses = findAllRelevantAnnotationClasses(project)
        val annotatedMethods = annotationClasses.flatMap {
            AnnotatedElementsSearch.searchPsiMethods(it, project.axonScope()).findAll()
        }
        return annotatedMethods.mapNotNull { this.createMessageHandler(it) }.distinct()
    }

    private fun findAllRelevantAnnotationClasses(project: Project): List<PsiClass> {
        return project.annotationResolver()
                .getAnnotationClassesForType(handlerType)
                .map { it.psiClass }
    }
}
