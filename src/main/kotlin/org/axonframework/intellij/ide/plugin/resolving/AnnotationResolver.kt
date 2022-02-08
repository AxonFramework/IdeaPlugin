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

/**
 * Responsible for managing (and caching) information regarding Axon annotations.
 */
class AnnotationResolver(val project: Project) {
    private val annotationCache = project.createCachedValue {
        PerformanceRegistry.measure(PerformanceSubject.AnnotationResolverCompute) { computeAnnotations() }
    }

    fun getAnnotationClassesForType(type: MessageHandlerType): List<PsiClass> {
        return getAnnotationClasses(type.annotation)
    }

    fun getAnnotationClasses(axonAnnotation: AxonAnnotation): List<PsiClass> {
        return annotationCache.value[axonAnnotation]
                ?: emptyList()
    }

    fun getMessageTypeForAnnotation(qualifiedName: String): MessageHandlerType? {
        val annotation = annotationCache.value.entries.firstOrNull { it.value.any { annClass -> annClass.qualifiedName == qualifiedName } }?.key
                ?: return null
        return MessageHandlerType.values().firstOrNull { it.annotation == annotation }
    }

    fun getClassByAnnotationName(qualifiedName: String): PsiClass? {
        return annotationCache.value.entries.flatMap { it.value }.firstOrNull { it.qualifiedName == qualifiedName }
    }

    fun getAllAnnotations(): Map<AxonAnnotation, List<PsiClass>> {
        return annotationCache.value
    }

    private fun computeAnnotations(): Map<AxonAnnotation, List<PsiClass>> {
        return AxonAnnotation.values().associateWith {
            scanAnnotation(it)
        }
    }

    private fun scanAnnotation(annotation: AxonAnnotation): List<PsiClass> {
        val clazz = JavaPsiFacade.getInstance(project).findClass(annotation.annotationName, project.allScope())
                ?: return listOf()

        return listOf(clazz) + scanDescendants(clazz, annotation.scanLevels)
    }

    private fun scanDescendants(clazz: PsiClass, remainingLevels: Int): List<PsiClass> {
        if (remainingLevels == 0) {
            return listOf(clazz)
        }
        return listOf(clazz) + AnnotatedElementsSearch.searchPsiClasses(clazz, project.allScope()).findAll()
                .filter { ht -> !MessageHandlerType.exists(ht.qualifiedName) }
                .flatMap { scanDescendants(it, remainingLevels - 1) }
    }
}
