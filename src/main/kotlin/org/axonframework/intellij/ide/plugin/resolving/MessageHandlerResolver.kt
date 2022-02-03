package org.axonframework.intellij.ide.plugin.resolving

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClassObjectAccessExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.axonframework.intellij.ide.plugin.api.MessageHandler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.util.areAssignable
import org.axonframework.intellij.ide.plugin.util.axonScope
import org.axonframework.intellij.ide.plugin.util.comparePsiElementsBasedOnDisplayName
import org.axonframework.intellij.ide.plugin.util.toQualifiedName

class MessageHandlerResolver(private val project: Project) {
    private val handlerCache = CachedValuesManager.getManager(project).createCachedValue() {
        CachedValueProvider.Result.create(executeFindMessageHandlers(), PsiModificationTracker.MODIFICATION_COUNT)
    }

    fun findHandlersForType(qualifiedName: String): List<MessageHandler> {
        return handlerCache.value
                .filter { areAssignable(project, it.payloadType, qualifiedName) }
                .filter { it.element.isValid }
    }

    fun findAllHandlers() = handlerCache.value

    fun findHandlerByElement(psiElement: PsiElement): MessageHandler? {
        return handlerCache.value.firstOrNull { it.element == psiElement }
    }

    private fun executeFindMessageHandlers(): List<MessageHandler> {
        val annotatedMethods = MessageHandlerType.findAnnotationsForProject(project).flatMap { annotationClass ->
            AnnotatedElementsSearch.searchPsiMethods(annotationClass, project.axonScope()).findAll()
        }

        val handlers = annotatedMethods.mapNotNull {
            val type = resolveType(it)?.toQualifiedName() ?: return@mapNotNull null
            val handlerType = resolveHandlerType(it) ?: return@mapNotNull null
            MessageHandler(it, handlerType, type, resolveProjectionName(it))
        }

        return handlers.distinct()
                .sortedWith { a, b -> comparePsiElementsBasedOnDisplayName(project, a.element, b.element) }
    }

    /**
     * Resolves the name of the projection an event handler is in. Looks for the @ProcessingGroup annotation and
     * gets its value. Otherwise looks at the package name
     */
    private fun resolveProjectionName(method: PsiMethod): String? {
        val containingClass = method.containingClass ?: return null
        return containingClass.annotations
                .firstOrNull { annotation -> annotation.hasQualifiedName("org.axonframework.config.ProcessingGroup") }
                ?.let { (it.findAttributeValue("value") as PsiLiteralExpression).value as String }
                ?: method.toPackageName()
    }

    private fun PsiMethod.toPackageName() = containingClass?.qualifiedName?.split(".")?.dropLast(1)?.joinToString(".")

    private fun resolveType(method: PsiMethod): PsiType? {
        val possibleAnnotations = MessageHandlerType.findAnnotationsForProject(project)
        val annotation = method.annotations.firstOrNull { possibleAnnotations.contains(it.resolveAnnotationType()) }
                ?: return null
        val value = annotation.findDeclaredAttributeValue("payloadType")
        if (value is PsiClassObjectAccessExpression) {
            return value.type
        }
        return method.parameters.getOrNull(0)?.type as PsiType?
    }

    private fun resolveHandlerType(method: PsiMethod): MessageHandlerType? {
        val possibleAnnotations = MessageHandlerType.findAnnotationsGroupedByTypeForProject(project)

        return MessageHandlerType.values().firstOrNull {
            method.annotations.any { ann -> possibleAnnotations!![it]!!.contains(ann.resolveAnnotationType()) }
        }
    }
}

