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
    val Publisher = IconLoader.getIcon("/icons/axon_publish.png", AxonIcons::class.java)
    val Handler = IconLoader.getIcon("/icons/axon_into.png", AxonIcons::class.java)
    val Saga = IconLoader.getIcon("/icons/saga_event.png", AxonIcons::class.java)
    val Aggregate = IconLoader.getIcon("/icons/aggregate_event.png", AxonIcons::class.java)
    val Both = IconLoader.getIcon("/icons/axon_eventsource.png", AxonIcons::class.java)
    val Bean = IconLoader.getIcon("/icons/plain_event.png", AxonIcons::class.java)
}
