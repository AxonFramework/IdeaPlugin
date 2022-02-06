package org.axonframework.intellij.ide.plugin.util

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiType
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.jetbrains.kotlin.idea.KotlinFileType

/**
 * Convenience method to fully qualified name of type.
 * Throws if we get a type we do not expect so we can support it.
 */
fun PsiType?.toQualifiedName(): String? = this?.let {
    if (this is PsiClassReferenceType) {
        return this.resolve()?.qualifiedName
    } else {
        throw IllegalArgumentException("Can not handle psiType of type " + this::class.qualifiedName)
    }
}

/**
 * Checks whether A can be assigned to B. For example:
 * A extends B, then A is assignable to B. Used for matching of java supertypes in handlers.
 */
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
fun Project.allScope() = GlobalSearchScope.allScope(this)

/**
 * Convenience method to quickly create a cached value for a project based on PSI modifications.
 */
fun <T> Project.createCachedValue(supplier: () -> T) = CachedValuesManager.getManager(this).createCachedValue() {
    CachedValueProvider.Result.create(supplier.invoke(), PsiModificationTracker.MODIFICATION_COUNT)
}

fun PsiClass?.isAggregate() = this?.hasAnnotation(AxonAnnotation.AGGREGATE.annotationName) == true
fun PsiClass?.isEntity() = this?.allFields?.any { it.hasAnnotation(AxonAnnotation.ENTITY_ID.annotationName) } == true
