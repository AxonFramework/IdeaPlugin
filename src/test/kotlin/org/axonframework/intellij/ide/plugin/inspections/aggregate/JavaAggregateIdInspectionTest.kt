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

class JavaAggregateIdInspectionTest : AbstractAxonFixtureTestCase() {
    fun `test should detect missing identifier`() {
        val file = addFile("MyAggregate.java", """
            @AggregateRoot
            public class MyAggregate {
            }
        """.trimIndent())

        myFixture.enableInspections(JavaAggregateIdInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        assertThat(highlights).anyMatch {
            it.text == "MyAggregate" && it.description.contains("requires a field annotated with @AggregateIdentifier")
        }
    }

    fun `test should not detect missing identifier if not an aggregate`() {
        val file = addFile("MyAggregate.java", """
            public class MyAggregate {
            }
        """.trimIndent())

        myFixture.enableInspections(JavaAggregateIdInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        assertThat(highlights).noneMatch {
            it.text == "MyAggregate"
        }
    }

    fun `test should not detect when aggregate has an identifier`() {
        val file = addFile("MyAggregate.java", """            
            @AggregateRoot
            public class MyAggregate {
                @AggregateIdentifier
                String myIdentifier;
            }
        """.trimIndent())

        myFixture.enableInspections(JavaAggregateIdInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        assertThat(highlights).noneMatch { it.text == "MyAggregate" }
    }

    fun `test should also allow EntityId annotation`() {
        val file = addFile("MyAggregate.java", """            
            @AggregateRoot
            public class MyAggregate {
                @EntityId
                String myIdentifier;
            }
        """.trimIndent())

        myFixture.enableInspections(JavaAggregateIdInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        assertThat(highlights).noneMatch { it.text == "MyAggregate" }
    }
}
