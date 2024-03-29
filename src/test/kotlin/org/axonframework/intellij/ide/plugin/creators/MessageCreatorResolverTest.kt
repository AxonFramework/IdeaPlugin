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

package org.axonframework.intellij.ide.plugin.creators

import org.assertj.core.api.Assertions
import org.axonframework.intellij.ide.plugin.AbstractAxonFixtureTestCase
import org.axonframework.intellij.ide.plugin.util.creatorResolver

class MessageCreatorResolverTest : AbstractAxonFixtureTestCase() {
    fun `test can resolve application of event during command handling`() {
        addFile(
            "MyComponent.kt", """
            data class MyCommand(@TargetAggregateIdentifier id: String)
            data class MyEvent(id: String)
            
            @AggregateRoot
            class MyAggregate {
                @CommandHandler
                constructor(command: MyCommand) {
                    AggregateLifecycle.apply(MyEvent(command.id))
                }
                
                @EventSourcingHandler
                fun handle(event: MyEvent) {
                   // Do something
                }
            }
        """.trimIndent()
        )
        val creators = project.creatorResolver().getCreatorsForPayload("test.MyEvent")
        Assertions.assertThat(creators).anyMatch {
            it.payload == "test.MyEvent" && it.renderContainerText() == null
        }
    }


    fun `test can resolve application of event during an event sourcing handler`() {
        addFile(
            "MyComponent.kt", """
            data class MyEvent(id: String)
            data class MyEvent2(id: String)
            
            @AggregateRoot
            class MyAggregate {                
                @EventSourcingHandler
                fun handle(event: MyEvent) {
                    AggregateLifecycle.apply(MyEvent2(event.id))
                }
                
                @EventSourcingHandler
                fun handle(event: MyEvent2) {
                }
            }
        """.trimIndent()
        )
        val creators = project.creatorResolver().getCreatorsForPayload("test.MyEvent2")
        Assertions.assertThat(creators).anyMatch {
            it.payload == "test.MyEvent2" && it.renderText() == "EventSourcingHandler MyAggregate"
        }
    }


    fun `test can resolve dispatch of command during SagaEventHandler`() {
        addFile(
            "MyComponent.kt", """
            data class MyCommand(@TargetAggregateIdentifier id: String)
            data class MyEvent(id: String)
            
            @AggregateRoot
            class MyAggregate {                
                @CommandHandler
                constructor(command: MyCommand) {
                    
                }
            }
            
            @Saga
            @ProcessingGroup("fancy-saga")
            class MySaga {
               @SagaEventHandler
               fun handle(event: MyEvent) {
                   MyCommand(event.id)
               }
            }
            
        """.trimIndent()
        )
        val creators = project.creatorResolver().getCreatorsForPayload("test.MyCommand")
        Assertions.assertThat(creators).anyMatch {
            it.payload == "test.MyCommand" &&
                    it.renderText() == "Saga: fancy-saga"
        }
    }

    fun `test can resolve dispatch of command from plain components`() {
        addFile(
            "MyComponent.kt", """
            data class MyCommand(@TargetAggregateIdentifier id: String)
            data class MyEvent(id: String)
            
            @AggregateRoot
            class MyAggregate {                
                @CommandHandler
                constructor(command: MyCommand) {
                    
                }
            }
            
            class PlainComponent {
               fun handleImportantHttpRequest() {
                   MyCommand(event.id)
               }
            }
            
        """.trimIndent()
        )
        val creators = project.creatorResolver().getCreatorsForPayload("test.MyCommand")
        Assertions.assertThat(creators).anyMatch {
            it.payload == "test.MyCommand" &&
                    it.renderText() == "PlainComponent.handleImportantHttpRequest"
            it.renderContainerText() == null
        }
    }

    fun `test can resolve query handlers without processing group`() {
        addFile(
            "MyComponent.kt", """
            data class MyQuery(id: String)
            
            class MyQueryDispatcher {
                fun dispatchQuery() {
                    MyQuery("")
                }
            }
            
            class MyQueryHandler {
               @QueryHandler
               fun handle(query: MyQuery) {
               }
            }
            
        """.trimIndent()
        )
        val creators = project.creatorResolver().getCreatorsForPayload("test.MyQuery")
        Assertions.assertThat(creators).anyMatch {
            it.payload == "test.MyQuery" &&
                    it.renderText() == "MyQueryHandler.handle"
            it.renderContainerText() == null
        }
    }

    fun `test resolves builder method as payload creator`() {
        addFile("MyBuilderBasedEvent.java", """
            public class MyBuilderBasedEvent {
                public static class Builder {
                    public MyBuilderBasedEvent build() {
                        return new MyBuilderBasedEvent();
                    }
                }
                
                public Builder builder() {
                    return new Builder();
                }
            }
        """.trimIndent())

        addFile(
            "MyAggregate.java", """        
            import test.MyBuilderBasedEvent;
            
            @AggregateRoot
            class MyAggregate {
               @CommandHandler
               public void handle(MyCommand command) {
                    AggregateLifecycle.apply(MyBuilderBasedEvent.builder().build());
               }
           }
        """.trimIndent(), open = true
        )

        val creators = project.creatorResolver().getCreatorsForPayload("test.MyBuilderBasedEvent")
        Assertions.assertThat(creators).anyMatch {
            it.payload == "test.MyBuilderBasedEvent" &&
                    it.renderText() == "MyAggregate.handle"
            it.renderContainerText() == null
        }
    }
}
