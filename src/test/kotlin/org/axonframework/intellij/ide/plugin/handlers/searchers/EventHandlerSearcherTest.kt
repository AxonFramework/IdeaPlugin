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

package org.axonframework.intellij.ide.plugin.handlers.searchers

import org.assertj.core.api.Assertions
import org.axonframework.intellij.ide.plugin.AbstractAxonFixtureTestCase
import org.axonframework.intellij.ide.plugin.handlers.types.EventHandler
import org.axonframework.intellij.ide.plugin.util.handlerResolver

class EventHandlerSearcherTest : AbstractAxonFixtureTestCase() {
    fun `test can find event handler in class without processing group`() {
        addFile("MyComponent.kt", """
            data class MyEvent(id: String)
            
            class MyComponent {
                @EventHandler
                fun handleMySpecialEvent(command: MyEvent) {
                    
                }
            }
        """.trimIndent())
        val handlers = project.handlerResolver().findAllHandlers()
                .filterIsInstance<EventHandler>()
        Assertions.assertThat(handlers).anyMatch {
            it.payload == "test.MyEvent" && it.processingGroup == "test" && it.element.name == "handleMySpecialEvent"
        }
    }


    fun `test can find event handler in class with processing group`() {
        addFile("MyComponent.kt", """
            data class MyEvent(id: String)
            
            @ProcessingGroup("some-processing-group")
            class MyComponent {
                @EventHandler
                fun handleMySpecialEvent(command: MyEvent) {
                    
                }
            }
        """.trimIndent())
        val handlers = project.handlerResolver().findAllHandlers()
                .filterIsInstance<EventHandler>()
        Assertions.assertThat(handlers).anyMatch {
            it.payload == "test.MyEvent" && it.processingGroup == "some-processing-group" && it.element.name == "handleMySpecialEvent"
        }
    }
}
