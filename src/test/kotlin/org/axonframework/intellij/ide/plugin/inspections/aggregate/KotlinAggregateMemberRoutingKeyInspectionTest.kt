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

package org.axonframework.intellij.ide.plugin.inspections.aggregate

import com.intellij.lang.annotation.HighlightSeverity
import org.assertj.core.api.Assertions
import org.axonframework.intellij.ide.plugin.AbstractAxonFixtureTestCase

class KotlinAggregateMemberRoutingKeyInspectionTest : AbstractAxonFixtureTestCase() {
    override fun setUp() {
        super.setUp()

        addFile(
            "MyEntityWithMissingKey.kt", """
            class MyEntityWithMissingKey {
            
            }
        """.trimIndent()
        )
    }

    fun `test should detect missing entity id in aggregate member when is in List`() {
        val file = addFile(
            "MyAggregate.kt", """
            import test.MyEntityWithMissingKey
            import kotlin.collections.List
            
            @AggregateRoot
            class MyAggregate {
                @AggregateMember
                private lateinit var entities: List<MyEntityWithMissingKey>
            }
        """.trimIndent()
        )

        myFixture.enableInspections(KotlinAggregateMemberRoutingKeyInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).anyMatch {
            it.text == "entities" && it.description.contains("requires a field or method annotated with @EntityId")
        }
    }

    fun `test should not detect missing entity id when actually has entity id`() {
        addFile(
            "MyEntityWithId.kt", """
            import java.lang.String
            
            class MyEntityWithId {
                @EntityId
                lateinit var id: String;
            }
        """.trimIndent()
        )

        val file = addFile(
            "MyAggregate.kt", """
            import test.MyEntityWithId
            import java.util.List
            
            @AggregateRoot
            class MyAggregate {
                @AggregateMember
                private lateinit var entities: List<MyEntityWithId>
            }
        """.trimIndent()
        )

        myFixture.enableInspections(KotlinAggregateMemberRoutingKeyInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).noneMatch {
            it.text == "entities"
        }
    }

    fun `test should not detect missing entity id if not in collection`() {
        val file = addFile(
            "MyAggregate.kt", """
            import test.MyEntityWithMissingKey
            
            @AggregateRoot
            class MyAggregate {
                @AggregateMember
                private val entity: MyEntityWithMissingKey = MyEntityWithMissingKey()
            }
        """.trimIndent()
        )

        myFixture.enableInspections(KotlinAggregateMemberRoutingKeyInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).noneMatch {
            it.text == "entity"
        }
    }

    fun `test should detect missing entity id in aggregate member when is in Collection`() {
        val file = addFile(
            "MyAggregate.kt", """
            import test.MyEntityWithMissingKey
            import kotlin.collections.Collection
            
            @AggregateRoot
            class MyAggregate {
                @AggregateMember
                private lateinit var entities: Collection<MyEntityWithMissingKey>;
            }
        """.trimIndent()
        )

        myFixture.enableInspections(KotlinAggregateMemberRoutingKeyInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).anyMatch {
            it.text == "entities" && it.description.contains("requires a field or method annotated with @EntityId")
        }
    }

    fun `test should detect missing entity id in aggregate member when is in Map`() {
        val file = addFile(
            "MyAggregate.kt", """
            import test.MyEntityWithMissingKey
            import kotlin.collections.Map
            import java.lang.String
            
            @AggregateRoot
            class MyAggregate {
                @AggregateMember
                private lateinit var entities: Map<String, MyEntityWithMissingKey>;
            }
        """.trimIndent()
        )

        myFixture.enableInspections(KotlinAggregateMemberRoutingKeyInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).anyMatch {
            it.text == "entities" && it.description.contains("requires a field or method annotated with @EntityId")
        }
    }


    fun `test should detect problem in entities themselves`() {
        val file = addFile(
            "MyMiddleEntity.kt", """
            import test.MyEntityWithMissingKey
            import java.util.Map
            import java.lang.String
            
            class MyMiddleEntity {
                @EntityId
                private lateinit var id: String
                
                @AggregateMember
                private lateinit var entitiesInEntity: Map<String, MyEntityWithMissingKey>
            }
        """.trimIndent()
        )

        addFile(
            "MyAggregate.kt", """
            import test.MyMiddleEntity
            import java.util.Map
            import java.lang.String
            
            @AggregateRoot
            class MyAggregate {
                @AggregateMember
                private lateinit var entities: Map<String, MyMiddleEntity>
            }
        """.trimIndent()
        )

        myFixture.enableInspections(KotlinAggregateMemberRoutingKeyInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).anyMatch {
            it.text == "entitiesInEntity" && it.description.contains("requires a field or method annotated with @EntityId")
        }
    }
}
