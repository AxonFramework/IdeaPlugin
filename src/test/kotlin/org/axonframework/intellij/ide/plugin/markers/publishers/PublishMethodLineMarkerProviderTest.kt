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

package org.axonframework.intellij.ide.plugin.markers.publishers

import org.assertj.core.api.Assertions.assertThat
import org.axonframework.intellij.ide.plugin.AbstractAxonFixtureTestCase
import org.axonframework.intellij.ide.plugin.AxonIcons

class PublishMethodLineMarkerProviderTest : AbstractAxonFixtureTestCase() {
    override fun setUp() {
        super.setUp()
        addFile(
            "models.kt", """
            data class MyEvent(val id: String)
            data class MyCommand(val id: String)
            data class MyQuery(val id: String)
        """.trimIndent()
        )
    }

    fun `test shows publish icon on constructor with event sourcing handler in java`() {
        addFile(
            "MyAggregate.java", """
            @AggregateRoot
            class MyAggregate {
               @CommandHandler
               public void handle(MyCommand command) {
                    AggregateLifecycle.apply(new MyEvent(command.id))<caret>
               }
               
               @EventSourcingHandler
               public void handle(MyEvent event) {
                   
               }
           }
        """.trimIndent(), open = true
        )

        assertThat(hasLineMarker(PublishMethodLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(PublishMethodLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("EventSourcingHandler MyAggregate", null, AxonIcons.Handler)
        )
    }

    fun `test shows publish icon on constructor with event sourcing handler in kotlin`() {
        addFile(
            "MyAggregate.kt", """            
            @AggregateRoot
            class MyAggregate {
               @CommandHandler
               fun handle(command: MyCommand) {
                    AggregateLifecycle.apply(MyEvent(command.id))<caret>
               }
               
               @EventSourcingHandler
               fun handle(event: MyEvent) {
                   
               }
           }
        """.trimIndent(), open = true
        )

        assertThat(hasLineMarker(PublishMethodLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(PublishMethodLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("EventSourcingHandler MyAggregate", null, AxonIcons.Handler)
        )
    }

    fun `test shows publish icon on constructor with command handler in java`() {
        addFile(
            "MyAggregate.java", """            
            @AggregateRoot
            class MyAggregate {
               @CommandHandler
               public void handle(MyCommand command) {
               }
           }
        """.trimIndent()
        )
        addFile(
            "MyPublisher.java", """
            class MyPublisher {
                public void publish() {
                    new MyCommand("")<caret>
                }
            }
        """.trimIndent(), open = true
        )

        assertThat(hasLineMarker(PublishMethodLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(PublishMethodLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("MyCommand", "MyAggregate", AxonIcons.Handler)
        )
    }

    fun `test shows publish icon on constructor with command handler in kotlin`() {
        addFile(
            "MyAggregate.kt", """            
            @AggregateRoot
            class MyAggregate {
               @CommandHandler
               fun handle(command: MyCommand) {
               }
           }
           
            class MyPublisher {
                public void publish() {
                    MyCommand("")<caret>
                }
            }
        """.trimIndent(), open = true
        )

        assertThat(hasLineMarker(PublishMethodLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(PublishMethodLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("MyCommand", "MyAggregate", AxonIcons.Handler)
        )
    }


    fun `test shows publish icon on constructor with query handler having processing group in kotlin`() {
        addFile(
            "MyAggregate.kt", """            
            @ProcessingGroup("some-group")
            class MyProcessingGroup {
               @QueryHandler
               fun handle(query: MyQuery) {
               }
           }
           
            class MyPublisher {
                public void publish() {
                    MyQuery("")<caret>
                }
            }
        """.trimIndent(), open = true
        )

        assertThat(hasLineMarker(PublishMethodLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(PublishMethodLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("some-group", null, AxonIcons.Handler)
        )
    }

    fun `test shows publish icon on constructor with query handler having no processing group`() {
        addFile(
            "MyAggregate.kt", """   
            class MyProcessingGroup {
               @QueryHandler
               fun handle(query: MyQuery) {
               }
           }
           
            class MyPublisher {
                public void publish() {
                    MyQuery("")<caret>
                }
            }
        """.trimIndent(), open = true
        )

        assertThat(hasLineMarker(PublishMethodLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(PublishMethodLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("test", null, AxonIcons.Handler)
        )
    }

    fun `test shows publish icon on constructor with event handler having processing group`() {
        addFile(
            "MyAggregate.kt", """        
            @ProcessingGroup("awesome-group")
            class MyProcessingGroup {
               @EventHandler
               fun handle(event: MyEvent) {
               }
           }
           
            @AggregateRoot
            class MyAggregate {
               @CommandHandler
               fun handle(command: MyCommand) {
                    AggregateLifecycle.apply(MyEvent(command.id))<caret>
               }
           }
        """.trimIndent(), open = true
        )

        assertThat(hasLineMarker(PublishMethodLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(PublishMethodLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("MyProcessingGroup", "awesome-group", AxonIcons.Handler)
        )
    }

    fun `test shows publish icon on constructor with event handler having no processing group`() {
        addFile(
            "MyAggregate.kt", """        
            class MyProcessingGroup {
               @EventHandler
               fun handle(event: MyEvent) {
               }
           }
           
            @AggregateRoot
            class MyAggregate {
               @CommandHandler
               fun handle(command: MyCommand) {
                    AggregateLifecycle.apply(MyEvent(command.id))<caret>
               }
           }
        """.trimIndent(), open = true
        )

        assertThat(hasLineMarker(PublishMethodLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(PublishMethodLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("MyProcessingGroup", "test", AxonIcons.Handler)
        )
    }
}
