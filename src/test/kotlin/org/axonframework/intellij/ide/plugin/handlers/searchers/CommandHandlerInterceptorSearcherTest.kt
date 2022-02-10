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
import org.axonframework.intellij.ide.plugin.handlers.types.CommandHandlerInterceptor
import org.axonframework.intellij.ide.plugin.util.handlerResolver

class CommandHandlerInterceptorSearcherTest : AbstractAxonFixtureTestCase() {

    fun `test can resolve command handler interceptors in aggregate`() {
        addFile("MyAggregate.kt", """
            data class MyCommand(@TargetAggregateIdentifier id: String)
            
            @AggregateRoot
            class MyAggregate {
                @CommandHandlerInterceptor
                fun intercept(command: MyCommand) {
                    
                }
            }
        """.trimIndent())
        val handlers = project.handlerResolver().findAllHandlers()
                .filterIsInstance<CommandHandlerInterceptor>()
        Assertions.assertThat(handlers).anyMatch {
            it.payload == "test.MyCommand" && it.componentName == "test.MyAggregate" && it.element.name == "intercept"
        }
    }

    fun `test can not resolve command handler outside of aggregates`() {
        addFile("MyAggregate.kt", """
            data class MyCommand(@TargetAggregateIdentifier id: String)
            
            class MyNonAggregate {
                @CommandHandlerInterceptor
                fun intercept(command: MyCommand) {
                    
                }
            }
        """.trimIndent())
        val handlers = project.handlerResolver().findAllHandlers()
                .filterIsInstance<CommandHandlerInterceptor>()
        Assertions.assertThat(handlers).isEmpty()
    }
}
