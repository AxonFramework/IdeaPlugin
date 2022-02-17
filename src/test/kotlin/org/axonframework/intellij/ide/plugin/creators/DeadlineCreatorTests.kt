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

package org.axonframework.intellij.ide.plugin.creators

import org.assertj.core.api.Assertions.assertThat
import org.axonframework.intellij.ide.plugin.AbstractAxonFixtureTestCase
import org.axonframework.intellij.ide.plugin.util.creatorResolver

class DeadlineCreatorTests : AbstractAxonFixtureTestCase() {
    fun `test can find creator of deadline based on the default Axon interface in kotlin`() {
        val file = addFile(
            "MyAggregate.kt", """      
            import java.time.Instant
            
            class MyCommand
            
            @AggregateRoot
            class MyAggregate {
                @CommandHandler
                fun handle(command: MyCommand, deadlineManager: DeadlineManager) {
                    deadlineManager.schedule(Instant.now(), "my_special_deadline")
                }
                
                @DeadlineHandler("my_special_deadline")
                fun handle() {
                }
            }
        """.trimIndent()
        )
        myFixture.openFileInEditor(file)
        val creators = project.creatorResolver().resolveAllCreators()
        assertThat(creators).anyMatch {
            it.payload == "my_special_deadline"
        }
    }

    fun `test can find creator of deadline based on custom manager in kotlin`() {
        addFile(
            "MyDeadlineManager.kt", """
            class MyDeadlineManager : DeadlineManager {
                fun scheduleInMyOwnVeryInterestingWay(deadlineName: String) {
                    
                }
            }
        """.trimIndent()
        )
        val file = addFile(
            "MyAggregate.kt", """      
            import test.MyDeadlineManager
            
            class MyCommand
            
            @AggregateRoot
            class MyAggregate {
                @CommandHandler
                fun handle(command: MyCommand, deadlineManager: MyDeadlineManager) {
                    deadlineManager.schedule("my_special_deadline")
                }
                
                @DeadlineHandler(deadlineName = "my_special_deadline")
                fun handle() {
                }
            }
        """.trimIndent()
        )
        myFixture.openFileInEditor(file)
        val creators = project.creatorResolver().resolveAllCreators()
        assertThat(creators).anyMatch {
            it.payload == "my_special_deadline"
        }
    }

    fun `test can find creator of deadline based on the default Axon interface in java`() {
        val file = addFile(
            "MyAggregate.java", """      
            import test.MyCommand;
            import java.time.Instant;
            
            @AggregateRoot
            public class MyAggregate {
                @CommandHandler
                public void handle(MyCommand command, DeadlineManager deadlineManager) {
                    deadlineManager.schedule(Instant.now(), "my_special_deadline");
                }
                
                @DeadlineHandler(deadlineName = "my_special_deadline")
                public void handle() { 
                }
            }
        """.trimIndent()
        )
        addFile(
            "MyCommand.java", """
            class MyCommand {
            }
        """.trimIndent()
        )
        myFixture.openFileInEditor(file)
        val creators = project.creatorResolver().resolveAllCreators()
        assertThat(creators).anyMatch {
            it.payload == "my_special_deadline"
        }
    }
}
