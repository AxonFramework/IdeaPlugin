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

package org.axonframework.intellij.ide.plugin.api


/**
 * Handler definitions with their Annotation and MessageType.
 *
 * Generic MessageHandlers are not supported, since they cannot be mapped to a type.
 * All custom annotations annotated with one of the annotations in this enum are supported.
 */
enum class MessageHandlerType(
    val annotation: AxonAnnotation,
    val messageType: MessageType,
) {
    COMMAND(AxonAnnotation.COMMAND_HANDLER, MessageType.COMMAND),
    EVENT(AxonAnnotation.EVENT_HANDLER, MessageType.EVENT),
    EVENT_SOURCING(AxonAnnotation.EVENT_SOURCING_HANDLER, MessageType.EVENT),
    QUERY(AxonAnnotation.QUERY_HANDLER, MessageType.QUERY),
    DEADLINE(AxonAnnotation.DEADLINE_HANDLER, MessageType.DEADLINE),
    COMMAND_INTERCEPTOR(AxonAnnotation.COMMAND_HANDLER_INTERCEPTOR, MessageType.COMMAND),
    SAGA(AxonAnnotation.SAGA_EVENT_HANDLER, MessageType.EVENT),
    ;

    val annotationName: String = annotation.annotationName

    companion object {
        fun exists(annotationName: String?): Boolean {
            if (annotationName == null) {
                return false
            }
            return values().any { type -> type.annotationName == annotationName }
        }
    }
}

