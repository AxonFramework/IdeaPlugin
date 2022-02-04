package org.axonframework.intellij.ide.plugin.resolving

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.searches.MethodReferencesSearch
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.PsiTreeUtil
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageCreator
import org.axonframework.intellij.ide.plugin.creators.DefaultMessageCreator
import org.axonframework.intellij.ide.plugin.util.areAssignable
import org.axonframework.intellij.ide.plugin.util.axonScope
import org.axonframework.intellij.ide.plugin.util.createCachedValue
import org.jetbrains.kotlin.psi.KtNamedFunction
import java.util.concurrent.ConcurrentHashMap

/**
 * Searches the codebase for places where a message payload is constructed.
 * It does this by searching for constructor references of compatible payloads. Inheritance is supported.
 *
 * Results are cached based on the Psi modifications of IntelliJ. This means the calculations are invalidated when
 * the PSI is modified (code is edited) or is collected by the garbage collector.
 */
class MessageCreationResolver(private val project: Project) {
    private val psiFacade = JavaPsiFacade.getInstance(project)
    private val handlerResolver = project.getService(MessageHandlerResolver::class.java)
    private val constructorsByPayloadCache = ConcurrentHashMap<String, CachedValue<List<MessageCreator>>>()


    fun getCreatorsForPayload(payloadQualifiedClassName: String): List<MessageCreator> {
        val cache = constructorsByPayloadCache.getOrPut(payloadQualifiedClassName) {
            project.createCachedValue {
                resolveCreatorForFqn(payloadQualifiedClassName)
            }
        }
        return cache.value
    }

    /**
     * Finds the already constructed/found creator in the caches. Useful for quick filtering in line marker popups.
     */
    fun findCreatorByElement(element: PsiElement): MessageCreator? {
        return constructorsByPayloadCache.values.filter { it.hasUpToDateValue() }
                .flatMap { it.value }
                .firstOrNull { it.element == element }
    }

    private fun resolveCreatorForFqn(qualifiedName: String): List<MessageCreator> {
        val classesForQualifiedName = handlerResolver.findAllHandlers()
                .map { it.payloadFullyQualifiedName }.distinct()
                .filter { areAssignable(project, qualifiedName, it) }
        return resolveCreatorsForFqns(classesForQualifiedName)
    }

    /**
     * This action is VERY expensive. Should only be used if the user does not depend on it or is expected to wait.
     * For example, when creating an Event Modeling board based on this info.
     */
    fun resolveAllCreators(): List<MessageCreator> {
        val handlers = handlerResolver.findAllHandlers()
        val handlerTypes = handlers.map { it.payloadFullyQualifiedName }.distinct()

        return resolveCreatorsForFqns(handlerTypes)
    }

    private fun resolveCreatorsForFqns(fqns: List<String>): List<MessageCreator> {
        return fqns.flatMap { typeFqn ->
            val clazz = psiFacade.findClass(typeFqn, project.axonScope()) ?: return@flatMap emptyList()
            clazz.constructors
                    .flatMap { MethodReferencesSearch.search(it, project.axonScope(), true) }
                    .map { ref -> createCreator(ref.element, typeFqn) }
                    .distinct()
        }
    }

    private fun createCreator(element: PsiElement, qualifiedName: String): MessageCreator {
        return DefaultMessageCreator(element, qualifiedName, findParentHandler(element))
    }

    private fun findParentHandler(element: PsiElement): Handler? {
        val ktMethodParent = PsiTreeUtil.getParentOfType(element, KtNamedFunction::class.java)
        if (ktMethodParent != null) {
            return handlerResolver.findHandlerByElement(ktMethodParent)
        }
        val javaMethodParent = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java)
        if (javaMethodParent != null) {
            return handlerResolver.findHandlerByElement(javaMethodParent)
        }

        return null
    }
}
