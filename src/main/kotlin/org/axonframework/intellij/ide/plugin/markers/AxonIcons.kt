package org.axonframework.intellij.ide.plugin.markers

import com.intellij.openapi.util.IconLoader

object AxonIcons {
    val Publisher = IconLoader.getIcon("/icons/axon_publish.png", AxonIcons::class.java)
    val Handler = IconLoader.getIcon("/icons/axon_into.png", AxonIcons::class.java)
    val Saga = IconLoader.getIcon("/icons/saga_event.png", AxonIcons::class.java)
    val Aggregate = IconLoader.getIcon("/icons/aggregate_event.png", AxonIcons::class.java)
    val Both = IconLoader.getIcon("/icons/axon_eventsource.png", AxonIcons::class.java)
}
