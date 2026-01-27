/*
 *  Copyright (c) 2022-2026. Axon Framework
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.axonframework.intellij.ide.plugin.resolving

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.ClassInheritorsSearch
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageType
import org.axonframework.intellij.ide.plugin.usage.AxonVersionService
import org.axonframework.intellij.ide.plugin.util.axonScope
import org.axonframework.intellij.ide.plugin.util.createCachedValue
import org.axonframework.intellij.ide.plugin.util.findCompleteSupers

/**
 * Searches the codebase for Message handlers based on the annotations defined in MessageHandlerType.
 *
 * Results are cached based on the Psi modifications of IntelliJ. This means the calculations are invalidated when
 * the PSI is modified (code is edited) or is collected by the garbage collector.
 *
 * Uses version-specific searchers provided by the VersionedComponentFactory to support both Axon 4 and 5.
 *
 * @see org.axonframework.intellij.ide.plugin.api.MessageHandlerType
 * @see org.axonframework.intellij.ide.plugin.api.VersionedComponentFactory
 */
class MessageHandlerResolver(private val project: Project) {
    private val versionService = project.service<AxonVersionService>()

    private val searchers by lazy {
        versionService.getComponentFactory()?.createHandlerSearchers() ?: emptyList()
    }

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

