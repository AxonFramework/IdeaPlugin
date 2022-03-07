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

package org.axonframework.intellij.ide.plugin.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import io.sentry.Sentry
import org.axonframework.intellij.ide.plugin.settings.AxonPluginSettings

fun <T> Project.measure(subject: String, task: String, block: () -> T): T {
    val settings = ApplicationManager.getApplication().getService(AxonPluginSettings::class.java)
    if (!settings.isAllowedToCollectMetrics()) {
        // Don't report to Sentry if user has not given permission
        return block.invoke()
    }
    val transaction = Sentry.getSpan()?.startChild("$subject.$task", task)
        ?: Sentry.startTransaction("$subject.$task", task, true)
    val result = block.invoke()
    transaction.finish()
    return result
}
