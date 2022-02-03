package org.axonframework.intellij.ide.plugin.resolving

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.MethodReferencesSearch
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.axonframework.intellij.ide.plugin.util.areAssignable
import org.axonframework.intellij.ide.plugin.util.axonScope
import org.jetbrains.kotlin.idea.KotlinFileType
import java.util.concurrent.ConcurrentHashMap

class MessagePublisherResolver(val project: Project) {
    private val searchScope = GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.projectScope(project), JavaFileType.INSTANCE, KotlinFileType.INSTANCE)
    private val constructorPublisherCache = ConcurrentHashMap<String, CachedValue<List<PsiElement>>>()

    fun getConstructorPublishersForType(qualifiedName: String): List<PsiElement> {
        val cache = constructorPublisherCache.getOrPut(qualifiedName) {
            CachedValuesManager.getManager(project).createCachedValue() {
                CachedValueProvider.Result.create(resolveConstructorPublishers(qualifiedName), PsiModificationTracker.MODIFICATION_COUNT)
            }
        }
        return cache.value
    }

    private fun resolveConstructorPublishers(qualifiedName: String): List<PsiElement> {
        val handlerResolver = project.getService(MessageHandlerResolver::class.java)

        // We need to take care of inheritance, so we find all classes assignable to the referenced type
        val psiFacade = JavaPsiFacade.getInstance(project)
        val classesForQN = handlerResolver.findAllHandlers()
                .map { it.payloadType }.distinct()
                .filter { areAssignable(project, qualifiedName, it) }
                .mapNotNull { psiFacade.findClass(it, project.axonScope()) }
        val constructors = classesForQN.map { it.constructors }
        return constructors.flatMap { it ->
            it.flatMap {
                MethodReferencesSearch.search(it, searchScope, true)
            }.map { ref -> ref.element }
        }
    }
}
