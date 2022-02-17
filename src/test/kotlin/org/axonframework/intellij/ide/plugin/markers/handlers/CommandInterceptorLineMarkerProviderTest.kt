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
import org.axonframework.intellij.ide.plugin.AxonIcons

class CommandInterceptorLineMarkerProviderTest : AbstractAxonFixtureTestCase() {
    fun `test shows marker without options on interceptor with no intercepted handlers`() {
        addFile(
            "MyAggregate.kt", """
            class MyCommand
            
            @AggregateRoot
            class MyAggregate {
            
                @CommandHandlerInterceptor<caret>
                fun intercept(command: MyCommand) {
                }
            }
        """.trimIndent(), open = true
        )
        assertThat(hasLineMarker(CommandInterceptorLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(CommandInterceptorLineMarkerProvider::class.java)).isEmpty()
    }

    fun `test shows intercepted command on interceptor in same class`() {
        addFile(
            "MyAggregate.kt", """
            class MyCommand
            
            @AggregateRoot
            class MyAggregate {
            
                @CommandHandlerInterceptor<caret>
                fun intercept(command: MyCommand) {
                }
                
                @CommandHandler
                fun handle(command: MyCommand) {
                }
            }
        """.trimIndent(), open = true
        )
        assertThat(hasLineMarker(CommandInterceptorLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(CommandInterceptorLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("MyCommand", "MyAggregate", AxonIcons.Handler)
        )
    }

    fun `test shows intercepted command when is interface of payload`() {
        addFile(
            "MyAggregate.kt", """
            interface MyCommandInterface
            class MyCommand : MyCommandInterface
            
            @AggregateRoot
            class MyAggregate {
            
                @CommandHandlerInterceptor<caret>
                fun intercept(commandInterface: MyCommandInterface) {
                }
                
                @CommandHandler
                fun handle(command: MyCommand) {
                }
            }
        """.trimIndent(), open = true
        )
        assertThat(hasLineMarker(CommandInterceptorLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(CommandInterceptorLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("MyCommand", "MyAggregate", AxonIcons.Handler)
        )
    }

    fun `test shows intercepted command on interceptor`() {
        addFile(
            "MyAggregate.kt", """
            interface MyCommandInterface
            class MyCommand : MyCommandInterface
            
            @AggregateRoot
            class MyAggregate {
            
                @CommandHandlerInterceptor<caret>
                fun intercept() {
                }
                
                @CommandHandler
                fun handle(command: MyCommand) {
                }
            }
        """.trimIndent(), open = true
        )
        assertThat(hasLineMarker(CommandInterceptorLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(CommandInterceptorLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("MyCommand", "MyAggregate", AxonIcons.Handler)
        )
    }

    fun `test shows intercepted entity command handler on parent aggregate command interceptor`() {
        addFile(
            "MyAggregate.kt", """
            class MyCommand 
            
            class MyEntity {
                
                @CommandHandler
                fun handle(command: MyCommand) {
                }
            }
            
            
            @AggregateRoot
            class MyAggregate {
                @AggregateMember
                private lateinit var entity: MyEntity
            
                @CommandHandlerInterceptor<caret>
                fun intercept() {
                }
            }
        """.trimIndent(), open = true
        )
        assertThat(hasLineMarker(CommandInterceptorLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(CommandInterceptorLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("MyCommand", "MyEntity", AxonIcons.Handler)
        )
    }
}
