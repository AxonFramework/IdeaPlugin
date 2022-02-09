package org.axonframework.intellij.ide.plugin.util

import com.intellij.openapi.diagnostic.logger
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiAnnotationMemberValue
import com.intellij.psi.PsiBinaryExpression
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiModifierListOwner
import com.intellij.psi.PsiReferenceExpression
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.resolving.AnnotationResolver
import org.axonframework.intellij.ide.plugin.resolving.ResolvedAnnotation


object PsiAnnotationUtils {}

val logger = logger<PsiAnnotationUtils>()

/**
 * Find the most specific annotation of a specific type on a PsiElement.
 * For example, if @MyAggregate is annotated with @Aggregate, which is annotation with @AggregateRoot
 * this method will return @MyAggregate
 *
 * @param annotation Axon's annotation to check
 * @param condition Optional condition to check relevant, e.g. for presence of payloadType
 */
fun PsiModifierListOwner.resolveAnnotation(annotation: AxonAnnotation, condition: (PsiAnnotation) -> Boolean = { true }): PsiAnnotation? {
    val annotationResolver = project.getService(AnnotationResolver::class.java)
    val annotations = annotationResolver.getAnnotationClasses(annotation)
    val mostGenericAnnotation = annotations.firstOrNull { it.parent == null } ?: return null

    fun recursivelyResolveChild(currentAnnotation: ResolvedAnnotation): PsiAnnotation? {
        val children = annotations.filter { it.parent == currentAnnotation }

        // First, check if one of the children (or their children) suffice
        for (child in children) {
            val result = recursivelyResolveChild(child)
            if (result != null) {
                return result
            }
        }

        return this.annotations.filter { it.hasQualifiedName(currentAnnotation.qualifiedName) }
                .firstOrNull(condition)
    }

    return recursivelyResolveChild(mostGenericAnnotation)
}

/**
 * Checks whether the element is annotated with a certain Axon annotation.
 * Also checks all descendants of that annotation.
 */
fun PsiModifierListOwner.hasAnnotation(annotation: AxonAnnotation): Boolean {
    val annotationResolver = project.getService(AnnotationResolver::class.java)
    val annotations = annotationResolver.getAnnotationClasses(annotation)
    return annotations.any { hasAnnotation(it.qualifiedName) }
}

/**
 * Resolve the string attribute value of one of the Axon Annottions.
 * Since they are meta, the annotations can be annotated, which can in turb contain the value. So we have to do it
 * the recursive way
 *
 * @param annotation Axon's annotation to target
 * @param attributeName key of annotation to look for. Most of the time this is "value"
 */
fun PsiModifierListOwner.resolveAnnotationStringValue(annotation: AxonAnnotation, attributeName: String): String? {
    val relevantAnnotation = resolveAnnotation(annotation) ?: return null
    val attribute = relevantAnnotation.findDeclaredAttributeValue(attributeName)
    if (attribute != null) {
        return resolveAttributeStringValue(attribute)
    }
    // The annotation itself might be annotated with one that contains the value
    val annType = relevantAnnotation.resolveAnnotationType() ?: return null
    return annType.resolveAnnotationStringValue(annotation, attributeName)
}

private fun resolveAttributeStringValue(attribute: PsiAnnotationMemberValue?): String? {
    // Is a direct value, @ProcessingGroup("MY_GROUP")
    if (attribute is PsiLiteralExpression) {
        return attribute.value as String?
    }

    // Is a binary expression, so a combination of two things. Since the attribute value is a string,
    // can only be a + operator.  e.g.  @ProcessingGroup(SomeClass.SOME_CONSTANT + "MyAddition")
    if (attribute is PsiBinaryExpression) {
        return resolveAttributeStringValue(attribute.lOperand) + resolveAttributeStringValue(attribute.rOperand)
    }
    // It references another value (e.g. @ProcessingGroup(SomeClass.SOME_CONSTANT).
    // References a String -> return the reference directly
    if (attribute is PsiReferenceExpression) {
        val resolvedElement = attribute.references[0].resolve() ?: return null
        val firstRelevantChild = resolvedElement.children.firstOrNull { it is PsiLiteralExpression || it is PsiReferenceExpression || it is PsiBinaryExpression }
        if (firstRelevantChild == null) {
            logger.warn("No relevant child found while resolving attribute of annotation. Found elements: " + resolvedElement.children.map { it })
            return null
        }
        return resolveAttributeStringValue(firstRelevantChild as PsiAnnotationMemberValue)
    }
    return null
}
