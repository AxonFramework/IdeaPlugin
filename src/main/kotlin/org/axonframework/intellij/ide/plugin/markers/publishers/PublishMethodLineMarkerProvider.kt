/*
 *  Copyright (c) 2022. Axon Framework
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
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.handlers.types.CommandHandlerInterceptor
import org.axonframework.intellij.ide.plugin.handlers.types.DeadlineHandler
import org.axonframework.intellij.ide.plugin.markers.AxonCellRenderer
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.axonframework.intellij.ide.plugin.util.handlerResolver
import org.axonframework.intellij.ide.plugin.util.toQualifiedName
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClassLiteralExpression
import org.jetbrains.uast.UIdentifier
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.UTypeReferenceExpression
import org.jetbrains.uast.UastCallKind
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.getQualifiedName
import org.jetbrains.uast.getUParentForIdentifier
import org.jetbrains.uast.toUElement
import org.jetbrains.uast.toUElementOfType

/**
 * Provides a gutter icon on constructor invocations when that type is also known as a message payload.
 * It is known as a message payload when a handler can be found for it.
 *
 * Note that the UAST tree of Java and Kotlin files is slightly different in this use-case, so detection of
 * the payload is based on the file type.
 *
 * @see MessageHandlerResolver
 */
class PublishMethodLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val payload = when (element.containingFile.fileType) {
            is JavaFileType -> qualifiedNameForJava(element)
            is KotlinFileType -> qualifiedNameForKotlin(element)
            else -> null
        } ?: return null

        val allHandlers = element.handlerResolver().findHandlersForType(payload)
            // Hide DeadlineHandlers here. These are handled by a more specific LineMarkerProvider
            .filter { it !is DeadlineHandler }
        val isCommand = allHandlers.all { it.handlerType == MessageHandlerType.COMMAND }
        val handlers = allHandlers.filter { it !is CommandHandlerInterceptor || isCommand } // Only show interceptors when is a command
        if (handlers.isEmpty()) {
            return null
        }
        return NavigationGutterIconBuilder.create(AxonIcons.Publisher)
            .setPopupTitle("Axon Message Handlers")
            .setTooltipText("Navigate to Axon message handlers")
            .setCellRenderer(AxonCellRenderer.getInstance())
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .setTargets(NotNullLazyValue.createValue { handlers.map { it.element } })
            .createLineMarkerInfo(element)
    }

    private fun qualifiedNameForKotlin(element: PsiElement): String? {
        val uElement = element.toUElementOfType<UIdentifier>() ?: return null
        val callExpression = uElement.getParentOfType(UCallExpression::class.java, false, USimpleNameReferenceExpression::class.java)
        if (callExpression != null && callExpression.kind == UastCallKind.CONSTRUCTOR_CALL) {
            return callExpression.classReference.getQualifiedName()
        }
        val qualifiedReference = uElement.getParentOfType(UClassLiteralExpression::class.java, true, UIdentifier::class.java)
        if (qualifiedReference != null) {
            return qualifiedReference.type.toQualifiedName()
        }
        return null
    }

    private fun qualifiedNameForJava(element: PsiElement): String? {
        val referenceExpression = getUParentForIdentifier(element) as? USimpleNameReferenceExpression ?: return null
        val uElementParent = element.parent.parent.toUElement()
        val isConstructor = uElementParent is UCallExpression && uElementParent.kind == UastCallKind.CONSTRUCTOR_CALL
        val isClassReference = uElementParent is UTypeReferenceExpression
                && uElementParent.uastParent is UClassLiteralExpression
                && uElementParent.getParentOfType<UAnnotation>() == null
        if (isConstructor || isClassReference) {
            return referenceExpression.getQualifiedName()
        }
        return null
    }
}
