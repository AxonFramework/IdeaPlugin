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

package org.axonframework.intellij.ide.plugin.markers

import org.assertj.core.api.Assertions.assertThat
import org.axonframework.intellij.ide.plugin.AbstractAxonFixtureTestCase
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.markers.handlers.CommonHandlerMethodLineMarkerProvider
import org.axonframework.intellij.ide.plugin.markers.publishers.PublishMethodLineMarkerProvider

/**
 * Tests whether line markers are shown on the appropriate language constructs.
 * Does not test the logic of element resolving deeply, this is the responsibility of other tests
 */
class JavaLineMarkerTest : AbstractAxonFixtureTestCase() {

    fun `test shows handler method marker when there are no creators, and will show marker at class`() {
        addCommand()
        addAggregate(true)

        val options =
            getLineMarkers(CommonHandlerMethodLineMarkerProvider::class.java)
        assertThat(options).isEmpty()
    }

    fun `test shows handler method marker with options when there are creators`() {
        addCommand()
        addProducer()
        addAggregate(true)

        val options =
            getLineMarkers(CommonHandlerMethodLineMarkerProvider::class.java)
        assertThat(options).containsExactly(
            OptionSummary("MyCreator.createCommand", null, AxonIcons.Publisher)
        )
    }

    fun `test shows gutter icon for class of command with correct options`() {
        addCommand(true)
        addProducer()
        addAggregate()

        val options = getLineMarkers(ClassLineMarkerProvider::class.java)
        assertThat(options).containsExactly(
            OptionSummary("MyCommand", "MyAggregate", AxonIcons.Model),
            OptionSummary("MyCreator.createCommand", null, AxonIcons.Publisher)
        )
    }

    fun `test shows gutter icon for creator of command`() {
        addCommand()
        addProducer(true)
        addAggregate()

        val options = getLineMarkers(PublishMethodLineMarkerProvider::class.java)
        assertThat(options).containsExactly(
            OptionSummary("MyCommand", "MyAggregate", AxonIcons.Model),
        )
    }

    private fun addCommand(open: Boolean = false) = this.addFile(
        "MyCommand.java", """
        public class MyCommand {<caret>
            public MyCommand(String id) {
                this.id = id
            }
            @TargetAggregateIdentifier
            private String id;
            
            public String getId() {
                return this.id;
            }
        }
    """.trimIndent(), open = open
    )

    private fun addProducer(open: Boolean = false) = this.addFile(
        "MyCreator.java", """
        import test.MyCommand;
        
        public class MyCreator {
            public void createCommand() {
                send(new MyCommand(""));<caret>
            }
            
            private void send(Object obj) {
            }
        }
    """.trimIndent(), open = open
    )

    private fun addAggregate(open: Boolean = false) = this.addFile(
        "MyAggregate.java", """
        import test.MyCommand;
        
        @AggregateRoot
        public class MyAggregate {
            private MyAggregate() {}

            @TargetAggregateIdentifier
            private String id;

            @CommandHandler<caret>
            public MyAggregate(MyCommand command) {
            }
        }
    """.trimIndent(), open = open
    )
}
