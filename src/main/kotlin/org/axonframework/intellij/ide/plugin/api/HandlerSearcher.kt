package org.axonframework.intellij.ide.plugin.api

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassObjectAccessExpression
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import org.axonframework.intellij.ide.plugin.util.allScope
import org.axonframework.intellij.ide.plugin.util.axonScope
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElement

/**
 * HandlerSearchers are responsible for finding the handlers of their `MessageHandlerType`.
 * They can choose to add additional information in their `MessageHandlerType` so this can be used when the handler
 * is displayed.
 *
 * In order to add a new HandlerSearcher:
 * - Add the Type to `MessageHandlerType`
 * - Create a `Handler` implementation, responsible for containing all data related to the handler.
 * - Create a `HandlerSearcher` implementation, responsible for creating the `Handler` instance.
 * - Add your handler to `plugin.xml` as an extension to the `org.axonframework.intellij.axonplugin.handlerSearcher` extension point.
 *
 * @see HandlerSearcher
 * @see Handler
 */
abstract class HandlerSearcher(private val handlerType: MessageHandlerType) {
    abstract fun createMessageHandler(method: PsiMethod): Handler?

    open fun search(project: Project): List<Handler> {
        val annotationClasses = findAllRelevantAnnotationClasses(project)
        val annotatedMethods = annotationClasses.flatMap {
            AnnotatedElementsSearch.searchPsiMethods(it, project.axonScope()).findAll()
        }
        return annotatedMethods.mapNotNull { this.createMessageHandler(it) }.distinct()
    }

    private fun findAllRelevantAnnotationClasses(project: Project): List<PsiClass> {
        val ownClass = getClass(project) ?: return emptyList()
        val annotatedAnnotations = AnnotatedElementsSearch.searchPsiClasses(ownClass, project.allScope()).findAll()
                .filter { !MessageHandlerType.exists(it.qualifiedName) }
        return listOf(ownClass) + annotatedAnnotations
    }

    private fun getClass(project: Project): PsiClass? {
        return JavaPsiFacade.getInstance(project).findClass(handlerType.annotationName, project.allScope())
    }

    /**
     * Resolves the payload type of the method. Looks at the first parameter of the method to determine the type.
     * If there is a `payloadType` attribute added on the annotation, use that instead.
     *
     * @return Payload Type
     */
    protected fun resolvePayloadType(method: PsiMethod): PsiType? {
        val possibleAnnotations = findAllRelevantAnnotationClasses(method.project)
        val annotation = method.annotations.firstOrNull { possibleAnnotations.contains(it.resolveAnnotationType()) }
        if (annotation != null) {
            val value = annotation.findDeclaredAttributeValue("payloadType")
            if (value is PsiClassObjectAccessExpression) {
                return value.type
            }
        }
        return method.toUElement(UMethod::class.java)?.uastParameters?.getOrNull(0)?.typeReference?.type
    }
}
