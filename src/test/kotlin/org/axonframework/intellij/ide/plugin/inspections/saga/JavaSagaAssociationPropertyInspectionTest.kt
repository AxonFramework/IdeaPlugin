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

class JavaSagaAssociationPropertyInspectionTest : AbstractAxonFixtureTestCase() {

    fun `test should detect missing associationProperty`() {
        addFile(
            "MyMessage.java", """
            public class MyMessage {
                
            }
        """.trimIndent()
        )
        val file = addFile(
            "MyHandler.java", """
                import test.MyMessage;
                
            public class MyHandler {
                @SagaEventHandler(associationProperty = "some")
                public void handle(MyMessage message) {}
            }
        """.trimIndent()
        )

        myFixture.enableInspections(JavaSagaAssociationPropertyInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).anyMatch {
            it.text == "handle" && it.description.contains("does not contain the specified associationProperty")
        }
    }


    fun `test should not detect missing associationProperty on custom resolver`() {
        addFile(
            "MyMessage.java", """
            public class MyMessage {
                
            }
        """.trimIndent()
        )
        val file = addFile(
            "MyHandler.java", """
                import test.MyMessage;
                
            public class MyHandler {
                @SagaEventHandler(associationProperty = "some", associationResolver = Object.class)
                public void handle(MyMessage message) {}
            }
        """.trimIndent()
        )

        myFixture.enableInspections(JavaSagaAssociationPropertyInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).noneMatch {
            it.text == "handle" && it.description.contains("does not contain the specified associationProperty")
        }
    }

    fun `test should detect defined property`() {
        addFile(
            "MyMessage.java", """
            public class MyMessage {
                String some
            }
        """.trimIndent()
        )
        val file = addFile(
            "MyHandler.java", """
                import test.MyMessage;
                
            public class MyHandler {
                @SagaEventHandler(associationProperty = "some")
                public void handle(MyMessage message) {}
            }
        """.trimIndent()
        )

        myFixture.enableInspections(JavaSagaAssociationPropertyInspection())
        myFixture.openFileInEditor(file)
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).noneMatch {
            it.text == "handle" && it.description.contains("does not contain the specified associationProperty")
        }
    }
}
