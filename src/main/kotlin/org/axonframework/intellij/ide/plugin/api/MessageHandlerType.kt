package org.axonframework.intellij.ide.plugin.api


/**
 * Handler definitions with their Annotation and MessageType.
 *
 * Generic MessageHandlers are not supported, since they cannot be mapped to a type.
 * All custom annotations annotated with one of the annotations in this enum are supported.
 */
enum class MessageHandlerType(
        annotation: AxonAnnotation,
        val messageType: MessageType,
) {
    COMMAND(AxonAnnotation.COMMAND_HANDLER, MessageType.COMMAND),
    EVENT(AxonAnnotation.EVENT_HANDLER, MessageType.EVENT),
    EVENT_SOURCING(AxonAnnotation.EVENT_SOURCING_HANDLER, MessageType.EVENT),
    QUERY(AxonAnnotation.QUERY_HANDLER, MessageType.QUERY),
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

