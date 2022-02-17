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

import com.intellij.ProjectTopics
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootEvent
import com.intellij.openapi.roots.ModuleRootListener
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.util.PerformanceRegistry
import org.axonframework.intellij.ide.plugin.util.allScope
import org.axonframework.intellij.ide.plugin.util.axonScope
import org.axonframework.intellij.ide.plugin.util.createCachedValue
import org.axonframework.intellij.ide.plugin.util.javaFacade

/**
 * Responsible for managing (and caching) information regarding Axon annotations.
 *
 * We differentiate between annotations found in libraries, which are scanned once every library change.
 * We do this by listening the the PROJECT_ROOTS topic of IntelliJ.
 *
 * Note that the scan mechanism for libraries and project files is the same, but only differs in scope. This makes
 * a great impact in performance (I measured 7 ms with axonScope(), 170ms with allScope()).
 *
 * Every PSI change we scan for annotations annotated with them. We differentiate to improve performance, drastically
 * reducing the search scope.
 */
class AnnotationResolver(val project: Project) {
    private val libraryAnnotationCache = LibraryAnnotationCache()
    private val annotationCache = project.createCachedValue {
        PerformanceRegistry.measure("AnnotationResolver.computeAnnotations") { computeAnnotations() }
    }

    /**
     * Gets all annotation classes for a certain MessageHandlerType
     *
     * @param type The handler type to get annotation classes for
     * @return List of classes
     */
    fun getAnnotationClassesForType(type: MessageHandlerType): List<ResolvedAnnotation> {
        return getAnnotationClasses(type.annotation)
    }

    /**
     * Gets all annotation classes for a certain AxonAnnotation
     *
     * @param axonAnnotation The AxonAnnotation to get annotation classes for
     * @return List of classes
     */
    fun getAnnotationClasses(axonAnnotation: AxonAnnotation): List<ResolvedAnnotation> {
        return annotationCache.value[axonAnnotation]
            ?: emptyList()
    }

    /**
     * Resolves the MessageHandlerType an annotation belongs to. Returns null if it's not an Axon annotation.
     *
     * @param qualifiedName The qualifiedName of the annotation to check
     * @return The MessageHandlerType
     */
    fun getMessageTypeForAnnotation(qualifiedName: String): MessageHandlerType? {
        val annotation = annotationCache.value.entries
            .firstOrNull { it.value.any { annClass -> annClass.psiClass.qualifiedName == qualifiedName } }
            ?.key
            ?: return null
        return MessageHandlerType.values().firstOrNull { it.annotation == annotation }
    }

    /**
     * Finds a specific PsiClass by the qualifiedName. Returns null if it's not an Axon annotation.
     *
     * @param qualifiedName The qualifiedName of the annotation to check
     * @return The resolved PsiClass
     */
    fun getClassByAnnotationName(qualifiedName: String): ResolvedAnnotation? {
        return annotationCache.value.entries.flatMap { it.value }
            .firstOrNull { it.psiClass.qualifiedName == qualifiedName }
    }

    /**
     * Get all Axon annotation classes in map form
     *
     * @return All Axon annotation classes categorized by AxonAnnotation
     */
    fun getAllAnnotations(): Map<AxonAnnotation, List<ResolvedAnnotation>> {
        return annotationCache.value
    }

    /**
     * Computes the actual annotations, based on the ones found in libraries.
     *
     * @return the annotations in the library cache and any specific in the current source code (axonScope)
     */
    private fun computeAnnotations(): Map<AxonAnnotation, List<ResolvedAnnotation>> {
        val libAnnotations = libraryAnnotationCache.getLibraryAnnotations()
        return AxonAnnotation.values().associateWith { axonAnn ->
            val specificLibAnnotations = libAnnotations.filter { axonAnn == it.axonAnnotation }
            specificLibAnnotations.flatMap { descAnn ->
                scanDescendants(axonAnn, descAnn, project.axonScope())
            }
        }
    }

    private fun scanAnnotation(annotation: AxonAnnotation, scope: GlobalSearchScope): List<ResolvedAnnotation> {
        val clazz = project.javaFacade().findClass(annotation.annotationName, scope)
            ?: return listOf()
        val start = ResolvedAnnotation(annotation, clazz, null)
        return scanDescendants(annotation, start, scope)
    }

    private fun scanDescendants(
        annotation: AxonAnnotation,
        parent: ResolvedAnnotation,
        scope: GlobalSearchScope
    ): List<ResolvedAnnotation> {
        return listOf(parent) + AnnotatedElementsSearch.searchPsiClasses(parent.psiClass, scope).findAll()
            .filter { it.isAnnotationType }
            .filter { ht -> !MessageHandlerType.exists(ht.qualifiedName) }
            .flatMap { scanDescendants(annotation, ResolvedAnnotation(annotation, it, parent), scope) }
    }

    /**
     * Private cache for library annotations, following the same lifecycle as the AnnotationResolver itself.
     * Encapsulates inner workings such as the invalidation of the cache based on the message bus.
     */
    private inner class LibraryAnnotationCache {
        // Set to false if libraries are updated
        private var libraryInitialized: Boolean = false
        private var libraryAnnotations: List<ResolvedAnnotation> = listOf()

        init {
            // Listen to root changes, and invalidate library scanning
            project.messageBus.connect().subscribe(ProjectTopics.PROJECT_ROOTS, object : ModuleRootListener {
                override fun rootsChanged(event: ModuleRootEvent) {
                    libraryInitialized = false
                }
            })
        }


        /**
         * Get all annotations in the library cache. If the cache is out-of-date, executes a scan.
         */
        fun getLibraryAnnotations(): List<ResolvedAnnotation> {
            if (!libraryInitialized) {
                updateLibraryAnnotations()
            }
            return libraryAnnotations
        }

        private fun updateLibraryAnnotations() = PerformanceRegistry.measure("AnnotationResolver.libraryAnnotation") {
            libraryAnnotations = AxonAnnotation.values().flatMap { scanAnnotation(it, project.allScope()) }
            libraryInitialized = true
        }
    }
}
