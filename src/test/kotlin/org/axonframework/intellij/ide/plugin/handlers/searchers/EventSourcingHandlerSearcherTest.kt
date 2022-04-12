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
import org.axonframework.intellij.ide.plugin.resolving.handlers.types.EventSourcingHandler
import org.axonframework.intellij.ide.plugin.util.handlerResolver

class EventSourcingHandlerSearcherTest : AbstractAxonFixtureTestCase() {
    fun `test can find event sourcing handler in class`() {
        addFile(
            "MyComponent.kt", """
            data class MyEvent(id: String)
            
            @AggregateRoot
            class MyAggregate {
                @EventSourcingHandler
                fun handleMySpecialEvent(command: MyEvent) {
                    
                }
            }
        """.trimIndent()
        )
        val handlers = project.handlerResolver().findAllHandlers()
            .filterIsInstance<EventSourcingHandler>()
        Assertions.assertThat(handlers).anyMatch {
            it.payload == "test.MyEvent" && it.entity == "test.MyAggregate" && it.element.name == "handleMySpecialEvent"
        }
    }
}
