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

package org.axonframework.intellij.ide.plugin.inspections.saga

import com.intellij.lang.annotation.HighlightSeverity
import org.assertj.core.api.Assertions
import org.axonframework.intellij.ide.plugin.AbstractAxonFixtureTestCase

class KotlinSagaAssociationPropertyInspectionTest : AbstractAxonFixtureTestCase() {
    fun `test should detect missing associationProperty`() {
        val file = addFile(
            "file.kt", """
                class MyMessage
                
                class MyHandler {
                    @SagaEventHandler(associationProperty = "some")
                    fun handle(message: MyMessage) {}
                }
        """.trimIndent()
        )

        myFixture.enableInspections(KotlinSagaAssociationPropertyInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).anyMatch {
            it.text == "\"some\"" && it.description.contains("The message does not declare a property")
        }
    }

    fun `test should not detect missing associationProperty on custom resolver`() {
        val file = addFile(
            "file.kt", """
                class MyMessage
                
                class MyHandler {
                    @SagaEventHandler(associationProperty = "some", associationResolver = Object.class)
                    fun handle(message: MyMessage) {}
                }
        """.trimIndent()
        )

        myFixture.enableInspections(KotlinSagaAssociationPropertyInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).noneMatch {
            it.text == "handle" && it.description.contains("The message does not declare a property")
        }
    }

    fun `test detect defined property`() {
        val file = addFile(
            "file.kt", """
                data class MyMessage(val some: String)
                
                class MyHandler {
                    @SagaEventHandler(associationProperty = "some")
                    fun handle(message: MyMessage) {}
                }
        """.trimIndent()
        )

        myFixture.enableInspections(KotlinSagaAssociationPropertyInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).noneMatch {
            it.text == "handle" && it.description.contains("The message does not declare a property")
        }
    }

    fun `test detect defined getter`() {
        val file = addFile(
            "file.kt", """
                class MyMessage {
                   fun getSome() = ""
                }
                
                class MyHandler {
                    @SagaEventHandler(associationProperty = "some")
                    fun handle(message: MyMessage) {}
                }
        """.trimIndent()
        )

        myFixture.enableInspections(KotlinSagaAssociationPropertyInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).noneMatch {
            it.text == "handle" && it.description.contains("The message does not declare a property")
        }
    }
}
