package org.axonframework.intellij.ide.plugin.window

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class MessageOverviewToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val window = MessageOverviewToolWindow(toolWindow, project)
        val factory = ContentFactory.SERVICE.getInstance()
        val content = factory.createContent(window.getContent(), "", false)
        toolWindow.contentManager.addContent(content)
    }
}
