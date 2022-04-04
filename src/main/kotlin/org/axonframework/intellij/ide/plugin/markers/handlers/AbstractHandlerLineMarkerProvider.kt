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

package org.axonframework.intellij.ide.plugin.markers.handlers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.util.annotationResolver
import org.axonframework.intellij.ide.plugin.util.resolvePayloadType
import org.axonframework.intellij.ide.plugin.util.toQualifiedName
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UIdentifier
import org.jetbrains.uast.UNamedExpression
import org.jetbrains.uast.getContainingUMethod
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.toUElementOfType

/**
 * Parent for all handler method line marker providers.
 * Determines the type of handler and the payload (if any present). The implementor should create a line marker (or not)
 * based on that information.
 */
abstract class AbstractHandlerLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val info = getInfoForAnnotatedFunction(element)
        if (info != null) {
            return createLineMarker(element, info.first, info.second)
        }
        return null
    }

    protected abstract fun createLineMarker(
        element: PsiElement,
        handlerType: MessageHandlerType,
        payload: String?,
    ): LineMarkerInfo<*>?

    private fun getInfoForAnnotatedFunction(element: PsiElement): Pair<MessageHandlerType, String?>? {
        val uElement = element.toUElementOfType<UIdentifier>() ?: return null
        val uAnnotation = uElement.getParentOfType<UAnnotation>() ?: return null
        if (uElement.getParentOfType<UNamedExpression>() != null) {
            // If we don't return here, all identifiers in an annotation will trigger this marker.
            // E.q. `@CommandHandler(payloadType = BookRoomCommand.class)` will make 3
            return null
        }
        val annotationName = uAnnotation.qualifiedName ?: return null
        val method = uAnnotation.getContainingUMethod() ?: return null
        val handlerType = element.annotationResolver().getMessageTypeForAnnotation(annotationName) ?: return null
        val qualifiedName = method.javaPsi.resolvePayloadType()?.toQualifiedName()
        return Pair(handlerType, qualifiedName)
    }
}
