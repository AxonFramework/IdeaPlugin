package org.axonframework.intellij.ide.plugin.resolving

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.util.PerformanceRegistry
import org.axonframework.intellij.ide.plugin.util.PerformanceSubject
import org.axonframework.intellij.ide.plugin.util.allScope
import org.axonframework.intellij.ide.plugin.util.createCachedValue

data class ResolvedAnnotation(
        val psiClass: PsiClass,
        val parent: ResolvedAnnotation?,
        val qualifiedName: String = psiClass.qualifiedName!!
)

/**
 * Responsible for managing (and caching) information regarding Axon annotations.
 */
class AnnotationResolver(val project: Project) {
    private val annotationCache = project.createCachedValue {
        PerformanceRegistry.measure(PerformanceSubject.AnnotationResolverCompute) { computeAnnotations() }
    }

    fun getAnnotationClassesForType(type: MessageHandlerType): List<ResolvedAnnotation> {
        return getAnnotationClasses(type.annotation)
    }

    fun getAnnotationClasses(axonAnnotation: AxonAnnotation): List<ResolvedAnnotation> {
        return annotationCache.value[axonAnnotation]
                ?: emptyList()
    }

    fun getMessageTypeForAnnotation(qualifiedName: String): MessageHandlerType? {
        val annotation = annotationCache.value.entries.firstOrNull { it.value.any { annClass -> annClass.psiClass.qualifiedName == qualifiedName } }?.key
                ?: return null
        return MessageHandlerType.values().firstOrNull { it.annotation == annotation }
    }

    fun getClassByAnnotationName(qualifiedName: String): ResolvedAnnotation? {
        return annotationCache.value.entries.flatMap { it.value }.firstOrNull { it.psiClass.qualifiedName == qualifiedName }
    }

    fun getAllAnnotations(): Map<AxonAnnotation, List<ResolvedAnnotation>> {
        return annotationCache.value
    }

    private fun computeAnnotations(): Map<AxonAnnotation, List<ResolvedAnnotation>> {
        return AxonAnnotation.values().associateWith {
            scanAnnotation(it)
        }
    }

    private fun scanAnnotation(annotation: AxonAnnotation): List<ResolvedAnnotation> {
        val clazz = JavaPsiFacade.getInstance(project).findClass(annotation.annotationName, project.allScope())
                ?: return listOf()
        val start = ResolvedAnnotation(clazz, null)
        return scanDescendants(start)
    }

    private fun scanDescendants(parent: ResolvedAnnotation): List<ResolvedAnnotation> {
        return listOf(parent) + AnnotatedElementsSearch.searchPsiClasses(parent.psiClass, project.allScope()).findAll()
                .filter { it.isAnnotationType }
                .filter { ht -> !MessageHandlerType.exists(ht.qualifiedName) }
                .flatMap { scanDescendants(ResolvedAnnotation(it, parent)) }
    }
}
