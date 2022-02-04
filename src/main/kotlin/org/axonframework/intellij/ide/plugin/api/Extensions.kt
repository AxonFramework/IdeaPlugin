package org.axonframework.intellij.ide.plugin.api

import com.intellij.openapi.extensions.ExtensionPointName

val HANDLER_SEARCHER_EP = ExtensionPointName.create<HandlerSearcher>("org.axonframework.intellij.axonplugin.handlerSearcher")
