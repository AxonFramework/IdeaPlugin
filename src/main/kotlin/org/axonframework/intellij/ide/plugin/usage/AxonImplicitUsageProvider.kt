package org.axonframework.intellij.ide.plugin.usage

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.util.isAggregate
import org.axonframework.intellij.ide.plugin.util.isAnnotated
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UField
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UParameter
import org.jetbrains.uast.getUastParentOfType
import org.jetbrains.uast.toUElement

/**
 * Marks certain elements in the code as used automatically.
 * - All handlers (command, query, event, and so on), as long as they are annotated.
 * - The first argument of all handlers
 * - fields annotated with EntityId or AggregateIdentifier
 */
class AxonImplicitUsageProvider : ImplicitUsageProvider {
    private val fieldAnnotations = listOf(
            AxonAnnotation.ROUTING_KEY,
            AxonAnnotation.ENTITY_ID,
    )

    override fun isImplicitUsage(element: PsiElement): Boolean {
        val uastElement = element.toUElement()

        if (uastElement is UMethod) {
            return uastElement.isAnnotatedWithAxon() || uastElement.isEmptyConstructorOfAggregate()
        }
        if (uastElement is UParameter && uastElement.uastParent is UMethod) {
            val uMethod = uastElement.uastParent as UMethod
            return uMethod.uastParameters[0] == uastElement && uMethod.isAnnotatedWithAxon()
        }
        if (uastElement is UField) {
            return uastElement.hasRelevantAnnotation()
        }

        return false
    }

    private fun UMethod.isEmptyConstructorOfAggregate() = isConstructor && uastParameters.isEmpty() && getUastParentOfType(UClass::class.java).isAggregate()

    override fun isImplicitRead(element: PsiElement): Boolean {
        val uastElement = element.toUElement()
        if (uastElement is UField) {
            return uastElement.hasRelevantAnnotation()
        }
        return false
    }

    override fun isImplicitWrite(element: PsiElement): Boolean {
        return false
    }


    private fun UMethod.isAnnotatedWithAxon() = MessageHandlerType.values().any {
        isAnnotated(it.annotation)
    }

    private fun UField.hasRelevantAnnotation() = fieldAnnotations.any {
        isAnnotated(it)
    }
}
