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

import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.OrderEnumerator
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.UserFeedback
import io.sentry.protocol.Message

/**
 * Responsible for reporting feedback and exceptions
 */
class ReportingService {
    init {
        val pluginDescriptor = PluginManager.getInstance().findEnabledPlugin(PluginId.getId("io.axoniq.ide.intellij"))

        Sentry.init { options ->
            options.dsn = "https://9b77d81e7522478daeb1351e9e651222@o1158005.ingest.sentry.io/6240788"
            options.release = pluginDescriptor?.version
            options.isAttachServerName = false
            options.sampleRate = 1.0
            options.tracesSampleRate = 1.0
        }
    }

    fun reportException(project: Project, throwable: Throwable, userInput: String?) {
        project.addLibraryVersionsToExtras()
        Sentry.captureException(throwable)
        if (userInput != null) {
            Sentry.captureUserFeedback(UserFeedback(Sentry.getLastEventId(), null, null, userInput))
        }
    }

    fun reportFeedback(project: Project, feedback: String) {
        project.addLibraryVersionsToExtras()
        val sentryEvent = SentryEvent()
        sentryEvent.message = Message()
        sentryEvent.message!!.message = "Feedback: $feedback"
        Sentry.captureEvent(sentryEvent)
        Sentry.captureUserFeedback(UserFeedback(Sentry.getLastEventId(), null, null, feedback))
    }

    private fun Project.addLibraryVersionsToExtras() {
        OrderEnumerator.orderEntries(this)
            .librariesOnly()
            .productionOnly()
            .satisfying { it.presentableName.matches(Regex(".*(axon|spring)+.*")) }
            .classes()
            .roots
            .forEach {
                val name = it.name.replace(".jar", "")
                val version = name.split("-").last()
                val actualName = name.replace("-${version}", "")
                Sentry.setExtra(actualName, version)
            }

    }
}
