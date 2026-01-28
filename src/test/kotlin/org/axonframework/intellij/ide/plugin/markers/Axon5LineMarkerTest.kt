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

package org.axonframework.intellij.ide.plugin.markers

import org.assertj.core.api.Assertions.assertThat
import org.axonframework.intellij.ide.plugin.AbstractAxon5FixtureTestCase
import org.axonframework.intellij.ide.plugin.markers.handlers.CommonHandlerMethodLineMarkerProvider

/**
 * Tests for line markers in Axon Framework 5 projects.
 * Verifies that EntityCreator, CommandHandler, and EventHandler line markers work correctly.
 */
class Axon5LineMarkerTest : AbstractAxon5FixtureTestCase() {

    fun `test entity creator shows line marker`() {
        val file = addFile(
            "MyAggregate.kt", """
            data class MyEvent(val id: String)

            @EventSourcedEntity
            class MyAggregate {
                @EntityCreator<caret>
                fun onCreate(event: MyEvent) {
                    // Entity creation handler
                }
            }
        """.trimIndent(), open = true
        )

        myFixture.openFileInEditor(file)

        // EntityCreator should show a marker (part of common handlers)
        assertThat(hasLineMarker(CommonHandlerMethodLineMarkerProvider::class.java))
            .describedAs("EntityCreator should have a line marker")
            .isTrue()
    }

    fun `test command handler shows line marker in Axon 5`() {
        addFile(
            "CreateMyAggregateCommand.kt", """
            data class CreateMyAggregateCommand(val id: String)
        """.trimIndent()
        )

        val file = addFile(
            "MyAggregate.kt", """
            @EventSourcedEntity
            class MyAggregate {
                @CommandHandler<caret>
                fun handle(command: CreateMyAggregateCommand) {
                    // Command handler with v5 annotation
                }
            }
        """.trimIndent(), open = true
        )

        myFixture.openFileInEditor(file)

        // Command handlers should still show markers
        assertThat(hasLineMarker(CommonHandlerMethodLineMarkerProvider::class.java))
            .describedAs("Command handler should have a line marker")
            .isTrue()
    }

    fun `test event handler shows line marker in Axon 5`() {
        addFile(
            "MyEvent.kt", """
            data class MyEvent(val id: String)
        """.trimIndent()
        )

        val file = addFile(
            "MyProjection.kt", """
            class MyProjection {
                @EventHandler<caret>
                fun on(event: MyEvent) {
                    // Event handler with v5 annotation
                }
            }
        """.trimIndent(), open = true
        )

        myFixture.openFileInEditor(file)

        // Event handlers should still show markers
        assertThat(hasLineMarker(CommonHandlerMethodLineMarkerProvider::class.java))
            .describedAs("Event handler should have a line marker")
            .isTrue()
    }

    fun `test EventSourced aggregate shows class line marker`() {
        addFile(
            "MyEvent.kt", """
            data class MyEvent(val id: String)
        """.trimIndent()
        )

        val file = addFile(
            "MyAggregate.kt", """
            @EventSourcedEntity
            class <caret>MyAggregate {
                @EntityCreator
                fun onCreate(event: MyEvent) {}
            }
        """.trimIndent(), open = true
        )

        myFixture.openFileInEditor(file)

        // Aggregates should show class-level markers
        assertThat(hasLineMarker(ClassLineMarkerProvider::class.java))
            .describedAs("EventSourced aggregate should have a class line marker")
            .isTrue()
    }
}
