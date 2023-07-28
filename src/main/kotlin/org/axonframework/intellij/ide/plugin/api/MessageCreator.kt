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

package org.axonframework.intellij.ide.plugin.api

/**
 * Represents an element that creates a message (payload).
 *
 * @see org.axonframework.intellij.ide.plugin.resolving.MessageCreationResolver
 */
interface MessageCreator : PsiElementWrapper {
    /**
     * The name of the payload being created. For deadlines, this is the deadlineName. For all other messages,
     * it's the payload type.
     */
    val payload: String
}

