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

package org.axonframework.intellij.ide.plugin.markers.handlers

import org.assertj.core.api.Assertions.assertThat
import org.axonframework.intellij.ide.plugin.AbstractAxonFixtureTestCase

class InterceptedCommandHandlerMethodLineMarkerProviderTest : AbstractAxonFixtureTestCase() {
    fun `test shows interceptor on intercepted command when is same payload`() {
        addFile(
            "MyAggregate.kt", """
            class MyCommand
            
            @AggregateRoot
            class MyAggregate {
            
                @CommandHandlerInterceptor
                fun intercept(command: MyCommand) {
                }
                
                @CommandHandler<caret>
                fun handle(command: MyCommand) {
                }
            }
        """.trimIndent(), open = true
        )
        val options = getLineMarkerOptions(InterceptedCommandHandlerMethodLineMarkerProvider::class.java)
        assertThat(options).anyMatch { it.text == "Command Interceptor intercept" && it.containerText == "MyAggregate" }
    }

    fun `test shows interceptor on intercepted command when is interface of payload`() {
        addFile(
            "MyAggregate.kt", """
            interface MyCommandInterface
            class MyCommand : MyCommandInterface
            
            @AggregateRoot
            class MyAggregate {
            
                @CommandHandlerInterceptor
                fun intercept(commandInterface: MyCommandInterface) {
                }
                
                @CommandHandler<caret>
                fun handle(command: MyCommand) {
                }
            }
        """.trimIndent(), open = true
        )
        val options = getLineMarkerOptions(InterceptedCommandHandlerMethodLineMarkerProvider::class.java)
        assertThat(options).anyMatch { it.text == "Command Interceptor intercept" && it.containerText == "MyAggregate" }
    }

    fun `test shows interceptor on intercepted command when no payload is defined`() {
        addFile(
            "MyAggregate.kt", """
            interface MyCommandInterface
            class MyCommand : MyCommandInterface
            
            @AggregateRoot
            class MyAggregate {
            
                @CommandHandlerInterceptor
                fun intercept() {
                }
                
                @CommandHandler<caret>
                fun handle(command: MyCommand) {
                }
            }
        """.trimIndent(), open = true
        )
        val options = getLineMarkerOptions(InterceptedCommandHandlerMethodLineMarkerProvider::class.java)
        assertThat(options).anyMatch { it.text == "Command Interceptor intercept" && it.containerText == "MyAggregate" }
    }

    fun `test shows interceptor on entity command handler when parent has a matching interceptor defined`() {
        addFile(
            "MyAggregate.kt", """
            class MyCommand 
            
            class MyEntity {
                
                @CommandHandler<caret>
                fun handle(command: MyCommand) {
                }
            }
            
            
            @AggregateRoot
            class MyAggregate {
                @AggregateMember
                private lateinit var entity: MyEntity
            
                @CommandHandlerInterceptor
                fun intercept() {
                }
            }
        """.trimIndent(), open = true
        )
        val options = getLineMarkerOptions(InterceptedCommandHandlerMethodLineMarkerProvider::class.java)
        assertThat(options).anyMatch { it.text == "Command Interceptor intercept" && it.containerText == "MyAggregate" }
    }

    fun `test shows no interceptor when command is in aggregate but interceptor in entity`() {
        addFile(
            "MyAggregate.kt", """
            class MyCommand
            
            class MyEntity {
                @CommandHandlerInterceptor
                fun intercept() {
                }
            }
            
            @AggregateRoot
            class MyAggregate {
                @AggregateMember
                private lateinit var entity: MyEntity
                
                @CommandHand<caret>ler
                fun handle(command: MyCommand) {
                }
            }
        """.trimIndent(), open = true
        )
        assertThat(areNoLineMarkers(InterceptedCommandHandlerMethodLineMarkerProvider::class.java)).isTrue
    }


    fun `test shows no interceptor when are no interceptors`() {
        addFile(
            "MyAggregate.kt", """
            interface MyCommandInterface
            class MyCommand : MyCommandInterface
            
            class MyEntity {
                @CommandHandlerInterceptor
                fun intercept() {
                }
            }
            
            @AggregateRoot
            class MyAggregate {
                @AggregateMember
                private lateinit var entity: MyEntity
                
                @CommandHandler<caret>
                fun handle(command: MyCommand) {
                }
            }
        """.trimIndent(), open = true
        )
        assertThat(areNoLineMarkers(InterceptedCommandHandlerMethodLineMarkerProvider::class.java)).isTrue
    }
}
