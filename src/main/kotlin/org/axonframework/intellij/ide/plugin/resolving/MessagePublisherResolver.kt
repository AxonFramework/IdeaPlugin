package org.axonframework.intellij.ide.plugin.resolving

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiType
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.MethodReferencesSearch
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.axonframework.intellij.ide.plugin.api.PublishingMethod
import org.axonframework.intellij.ide.plugin.search.comparePsiElementsBasedOnDisplayName
import org.jetbrains.kotlin.idea.KotlinFileType
import java.util.concurrent.ConcurrentHashMap

class MessagePublisherResolver(val project: Project) {
    private val searchScope = GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.projectScope(project), JavaFileType.INSTANCE, KotlinFileType.INSTANCE)
    private val constructorPublisherCache = ConcurrentHashMap<PsiType, CachedValue<List<PsiElement>>>()

    fun getConstructorPublishersForType(psiType: PsiType): List<PsiElement> {
        val cache = constructorPublisherCache.getOrPut(psiType) {
            CachedValuesManager.getManager(project).createCachedValue() {
                CachedValueProvider.Result.create(resolveConstructorPublishers(psiType), PsiModificationTracker.MODIFICATION_COUNT)
            }
        }
        return cache.value
    }


    fun getMethodPublishers(): Set<PsiElement> {
        return publishMethodPublisherCachedValue.value.keys
    }

    fun getMethodPublishersForType(psiType: PsiType): Set<PsiElement> {
        return publishMethodPublisherCachedValue.value.filter { it.value == psiType }.keys
    }

    private fun resolveConstructorPublishers(psiType: PsiType): List<PsiElement> {
        if (psiType !is PsiClassReferenceType) {
            return emptyList()
        }
        return psiType.resolve()?.constructors?.flatMap {
            MethodReferencesSearch.search(it, searchScope, true)
                    .mapping { ref -> ref.element }
                    .sortedWith { a, b -> comparePsiElementsBasedOnDisplayName(project, a, b) }
        } ?: emptyList()
    }

    private val publishMethodPublisherCachedValue = CachedValuesManager.getManager(project).createCachedValue() {
        CachedValueProvider.Result.create(resolveApplyPublishers(), PsiModificationTracker.MODIFICATION_COUNT)
    }

    private fun resolveApplyPublishers(): Map<PsiElement, PsiType> {
        return PublishingMethod.values()
                .flatMap { annotation -> findReferencesOfMethod(annotation) }
                .mapNotNull { findTypeOfMethod(it) }
                .toMap()
    }

    private fun findReferencesOfMethod(annotation: PublishingMethod): Iterable<PsiReference> {
        val method = annotation.getMethod(project) ?: return emptyList()
        return MethodReferencesSearch.search(method, searchScope, false)
    }

    private fun findTypeOfMethod(it: PsiReference): Pair<PsiElement, PsiType>? {
        val element = it.element
        val parent = element.parent
        if (parent !is PsiMethodCallExpression) {
            return null
        }
        val type = parent.argumentList.expressions.getOrNull(0)?.type ?: return null
        return element to type
    }
}
