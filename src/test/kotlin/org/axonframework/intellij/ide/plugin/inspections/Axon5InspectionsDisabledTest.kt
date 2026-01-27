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

package org.axonframework.intellij.ide.plugin.inspections

import com.intellij.lang.annotation.HighlightSeverity
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.intellij.ide.plugin.AbstractAxon5FixtureTestCase
import org.axonframework.intellij.ide.plugin.inspections.aggregate.JavaAggregateConstructorInspection
import org.axonframework.intellij.ide.plugin.inspections.aggregate.JavaAggregateIdInspection
import org.axonframework.intellij.ide.plugin.inspections.aggregate.JavaAggregateMemberRoutingKeyInspection
import org.axonframework.intellij.ide.plugin.inspections.aggregate.KotlinAggregateConstructorInspection
import org.axonframework.intellij.ide.plugin.inspections.saga.JavaSagaAssociationPropertyInspection
import org.axonframework.intellij.ide.plugin.inspections.saga.KotlinSagaAssociationPropertyInspection

/**
 * Tests to verify that Axon 4-specific inspections are properly disabled
 * when running in an Axon 5 project.
 */
class Axon5InspectionsDisabledTest : AbstractAxon5FixtureTestCase() {

    fun `test aggregate constructor inspection disabled for Axon 5`() {
        val file = addFile(
            "MyAggregate.java", """
            @EventSourcedEntity
            public class MyAggregate {
                // No empty constructor - but this is OK in Axon 5
            }
        """.trimIndent()
        )

        myFixture.enableInspections(JavaAggregateConstructorInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)

        assertThat(highlights).noneMatch {
            it.text == "MyAggregate" && it.description?.contains("empty constructor") == true
        }
    }

    fun `test Kotlin aggregate constructor inspection disabled for Axon 5`() {
        val file = addFile(
            "MyAggregate.kt", """
            @EventSourcedEntity
            class MyAggregate {
                // No empty constructor - but this is OK in Axon 5
            }
        """.trimIndent()
        )

        myFixture.enableInspections(KotlinAggregateConstructorInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)

        assertThat(highlights).noneMatch {
            it.text == "MyAggregate" && it.description?.contains("empty constructor") == true
        }
    }

    fun `test aggregate ID inspection disabled for Axon 5`() {
        val file = addFile(
            "MyAggregate.java", """
            @EventSourcedEntity
            public class MyAggregate {
                // No @EntityId annotation - but this is OK in Axon 5
                private String id;
            }
        """.trimIndent()
        )

        myFixture.enableInspections(JavaAggregateIdInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)

        assertThat(highlights).noneMatch {
            it.description?.contains("identifier") == true || it.description?.contains("EntityId") == true
        }
    }

    fun `test aggregate member routing key inspection disabled for Axon 5`() {
        val file = addFile(
            "MyAggregate.java", """
            @EventSourcedEntity
            public class MyAggregate {
                @AggregateMember
                private java.util.List<MyEntity> entities;
            }

            class MyEntity {}
        """.trimIndent()
        )

        myFixture.enableInspections(JavaAggregateMemberRoutingKeyInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)

        assertThat(highlights).noneMatch {
            it.description?.contains("EntityId") == true || it.description?.contains("routing") == true
        }
    }

    fun `test saga inspection disabled for Axon 5`() {
        // Sagas don't exist in Axon 5, so saga inspections should not run
        val file = addFile(
            "NotASaga.java", """
            public class NotASaga {
                // This would have been a saga in v4, but v5 doesn't have sagas
                public void handle(MyEvent event) {}
            }

            class MyEvent {
                private String id;
            }
        """.trimIndent()
        )

        myFixture.enableInspections(JavaSagaAssociationPropertyInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)

        // Should have no saga-related warnings
        assertThat(highlights).noneMatch {
            it.description?.contains("saga") == true || it.description?.contains("association") == true
        }
    }

    fun `test Kotlin saga inspection disabled for Axon 5`() {
        val file = addFile(
            "NotASaga.kt", """
            class NotASaga {
                fun handle(event: MyEvent) {}
            }

            data class MyEvent(val id: String)
        """.trimIndent()
        )

        myFixture.enableInspections(KotlinSagaAssociationPropertyInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)

        // Should have no saga-related warnings
        assertThat(highlights).noneMatch {
            it.description?.contains("saga") == true || it.description?.contains("association") == true
        }
    }

    fun `test EventSourced aggregate without issues produces no warnings`() {
        val file = addFile(
            "MyAggregate.kt", """
            data class MyEvent(val id: String)

            @EventSourcedEntity
            class MyAggregate {
                @EntityCreator
                fun onCreate(event: MyEvent) {
                    // Proper Axon 5 aggregate
                }
            }
        """.trimIndent()
        )

        // Enable all Axon 4 inspections
        myFixture.enableInspections(
            KotlinAggregateConstructorInspection(),
            JavaAggregateConstructorInspection(),
            JavaAggregateIdInspection(),
            JavaAggregateMemberRoutingKeyInspection()
        )

        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)

        // Should have no Axon-related warnings (v4 inspections are disabled)
        assertThat(highlights).noneMatch {
            it.description?.contains("Axon") == true ||
            it.description?.contains("aggregate") == true ||
            it.description?.contains("constructor") == true
        }
    }
}
