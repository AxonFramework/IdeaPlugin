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

class CommandHandlerMethodLineMarkerProviderTest : AbstractAxonFixtureTestCase() {
    override fun setUp() {
        super.setUp()
        addFile(
            "MyCommand.kt", """
            data class MyCommand(val id: String)
        """.trimIndent()
        )
    }


    fun `test marks command handler on function even when there are no producers in kotlin`() {
        addFile(
            "MyAggregate.kt", """
            @AggregateRoot
            class MyAggregate {
               @CommandHandler<caret>
               fun handle(command: MyCommand) {
               }
            }
        """.trimIndent(), open = true
        )

        assertThat(hasLineMarker(CommandHandlerMethodLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(CommandHandlerMethodLineMarkerProvider::class.java)).isEmpty()
    }

    fun `test marks command handler on constructor even when there are no producers in kotlin`() {
        addFile(
            "MyAggregate.kt", """
            @AggregateRoot
            class MyAggregate {
               @CommandHandler<caret>
               constructor(command: MyCommand) {
               }
            }
        """.trimIndent(), open = true
        )

        assertThat(hasLineMarker(CommandHandlerMethodLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(CommandHandlerMethodLineMarkerProvider::class.java)).isEmpty()
    }

    fun `test marks command handler on function even when there are no producers in java`() {
        addFile(
            "MyAggregate.java", """
            @AggregateRoot
            class MyAggregate {
               @CommandHandler<caret>
               public void handle(MyCommand command) {
               }
            }
        """.trimIndent(), open = true
        )

        assertThat(hasLineMarker(CommandHandlerMethodLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(CommandHandlerMethodLineMarkerProvider::class.java)).isEmpty()
    }

    fun `test marks command handler on constructor even when there are no producers in java`() {
        addFile(
            "MyAggregate.java", """
            @AggregateRoot
            class MyAggregate {
               @CommandHandler<caret>
               public MyAggregate(MyCommand command) {
               }
            }
        """.trimIndent(), open = true
        )

        assertThat(hasLineMarker(CommandHandlerMethodLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(CommandHandlerMethodLineMarkerProvider::class.java)).isEmpty()
    }

    fun `test marks command handler as intercepted when command interceptor matches`() {
        addFile(
            "MyAggregate.java", """
            @AggregateRoot
            class MyAggregate {
               @CommandHandler<caret>
               public MyAggregate(MyCommand command) {
               }
               
               @CommandHandlerInterceptor
               fun intercept() {}
            }
        """.trimIndent(), open = true
        )

        assertThat(hasLineMarker(CommandHandlerMethodLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(CommandHandlerMethodLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("Command Interceptor of MyAggregate", "intercept", AxonIcons.Interceptor)
        )
    }

    fun `test shows command created by saga in command handler marker in kotlin`() {
        addFile(
            "MyAggregate.kt", """
            data class MySagaTriggeringEvent(val id: String)
            
            @Saga
            class MySaga {
                @SagaEventHandler
                fun handleSagaEvent(event: MySagaTriggeringEvent) {
                    MyCommand(event.id)
                }
            }
            
            @AggregateRoot
            class MyAggregate {
               @CommandHandler<caret>
               fun handle(command: MyCommand) {
               }
            }
        """.trimIndent(), open = true
        )

        assertThat(hasLineMarker(CommandHandlerMethodLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(CommandHandlerMethodLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("Saga: test", null, AxonIcons.Handler)
        )
    }

    fun `test shows command created by saga in command handler marker in java`() {
        addFile(
            "MySagaTriggeringEvent.kt", """
            data class MySagaTriggeringEvent(val id: String)
        """.trimIndent()
        )
        addFile(
            "MySaga.java", """
            @Saga
            class MySaga {
                @SagaEventHandler
                public void handleSagaEvent(MySagaTriggeringEvent event) {
                    new MyCommand(event.id);
                }
            }
            
        """.trimIndent()
        )
        addFile(
            "MyAggregate.java", """
            @AggregateRoot
            class MyAggregate {
               @CommandHandler<caret>
               public void handle(MyCommand command) {
               }
            }
        """.trimIndent(), open = true
        )

        assertThat(hasLineMarker(CommandHandlerMethodLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(CommandHandlerMethodLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("Saga: test", null, AxonIcons.Handler)
        )
    }
}
