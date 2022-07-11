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

import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import org.assertj.core.api.Assertions.assertThat


internal class ProjectUtilsKtTest : LightJavaCodeInsightFixtureTestCase() {
    fun testDependencyExtraction() {
        assertThat(extractVersion("axon-spring-boot-autoconfigure-4.5.10.jar")).isEqualTo("axon-spring-boot-autoconfigure" to "4.5.10")
        assertThat(extractVersion("axon-spring-boot-autoconfigure-4.6.0-SNAPSHOT.jar")).isEqualTo("axon-spring-boot-autoconfigure" to "4.6.0-SNAPSHOT")
        assertThat(extractVersion("axon-spring-boot-autoconfigure-4.6.0-20220622.093707-365.jar")).isEqualTo("axon-spring-boot-autoconfigure" to "4.6.0-20220622.093707-365")
    }
}
