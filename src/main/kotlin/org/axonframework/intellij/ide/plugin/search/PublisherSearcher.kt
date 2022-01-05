package org.axonframework.intellij.ide.plugin.search

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiType
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.MethodReferencesSearch
import org.jetbrains.kotlin.idea.search.getKotlinFqName
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

object PublisherSearcher {
    fun findPublishersForType(project: Project, psiType: PsiType): List<PsiElement> {
        val fqName = if (psiType is PsiClassReferenceType) {
            psiType.resolve()?.getKotlinFqName()
        } else {
            return emptyList()
        }
        if (fqName == null) {
            return emptyList()
        }
        return JavaPsiFacade.getInstance(project).findClass(fqName.asString(), GlobalSearchScope.allScope(project))?.constructors?.flatMap {
            MethodReferencesSearch.search(it).findAll().map { ref ->
                ref.element
            }
        } ?: emptyList()
    }
}
