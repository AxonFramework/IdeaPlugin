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

package org.axonframework.intellij.ide.plugin.resolving.handlers.searchers

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.resolving.handlers.types.DeadlineHandler
import org.axonframework.intellij.ide.plugin.util.resolveAnnotationStringValue
import org.axonframework.intellij.ide.plugin.util.resolvePayloadType
import org.axonframework.intellij.ide.plugin.util.toQualifiedName

/**
 * Searches for any handlers that can handle deadlines.
 * If there is not payload specific, the payload is java.lang.Object as fallback.
 * In addition, deadline handlers have a name.
 *
 * @see org.axonframework.intellij.ide.plugin.handlers.types.DeadlineHandler
 */
class DeadlineHandlerSearcher : AbstractHandlerSearcher(MessageHandlerType.DEADLINE) {
    override fun createMessageHandler(method: PsiMethod, annotation: PsiClass?): Handler {
        val payloadType = method.resolvePayloadType()?.toQualifiedName() ?: "java.lang.Object"
        val deadlineName = method.resolveAnnotationStringValue(AxonAnnotation.DEADLINE_HANDLER, "deadlineName")
        return DeadlineHandler(method, payloadType, deadlineName ?: payloadType)
    }
}
