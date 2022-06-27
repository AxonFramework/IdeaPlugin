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

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.OrderEnumerator

const val moduleNamePart = "^.*"
const val versionPart = "4\\.\\d+\\.\\d"

val versionRegex = Regex("^$versionPart$")
val capturingRegex = Regex("($moduleNamePart)-($versionPart)(?:-.*)*")

fun Project.isAxon4Project() = getAxonVersions().values.any {
    return it.matches(versionRegex)
}

fun Project.getAxonVersions() = OrderEnumerator.orderEntries(this)
    .librariesOnly()
    .productionOnly()
    .satisfying { it.presentableName.matches(Regex(".*(org\\.axonframework)+.*")) }
    .classes()
    .roots.associate {
        val match = capturingRegex.find(it.name)!!
        val (moduleName, version) = match.destructured
        moduleName to version
    }
