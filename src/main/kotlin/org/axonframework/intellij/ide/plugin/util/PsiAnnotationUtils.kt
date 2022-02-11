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

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiAnnotationMemberValue
import com.intellij.psi.PsiModifierListOwner
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.resolving.AnnotationResolver
import org.axonframework.intellij.ide.plugin.resolving.ResolvedAnnotation
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.toUElement

/**
 * Find the most specific annotation of a specific type on a PsiElement.
 * For example, if @MyAggregate is annotated with @Aggregate, which is annotation with @AggregateRoot
 * this method will return @MyAggregate
 *
 * @param annotation Axon's annotation to check
 * @param condition Optional condition to check relevant, e.g. for presence of payloadType
 */
fun PsiModifierListOwner.resolveAnnotation(annotation: AxonAnnotation, condition: (PsiAnnotation) -> Boolean = { true }): PsiAnnotation? {
    val annotationResolver = project.getService(AnnotationResolver::class.java)
    val annotations = annotationResolver.getAnnotationClasses(annotation)
    val mostGenericAnnotation = annotations.firstOrNull { it.parent == null } ?: return null

    fun recursivelyResolveChild(currentAnnotation: ResolvedAnnotation): PsiAnnotation? {
        val children = annotations.filter { it.parent == currentAnnotation }

        // First, check if one of the children (or their children) suffice
        for (child in children) {
            val result = recursivelyResolveChild(child)
            if (result != null) {
                return result
            }
        }

        return this.annotations.filter { it.hasQualifiedName(currentAnnotation.qualifiedName) }
                .firstOrNull(condition)
    }

    return recursivelyResolveChild(mostGenericAnnotation)
}

/**
 * Checks whether the element is annotated with a certain Axon annotation.
 * Also checks all descendants of that annotation.
 */
fun PsiModifierListOwner.hasAnnotation(annotation: AxonAnnotation): Boolean {
    val annotationResolver = project.getService(AnnotationResolver::class.java)
    val annotations = annotationResolver.getAnnotationClasses(annotation)
    return annotations.any { hasAnnotation(it.qualifiedName) }
}

/**
 * Resolve the string attribute value of one of the Axon Annotations.
 * Since they are meta, the annotations can be annotated, which can in turn contain the value. So we have to do it
 * the recursive way
 *
 * @param annotation Axon's annotation to target
 * @param attributeName key of annotation to look for. Most of the time this is "value"
 */
fun PsiModifierListOwner.resolveAnnotationStringValue(annotation: AxonAnnotation, attributeName: String): String? {
    val relevantAnnotation = resolveAnnotation(annotation) ?: return null
    val attribute = relevantAnnotation.findDeclaredAttributeValue(attributeName)
    if (attribute != null) {
        return resolveAttributeStringValue(attribute)
    }
    // The annotation itself might be annotated with one that contains the value
    // Note: resolveAnnotationType() does not work with kotlin code somehow. Resolve class by qualified name
    val qualifiedName = relevantAnnotation.qualifiedName ?: return null
    val annClass = project.javaFacade().findClass(qualifiedName, project.allScope()) ?: return null
    return annClass.resolveAnnotationStringValue(annotation, attributeName)
}

private fun resolveAttributeStringValue(attribute: PsiAnnotationMemberValue?): String? {
    return attribute?.toUElement()?.getParentOfType(UExpression::class.java)?.evaluate() as String? ?: return null
}
