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
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiType
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.api.Model
import org.axonframework.intellij.ide.plugin.api.ModelChild
import org.axonframework.intellij.ide.plugin.util.annotationResolver
import org.axonframework.intellij.ide.plugin.util.axonScope
import org.axonframework.intellij.ide.plugin.util.createCachedValue
import org.axonframework.intellij.ide.plugin.util.isAnnotated
import org.axonframework.intellij.ide.plugin.util.javaFacade
import org.axonframework.intellij.ide.plugin.util.toQualifiedName

/**
 * Scans the application for aggregates and its members, analyzing whether they have been configured correctly.
 *
 * Used for multiple inspections to understand the model.
 */
class AggregateStructureResolver(private val project: Project) {
    private val cache = project.createCachedValue { resolve() }

    fun getModels(): List<Model> = cache.value

    fun getFlattendedModelsAndEntities(): List<Model> = getModels().flatMap { it.flatten() }

    fun getHierarchyOwnerForName(name: String): Model? {
        val member = getMemberForName(name) ?: return null
        if (member.parent != null) {
            return getHierarchyOwnerForName(member.parent)
        }
        return member
    }

    fun getAllModelsRelatedToName(name: String): List<Model> {
        getMemberForName(name) ?: return emptyList()
        return getModels().firstOrNull { it.contains(name) }?.flatten() ?: emptyList()
    }

    private fun Model.contains(name: String): Boolean {
        return this.name == name || children.any { child -> child.member.contains(name) }
    }

    fun getMemberForName(name: String): Model? {
        return getFlattendedModelsAndEntities().firstOrNull { it.name == name }
    }

    fun getMemberWithSubEntities(name: String): List<Model> {
        val member = getMemberForName(name) ?: return emptyList()
        return member.flatten()
    }

    private fun Model.flatten() = listOf(this) + children.map { it.member }

    private fun resolve(): List<Model> = project.annotationResolver()
        .getAnnotationClasses(AxonAnnotation.AGGREGATE_ROOT)
        .flatMap {
            AnnotatedElementsSearch.searchPsiClasses(it.psiClass, project.axonScope()).findAll()
        }
        .mapNotNull { inspect(it, null) }

    private fun inspect(clazz: PsiClass, parent: PsiClass?): Model? {
        if (clazz.isEnum) {
            return null
        }
        val children = clazz.fields.toList()
            .filter { it.isAnnotated(AxonAnnotation.AGGREGATE_MEMBER) }
            .mapNotNull { field ->
                val isCollection = field.type.isCollection()
                val psiType = if (isCollection) deriveTypeFromCollection(field.type) else field.type
                val qualifiedName = psiType.toQualifiedName() ?: return@mapNotNull null
                val targetClass = clazz.javaFacade().findClass(qualifiedName, clazz.project.axonScope())
                    ?: return@mapNotNull null
                val modelMember = inspect(targetClass, clazz) ?: return@mapNotNull null
                ModelChild(field, field.name, modelMember, isCollection)
            }
        val entityIdPresent = clazz.fields.any { it.isAnnotated(AxonAnnotation.ENTITY_ID) } || clazz.methods.any {
            it.isAnnotated(AxonAnnotation.ENTITY_ID)
        }
        return Model(clazz.qualifiedName!!, clazz, parent?.qualifiedName, entityIdPresent, children)
    }

    /**
     * Unwraps the generic part of a collection type. Currently List and Map are supported
     */
    private fun deriveTypeFromCollection(type: PsiType): PsiType {
        if (type !is PsiClassType) {
            return type
        }
        if (type.isMap()) {
            return type.parameters[1]
        }

        if (type.isList() || type.isCollection()) {
            return type.parameters[0]
        }
        return type
    }

    private fun PsiType.isCollection() = this is PsiClassType && (isMap() || isList() || isCollection())
    private fun PsiClassType.isMap() = isOfType("java.util.Map") || isOfType("kotlin.collections.Map")
    private fun PsiClassType.isList() = isOfType("java.util.List") || isOfType("kotlin.collections.List")
    private fun PsiClassType.isCollection() = isOfType("java.util.Collection") || isOfType("kotlin.collections.Collection")

    private fun PsiClassType.isOfType(type: String) = toQualifiedName() == type || superTypes.any { it.toQualifiedName() == type }
}
