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
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.ClassInheritorsSearch
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageType
import org.axonframework.intellij.ide.plugin.resolving.handlers.searchers.AggregateConstructorSearcher
import org.axonframework.intellij.ide.plugin.resolving.handlers.searchers.CommandHandlerSearcher
import org.axonframework.intellij.ide.plugin.resolving.handlers.searchers.DeadlineHandlerSearcher
import org.axonframework.intellij.ide.plugin.resolving.handlers.searchers.EventHandlerSearcher
import org.axonframework.intellij.ide.plugin.resolving.handlers.searchers.EventSourcingHandlerSearcher
import org.axonframework.intellij.ide.plugin.resolving.handlers.searchers.QueryHandlerSearcher
import org.axonframework.intellij.ide.plugin.resolving.handlers.searchers.SagaEventHandlerSearcher
import org.axonframework.intellij.ide.plugin.util.axonScope
import org.axonframework.intellij.ide.plugin.util.createCachedValue
import org.axonframework.intellij.ide.plugin.util.findCompleteSupers

/**
 * Searches the codebase for Message handlers based on the annotations defined in MessageHandlerType.
 *
 * Results are cached based on the Psi modifications of IntelliJ. This means the calculations are invalidated when
 * the PSI is modified (code is edited) or is collected by the garbage collector.
 *
 * @see org.axonframework.intellij.ide.plugin.api.MessageHandlerType
 */
class MessageHandlerResolver(private val project: Project) {
    private val searchers = listOf(
        CommandHandlerSearcher(),
        EventHandlerSearcher(),
        EventSourcingHandlerSearcher(),
        QueryHandlerSearcher(),
        SagaEventHandlerSearcher(),
        AggregateConstructorSearcher(),
        DeadlineHandlerSearcher(),
    )

    private val handlerCache = project.createCachedValue {
        executeFindMessageHandlers()
    }

    /**
     * finds handlers for a certain payload. Can be reversed, in the case of interceptors.
     */
    fun findHandlersForType(
        qualifiedName: String,
        messageType: MessageType? = null,
    ): List<Handler> {
        val baseClass = JavaPsiFacade.getInstance(project).findClasses(qualifiedName, project.axonScope()).firstOrNull()
            ?: return emptyList()

        val additionalSupersOrImplementors = searchRelatedClasses(baseClass)
        val completeList = listOf(qualifiedName) + additionalSupersOrImplementors
        return handlerCache.value
            .filter { messageType == null || it.handlerType.messageType == messageType }
            .filter { completeList.contains(it.payload) }
            .filter { it.element.isValid }
    }

    private fun searchRelatedClasses(baseClass: PsiClass): List<String> {
        val implementors = ClassInheritorsSearch.search(baseClass, project.axonScope(), true)
            .mapNotNull { it.qualifiedName }
            .distinct()

        // The supers call only returns one level at a time. We need to do this recursively
        return implementors + baseClass.findCompleteSupers()
            .mapNotNull { it.qualifiedName }
            .distinct()
    }

    fun findAllHandlers(): List<Handler> = handlerCache.value

    fun findHandlerByElement(psiElement: PsiElement): Handler? {
        return handlerCache.value.firstOrNull { it.element == psiElement }
    }

    private fun executeFindMessageHandlers(): List<Handler> {
        return searchers
            .flatMap { it.search(project) }
            .distinct()
    }
}

