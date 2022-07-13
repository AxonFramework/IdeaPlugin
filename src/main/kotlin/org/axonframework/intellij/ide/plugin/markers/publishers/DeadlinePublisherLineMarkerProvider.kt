/*
 *  Copyright (c) (2010-2022). Axon Framework
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.axonframework.intellij.ide.plugin.markers.publishers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.markers.AxonNavigationGutterIconRenderer
import org.axonframework.intellij.ide.plugin.markers.handlers.ValidatingLazyValue
import org.axonframework.intellij.ide.plugin.resolving.handlers.types.DeadlineHandler
import org.axonframework.intellij.ide.plugin.util.deadlineMethodResolver
import org.axonframework.intellij.ide.plugin.util.handlerResolver
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.lexer.KtToken
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UIdentifier
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.evaluateString
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.toUElement
import org.jetbrains.uast.toUElementOfType

/**
 * Shows a gutter icon whenever a called method matches one of the deadline schedule or cancel methods.
 *
 * @see DeadlineHandler
 * @see org.axonframework.intellij.ide.plugin.resolving.DeadlineManagerMethodResolver
 * @see org.axonframework.intellij.ide.plugin.creators.searchers.DeadlineMessageCreatorSearcher
 */
class DeadlinePublisherLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val deadlineName = when (element.containingFile.fileType) {
            is JavaFileType -> getDeadlineNameForJava(element)
            is KotlinFileType -> getDeadlineNameForKotlin(element)
            else -> null
        } ?: return null



        return AxonNavigationGutterIconRenderer(
            icon = AxonIcons.Publisher,
            popupTitle = "Axon Deadline Handlers",
            tooltipText = "Navigate to Axon deadline handlers",
            emptyText = "No deadline handlers were found",
            elements = ValidatingLazyValue(element)  {
                element.project.handlerResolver().findAllHandlers()
                    .filterIsInstance<DeadlineHandler>()
                    .filter { it.deadlineName == deadlineName }
            }).createLineMarkerInfo(element)
    }

    /*
    * Simply resolving the method reference (like in Java) does not work in Kotlin,
    * so we have to work around this using a combination of UAST and Psi.
     */
    private fun getDeadlineNameForKotlin(element: PsiElement): String? {
        if (element.elementType !is KtToken) {
            return null
        }
        val parent = PsiTreeUtil.findFirstParent(element) { it is KtNameReferenceExpression } ?: return null
        val methodCall = element.toUElement()?.getParentOfType<UCallExpression>() ?: return null
        val methods = element.deadlineMethodResolver().getAllReferencedMethods()
        val matchingMethods = methods.filter { method ->
            parent.references.any { reference ->
                reference.isReferenceTo(method)
            }
        }
        if (matchingMethods.isNotEmpty()) {
            val parameterIndex = element.deadlineMethodResolver().getDeadlineParameterIndex(matchingMethods[0]) ?: return null
            return methodCall.valueArguments[parameterIndex].evaluateString()
        }
        return null
    }

    private fun getDeadlineNameForJava(element: PsiElement): String? {
        val methodCall = element.toUElementOfType<UIdentifier>()
            ?.getParentOfType(
                UCallExpression::class.java,
                true,
                USimpleNameReferenceExpression::class.java,
                UQualifiedReferenceExpression::class.java
            )
            ?: return null
        val referencedMethod = methodCall.resolve() ?: return null
        val methods = element.deadlineMethodResolver().getAllReferencedMethods()
        if (methods.contains(referencedMethod)) {
            val parameterIndex = element.deadlineMethodResolver().getDeadlineParameterIndex(referencedMethod) ?: return null
            return methodCall.valueArguments[parameterIndex].evaluateString()
        }
        return null

    }
}
