/*
 *  Copyright (c) 2022-2026. Axon Framework
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.axonframework.intellij.ide.plugin.resolving.handlers.types

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.util.toShortName

/**
 * Represents an entity creator constructor annotated with @EntityCreator in Axon Framework 5.
 * This constructor can accept the first event as a payload and creates the entity based on that event.
 * It will only be detected as message handler if it has a payload defined. Otherwise, it's just a constructor invocation,
 * not a message handler.
 *
 * Pattern: `@EntityCreator MyEntity(MyCreatedEvent event)`
 *
 * @see org.axonframework.intellij.ide.plugin.resolving.handlers.searchers.axon5.EntityCreatorSearcher
 */
data class EntityCreator(
    override val element: PsiMethod,
    override val payload: String,
) : Handler, PsiElement by element {
    override val handlerType: MessageHandlerType = MessageHandlerType.EVENT_SOURCING

    override fun renderText(): String {
        return "Entity creation: " + payload.toShortName()
    }
}
