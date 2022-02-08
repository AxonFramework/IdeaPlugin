package org.axonframework.intellij.ide.plugin.usage

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.axonframework.intellij.ide.plugin.util.isAggregate
import org.jetbrains.uast.UClass
import org.jetbrains.uast.getUParentForIdentifier

/**
 * Shows a warning on classes annotated with `@Aggregate` when:
 * - They have no zero-argument constructor. This constructor is required by Axon Framework to construct the aggregate.
 * - They have no AggregateIdentifier field. This is required by Axon Framework.
 *
 */
class CommandMessageAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val uElement = getUParentForIdentifier(element) ?: return
        if (uElement !is UClass || uElement.isAggregate()) {
            return
        }
        val qualifiedName = uElement.qualifiedName ?: return
        val handlers = element.project.getService(MessageHandlerResolver::class.java).findHandlersForType(qualifiedName)
        if (handlers.isEmpty()) {
            return
        }
        val isCommandMessage = handlers.any { it.handlerType == MessageHandlerType.COMMAND }
        if (isCommandMessage) {
            val missingTargetId = uElement.fields.none { f -> AxonAnnotation.TARGET_AGGREGATE_IDENTIFIER.fieldIsAnnotated(f) }
            if (missingTargetId) {
                holder.newAnnotation(HighlightSeverity.ERROR, "This message is used as a command, but no @TargetAggregateIdentifier was found")
                        .needsUpdateOnTyping()
                        .create()
            }
        }
    }
}
