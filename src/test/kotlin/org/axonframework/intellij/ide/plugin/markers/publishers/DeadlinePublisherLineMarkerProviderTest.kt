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

package org.axonframework.intellij.ide.plugin.markers.publishers

import org.assertj.core.api.Assertions.assertThat
import org.axonframework.intellij.ide.plugin.AbstractAxonFixtureTestCase
import org.axonframework.intellij.ide.plugin.AxonIcons

class DeadlinePublisherLineMarkerProviderTest : AbstractAxonFixtureTestCase() {
    override fun setUp() {
        super.setUp()
        addFile(
            "MyCommand.kt", """
            data class MyCommand(val id: String)
        """.trimIndent()
        )
    }

    fun `test shows publisher icon when there are no handlers in kotlin`() {
        addFile(
            "MyAggregate.kt", """            
            @AggregateRoot
            class MyAggregate {
                @CommandHandler
                fun handle(command: MyCommand, deadlineManager: DeadlineManager) {
                    deadlineManager.schedule(Instant.now(), "my_special_deadline")<caret>
                }
            }
            
        """.trimIndent(), open = true
        )
        assertThat(hasLineMarker(DeadlinePublisherLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(DeadlinePublisherLineMarkerProvider::class.java)).isEmpty()
    }

    fun `test shows publisher icon when there are no handlers in java`() {
        addFile(
            "MyAggregate.java", """            
            @AggregateRoot
            class MyAggregate {
                @CommandHandler
                public void handle(MyCommand command, DeadlineManager deadlineManager) {
                    deadlineManager.schedule(Instant.now(), "my_special_deadline")<caret>
                }
            }
            
        """.trimIndent(), open = true
        )
        assertThat(hasLineMarker(DeadlinePublisherLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(DeadlinePublisherLineMarkerProvider::class.java)).isEmpty()
    }

    fun `test detects wrapped implementations and shows marker in kotlin`() {
        addFile(
            "MyDeadlineManager.kt", """
            class MyDeadlineManager : DeadlineManager {
                fun scheduleInMyOwnVeryInterestingWay(deadlineName: String) {
                    
                }
            }
        """.trimIndent()
        )
        addFile(
            "MyAggregate.kt", """            
            import test.MyDeadlineManager
            
            @AggregateRoot
            class MyAggregate {
                @CommandHandler
                fun handle(command: MyCommand, deadlineManager: MyDeadlineManager) {
                    deadlineManager.scheduleInMyOwnVeryInterestingWay("my_special_deadline")<caret>
                }
            }
            
        """.trimIndent(), open = true
        )
        assertThat(hasLineMarker(DeadlinePublisherLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(DeadlinePublisherLineMarkerProvider::class.java)).isEmpty()
    }

    fun `test detects wrapped implementations and shows marker in java`() {
        addFile(
            "MyDeadlineManager.java", """
            class MyDeadlineManager implements DeadlineManager {
                public void scheduleInMyOwnVeryInterestingWay(String deadlineName) {
                    
                }
            }
        """.trimIndent()
        )
        addFile(
            "MyAggregate.java", """            
            import test.MyDeadlineManager;
            
            @AggregateRoot
            class MyAggregate {
                @CommandHandler
                public void handle(MyCommand command, MyDeadlineManager deadlineManager) {
                    deadlineManager.scheduleInMyOwnVeryInterestingWay("my_special_deadline")<caret>
                }
            }
            
        """.trimIndent(), open = true
        )
        assertThat(hasLineMarker(DeadlinePublisherLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(DeadlinePublisherLineMarkerProvider::class.java)).isEmpty()
    }


    fun `test shows publisher icon with options when there no handlers in kotlin`() {
        addFile(
            "MyAggregate.kt", """            
            @AggregateRoot
            class MyAggregate {
                @CommandHandler
                fun handle(command: MyCommand, deadlineManager: DeadlineManager) {
                    deadlineManager.schedule(Instant.now(), "my_special_deadline")<caret>
                }
                
                @DeadlineHandler("my_special_deadline")
                fun handle() {
                }
            }
            
        """.trimIndent(), open = true
        )
        assertThat(hasLineMarker(DeadlinePublisherLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(DeadlinePublisherLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("my_special_deadline", null, AxonIcons.DeadlineHandler)
        )
    }

    fun `test shows publisher icon with options when there no handlers in java`() {
        addFile(
            "MyAggregate.java", """            
            @AggregateRoot
            class MyAggregate {
                @CommandHandler
                public void handle(MyCommand command, DeadlineManager deadlineManager) {
                    deadlineManager.schedule(Instant.now(), "my_special_deadline")<caret>
                }
                
                @DeadlineHandler(deadlineName = "my_special_deadline")
                public void handle() {
                
                }
            }
            
        """.trimIndent(), open = true
        )
        assertThat(hasLineMarker(DeadlinePublisherLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(DeadlinePublisherLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("my_special_deadline", null, AxonIcons.DeadlineHandler)
        )
    }
}
