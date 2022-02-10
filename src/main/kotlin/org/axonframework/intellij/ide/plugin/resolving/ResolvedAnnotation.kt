package org.axonframework.intellij.ide.plugin.resolving

import com.intellij.psi.PsiClass
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation

/**
 * Represents an AxonNotification while being resolved (linked to runtime classes).
 * Contains a hierarchy, the parent annotation is included for these purposes.
 */
data class ResolvedAnnotation(
        val axonAnnotation: AxonAnnotation,
        val psiClass: PsiClass,
        val parent: ResolvedAnnotation?,
        val qualifiedName: String = psiClass.qualifiedName!!
)
