package org.axonframework.intellij.ide.plugin

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import org.axonframework.intellij.ide.plugin.search.HandlerSearcher

private val logger = logger<AxonProjectManagementListener>()

class AxonProjectManagementListener : StartupActivity {
    override fun runActivity(project: Project) {
        logger.warn("Starting scan")
        val handlers = HandlerSearcher.findAllMessageHandlers(project)
        logger.warn("Done ${handlers.size}")
    }
}
