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

package org.axonframework.intellij.ide.plugin.util

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassObjectAccessExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.impl.source.PsiImmediateClassType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopes
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.resolving.AnnotationResolver
import org.axonframework.intellij.ide.plugin.resolving.MessageCreationResolver
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElement

/**
 * Convenience method to fully qualified name of type.
 * Throws if we get a type we do not expect so we can support it.
 */
fun PsiType?.toQualifiedName(): String? = this?.let {
    return when (this) {
        is PsiClassReferenceType -> this.resolve()?.qualifiedName
        // Class<SomeClass> object. Extract the <SomeClass> and call this method recursively to resolve it
        is PsiImmediateClassType -> (this.parameters.first() as PsiClassReferenceType).toQualifiedName()
        else -> throw IllegalArgumentException("Can not handle psiType of type " + this::class.qualifiedName)
    }
}

/**
 * Resolves the payload type of the method. Looks at the first parameter of the method to determine the type.
 * If there is a `payloadType` attribute added on the annotation, use that instead.
 *
 * @return Payload Type
 */
fun PsiMethod.resolvePayloadType(): PsiType? = PerformanceRegistry.measure("PsiMethod.resolvePayloadType") {
    val annotationResolver = this.project.getService(AnnotationResolver::class.java)
    val annotation = annotations.firstOrNull { it.qualifiedName != null && annotationResolver.getClassByAnnotationName(it.qualifiedName!!) != null }
    if (annotation != null) {
        val value = annotation.findDeclaredAttributeValue("payloadType")
        if (value is PsiClassObjectAccessExpression) {
            return@measure value.type
        }
    }
    toUElement(UMethod::class.java)?.uastParameters?.getOrNull(0)?.typeReference?.type
}

/**
 * Checks whether A can be assigned to B. For example:
 * A extends B, then A is assignable to B. Used for matching of java supertypes in handlers.
 */
fun areAssignable(project: Project, qualifiedNameA: String, qualifiedNameB: String): Boolean {
    if (qualifiedNameA == qualifiedNameB) {
        return true
    }
    return PerformanceRegistry.measure("areAssignable") {
        val classesA = JavaPsiFacade.getInstance(project).findClasses(qualifiedNameA, project.allScope())
        val classesB = JavaPsiFacade.getInstance(project).findClasses(qualifiedNameB, project.allScope())

        classesA.any { a -> a.supers.any { b -> classesB.contains(b) } }
    }
}

/**
 * Creates the default scope for our searches. This includes all production files of JAVA and Kotlin types.
 * For now we don't search for references in tests to keep information concise and the plugin performing well.
 */
fun Project.axonScope() = GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScopes.projectProductionScope(this), JavaFileType.INSTANCE, KotlinFileType.INSTANCE)

/**
 * The 'allScope' represents all java and kotlin files, including libraries. Used for searching Axon-specific classes.
 */
fun Project.allScope() = GlobalSearchScope.allScope(this)

/**
 * Quick method to retrieve a JavaPsiFacade instance.
 */
fun Project.javaFacade(): JavaPsiFacade = JavaPsiFacade.getInstance(this)
fun Project.annotationResolver(): AnnotationResolver = getService(AnnotationResolver::class.java)
fun PsiElement.annotationResolver(): AnnotationResolver = project.annotationResolver()
fun Project.handlerResolver(): MessageHandlerResolver = getService(MessageHandlerResolver::class.java)
fun PsiElement.handlerResolver(): MessageHandlerResolver = project.handlerResolver()
fun Project.creatorResolver(): MessageCreationResolver = getService(MessageCreationResolver::class.java)
fun PsiElement.creatorResolver(): MessageCreationResolver = project.creatorResolver()

/**
 * Convenience method to quickly create a cached value for a project based on PSI modifications.
 */
fun <T> Project.createCachedValue(supplier: () -> T) = CachedValuesManager.getManager(this).createCachedValue() {
    CachedValueProvider.Result.create(supplier.invoke(), PsiModificationTracker.MODIFICATION_COUNT)
}

fun PsiClass?.isAggregate() = this?.hasAnnotation(AxonAnnotation.AGGREGATE_ROOT) == true

fun PsiClass?.isEntity() = this?.allFields?.any { it.hasAnnotation(AxonAnnotation.ENTITY_ID.annotationName) } == true
