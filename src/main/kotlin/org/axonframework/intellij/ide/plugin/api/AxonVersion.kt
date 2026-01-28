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

package org.axonframework.intellij.ide.plugin.api

/**
 * Represents the major version of Axon Framework detected in the project.
 */
enum class AxonVersion {
    /**
     * Axon Framework 4.x - Current supported version
     */
    V4,

    /**
     * Axon Framework 5.x - New version with breaking changes
     */
    V5,

    /**
     * Unknown or unsupported version (older than 4.0 or no Axon dependency found)
     */
    UNKNOWN
}
