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

import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij.platform") version "2.6.0"
    // Gradle Changelog Plugin
    id("org.jetbrains.changelog") version "2.2.1"
    // Gradle Qodana Plugin
    id("org.jetbrains.qodana") version "0.1.13"
}

group = properties("pluginGroup")
version = properties("pluginVersion")


intellijPlatform {
    pluginConfiguration {
        name = properties("pluginName")
        version = properties("pluginVersion")
        ideaVersion {
            sinceBuild = properties("pluginSinceBuild")
            untilBuild = provider { null }
        }


        description = projectDir.resolve("README.md").readText().lines().run {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"
            if (!containsAll(listOf(start, end))) {
                throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
            }
            subList(indexOf(start) + 1, indexOf(end))
        }.joinToString("\n").run { markdownToHTML(this) }

        changeNotes = provider {
            changelog.renderItem(
                changelog
                    .get(properties("pluginVersion"))
                    .withHeader(false)
                    .withEmptySections(false),
                Changelog.OutputType.HTML
            )
        }
    }

    publishing {
        token = System.getenv("PUBLISH_TOKEN")
        channels = listOf("default")
    }

    signing {
        certificateChain = System.getenv("CERTIFICATE_CHAIN")
        privateKey = System.getenv("PRIVATE_KEY")
        password = System.getenv("PRIVATE_KEY_PASSWORD")
    }

    pluginVerification {
        ides {
            select {
                types = listOf(IntelliJPlatformType.IntellijIdeaCommunity)
                channels = listOf(ProductRelease.Channel.RELEASE)
                sinceBuild = properties("pluginSinceBuild")
                untilBuild = "252.*"
            }
        }

        freeArgs = listOf(
            // Mute some inspections that should be ignored (as we already uploaded and the id can't be changed)
            "-mute", "TemplateWordInPluginId,ForbiddenPluginIdPrefix"
        )
    }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(properties("pluginVersion"))
    groups.set(emptyList())
}

// Configure Gradle Qodana Plugin - read more: https://github.com/JetBrains/gradle-qodana-plugin
qodana {
    cachePath.set(projectDir.resolve(".qodana").canonicalPath)
    reportPath.set(projectDir.resolve("build/reports/inspections").canonicalPath)
    saveReport.set(true)
    showReport.set(System.getenv("QODANA_SHOW_REPORT")?.toBoolean() ?: false)
}

tasks {
    // Set the JVM compatibility versions
    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = it
        }
    }

    runIde {
        jvmArgs("-Xmx2G")
    }

    buildSearchableOptions {
        // Disable for hot-reload to work
        enabled = false
    }

    test {
        // Pass Axon versions to tests as system properties
        systemProperty("axonVersion", properties("axonVersion"))
        systemProperty("axon5Version", properties("axon5Version"))
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity(properties("platformVersion"))
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")
        pluginVerifier()
        zipSigner()
        instrumentationTools()

        testFramework(TestFrameworkType.Plugin.Java)
    }

    implementation("io.sentry:sentry:6.32.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.assertj:assertj-core:3.27.0")

    // NOTE: No Axon dependencies needed at compile time!
    // Tests dynamically download JARs from Maven Central and add via PsiTestUtil.
    // See AbstractAxonFixtureTestCase and AbstractAxon5FixtureTestCase.
    testImplementation("org.axonframework:axon-modelling:${properties("axonVersion")}")
    testImplementation("org.axonframework:axon-messaging:${properties("axonVersion")}")
    testImplementation("org.axonframework:axon-eventsourcing:${properties("axonVersion")}")
    testImplementation("org.axonframework:axon-configuration:${properties("axonVersion")}")
}

// Configure project's dependencies
repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}
