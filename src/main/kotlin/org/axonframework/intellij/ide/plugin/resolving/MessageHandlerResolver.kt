package org.axonframework.intellij.ide.plugin.resolving

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassObjectAccessExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import com.intellij.psi.search.GlobalSearchScope.getScopeRestrictedByFileTypes
import com.intellij.psi.search.GlobalSearchScope.projectScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.axonframework.intellij.ide.plugin.api.MessageHandler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.search.comparePsiElementsBasedOnDisplayName
import org.jetbrains.kotlin.idea.KotlinFileType

class MessageHandlerResolver(private val project: Project) {
    private val searchScope = getScopeRestrictedByFileTypes(projectScope(project), JavaFileType.INSTANCE, KotlinFileType.INSTANCE)
    private val handlerCache = CachedValuesManager.getManager(project).createCachedValue() {
        CachedValueProvider.Result.create(executeFindMessageHandlers(), PsiModificationTracker.MODIFICATION_COUNT)
    }

    fun findHandlersForType(psiType: PsiType): List<MessageHandler> {
        return handlerCache.value
                .filter { psiType.isAssignableFrom(it.payloadType) }
                .filter { it.element.isValid }
    }

    fun findHandlerByElement(psiElement: PsiElement): MessageHandler? {
        return handlerCache.value.firstOrNull { it.element == psiElement }
    }

    fun findAllMessageHandlers(): List<MessageHandler> {
        return handlerCache.value
    }

    private fun executeFindMessageHandlers(): List<MessageHandler> {
        return MessageHandlerType.values().flatMap { handlerType ->
            handlerType.findAllRelevantAnnotationClasses(project).flatMap { annotationClass ->
                AnnotatedElementsSearch.searchPsiMethods(annotationClass, searchScope)
                        .findAll()
                        .map { method -> method to resolveType(method, annotationClass) }
                        .filter { (_, payloadType) -> payloadType != null }
                        .map { (method, payloadType) -> MessageHandler(method, handlerType, payloadType!!, resolveProjectionName(method, handlerType)) }
            }
        }
                .distinct()
                .sortedWith { a, b -> comparePsiElementsBasedOnDisplayName(project, a.element, b.element) }
    }

    /**
     * Resolves the name of the projection an event handler is in. Looks for the @ProcessingGroup annotation and
     * gets its value. Otherwise looks at the package name
     */
    private fun resolveProjectionName(method: PsiMethod, type: MessageHandlerType): String? {
        val containingClass = method.containingClass ?: return null
        if (type != MessageHandlerType.EVENT) return null
        return containingClass.annotations
                .firstOrNull { annotation -> annotation.hasQualifiedName("org.axonframework.config.ProcessingGroup") }
                ?.let { (it.findAttributeValue("value") as PsiLiteralExpression).value as String }
                ?: method.toPackageName()
    }

    private fun PsiMethod.toPackageName() = containingClass?.qualifiedName?.split(".")?.dropLast(1)?.joinToString(".")

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

