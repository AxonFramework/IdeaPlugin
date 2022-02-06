package org.axonframework.intellij.ide.plugin.usage

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.util.isAggregate
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
    override fun isImplicitUsage(element: PsiElement): Boolean {
        val uastElement = element.toUElement()

        if (uastElement is UMethod) {
            return uastElement.isAnnotatedWIthAxon() || uastElement.isEmptyConstructorOfAggregate()
        }
        if (uastElement is UParameter && uastElement.uastParent is UMethod) {
            val uMethod = uastElement.uastParent as UMethod
            return uMethod.uastParameters[0] == uastElement && uMethod.isAnnotatedWIthAxon()
        }
        if (uastElement is UField) {
            return uastElement.hasRelevantAnnotation()
        }

        return false
    }

    private fun UMethod.isAnnotatedWIthAxon() = MessageHandlerType.values().any { this.uAnnotations.any { ann -> ann.qualifiedName == it.annotationName } }
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

    private fun UField.hasRelevantAnnotation() =
            AxonAnnotation.AGGREGATE_IDENTIFIER.fieldIsAnnotated(this) ||
                    AxonAnnotation.ENTITY_ID.fieldIsAnnotated(this)
}
