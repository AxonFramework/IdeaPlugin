package org.axonframework.intellij.ide.plugin.api

/**
 * The type of the message being handled. This is not an exhaustive list of the message types supported by
 * Axon Framework, but this is an exhaustive list of the message types supported by the IntelliJ plugin.
 */
enum class MessageType {
    COMMAND,
    EVENT,
    QUERY,
}
