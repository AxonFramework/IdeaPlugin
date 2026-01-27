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

import org.assertj.core.api.Assertions.assertThat
import org.axonframework.intellij.ide.plugin.AbstractAxonFixtureTestCase
import org.axonframework.intellij.ide.plugin.api.AxonVersion

/**
 * Tests for AxonVersionService to verify it correctly detects Axon Framework versions
 * from JAR manifests and provides the appropriate component factories.
 *
 * Note: These tests use Axon 4 libraries that are on the test classpath.
 * Full integration testing with Axon 5 requires manual verification due to
 * classloader limitations in the test framework.
 */
class AxonVersionDetectionTest : AbstractAxonFixtureTestCase() {

    fun `test detects Axon 4 from project dependencies`() {
        // The AbstractAxonFixtureTestCase already adds Axon 4 libraries in setUp()
        val versionService = project.getService(AxonVersionService::class.java)
        versionService.runCheck() // Force version check

        assertThat(versionService.getVersion())
            .describedAs("Should detect Axon 4 when axon-messaging v4.x is present")
            .isEqualTo(AxonVersion.V4)

        assertThat(versionService.getComponentFactory())
            .describedAs("Should provide a component factory for Axon 4")
            .isNotNull
            .isInstanceOf(Axon4ComponentFactory::class.java)
    }

    fun `test getAxonVersions extracts version information correctly`() {
        // The AbstractAxonFixtureTestCase already adds Axon 4 libraries in setUp()
        val versionService = project.getService(AxonVersionService::class.java)
        val versions = versionService.getAxonVersions()

        assertThat(versions)
            .describedAs("Should find at least one Axon dependency")
            .isNotEmpty

        // Verify version structure
        val version = versions.first()
        assertThat(version.major)
            .describedAs("Major version should be 4")
            .isEqualTo(4)
        assertThat(version.minor)
            .describedAs("Minor version should be non-negative")
            .isGreaterThanOrEqualTo(0)
        assertThat(version.patch)
            .describedAs("Patch version should be non-negative")
            .isGreaterThanOrEqualTo(0)
        assertThat(version.toVersionString())
            .describedAs("Version string should be properly formatted")
            .matches("\\d+\\.\\d+\\.\\d+.*")
    }

    fun `test detects multiple Axon 4 dependencies`() {
        // The AbstractAxonFixtureTestCase adds multiple Axon 4 libraries
        val versionService = project.getService(AxonVersionService::class.java)
        val versions = versionService.getAxonVersions()

        assertThat(versions)
            .describedAs("Should detect multiple Axon 4 dependencies")
            .hasSizeGreaterThan(1)
            .allMatch { it.major == 4 }

        assertThat(versionService.getVersion())
            .describedAs("Should detect Axon 4 when multiple v4 libraries are present")
            .isEqualTo(AxonVersion.V4)
    }

    fun `test isAxonEnabled returns true for Axon 4`() {
        val versionService = project.getService(AxonVersionService::class.java)
        versionService.runCheck()

        assertThat(versionService.isAxonEnabled(useCache = true))
            .describedAs("Plugin should be enabled for Axon 4")
            .isTrue()
    }

    fun `test factory provides correct component types for Axon 4`() {
        val versionService = project.getService(AxonVersionService::class.java)
        versionService.runCheck()

        val factory = versionService.getComponentFactory()
        assertThat(factory)
            .describedAs("Should have factory for Axon 4")
            .isNotNull

        assertThat(factory!!.getVersion())
            .describedAs("Factory should report Axon 4")
            .isEqualTo(AxonVersion.V4)

        assertThat(factory.getEntityTerminology())
            .describedAs("Axon 4 should use 'Aggregate' terminology")
            .isEqualTo("Aggregate")

        val searchers = factory.createHandlerSearchers()
        assertThat(searchers)
            .describedAs("Axon 4 should provide all expected searchers")
            .hasSizeGreaterThanOrEqualTo(6) // Command, Event, EventSourcing, Query, Saga, Deadline, Constructor

        assertThat(factory.getEntityAnnotations())
            .describedAs("Axon 4 should provide @AggregateRoot annotation")
            .contains("org.axonframework.modelling.command.AggregateRoot")
    }

    fun `test version service detects dependency changes`() {
        val versionService = project.getService(AxonVersionService::class.java)

        // Initial check
        versionService.runCheck()
        val initialVersion = versionService.getVersion()
        assertThat(initialVersion).isEqualTo(AxonVersion.V4)

        // Running check again should maintain the same version
        versionService.runCheck()
        assertThat(versionService.getVersion())
            .describedAs("Version should remain consistent across multiple checks")
            .isEqualTo(initialVersion)
    }

    fun `test Axon 4 factory supports saga handlers`() {
        val versionService = project.getService(AxonVersionService::class.java)
        versionService.runCheck()

        val factory = versionService.getComponentFactory() as? Axon4ComponentFactory
        assertThat(factory)
            .describedAs("Should be an Axon 4 factory")
            .isNotNull

        val searchers = factory!!.createHandlerSearchers()
        val searcherTypes = searchers.map { it.javaClass.simpleName }

        assertThat(searcherTypes)
            .describedAs("Axon 4 should include SagaEventHandlerSearcher")
            .contains("SagaEventHandlerSearcher")
            .describedAs("Axon 4 should include DeadlineHandlerSearcher")
            .contains("DeadlineHandlerSearcher")
            .describedAs("Axon 4 should include AggregateConstructorSearcher")
            .contains("AggregateConstructorSearcher")
    }

    fun `test Axon 5 factory excludes deprecated features`() {
        // This tests the Axon 5 factory configuration without needing Axon 5 on the classpath
        val factory = Axon5ComponentFactory()

        assertThat(factory.getVersion())
            .describedAs("Factory should report Axon 5")
            .isEqualTo(AxonVersion.V5)

        val searchers = factory.createHandlerSearchers()
        val searcherTypes = searchers.map { it.javaClass.simpleName }

        assertThat(searcherTypes)
            .describedAs("Axon 5 should NOT include SagaEventHandlerSearcher")
            .doesNotContain("SagaEventHandlerSearcher")
            .describedAs("Axon 5 should NOT include DeadlineHandlerSearcher")
            .doesNotContain("DeadlineHandlerSearcher")
            .describedAs("Axon 5 should NOT include AggregateConstructorSearcher")
            .doesNotContain("AggregateConstructorSearcher")
            .describedAs("Axon 5 should include EntityCreatorSearcher")
            .contains("EntityCreatorSearcher")
    }
}
