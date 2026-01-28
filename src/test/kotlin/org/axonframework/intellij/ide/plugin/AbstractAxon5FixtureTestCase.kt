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

package org.axonframework.intellij.ide.plugin

import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.java.LanguageLevel
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.PsiTestUtil

/**
 * Base test class for Axon Framework 5 tests. Adds Axon 5 libraries and provides
 * test utilities with v5-specific imports.
 */
abstract class AbstractAxon5FixtureTestCase : AbstractBaseAxonFixtureTestCase() {
    override fun setUp() {
        super.setUp()

        // Download and add Axon 5 JARs
        val axon5Version = System.getProperty("axon5Version") ?: "5.0.0"
        val librariesDir = java.io.File("src/test/resources/libraries")
        librariesDir.mkdirs()

        addAxon5Library("axon-messaging", axon5Version, librariesDir)
        addAxon5Library("axon-modelling", axon5Version, librariesDir)
        addAxon5Library("axon-eventsourcing", axon5Version, librariesDir)

        // Force version detection to run after adding libraries
        // This ensures the plugin recognizes this as an Axon 5 project
        project.getService(org.axonframework.intellij.ide.plugin.usage.AxonVersionService::class.java).runCheck()

        /* Mock the Instant class, or some tests won't run */
        addFile(
            "Instant.java", """
            public class Instant {
                public static Instant now() {
                    return null;
                }
            }
        """.trimIndent(), "java.time"
        )
    }

    private fun addAxon5Library(artifactName: String, version: String, librariesDir: java.io.File) {
        val jarFile = java.io.File(librariesDir, "$artifactName-$version.jar")

        // Download from Maven Central if not exists
        if (!jarFile.exists()) {
            val url = "https://repo1.maven.org/maven2/org/axonframework/$artifactName/$version/$artifactName-$version.jar"
            println("Downloading $artifactName-$version.jar from Maven Central...")
            try {
                java.net.URL(url).openStream().use { input ->
                    java.io.FileOutputStream(jarFile).use { output ->
                        input.copyTo(output)
                    }
                }
                println("Downloaded $artifactName-$version.jar")
            } catch (e: Exception) {
                println("Failed to download $artifactName-$version.jar: ${e.message}")
                return
            }
        }

        // Add as project library (production scope) so version detection works
        PsiTestUtil.addProjectLibrary(module, "$artifactName-$version", listOf(jarFile.absolutePath))
    }

    override fun getTestDataPath(): String {
        return "src/test/testData/"
    }

    override fun getProjectDescriptor(): LightProjectDescriptor {
        return object : ProjectDescriptor(LanguageLevel.JDK_11) {
            override fun getSdk(): Sdk {
                return IdeaTestUtil.createMockJdk("java 11", "jdk/mockJDK-11")
            }
        }
    }

    /**
     * Automatically added to source files in testcases, for convenience
     * These are Axon 5 imports
     */
    private val autoImports = listOf(
        // Axon 5 handler annotations (different package)
        "org.axonframework.messaging.commandhandling.annotation.CommandHandler",
        "org.axonframework.messaging.eventhandling.annotation.EventHandler",
        "org.axonframework.messaging.queryhandling.annotation.QueryHandler",
        "org.axonframework.eventsourcing.annotation.EventSourcingHandler",

        // Axon 5 event sourcing annotations
        "org.axonframework.eventsourcing.annotation.EventSourcedEntity",
        "org.axonframework.eventsourcing.annotation.reflection.EntityCreator",

        // Message annotations (new in v5)
        "org.axonframework.commandhandling.Command",
        "org.axonframework.eventhandling.Event",
        "org.axonframework.queryhandling.Query",

        // Common
        "java.time.Instant",
    )

    /**
     * Adds a file to the project with given name/path and content.
     * Auto-imports Axon 5 annotations.
     */
    fun addFile(name: String, text: String, pckg: String = "test", open: Boolean = false): VirtualFile {
        val newLineChar = if (name.endsWith(".java")) ";" else ""
        var content = ""
        content += "package $pckg$newLineChar\n\n"
        autoImports.forEach { content += "import $it$newLineChar\n" }
        content += ("\n" + text)
        // Save <caret> position
        val caretPosition = content.indexOf("<caret>")
        content = content.replace("<caret>", "")
        // Add file and optionally open it
        val file = myFixture.addFileToProject(name, content).virtualFile
        if (open) {
            myFixture.openFileInEditor(file)
            if (caretPosition != -1) {
                myFixture.editor.caretModel.moveToOffset(caretPosition)
            }
        }
        return file
    }
}
