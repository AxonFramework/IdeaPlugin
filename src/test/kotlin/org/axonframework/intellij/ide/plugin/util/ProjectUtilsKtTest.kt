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

package org.axonframework.intellij.ide.plugin.util

import org.assertj.core.api.Assertions.assertThat
import org.axonframework.intellij.ide.plugin.AbstractAxonFixtureTestCase


internal class ProjectUtilsKtTest : AbstractAxonFixtureTestCase() {
    fun testGetAxonDependencies() {
        val axonVersions = project.versionService().getAxonVersions()
        assertThat(axonVersions).anySatisfy { it.dependency.moduleName == "axon-eventsourcing" && it.major == 4 }
        assertThat(axonVersions).anySatisfy { it.dependency.moduleName == "axon-modelling" && it.major == 4 }
        assertThat(axonVersions).anySatisfy { it.dependency.moduleName == "axon-messaging" && it.major == 4 }
        assertThat(axonVersions).anySatisfy { it.dependency.moduleName == "axon-configuration" && it.major == 4 }

        assertThat(project.versionService().isAxonEnabled()).isTrue
        assertThat(project.versionService().isAxonEnabled(true)).isTrue
    }
}
