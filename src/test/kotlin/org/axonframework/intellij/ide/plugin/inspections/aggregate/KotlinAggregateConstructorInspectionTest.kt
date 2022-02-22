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
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.intellij.ide.plugin.AbstractAxonFixtureTestCase

class KotlinAggregateConstructorInspectionTest : AbstractAxonFixtureTestCase() {
    fun `test should detect missing aggregate constructor`() {
        val file = addFile(
            "MyAggregate.kt", """
            @AggregateRoot
            class MyAggregate {
            }
        """.trimIndent()
        )

        myFixture.enableInspections(KotlinAggregateConstructorInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        assertThat(highlights).anyMatch {
            it.text == "MyAggregate" && it.description.contains("requires an empty constructor")
        }
    }

    fun `test should detect missing aggregate constructor when is not an aggregate`() {
        val file = addFile(
            "MyAggregate.kt", """
            class MyAggregate {
            }
        """.trimIndent()
        )

        myFixture.enableInspections(KotlinAggregateConstructorInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        assertThat(highlights).noneMatch {
            it.text == "MyAggregate"
        }
    }

    fun `test should not detect when aggregate has empty constructor`() {
        val file = addFile(
            "MyAggregate.kt", """
            @AggregateRoot
            class MyAggregate {
                constructor() {}
            }
        """.trimIndent()
        )

        myFixture.enableInspections(KotlinAggregateConstructorInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        assertThat(highlights).noneMatch { it.text == "MyAggregate" }
    }
}
