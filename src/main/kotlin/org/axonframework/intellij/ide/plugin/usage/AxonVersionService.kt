/*
 *  Copyright (c) (2010-2022). Axon Framework
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

package org.axonframework.intellij.ide.plugin.usage

import com.intellij.ProjectTopics
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootEvent
import com.intellij.openapi.roots.ModuleRootListener
import com.intellij.openapi.roots.OrderEnumerator

class AxonVersionService(val project: Project) {
    private var enabled = false
    private var messageShown = false
    private val regex = Regex("(axon-.*)-(\\d+)\\.(\\d+)\\.(\\d+)(.*)\\.jar")


    init {
        // Listen to root changes (meaning library changes) and recheck
        project.messageBus.connect().subscribe(ProjectTopics.PROJECT_ROOTS, object : ModuleRootListener {
            override fun rootsChanged(event: ModuleRootEvent) {
                runCheck()
            }
        })
        runCheck()
    }

    fun runCheck() {
        val versions = getAxonVersions()
        if (versions.isEmpty()) {
            enabled = false
        }

        val outdatedDeps = versions.outdated()
        if (outdatedDeps.isEmpty()) {
            enabled = true
            if(messageShown) {
                showReEnabledMessage()
            }
            return
        }
        enabled = false
        if (messageShown) {
            // Was already shown before
            return
        }

        showDisabledMessage(outdatedDeps)
        messageShown = true
    }

    private fun showDisabledMessage(outdatedDeps: List<AxonDependency>) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("AxonNotificationGroup")
            .createNotification(
                "Your project has an Axon Framework version older than 4. The plugin has been disabled. The specific dependencies are: " + outdatedDeps.joinToString(
                    separator = ","
                ) { it.name + "(${it.toVersionString()})" }, NotificationType.ERROR
            )
            .notify(project)
    }

    private fun showReEnabledMessage() {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("AxonNotificationGroup")
            .createNotification(
                "Your project no longer has any outdated Axon Framework dependencies. Plugin functionality has been re-enabled.",
                NotificationType.INFORMATION
            )
            .notify(project)
        messageShown = false
    }

    fun isAxonEnabled(useCache: Boolean = false): Boolean {
        if(useCache) {
            return enabled
        }
        val axonVersions = getAxonVersions()
        return axonVersions.isNotEmpty() && axonVersions.all {
            it.major >= 4
        }
    }

    private fun List<AxonDependency>.outdated() = filter { it.major < 4 }

    fun getAxonVersions() = OrderEnumerator.orderEntries(project)
        .librariesOnly()
        .productionOnly()
        .classes()
        .roots
        .filter { it.presentableName.matches(regex) }
        .map {
            extractVersion(it.name)
        }

    private fun extractVersion(name: String): AxonDependency {
        val match = regex.find(name)!!
        val (moduleName, majorVersion, minorVersion, patchVersion, remaining) = match.destructured
        return AxonDependency(moduleName,
            Integer.parseInt(majorVersion),
            Integer.parseInt(minorVersion),
            Integer.parseInt(patchVersion), remaining)
    }

    data class AxonDependency(val name: String, val major: Int, val minor: Int, val patch: Int, val remaining: String) {
        fun toVersionString() = "$major.$minor.$patch$remaining"
    }
}
