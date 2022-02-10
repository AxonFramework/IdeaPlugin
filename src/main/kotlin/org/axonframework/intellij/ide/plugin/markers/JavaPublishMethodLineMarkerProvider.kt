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

package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClassLiteralExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.UTypeReferenceExpression
import org.jetbrains.uast.UastCallKind
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.getQualifiedName
import org.jetbrains.uast.getUParentForIdentifier
import org.jetbrains.uast.toUElement

/**
 * Provides a gutter icon on constructor invocations when that type is also known as a message payload.
 * It is known as a message payload when a handler can be found for it.
 *
 * This one is only for Java files. Kotlin and Java apparently have different UAST trees as well.
 *
 * @see MessageHandlerResolver
 */
class JavaPublishMethodLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val referenceExpression = getUParentForIdentifier(element) as? USimpleNameReferenceExpression ?: return null
        val uElementParent = element.parent.parent.toUElement()
        val isConstructor = uElementParent is UCallExpression && uElementParent.kind == UastCallKind.CONSTRUCTOR_CALL
        val isClassReference = uElementParent is UTypeReferenceExpression && uElementParent.uastParent is UClassLiteralExpression && uElementParent.getParentOfType<UAnnotation>() == null
        if (!isConstructor && !isClassReference) {
            return null
        }
        val qualifiedName = referenceExpression.getQualifiedName() ?: return null
        return element.markerForQualifiedName(qualifiedName)
    }
}
