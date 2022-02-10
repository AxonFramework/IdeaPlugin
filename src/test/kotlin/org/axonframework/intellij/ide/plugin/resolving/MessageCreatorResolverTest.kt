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

package org.axonframework.intellij.ide.plugin.resolving

import org.assertj.core.api.Assertions
import org.axonframework.intellij.ide.plugin.AbstractAxonFixtureTestCase
import org.axonframework.intellij.ide.plugin.util.creatorResolver
import org.axonframework.intellij.ide.plugin.util.toElementText

class MessageCreatorResolverTest : AbstractAxonFixtureTestCase() {
    fun `test can resolve application of event during command handling`() {
        addFile("MyComponent.kt", """
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
        """.trimIndent())
        val creators = project.creatorResolver().getCreatorsForPayload("test.MyEvent")
        Assertions.assertThat(creators).anyMatch {
            it.payload == "test.MyEvent" && it.element.toElementText() == "MyCommand" && it.containerText == null
        }
    }


    fun `test can resolve application of event during an event sourcing handler`() {
        addFile("MyComponent.kt", """
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
        """.trimIndent())
        val creators = project.creatorResolver().getCreatorsForPayload("test.MyEvent2")
        Assertions.assertThat(creators).anyMatch {
            it.payload == "test.MyEvent2" && it.element.toElementText() == "MyEvent" && it.containerText == "Side effect of EventSourcingHandler"
        }
    }


    fun `test can resolve dispatch of command during SagaEventHandler`() {
        addFile("MyComponent.kt", """
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
            
        """.trimIndent())
        val creators = project.creatorResolver().getCreatorsForPayload("test.MyCommand")
        Assertions.assertThat(creators).anyMatch {
            it.payload == "test.MyCommand" &&
                    it.element.toElementText() == "MyEvent"
            it.containerText == "Saga fancy-saga"
        }
    }

    fun `test can resolve dispatch of command from plain components`() {
        addFile("MyComponent.kt", """
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
            
        """.trimIndent())
        val creators = project.creatorResolver().getCreatorsForPayload("test.MyCommand")
        Assertions.assertThat(creators).anyMatch {
            it.payload == "test.MyCommand" &&
                    it.element.toElementText() == "PlainComponent.handleImportantHttpRequest"
            it.containerText == null
        }
    }

    fun `test can resolve query handlers without processing group`() {
        addFile("MyComponent.kt", """
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
            
        """.trimIndent())
        val creators = project.creatorResolver().getCreatorsForPayload("test.MyQuery")
        Assertions.assertThat(creators).anyMatch {
            it.payload == "test.MyQuery" &&
                    it.element.toElementText() == "MyQueryHandler.handle"
            it.containerText == null
        }
    }
}
