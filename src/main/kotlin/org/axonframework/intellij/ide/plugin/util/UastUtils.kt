package org.axonframework.intellij.ide.plugin.util

import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.resolving.ResolvedAnnotation
import org.jetbrains.uast.UAnnotated

/**
 * Checks whether the element is annotated with one of axon's annotations
 */
fun UAnnotated.isAnnotated(axonAnnotation: AxonAnnotation): Boolean {
    val psi = sourcePsi ?: return false
    return psi.annotationResolver().getAnnotationClasses(axonAnnotation).any { annotationClass ->
        isAnnotated(annotationClass)
    }
}

fun UAnnotated.isAnnotated(annotationClass: ResolvedAnnotation) =
        uAnnotations.any { uAnnotation -> uAnnotation.qualifiedName == annotationClass.qualifiedName }
