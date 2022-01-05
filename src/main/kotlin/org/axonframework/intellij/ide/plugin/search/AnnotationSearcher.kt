package org.axonframework.intellij.ide.plugin.search

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch

object AnnotationSearcher {
    fun findMessageHandlingAnnotation(project: Project): List<PsiClass> {
        val instance = JavaPsiFacade.getInstance(project)
        val projectScope = GlobalSearchScope.allScope(project)

        val messageHandlerClass = instance.findClass("org.axonframework.messaging.annotation.MessageHandler", projectScope) ?: return emptyList()
        val levelOneHandlerClasses = searchForAnnotations(messageHandlerClass, projectScope)
        val levelTwoHandlerClasses = levelOneHandlerClasses.flatMap { searchForAnnotations(it, projectScope) }

        return listOf(messageHandlerClass) + levelOneHandlerClasses + levelTwoHandlerClasses
    }

    private fun searchForAnnotations(messageHandlerClass: PsiClass, projectScope: GlobalSearchScope): Collection<PsiClass> =
            AnnotatedElementsSearch.searchPsiClasses(messageHandlerClass, projectScope).findAll()
}
