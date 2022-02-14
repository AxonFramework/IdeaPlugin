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

package org.axonframework.intellij.ide.plugin.deadline

import org.assertj.core.api.Assertions
import org.axonframework.intellij.ide.plugin.AbstractAxonFixtureTestCase
import org.axonframework.intellij.ide.plugin.handlers.types.DeadlineHandler
import org.axonframework.intellij.ide.plugin.util.handlerResolver

class DeadlineHandlerSearcherTest : AbstractAxonFixtureTestCase() {
    fun `test can find deadline handler without name and will base it on payload`() {
        addFile("MyAggregate.kt", """
            class MyDeadlinePayload
            
            @AggregateRoot
            class MyAggregate {
                @DeadlineHandler
                fun handle(deadline: MyDeadlinePayload) {
                    
                }
            }
        """.trimIndent())
        val handlers = project.handlerResolver().findAllHandlers()
                .filterIsInstance<DeadlineHandler>()
        Assertions.assertThat(handlers).anyMatch {
            it.payload == "test.MyDeadlinePayload" && it.deadlineName == "test.MyDeadlinePayload" && it.element.name == "handle"
        }
    }

    fun `test can find deadline handler with name`() {
        addFile("MyAggregate.kt", """
            class MyDeadlinePayload
            
            @AggregateRoot
            class MyAggregate {
                @DeadlineHandler(deadlineName = "MY_AWESOME_DEADLINE")
                fun handle(deadline: MyDeadlinePayload) {
                    
                }
            }
        """.trimIndent())
        val handlers = project.handlerResolver().findAllHandlers()
                .filterIsInstance<DeadlineHandler>()
        Assertions.assertThat(handlers).anyMatch {
            it.payload == "test.MyDeadlinePayload" && it.deadlineName == "MY_AWESOME_DEADLINE" && it.element.name == "handle"
        }
    }

    fun `test can find deadline handler with name without payload`() {
        addFile("MyAggregate.kt", """
            class MyDeadlinePayload
            
            @AggregateRoot
            class MyAggregate {
                @DeadlineHandler(deadlineName = "MY_AWESOME_DEADLINE")
                fun handle() {
                    
                }
            }
        """.trimIndent())
        val handlers = project.handlerResolver().findAllHandlers()
                .filterIsInstance<DeadlineHandler>()
        Assertions.assertThat(handlers).anyMatch {
            it.payload == "java.lang.Object" && it.deadlineName == "MY_AWESOME_DEADLINE" && it.element.name == "handle"
        }
    }

    fun `test can find deadline handler with name based on string ref`() {
        addFile("MyAggregate.kt", """
            class MyDeadlinePayload
            
            const val deadlineName = "MY_AWESOME_DEADLINE"
            
            @AggregateRoot
            class MyAggregate {
                @DeadlineHandler(deadlineName = deadlineName)
                fun handle(deadline: MyDeadlinePayload) {
                    
                }
            }
        """.trimIndent())
        val handlers = project.handlerResolver().findAllHandlers()
                .filterIsInstance<DeadlineHandler>()
        Assertions.assertThat(handlers).anyMatch {
            it.payload == "test.MyDeadlinePayload" && it.deadlineName == "MY_AWESOME_DEADLINE" && it.element.name == "handle"
        }
    }
}
