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

package org.axonframework.intellij.ide.plugin

import com.intellij.openapi.util.IconLoader

/**
 * Wrapper for the Icons provided in the plugin.
 *
 * Heavily WIP, new icons need to be made. Some are currently abused because of the lack of a better option.
 *
 * NOTE: They must be either an SVG or 12x12 pixels. For details, SVG is probably better.
 */
object AxonIcons {
    val Publisher = IconLoader.getIcon("/icons/publisher.svg", AxonIcons::class.java)
    val Handler = IconLoader.getIcon("/icons/handler.svg", AxonIcons::class.java)
    val Saga = IconLoader.getIcon("/icons/saga.svg", AxonIcons::class.java)
    val Model = IconLoader.getIcon("/icons/model.svg", AxonIcons::class.java)
    val Interceptor = IconLoader.getIcon("/icons/interceptor.svg", AxonIcons::class.java)
    val DeadlinePublisher = IconLoader.getIcon("/icons/deadline_publish.svg", AxonIcons::class.java)
    val DeadlineHandler = IconLoader.getIcon("/icons/deadline_handler.svg", AxonIcons::class.java)
}
