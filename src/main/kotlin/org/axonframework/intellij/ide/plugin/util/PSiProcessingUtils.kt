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
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiJvmModifiersOwner
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import com.intellij.psi.PsiWildcardType
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.impl.source.PsiImmediateClassType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopes
import com.intellij.psi.search.searches.MethodReferencesSearch
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.resolving.AggregateStructureResolver
import org.axonframework.intellij.ide.plugin.resolving.AnnotationResolver
import org.axonframework.intellij.ide.plugin.resolving.DeadlineManagerMethodResolver
import org.axonframework.intellij.ide.plugin.resolving.DeadlineManagerReferenceResolver
import org.axonframework.intellij.ide.plugin.resolving.MessageCreationResolver
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.getContainingUMethod
import org.jetbrains.uast.toUElement
import java.util.Locale

/**
 * Convenience method to fully qualified name of type.
 * Throws if we get a type we do not expect so we can support it.
 */
fun PsiType?.toQualifiedName(): String? = this?.let {
    return when (this) {
        is PsiClassReferenceType -> this.resolve()?.qualifiedName
        // Class<SomeClass> object. Extract the <SomeClass> and call this method recursively to resolve it
        is PsiImmediateClassType -> (this.parameters.firstOrNull() as PsiClassType?)?.toQualifiedName()
        is PsiWildcardType -> "java.lang.Object"
        else -> null
    }
}

/**
 * Resolves the payload type of the method. Looks at the first parameter of the method to determine the type.
 * If there is a `payloadType` attribute added on the annotation, use that instead.
 *
 * @return Payload Type
 */
fun PsiMethod.resolvePayloadType(): PsiType? {
    val annotationResolver = this.project.getService(AnnotationResolver::class.java)
    val annotation = annotations
        .firstOrNull { it.qualifiedName != null && annotationResolver.getClassByAnnotationName(it.qualifiedName!!) != null }
    if (annotation != null) {
        val value = annotation.findDeclaredAttributeValue("payloadType")
        if (value is PsiClassObjectAccessExpression) {
            return value.type
        }
    }
    val type = toUElement(UMethod::class.java)?.uastParameters?.getOrNull(0)?.typeReference?.type ?: return null
    return if (type is PsiClassType && type.hasParameters()) {
        // For example, CommandMessage<Class>
        type.parameters[0]
    } else {
        type
    }
}

fun Project.toClass(type: PsiType, scope: GlobalSearchScope = this.axonScope()): PsiClass? {
    val toQualifiedName = type.toQualifiedName() ?: return null
    return javaFacade().findClass(toQualifiedName, scope)
}

fun PsiClass.hasAccessor(name: String): Boolean = this.getAccessor(name) != null

fun PsiClass.getAccessor(name: String): PsiElement? {
    return fields.firstOrNull { it.name == name } ?: methods.firstOrNull { it.name == name.toGetterRepresentation() }
}

/**
 * Checks whether A can be assigned to B. For example:
 * A extends B, then A is assignable to B. Used for matching of java supertypes in handlers.
 */
fun areAssignable(project: Project, qualifiedNameA: String, qualifiedNameB: String): Boolean {
    if (qualifiedNameA == qualifiedNameB) {
        return true
    }
    val classesA = JavaPsiFacade.getInstance(project).findClasses(qualifiedNameA, project.axonScope())

    return classesA.any { a -> a.supers.any { b -> b.qualifiedName == qualifiedNameB } }
}

/**
 * Creates the default scope for our searches. This includes all production files of JAVA and Kotlin types.
 * For now we don't search for references in tests to keep information concise and the plugin performing well.
 */
fun Project.axonScope() = GlobalSearchScope.getScopeRestrictedByFileTypes(
    GlobalSearchScopes.projectProductionScope(this),
    JavaFileType.INSTANCE,
    KotlinFileType.INSTANCE
)

/**
 * The 'allScope' represents all java and kotlin files, including libraries. Used for searching Axon-specific classes.
 */
fun Project.allScope() = GlobalSearchScope.allScope(this)

/**
 * Quick methods to retrieve project services
 */
fun Project.javaFacade(): JavaPsiFacade = JavaPsiFacade.getInstance(this)
fun PsiElement.javaFacade(): JavaPsiFacade = project.javaFacade()
fun Project.annotationResolver(): AnnotationResolver = getService(AnnotationResolver::class.java)
fun PsiElement.annotationResolver(): AnnotationResolver = project.annotationResolver()
fun Project.handlerResolver(): MessageHandlerResolver = getService(MessageHandlerResolver::class.java)
fun PsiElement.handlerResolver(): MessageHandlerResolver = project.handlerResolver()
fun Project.creatorResolver(): MessageCreationResolver = getService(MessageCreationResolver::class.java)
fun PsiElement.creatorResolver(): MessageCreationResolver = project.creatorResolver()
fun Project.deadlineMethodResolver(): DeadlineManagerMethodResolver = getService(DeadlineManagerMethodResolver::class.java)
fun PsiElement.deadlineMethodResolver(): DeadlineManagerMethodResolver = project.deadlineMethodResolver()
fun Project.deadlineReferenceResolver(): DeadlineManagerReferenceResolver = getService(DeadlineManagerReferenceResolver::class.java)
fun PsiElement.deadlineReferenceResolver(): DeadlineManagerReferenceResolver = project.deadlineReferenceResolver()
fun Project.aggregateResolver(): AggregateStructureResolver = getService(AggregateStructureResolver::class.java)
fun PsiElement.aggregateResolver(): AggregateStructureResolver = project.aggregateResolver()

fun PsiClass?.isAggregate() = this?.hasAnnotation(AxonAnnotation.AGGREGATE_ROOT) == true

/**
 * Checks whether the element is annotated with one of axon's annotations
 */
fun PsiJvmModifiersOwner.isAnnotated(axonAnnotation: AxonAnnotation): Boolean {
    return annotationResolver().getAnnotationClasses(axonAnnotation).any { annotationClass ->
        hasAnnotation(annotationClass.qualifiedName)
    }
}

/**
 * Finds all parent handlers of a method. This is kind of intense on IntelliJ, so we should monitor performance
 * on this and perhaps reduce the recursion limit. The recursion limit the depth of the call tree that is searched.
 */
fun PsiElement.findParentHandlers(depth: Int = 0): List<Handler> {
    if (depth > 3) {
        // Recursion guard
        return listOf()
    }
    val parent = toUElement()?.getContainingUMethod()?.javaPsi ?: return listOf()
    val parentHandler = handlerResolver().findHandlerByElement(parent)
    if (parentHandler != null) {
        return listOf(parentHandler)
    }

    val references = MethodReferencesSearch.search(parent, project.axonScope(), true)
    return references.flatMap { it.element.findParentHandlers(depth + 1) }
}

fun String.toGetterRepresentation(): String {
    return "get${this.capitalize()}"
}

fun String.toFieldRepresentation(): String {
    val removedGet = this.removePrefix("get")
    return removedGet.decapitalize(Locale.getDefault())
}

