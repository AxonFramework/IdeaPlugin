/*
 *  Copyright (c) (2010-2023). Axon Framework
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

    private val regex = Regex(".*(axon-.*)-(\\d+)\\.(\\d+)\\.(\\d+)(.*)\\.jar")


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

    private fun showDisabledMessage(outdatedDeps: List<AxonDependencyVersion>) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("AxonNotificationGroup")
            .createNotification(
                "Your project has an Axon Framework version older than 4. The plugin has been disabled. The specific dependencies are: " + outdatedDeps.joinToString(
                    separator = ","
                ) { it.dependency.moduleName + "(${it.toVersionString()})" }, NotificationType.ERROR
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
        return getAxonVersions().outdated().isEmpty()
    }

    private fun List<AxonDependencyVersion>.outdated() = filter { it.dependency.checkVersion && it.major < 4 }

    fun getAxonVersions() = OrderEnumerator.orderEntries(project)
        .librariesOnly()
        .productionOnly()
        .classes()
        .roots
        .filter { !it.presentableName.contains("inspector") }
        .filter { it.presentableName.matches(regex) }
        .mapNotNull {
            extractVersion(it.name)
        }

    private fun extractVersion(name: String): AxonDependencyVersion? {
        val match = regex.find(name)!!
        val (moduleName, majorVersion, minorVersion, patchVersion, remaining) = match.destructured
        val dependency = AxonDependency.values().firstOrNull { it.moduleName == moduleName } ?: return null
        return AxonDependencyVersion(dependency,
            Integer.parseInt(majorVersion),
            Integer.parseInt(minorVersion),
            Integer.parseInt(patchVersion), remaining)
    }

    data class AxonDependencyVersion(val dependency: AxonDependency, val major: Int, val minor: Int, val patch: Int, val remaining: String) {
        fun toVersionString() = "$major.$minor.$patch$remaining"
    }
}
