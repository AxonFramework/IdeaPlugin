package org.axonframework.intellij.ide.plugin.util

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiType
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.idea.KotlinFileType

fun PsiType?.toQualifiedName(): String? = this?.let {
    if (this is PsiClassReferenceType) {
        return this.resolve()?.qualifiedName
    } else {
        throw IllegalArgumentException("Can not handle psiType of type " + this::class.qualifiedName)
    }
}

fun areAssignable(project: Project, qualifiedNameA: String, qualifiedNameB: String): Boolean {
    if (qualifiedNameA == qualifiedNameB) {
        return true
    }
    val classesA = JavaPsiFacade.getInstance(project).findClasses(qualifiedNameA, project.axonScope())
    val classesB = JavaPsiFacade.getInstance(project).findClasses(qualifiedNameB, project.axonScope())

    return classesA.any { a ->
        classesB.any { b ->
            b.isInheritor(a, true)
        }
    }
}


fun Project.axonScope() = GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.projectScope(this), JavaFileType.INSTANCE, KotlinFileType.INSTANCE)
