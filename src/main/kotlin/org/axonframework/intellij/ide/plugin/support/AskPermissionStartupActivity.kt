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

import com.intellij.ide.util.RunOnceUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.ui.Messages
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.settings.AxonPluginSettings

/**
 * Action that shows a feedback window, so the user can report it to us!
 */
class AskPermissionStartupActivity : StartupActivity {
    private val settings = ApplicationManager.getApplication().getService(AxonPluginSettings::class.java)

    override fun runActivity(project: Project) {
        RunOnceUtil.runOnceForApp("AxonAskMetricsPermission3") {
            ApplicationManager.getApplication().invokeLater() {
                val result = Messages.showCheckboxOkCancelDialog(
                    "The Axon Framework plugin would like to collect non-identifiable performance metrics.\n This way we can ensure that IntelliJ runs smoothly with the plugin installed.",
                    "Axon Framework Metric Collection",
                    "I agree that the Axon Framework plugin sends performance metrics to Sentry",
                    true,
                    0,
                    0,
                    AxonIcons.Axon
                )
                settings.reportUserPermission(result == 1)
            }
        }
    }

}
