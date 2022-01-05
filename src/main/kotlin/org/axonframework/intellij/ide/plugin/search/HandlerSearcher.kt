package org.axonframework.intellij.ide.plugin.search

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassObjectAccessExpression
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import com.intellij.psi.impl.PsiModificationTrackerImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.kotlin.idea.KotlinFileType

object HandlerSearcher {
    private val cacheMap: MutableMap<Project, CachedValue<List<HandlerSearchResult>>> = HashMap()

    fun findAllMessageHandlers(project: Project): List<HandlerSearchResult> {
        val cache = cacheMap.computeIfAbsent(project) {
            CachedValuesManager.getManager(project).createCachedValue() {
                val handlers = executeFindMessageHandlers(project)
                CachedValueProvider.Result.create(handlers, PsiModificationTrackerImpl(project))
            }
        }

        return cache.value
    }

    private fun executeFindMessageHandlers(project: Project): List<HandlerSearchResult> {
        val projectScope = GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(project), JavaFileType.INSTANCE, KotlinFileType.INSTANCE)
        val messageHandlingAnnotations = AnnotationSearcher.findMessageHandlingAnnotation(project)
        val handlers = messageHandlingAnnotations.flatMap { annotationClass ->
            AnnotatedElementsSearch.searchPsiMethods(annotationClass, projectScope)
                    .findAll()
                    .map { method -> method to resolveType(method, annotationClass) }
                    .filter { it.second != null }
                    .map { HandlerSearchResult(it.first, it.second!!) }
        }
        return handlers
    }

    private fun resolveType(method: PsiMethod, annotationClass: PsiClass): PsiType? {
        val annotation = method.getAnnotation(annotationClass.qualifiedName!!)
        if (annotation != null) {
            val value = annotation.findDeclaredAttributeValue("payloadType")
            if (value is PsiClassObjectAccessExpression) {
                return value.type
            }
        }
        return method.parameters.getOrNull(0)?.type as PsiType?
    }
}
