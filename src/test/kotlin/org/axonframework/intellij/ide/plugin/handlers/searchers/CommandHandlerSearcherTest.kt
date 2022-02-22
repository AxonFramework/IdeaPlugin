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
import org.axonframework.intellij.ide.plugin.resolving.handlers.types.CommandHandler
import org.axonframework.intellij.ide.plugin.util.handlerResolver

class CommandHandlerSearcherTest : AbstractAxonFixtureTestCase() {

    fun `test can resolve constructor command handler in aggregate`() {
        addFile(
            "MyAggregate.kt", """
            data class MyCommand(@TargetAggregateIdentifier id: String)
            
            @AggregateRoot
            class MyAggregate {
                @CommandHandler
                constructor(command: MyCommand) {
                    
                }
            }
        """.trimIndent()
        )
        val handlers = project.handlerResolver().findAllHandlers()
            .filterIsInstance<CommandHandler>()
        Assertions.assertThat(handlers).anyMatch {
            it.payload == "test.MyCommand" && it.componentName == "test.MyAggregate" && it.element.name == "MyAggregate"
        }
    }

    fun `test can resolve normal command handler in aggregate`() {
        addFile(
            "MyAggregate.kt", """
            data class MyCommand(@TargetAggregateIdentifier id: String)
            
            @AggregateRoot
            class MyAggregate {
                @CommandHandler
                fun handle(command: MyCommand) {
                    
                }
            }
        """.trimIndent()
        )
        val handlers = project.handlerResolver().findAllHandlers()
            .filterIsInstance<CommandHandler>()
        Assertions.assertThat(handlers).anyMatch {
            it.payload == "test.MyCommand" && it.componentName == "test.MyAggregate" && it.element.name == "handle"
        }
    }

    fun `test can resolve command handler outside aggregate`() {
        addFile(
            "MyAggregate.kt", """
            data class MyCommand(id: String)
            
            class MyComponent {
                @CommandHandler
                fun handle(command: MyCommand) {
                    
                }
            }
        """.trimIndent()
        )
        val handlers = project.handlerResolver().findAllHandlers()
            .filterIsInstance<CommandHandler>()
        Assertions.assertThat(handlers).anyMatch {
            it.payload == "test.MyCommand" && it.componentName == "test.MyComponent" && it.element.name == "handle"
        }
    }
}
