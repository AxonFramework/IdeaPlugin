/*
 *  Copyright (c) 2022. Axon Framework
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.axonframework.intellij.ide.plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.support.ReportingService

/**
 * Action that shows a feedback window, so the user can report it to us!
 */
class ReportFeedbackAction : AnAction(AxonIcons.Axon) {
    private val service = ApplicationManager.getApplication().getService(ReportingService::class.java)

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }

    override fun actionPerformed(e: AnActionEvent) {
        ApplicationManager.getApplication().invokeLater() {
            val feedback = Messages.showInputDialog(
                e.project!!,
                "Thank you for providing feedback about the Axon Framework plugin to AxonIQ, what is your feedback?",
                "Axon Framework Plugin Feedback",
                AxonIcons.Axon
            )
            if (feedback.isNullOrEmpty()) {
                return@invokeLater
            }
            service.reportFeedback(e.project!!, feedback)
        }
    }
}
