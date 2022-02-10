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
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.util.PerformanceRegistry
import org.axonframework.intellij.ide.plugin.util.allScope
import org.axonframework.intellij.ide.plugin.util.createCachedValue
import org.axonframework.intellij.ide.plugin.util.javaFacade

/**
 * Responsible for managing (and caching) information regarding Axon annotations.
 */
class AnnotationResolver(val project: Project) {
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
        return annotationCache.value.entries.flatMap { it.value }.firstOrNull { it.psiClass.qualifiedName == qualifiedName }
    }

    /**
     * Get all Axon annotation classes in map form
     *
     * @return All Axon annotation classes categorized by AxonAnnotation
     */
    fun getAllAnnotations(): Map<AxonAnnotation, List<ResolvedAnnotation>> {
        return annotationCache.value
    }

    private fun computeAnnotations(): Map<AxonAnnotation, List<ResolvedAnnotation>> {
        return AxonAnnotation.values().associateWith {
            scanAnnotation(it)
        }
    }

    private fun scanAnnotation(annotation: AxonAnnotation): List<ResolvedAnnotation> {
        val clazz = project.javaFacade().findClass(annotation.annotationName, project.allScope())
                ?: return listOf()
        val start = ResolvedAnnotation(annotation, clazz, null)
        return scanDescendants(annotation, start)
    }

    private fun scanDescendants(annotation: AxonAnnotation, parent: ResolvedAnnotation): List<ResolvedAnnotation> {
        return listOf(parent) + AnnotatedElementsSearch.searchPsiClasses(parent.psiClass, project.allScope()).findAll()
                .filter { it.isAnnotationType }
                .filter { ht -> !MessageHandlerType.exists(ht.qualifiedName) }
                .flatMap { scanDescendants(annotation, ResolvedAnnotation(annotation, it, parent)) }
    }
}
