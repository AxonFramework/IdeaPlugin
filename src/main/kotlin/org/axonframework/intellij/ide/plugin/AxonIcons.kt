package org.axonframework.intellij.ide.plugin

import com.intellij.openapi.util.IconLoader

/**
 * Wrapper for the Icons provided in the plugin.
 *
 * Heavily WIP, new icons need to be made. Some are currently abused because of the lack of a better option.
 *
 * NOTE: They must be either an SVG or 12x12 pixels. For details, SVG is probably better.
 */
object AxonIcons {
    val Publisher = IconLoader.getIcon("/icons/publisher.svg", AxonIcons::class.java)
    val Handler = IconLoader.getIcon("/icons/handler.svg", AxonIcons::class.java)
    val Saga = IconLoader.getIcon("/icons/saga.svg", AxonIcons::class.java)
    val Model = IconLoader.getIcon("/icons/model.svg", AxonIcons::class.java)
    val Interceptor = IconLoader.getIcon("/icons/interceptor.svg", AxonIcons::class.java)
}
