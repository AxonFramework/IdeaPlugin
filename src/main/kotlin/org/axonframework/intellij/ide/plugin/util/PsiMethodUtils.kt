package org.axonframework.intellij.ide.plugin.util

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation

/**
 * Resolves the name of the projection an event handler is in. Looks for the @ProcessingGroup annotation and
 * gets its value. Otherwise, looks at the package name
 */
fun PsiMethod.findProcessingGroup(): String {
    val containingClass = this.containingClass ?: return ""
    return containingClass.resolveAnnotationStringValue(AxonAnnotation.PROCESSING_GROUP, "value") ?: toPackageName()
}

fun PsiMethod.containingClassname() = containingClass?.name ?: ""
fun PsiMethod.containingClassFqn() = containingClass?.qualifiedName ?: ""

private fun PsiMethod.toPackageName() = containingClass?.qualifiedName?.split(".")?.dropLast(1)?.joinToString(".") ?: ""
