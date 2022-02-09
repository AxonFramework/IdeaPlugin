package org.axonframework.intellij.ide.plugin.util

import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.PsiElementWrapper
import org.axonframework.intellij.ide.plugin.resolving.MessageCreationResolver
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.toUElement
import javax.swing.Icon

fun sortingByDisplayName() = { elementA: PsiElementWrapper, elementB: PsiElementWrapper ->
    comparePsiElementsBasedOnDisplayName(elementA.element, elementB.element)
}

/**
 * Sorts the provided PSI elements. It's sorted by:
 * - Whether it's in a test source file
 * - Its name
 */
fun comparePsiElementsBasedOnDisplayName(elementA: PsiElement, elementB: PsiElement): Int {
    return elementA.toElementText().compareTo(elementB.toElementText())
}

fun String.toShortName() = split(".").last()

/**
 * Creates a description of the element as used in lists.
 *
 * @see org.axonframework.intellij.ide.plugin.markers.AxonCellRenderer
 */
fun PsiElement.toElementText(): String {
    val handlerResolver = project.getService(MessageHandlerResolver::class.java)
    val handler = handlerResolver.findHandlerByElement(this)
    if (handler != null) {
        return handler.renderText()
    }

    val creatorResolver = project.getService(MessageCreationResolver::class.java)
    val creator = creatorResolver.findCreatorByElement(this)
    if (creator?.parentHandler != null) {
        return creator.parentHandler!!.renderText()
    }

    val methodParent = toUElement()?.getParentOfType<UMethod>()
    if (methodParent != null) {
        return methodParent.containingClassname() + "." + methodParent.name
    }

    return this.containingFile.name
}

/**
 * Creates a description of the element's container as used in lists.
 *
 * @see org.axonframework.intellij.ide.plugin.markers.AxonCellRenderer
 */
fun PsiElement.toContainerText(): String? {
    val handlerResolver = project.getService(MessageHandlerResolver::class.java)

    val handler = handlerResolver.findHandlerByElement(this)
    if (handler != null) {
        return handler.renderContainerText()
    }

    val creatorResolver = project.getService(MessageCreationResolver::class.java)
    val creator = creatorResolver.findCreatorByElement(this)
    if (creator != null) {
        return creator.renderContainerText()
    }
    return null
}

/**
 * Determines the icon that should be shown for this PsiElement
 *
 * @see org.axonframework.intellij.ide.plugin.markers.AxonCellRenderer
 */
fun PsiElement.toIcon(): Icon {
    val handler = project.getService(MessageHandlerResolver::class.java).findHandlerByElement(this)
    if (handler != null) {
        return handler.getIcon()
    }
    val creator = project.getService(MessageCreationResolver::class.java).findCreatorByElement(this)
    if (creator != null) {
        return creator.parentHandler?.getIcon() ?: creator.getIcon()
    }
    return AxonIcons.Publisher
}
