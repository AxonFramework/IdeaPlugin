package org.axonframework.intellij.ide.plugin.actions

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Action that can be invoked using the "Open Axon Reference Guide" in IntelliJ.
 */
class AxonReferenceGuideAction : AnAction() {
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }

    override fun actionPerformed(e: AnActionEvent) {
        BrowserUtil.browse("https://docs.axoniq.io/reference-guide/")
    }
}
