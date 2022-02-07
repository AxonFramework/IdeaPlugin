package org.axonframework.intellij.ide.plugin.actions

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.intellij.json.JsonLanguage
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.testFramework.LightVirtualFile
import org.axonframework.intellij.ide.plugin.util.PerformanceRegistry

/**
 * Action that can be invoked using the "Dump Axon Plugin Performance" in IntelliJ.
 * Useful for debugging bug reports.
 *
 * Does not contain sensitive data
 */
class DumpAxonPerformanceAction : AnAction() {
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val dump = ObjectMapper().findAndRegisterModules().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(PerformanceRegistry.getMeasurements())
        FileEditorManager.getInstance(project).openEditor(
                OpenFileDescriptor(project, LightVirtualFile("axon-performance.json", JsonLanguage.INSTANCE, dump)),
                true
        )
    }
}
