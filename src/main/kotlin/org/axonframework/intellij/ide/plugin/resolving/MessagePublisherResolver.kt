package org.axonframework.intellij.ide.plugin.resolving

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiType
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.MethodReferencesSearch
import org.axonframework.intellij.ide.plugin.search.comparePsiElementsBasedOnDisplayName
import org.jetbrains.kotlin.idea.KotlinFileType

class MessagePublisherResolver(val project: Project) {
    private val searchScope = GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.projectScope(project), JavaFileType.INSTANCE, KotlinFileType.INSTANCE)
    val publishers = mutableMapOf<PsiType, MutableList<PsiElement>>()


    fun getPublishersForType(psiType: PsiType): List<PsiElement> {
        if (psiType !is PsiClassReferenceType) {
            return emptyList()
        }
        return psiType.resolve()?.constructors?.flatMap {
            MethodReferencesSearch.search(it, searchScope, true)
                    .mapping { ref -> ref.element }
                    .findAll()
                    .sortedWith { a, b -> comparePsiElementsBasedOnDisplayName(project, a, b) }
        } ?: emptyList()
    }
}
