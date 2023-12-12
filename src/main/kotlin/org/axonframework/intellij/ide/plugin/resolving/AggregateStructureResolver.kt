/*
 *  Copyright (c) (2010-2022). Axon Framework
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
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMember
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.api.Entity
import org.axonframework.intellij.ide.plugin.api.EntityMember
import org.axonframework.intellij.ide.plugin.util.annotationResolver
import org.axonframework.intellij.ide.plugin.util.axonScope
import org.axonframework.intellij.ide.plugin.util.createCachedValue
import org.axonframework.intellij.ide.plugin.util.isAnnotated
import org.axonframework.intellij.ide.plugin.util.javaFacade
import org.axonframework.intellij.ide.plugin.util.resolveAnnotationClassValue
import org.axonframework.intellij.ide.plugin.util.resolveAnnotationStringValue
import org.axonframework.intellij.ide.plugin.util.toFieldRepresentation
import org.axonframework.intellij.ide.plugin.util.toQualifiedName

/**
 * Scans the application for entities (including aggregates) and their members, analyzing whether they have been configured correctly.
 *
 * Used for multiple inspections to understand the model.
 */
class AggregateStructureResolver(private val project: Project) {
    private val cache = project.createCachedValue { resolve() }

    /**
     * Finds the uppermost Entity owning the Entity with this name.
     * So, If Entity A owns Entity B, which in turns owns Entity C. When requesting the owner for C, it will return A.
     */
    fun getTopEntityOfEntityWithName(name: String): Entity? {
        val entity = getEntityByName(name) ?: return null
        if (entity.parent != null) {
            return getTopEntityOfEntityWithName(entity.parent)
        }
        return entity
    }


    /**
     * Returns the models representing the child field in the parent. Not the parent itself!
     * The structure is Entity A -> EntityMember -> Entity B.
     */
    fun getEntityMembersByName(name: String): List<EntityMember> {
        return cache.value
            .flatMap { it.members }
            .filter { c -> c.member.name == name }
    }

    /**
     * Returns the Entity for this name
     */
    fun getEntityByName(name: String): Entity? {
        return cache.value.firstOrNull { it.name == name }
    }

    /**
     * Finds the entity with given name and all it's sub-entities down in the hierarchy.
     */
    fun getEntityAndAllChildrenRecursively(name: String): List<Entity> {
        val member = getEntityByName(name) ?: return emptyList()
        return getEntityAndAllChildrenRecursively(member)
    }

    private fun getEntityAndAllChildrenRecursively(model: Entity): List<Entity> {
        if(model.members.isEmpty()) {
            return listOf(model)
        }
        return model.members.flatMap { getEntityAndAllChildrenRecursively(it.member) }
    }

    private fun Entity.flatten() = listOf(this) + members.map { it.member }

    private fun resolve(): List<Entity> = project.annotationResolver()
        .getAnnotationClasses(AxonAnnotation.AGGREGATE_ROOT)
        .flatMap {
            AnnotatedElementsSearch.searchPsiClasses(it.psiClass, project.axonScope()).findAll()
        }
        .mapNotNull { inspect(it, emptyList()) }
        .flatMap { it.flatten() }

    private fun inspect(clazz: PsiClass, parents: List<PsiClass>, depth: Int = 0): Entity? {
        if (clazz.isEnum) {
            return null
        }
        if(parents.contains(clazz) || depth > 20) {
            // Guard for infinite recursion; we already have this class indexed, or we exceed an exorbitant depth
            return null
        }
        val parent = parents.lastOrNull()
        val children = clazz.fields.toList()
            .filter { it.isAnnotated(AxonAnnotation.AGGREGATE_MEMBER) }
            .mapNotNull { field ->
                val isCollection = field.type.isCollection()
                val psiType = if (isCollection) deriveTypeFromCollection(field.type) else field.type
                val qualifiedName = psiType.toQualifiedName() ?: return@mapNotNull null
                val targetClass = clazz.javaFacade().findClass(qualifiedName, clazz.project.axonScope())
                    ?: return@mapNotNull null
                val modelMember = inspect(targetClass, parents + clazz, depth + 1) ?: return@mapNotNull null
                val routingKey = field.resolveAnnotationStringValue(AxonAnnotation.AGGREGATE_MEMBER, "routingKey")
                val eventForwardingMode = field.resolveAnnotationClassValue(AxonAnnotation.AGGREGATE_MEMBER, "eventForwardingMode")
                EntityMember(field, field.name, modelMember, isCollection, routingKey, eventForwardingMode)
            }

        val routingKeyMember = findRoutingKeyMember(clazz)
        val routingKey = routingKeyMember?.resolveRoutingKey()
        val routingKeyType = routingKeyMember?.resolveRoutingKeyType()
        return Entity(clazz.qualifiedName!!, clazz, parent?.qualifiedName, routingKey, routingKeyType, children)
    }

    private fun PsiMember.resolveRoutingKey(): String? {
        val annotationValue = resolveAnnotationStringValue(AxonAnnotation.ENTITY_ID, "routingKey")
        if (annotationValue != null) {
            return annotationValue
        }

        if (this is PsiField) {
            return name
        }
        if (this is PsiMethod) {
            return name.toFieldRepresentation()
        }
        return null
    }

    private fun PsiMember.resolveRoutingKeyType(): String? {
        if (this is PsiField) {
            return type.toQualifiedName()
        }
        if (this is PsiMethod) {
            return returnType.toQualifiedName()
        }
        return null
    }

    private fun findRoutingKeyMember(clazz: PsiClass): PsiMember? {
        val ownField = clazz.fields.firstOrNull { it.isAnnotated(AxonAnnotation.ENTITY_ID) }
        if (ownField != null) {
            return ownField
        }
        val ownMethod = clazz.methods.firstOrNull { it.isAnnotated(AxonAnnotation.ENTITY_ID) }
        if (ownMethod != null) {
            return ownMethod
        }

        for (superPsiClass in clazz.supers) {
            val result = findRoutingKeyMember(superPsiClass)
            if (result != null) {
                return result
            }
        }

        return null
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
