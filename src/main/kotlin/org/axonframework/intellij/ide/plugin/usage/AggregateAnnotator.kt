package org.axonframework.intellij.ide.plugin.usage

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.util.isAggregate
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UIdentifier
import org.jetbrains.uast.toUElement

/**
 * Shows a warning on classes annotated with `@Aggregate` when:
 * - They have no zero-argument constructor. This constructor is required by Axon Framework to construct the aggregate.
 * - They have no AggregateIdentifier field. This is required by Axon Framework.
 *
 */
class AggregateAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element.toUElement(UIdentifier::class.java) == null) {
            return
        }
        val clazz = element.parent.toUElement(UClass::class.java) ?: return
        if (!clazz.isAggregate()) {
            return
        }
        val isMissingEmptyConstructor = clazz.constructors.none { !it.hasParameters() }
        if (isMissingEmptyConstructor) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Axon Framework requires an empty constructor on aggregates.")
                    .needsUpdateOnTyping()
                    .create()
        }
        val isMissingFieldWithAnnotation = clazz.fields.none {
            AxonAnnotation.AGGREGATE_IDENTIFIER.fieldIsAnnotated(it)
        }

        if (isMissingFieldWithAnnotation) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Axon Framework requires a field annotated with @AggregateIdentifier in Aggregates.")
                    .needsUpdateOnTyping()
                    .create()
        }
    }
}
