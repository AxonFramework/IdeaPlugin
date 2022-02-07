package org.axonframework.intellij.ide.plugin.resolving

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.util.allScope
import org.axonframework.intellij.ide.plugin.util.createCachedValue

/**
 * Responsible for managing (and caching) information regarding Axon annotations
 */
class AnnotationResolver(val project: Project) {
    private val annotationCache = project.createCachedValue { computeAnnotations() }

    fun getAnnotationClassesForType(type: MessageHandlerType): List<PsiClass> = annotationCache.value[type]
            ?: emptyList()

    fun getMessageTypeForAnnotation(qualifiedName: String): MessageHandlerType? {
        return annotationCache.value.entries.firstOrNull { it.value.any { annClass -> annClass.qualifiedName == qualifiedName } }?.key
    }

    fun getClassByAnnotationName(qualifiedName: String): PsiClass? {
        return annotationCache.value.entries.flatMap { it.value }.firstOrNull { it.qualifiedName == qualifiedName }
    }

    private fun computeAnnotations(): Map<MessageHandlerType, List<PsiClass>> {
        return MessageHandlerType.values().associateWith {
            val clazz = JavaPsiFacade.getInstance(project).findClass(it.annotationName, project.allScope())
                    ?: return@associateWith listOf()
            listOf(clazz) + AnnotatedElementsSearch.searchPsiClasses(clazz, project.allScope()).findAll()
                    .filter { ht -> !MessageHandlerType.exists(ht.qualifiedName) }
        }

    }
}
