/*
 *  Copyright (c) 2022-2026. Axon Framework
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.axonframework.intellij.ide.plugin.handlers.searchers

import org.assertj.core.api.Assertions
import org.axonframework.intellij.ide.plugin.AbstractAxon5FixtureTestCase
import org.axonframework.intellij.ide.plugin.resolving.handlers.types.CommandHandler
import org.axonframework.intellij.ide.plugin.resolving.handlers.types.EntityCreator
import org.axonframework.intellij.ide.plugin.resolving.handlers.types.EventHandler
import org.axonframework.intellij.ide.plugin.util.handlerResolver

/**
 * Tests for Axon Framework 5 handler detection.
 * Verifies that v5-specific handlers (EntityCreator) are detected and
 * deprecated handlers (Saga, Deadline) are not searched.
 */
class Axon5HandlerSearcherTest : AbstractAxon5FixtureTestCase() {

    fun `test can resolve EntityCreator handler`() {
        addFile(
            "MyAggregate.kt", """
            data class MyEvent(val id: String)

            @EventSourcedEntity
            class MyAggregate {
                @EntityCreator
                fun onCreate(event: MyEvent) {
                }
            }
        """.trimIndent()
        )

        val handlers = project.handlerResolver().findAllHandlers()
            .filterIsInstance<EntityCreator>()

        Assertions.assertThat(handlers).anyMatch {
            it.payload == "test.MyEvent" && it.element.name == "onCreate"
        }
    }

    fun `test can resolve command handler with v5 annotation package`() {
        addFile(
            "MyAggregate.kt", """
            data class MyCommand(val id: String)

            @EventSourcedEntity
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
            it.payload == "test.MyCommand" && it.element.name == "handle"
        }
    }

    fun `test can resolve event handler with v5 annotation package`() {
        addFile(
            "MyProjection.kt", """
            data class MyEvent(val id: String)

            class MyProjection {
                @EventHandler
                fun on(event: MyEvent) {
                }
            }
        """.trimIndent()
        )
        val handlers = project.handlerResolver().findAllHandlers()
            .filterIsInstance<EventHandler>()

        Assertions.assertThat(handlers).anyMatch {
            it.payload == "test.MyEvent" && it.element.name == "on"
        }
    }

    fun `test EventSourced aggregate is recognized`() {
        addFile(
            "MyAggregate.kt", """
            @EventSourcedEntity
            class MyAggregate {
                @EntityCreator
                fun onCreate(event: MyEvent) {}
            }

            data class MyEvent(val id: String)
        """.trimIndent()
        )

        val handlers = project.handlerResolver().findAllHandlers()
        Assertions.assertThat(handlers).isNotEmpty
    }

    fun `test EventSourcedEntity child aggregate works`() {
        addFile(
            "MyEntity.kt", """
            data class MyEvent(val id: String)

            @EventSourcedEntity
            class MyEntity {
                @EntityCreator
                fun onCreate(event: MyEvent) {
                }
            }
        """.trimIndent()
        )

        val handlers = project.handlerResolver().findAllHandlers()
            .filterIsInstance<EntityCreator>()

        Assertions.assertThat(handlers).anyMatch {
            it.element.name == "onCreate"
        }
    }

    fun `test constructor with EntityCreator`() {
        addFile(
            "MyAggregate.kt", """
            data class MyEvent(val id: String)

            @EventSourcedEntity
            class MyAggregate {
                @EntityCreator
                constructor(event: MyEvent) {
                }
            }
        """.trimIndent()
        )

        val handlers = project.handlerResolver().findAllHandlers()
            .filterIsInstance<EntityCreator>()

        Assertions.assertThat(handlers).anyMatch {
            it.element.name == "MyAggregate" && it.payload == "test.MyEvent"
        }
    }

    fun `test EntityCreator without parameters shows no line marker`() {
        addFile(
            "MyAggregate.kt", """
            @EventSourcedEntity
            class MyAggregate {
                @EntityCreator<caret>
                constructor() {
                }
            }
        """.trimIndent()
        )

        val handlers = project.handlerResolver().findAllHandlers()
            .filterIsInstance<EntityCreator>()

        Assertions.assertThat(handlers)
            .describedAs("EntityCreator without parameters should not be detected as a handler")
            .isEmpty()
    }
}
