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

package org.axonframework.intellij.ide.plugin.specifics

import org.assertj.core.api.Assertions.assertThat
import org.axonframework.intellij.ide.plugin.AbstractAxonFixtureTestCase
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver

/**
 * Test reference resolving in annotations
 */
class ConstantsInAnnotationTest : AbstractAxonFixtureTestCase() {

    fun `test can handle kotlin to kotlin object constant references`() {
        addFile("myfile.kt", """
            object ProcessingGroupName {
                const val SOME_PROJECTOR = "some-projector"
            }
            
            data class SomeEvent(val data: String)

            @ProcessingGroup(ProcessingGroupName.SOME_PROJECTOR)
            class LevelOneProcessingGroupProcessor {
                @EventHandler
                fun on(event: SomeEvent) {

                }
            }

        """)

        val resolver = project.getService(MessageHandlerResolver::class.java)
        val handlers = resolver.findAllHandlers()
        assertThat(handlers).anyMatch { it.payload == "test.SomeEvent" && it.renderContainerText() == "some-projector" }
    }

    fun `test can handle java to kotlin object constant references`() {
        addFile("myfile.kt", """
            object ProcessingGroupName {
                const val SOME_PROJECTOR = "some-projector"
            }
            
            data class SomeEvent(val data: String)
        """)
        this.addFile("EventHandler.java", """            
            @ProcessingGroup(ProcessingGroupName.SOME_PROJECTOR)
            public class LevelOneProcessingGroupProcessor {
                @EventHandler
                public void on(SomeEvent event) {

                }
            }
        """)

        val resolver = project.getService(MessageHandlerResolver::class.java)
        val handlers = resolver.findAllHandlers()
        // Java can't reach kotlin constant declarations somehow. For now the fallback is to show the text, can be improved in the future
        // Ideally, we would test for "some-projector" here.
        assertThat(handlers).anyMatch { it.payload == "test.SomeEvent" && it.renderContainerText() == "ProcessingGroupName.SOME_PROJECTOR" }
    }

    fun `test can handle kotlin to java object constant references`() {
        this.addFile("ProcessingGroupName.java", """
            public class ProcessingGroupName {
                public static final String SOME_PROJECTOR = "some-projector";
            }
        """)
        addFile("event.kt", "data class SomeEvent(val data: String)")
        addFile("myfile.kt", """
            @ProcessingGroup(ProcessingGroupName.SOME_PROJECTOR)
            class LevelOneProcessingGroupProcessor {
                @EventHandler
                fun on(event: SomeEvent) {

                }
            }
        """)

        val resolver = project.getService(MessageHandlerResolver::class.java)
        val handlers = resolver.findAllHandlers()
        // Will just return the package name because kotlin will return null resolving the value
        // At least it won't crash, but this would be something to improve in the future.
        assertThat(handlers).anyMatch { it.payload == "test.SomeEvent" && it.renderContainerText() == "some-projector" }
    }
}
