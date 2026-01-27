/*
 *  Copyright (c) 2022-2026. Axon Framework
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import com.intellij.openapi.vfs.VfsUtilCore
import org.axonframework.intellij.ide.plugin.api.AxonVersion
import org.axonframework.intellij.ide.plugin.api.VersionedComponentFactory
import java.util.Properties
import java.util.jar.JarFile

class AxonVersionService(val project: Project) {
    private var enabled = false
    private var messageShownOutdated = false
    private var messageShownExperimental = false
    private var detectedVersion: AxonVersion = AxonVersion.UNKNOWN
    private var cachedFactory: VersionedComponentFactory? = null

    private val versionRegex = Regex("(\\d+)\\.(\\d+)\\.(\\d+)(.*)")

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
            detectedVersion = AxonVersion.UNKNOWN
            cachedFactory = null
            return
        }

        val outdatedDeps = versions.outdated()
        val unsupportedDeps = versions.unsupported()
        if (outdatedDeps.isEmpty() && unsupportedDeps.isEmpty()) {
            enabled = true

            // Detect version (v4 or v5)
            val maxMajorVersion = versions.maxOfOrNull { it.major } ?: 4
            detectedVersion = when (maxMajorVersion) {
                4 -> AxonVersion.V4
                5 -> AxonVersion.V5
                else -> AxonVersion.UNKNOWN
            }

            // Create factory for detected version
            cachedFactory = createFactory(detectedVersion)

            if (messageShownOutdated) {
                showReEnabledMessageForOutdatedDeps()
            }
            if (messageShownExperimental) {
                showReEnabledMessageForExperimentalDeps()
            }
            return
        }

        enabled = false
        detectedVersion = AxonVersion.UNKNOWN
        cachedFactory = null

        if (!messageShownOutdated && outdatedDeps.isNotEmpty()) {
            showOutdatedMessage(outdatedDeps)
            messageShownOutdated = true
        }
        if (!messageShownExperimental && unsupportedDeps.isNotEmpty()) {
            showNotYetSupported(unsupportedDeps)
            messageShownExperimental = true
        }
    }

    private fun showOutdatedMessage(outdatedDeps: List<AxonDependencyVersion>) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("AxonNotificationGroup")
            .createNotification(
                "Your project has an Axon Framework version older than 4. The plugin has been disabled. The specific dependencies are: " + outdatedDeps.joinToString(
                    separator = ","
                ) { it.dependency.moduleName + "(${it.toVersionString()})" }, NotificationType.ERROR
            )
            .notify(project)
    }

    private fun showNotYetSupported(outdatedDeps: List<AxonDependencyVersion>) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("AxonNotificationGroup")
            .createNotification(
                "Your project has an Axon Framework version greater than 5, which is not yet supported. The specific dependencies are: " + outdatedDeps.joinToString(
                    separator = ","
                ) { it.dependency.moduleName + "(${it.toVersionString()})" }, NotificationType.ERROR
            )
            .notify(project)
    }

    private fun showReEnabledMessageForOutdatedDeps() {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("AxonNotificationGroup")
            .createNotification(
                "Your project no longer has any experimental Axon Framework dependencies. Plugin functionality has been re-enabled.",
                NotificationType.INFORMATION
            )
            .notify(project)
        messageShownOutdated = false
    }


    private fun showReEnabledMessageForExperimentalDeps() {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("AxonNotificationGroup")
            .createNotification(
                "Your project no longer has any outdated Axon Framework dependencies. Plugin functionality has been re-enabled.",
                NotificationType.INFORMATION
            )
            .notify(project)
        messageShownExperimental = false
    }

    fun isAxonEnabled(useCache: Boolean = false): Boolean {
        if (useCache) {
            return enabled
        }
        val versions = getAxonVersions()
        return versions.outdated().isEmpty() && versions.unsupported().isEmpty()
    }

    /**
     * Returns the detected Axon Framework version in the project.
     * @return The detected Axon version (V4, V5, or UNKNOWN)
     */
    fun getVersion(): AxonVersion {
        return detectedVersion
    }

    /**
     * Returns the component factory for the detected Axon version.
     * This factory creates version-specific components.
     * @return The component factory, or null if no valid version is detected
     */
    fun getComponentFactory(): VersionedComponentFactory? {
        return cachedFactory
    }

    private fun createFactory(version: AxonVersion): VersionedComponentFactory? {
        return when (version) {
            AxonVersion.V4 -> Axon4ComponentFactory()
            AxonVersion.V5 -> Axon5ComponentFactory()
            AxonVersion.UNKNOWN -> null
        }
    }

    private fun List<AxonDependencyVersion>.outdated() = filter { it.dependency.checkVersion && it.major < 4 }
    private fun List<AxonDependencyVersion>.unsupported() = filter { it.dependency.checkVersion && it.major > 5 }

    fun getAxonVersions(): List<AxonDependencyVersion> = OrderEnumerator.orderEntries(project)
        .librariesOnly()
        .productionOnly()
        .classes()
        .roots
        .filter { it.presentableName.contains("axon") }
        .flatMap { root ->
            val jarFile = VfsUtilCore.virtualToIoFile(root)
            if (jarFile.extension == "jar") {
                val jar = JarFile(jarFile)
                jar.entries()
                    .toList()
                    .filter { it.name.startsWith("META-INF/maven/") && it.name.endsWith("pom.properties") }
                    .mapNotNull { entry ->
                        jar.getInputStream(entry).use { input ->
                            // Process the input stream as needed
                            val properties = Properties().apply { load(input) }
                            extractVersion(properties)
                        }
                    }
            } else {
                emptyList()
            }
        }

    private fun extractVersion(properties: Properties): AxonDependencyVersion? {
        try {
            val groupId = properties.getProperty("groupId")
            val artifactId = properties.getProperty("artifactId")
            val version = properties.getProperty("version")
            if (groupId.isNullOrEmpty() || artifactId.isNullOrEmpty() || version.isNullOrEmpty()) {
                return null
            }
            val dependency = AxonDependency.entries.firstOrNull { it.groupId == groupId && it.artifactId == artifactId }
            if (dependency == null) {
                return null
            }
            val (majorVersion, minorVersion, patchVersion, remaining) = versionRegex.find(version)?.destructured ?: return null
            return AxonDependencyVersion(
                dependency,
                Integer.parseInt(majorVersion),
                Integer.parseInt(minorVersion),
                Integer.parseInt(patchVersion),
                remaining
            )
        } catch (e: Exception) {
            // Ignore
            return null
        }
    }

    data class AxonDependencyVersion(
        val dependency: AxonDependency,
        val major: Int,
        val minor: Int,
        val patch: Int,
        val remaining: String
    ) {
        fun toVersionString() = "$major.$minor.$patch$remaining"
    }
}
