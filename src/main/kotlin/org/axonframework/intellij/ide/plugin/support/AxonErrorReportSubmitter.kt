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

package org.axonframework.intellij.ide.plugin.support

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import com.intellij.util.Consumer
import io.sentry.Sentry
import java.awt.Component

class AxonErrorReportSubmitter : ErrorReportSubmitter() {
    init {
        Sentry.init { options ->
            options.dsn = "https://examplePublicKey@o0.ingest.sentry.io/0"
        }
    }

    override fun getReportActionText(): String {
        return "Report to AxonIQ"
    }

    override fun submit(
        events: Array<out IdeaLoggingEvent>,
        additionalInfo: String?,
        parentComponent: Component,
        consumer: Consumer<in SubmittedReportInfo>
    ): Boolean {
        val mgr = DataManager.getInstance();
        val context = mgr.getDataContext(parentComponent);
        val project = CommonDataKeys.PROJECT.getData(context);

        // make use of IntelliJ's background tasks
        object : Task.Backgroundable(project, "Sending error report") {
            override fun run(indicator: ProgressIndicator) {
                try {
                    events.forEach { event ->
                        Sentry.captureException(event.throwable, additionalInfo)
                    }
                    consumer.consume(SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE))
                    showThankYou()
                } catch (e: Exception) {
                    consumer.consume(SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.FAILED))
                }
            }

        }.queue()
        return true;
    }

    private fun showThankYou() {
        ApplicationManager.getApplication().invokeLater() {
            Messages.showInfoMessage(
                "Thank you for reporting the bug to AxonIQ. We will get working on it as soon as possible!",
                "Thank You!"
            )
        }
    }
}
