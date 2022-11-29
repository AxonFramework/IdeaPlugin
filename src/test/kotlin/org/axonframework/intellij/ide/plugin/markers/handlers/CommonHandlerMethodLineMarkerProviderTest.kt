/*
 *  Copyright (c) (2010-2022). Axon Framework
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

class CommonHandlerMethodLineMarkerProviderTest : AbstractAxonFixtureTestCase() {
    override fun setUp() {
        super.setUp()
        addFile(
            "MyCommand.kt", """
            data class MyCommand(val id: String)
        """.trimIndent()
        )
    }

    fun `test shows event produced by command on event sourcing handler in kotlin`() {
        addFile(
            "MyAggregate.kt", """
            data class MyEvent(val id: String)
            
            @AggregateRoot
            class MyAggregate {
               @CommandHandler
               fun handle(command: MyCommand) {
                    AggregateLifecycle.apply(MyEvent(command.id))
               }
               
               @EventSourcingHandler<caret>
               fun handle(event: MyEvent) {
                   
               }
            }
        """.trimIndent(), open = true
        )

        assertThat(hasLineMarker(CommonHandlerMethodLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(CommonHandlerMethodLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("MyAggregate.handle(MyCommand):${actualLineNumber(7)}", null, AxonIcons.Publisher)
        )
    }

    fun `test shows event produced by command on event sourcing handler in java`() {
        addFile(
            "events.kt", """
            data class MyEvent(val id: String)
        """.trimIndent()
        )
        addFile(
            "MyAggregate.java", """
            @AggregateRoot
            class MyAggregate {
               @CommandHandler
               public void handle(MyCommand command) {
                    AggregateLifecycle.apply(new MyEvent(command.id))
               }
               
               @EventSourcingHandler<caret>
               public void handle(MyEvent event) {
                   
               }
           }
        """.trimIndent(), open = true
        )

        assertThat(hasLineMarker(CommonHandlerMethodLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(CommonHandlerMethodLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("MyAggregate.handle(MyCommand):${actualLineNumber(5)}", null, AxonIcons.Publisher)
        )
    }

    fun `test shows event produced by event sourcing handler on event sourcing handler in kotlin`() {
        addFile(
            "MyAggregate.kt", """
            data class MyEventOne(val id: String)
            data class MyEventTwo(val id: String)
            
            @AggregateRoot
            class MyAggregate {
               
               @EventSourcingHandler
               fun handle(event: MyEventOne) {
                   MyEventTwo(event.id)
               }
               
               @EventSourcingHandler<caret>
               fun handle(event: MyEventTwo) {
                   
               }
            }
        """.trimIndent(), open = true
        )

        assertThat(hasLineMarker(CommonHandlerMethodLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(CommonHandlerMethodLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("MyAggregate.handle(MyEventOne):${actualLineNumber(9)}", null, AxonIcons.Publisher)
        )
    }


    fun `test shows event produced by event sourcing handler on event sourcing handler in java`() {
        addFile(
            "events.kt", """
            data class MyEventOne(val id: String)
            data class MyEventTwo(val id: String)
        """.trimIndent()
        )
        addFile(
            "MyAggregate.java", """
            
            @AggregateRoot
            class MyAggregate {
               
               @EventSourcingHandler
               public void handle(MyEventOne event) {
                   new MyEventTwo(event.id)
               }
               
               @EventSourcingHandler<caret>
               public void handle(MyEventTwo event) {
                   
               }
            }
        """.trimIndent(), open = true
        )

        assertThat(hasLineMarker(CommonHandlerMethodLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(CommonHandlerMethodLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("MyAggregate.handle(MyEventOne):${actualLineNumber(6)}", null, AxonIcons.Publisher)
        )
    }

    fun `test shows event created by command in saga event handler in kotlin`() {
        addFile(
            "code.kt", """
            data class MySagaTriggeringEvent(val id: String)
            
            @Saga
            class MySaga {
                @SagaEventHandler<caret>
                fun handleSagaEvent(event: MySagaTriggeringEvent) {
                }
            }
            
            @AggregateRoot
            class MyAggregate {
               @CommandHandler
               fun handle(command: MyCommand) {
                   AggregateLifecycle.apply(MySagaTriggeringEvent(""))
               }
            }
        """.trimIndent(), open = true
        )

        assertThat(hasLineMarker(CommonHandlerMethodLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(CommonHandlerMethodLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("MyAggregate.handle(MyCommand):${actualLineNumber(14)}", null, AxonIcons.Publisher)
        )
    }

    fun `test shows event created by command in saga event handler in java`() {
        addFile(
            "MySagaTriggeringEvent.java", """
            public class MySagaTriggeringEvent {
                public MySagaTriggeringEvent(String id) {}
            }
        """.trimIndent()
        )
        addFile(
            "MyAggregate.java", """
            import test.MySagaTriggeringEvent;
            
            @AggregateRoot
            class MyAggregate {
               @CommandHandler
               public void handle(MyCommand MySagaTriggeringEvent) {
                   AggregateLifecycle.apply(new MySagaTriggeringEvent(""))
               }
            }
        """.trimIndent()
        )
        addFile(
            "MySaga.java", """
            import test.MySagaTriggeringEvent;
            
            @Saga
            public class MySaga {
                @SagaEventHandler<caret>
                public void handleSagaEvent(MySagaTriggeringEvent event) {
                }
            }
        """.trimIndent(), open = true
        )

        assertThat(hasLineMarker(CommonHandlerMethodLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(CommonHandlerMethodLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("MyAggregate.handle(MyCommand):${actualLineNumber(7)}", null, AxonIcons.Publisher)
        )
    }


    fun `test shows event created by command in event handler in kotlin`() {
        addFile(
            "code.kt", """
            data class MyEvent(val id: String)
            
           
            class MyProcessor {
                @EventHandler<caret>
                fun handleEvent(event: MyEvent) {
                }
            }
            
            @AggregateRoot
            class MyAggregate {
               @CommandHandler
               fun handle(command: MyCommand) {
                   AggregateLifecycle.apply(MyEvent(""))
               }
            }
        """.trimIndent(), open = true
        )

        assertThat(hasLineMarker(CommonHandlerMethodLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(CommonHandlerMethodLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("MyAggregate.handle(MyCommand):${actualLineNumber(14)}", null, AxonIcons.Publisher)
        )
    }
}
