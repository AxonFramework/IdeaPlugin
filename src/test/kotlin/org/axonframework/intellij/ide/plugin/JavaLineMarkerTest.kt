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

package org.axonframework.intellij.ide.plugin

import org.assertj.core.api.Assertions.assertThat
import org.axonframework.intellij.ide.plugin.markers.ClassLineMarkerProvider
import org.axonframework.intellij.ide.plugin.markers.HandlerMethodLineMarkerProvider
import org.axonframework.intellij.ide.plugin.markers.PublishMethodLineMarkerProvider

/**
 * Tests whether line markers are shown on the appropriate language constructs.
 * Does not test the logic of element resolving deeply, this is the responsibility of other tests
 */
class JavaLineMarkerTest : AbstractAxonFixtureTestCase() {

    fun `test shows handler method marker when there are no creators, and will show marker at class`() {
        addCommand()
        val aggregate = addAggregate()
        myFixture.openFileInEditor(aggregate)

        val options = getOptionsGivenByMarkerProviderAtCaretPosition(10, HandlerMethodLineMarkerProvider::class.java)
        assertThat(options).isEmpty()
    }

    fun `test shows handler method marker with options when there are creators`() {
        addCommand()
        addProducer()
        val aggregate = addAggregate()
        myFixture.openFileInEditor(aggregate)

        val options = getOptionsGivenByMarkerProviderAtCaretPosition(10, HandlerMethodLineMarkerProvider::class.java)
        assertThat(options).containsExactly(
                OptionSummary("MyCreator.createCommand", null, AxonIcons.Publisher)
        )
    }

    fun `test shows gutter icon for class of command with correct options`() {
        val command = addCommand()
        addProducer()
        addAggregate()
        myFixture.openFileInEditor(command)

        val options = getOptionsGivenByMarkerProviderAtCaretPosition(1, ClassLineMarkerProvider::class.java)
        assertThat(options).containsExactly(
                OptionSummary("MyCommand", "MyAggregate", AxonIcons.Model),
                OptionSummary("MyCreator.createCommand", null, AxonIcons.Publisher)
        )
    }

    fun `test shows gutter icon for creator of command`() {
        addCommand()
        val producer = addProducer()
        addAggregate()
        myFixture.openFileInEditor(producer)

        val options = getOptionsGivenByMarkerProviderAtCaretPosition(5, PublishMethodLineMarkerProvider::class.java)
        assertThat(options).containsExactly(
                OptionSummary("MyCommand", "MyAggregate", AxonIcons.Model),
        )
    }

    private fun addCommand() = addJavaFile("MyCommand.java", """
            public class MyCommand {
                public MyCommand(String id) {
                    this.id = id
                }
                @TargetAggregateIdentifier
                private String id;
                
                public String getId() {
                    return this.id;
                }
            }
        """.trimIndent())

    private fun addProducer() = addJavaFile("MyCreator.java", """
            import test.MyCommand;
            
            public class MyCreator {
                public void createCommand() {
                    send(new MyCommand(""));
                }
                
                private void send(Object obj) {
                }
            }
        """.trimIndent())

    private fun addAggregate() = addJavaFile("MyAggregate.java", """
            import test.MyCommand;
            
            @AggregateRoot
            public class MyAggregate {
                private MyAggregate() {}

                @TargetAggregateIdentifier
                private String id;

                @CommandHandler
                public MyAggregate(MyCommand command) {
                }
            }
        """.trimIndent())
}
