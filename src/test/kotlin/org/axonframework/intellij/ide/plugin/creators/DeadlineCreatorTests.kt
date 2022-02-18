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
    fun `test can find creator of deadline based on the default DeadlineManager_schedule in kotlin`() {
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
            }
        """.trimIndent()
        )
        myFixture.openFileInEditor(file)
        val creators = project.creatorResolver().resolveAllCreators()
        assertThat(creators).anyMatch {
            it.payload == "my_special_deadline"
        }
    }

    fun `test can find creator of deadline based on the default DeadlineManager_cancelSchedule in kotlin`() {
        val file = addFile(
            "MyAggregate.kt", """ 
            
            class MyCommand
            
            @AggregateRoot
            class MyAggregate {
                @CommandHandler
                fun handle(command: MyCommand, deadlineManager: DeadlineManager) {
                    deadlineManager.cancelSchedule("my_special_deadline", "id")
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

    fun `test can find creator of deadline based on CustomManager_schedule in kotlin`() {
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
                    deadlineManager.scheduleInMyOwnVeryInterestingWay("my_special_deadline")
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

    fun `test can find creator of deadline based on CustomManager_cancel in kotlin`() {
        addFile(
            "MyDeadlineManager.kt", """
            class MyDeadlineManager : DeadlineManager {
                fun cancelInMyOwnVeryInterestingWay(deadlineName: String) {
                    
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
                    deadlineManager.cancelInMyOwnVeryInterestingWay("my_special_deadline")
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

    fun `test can find creator of deadline based on the DeadlineManager_schedule in java`() {
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

    fun `test can find creator of deadline based on the CustomManager_schedule in java`() {
        addFile(
            "CustomManager.java", """
            public class CustomManager implements DeadlineManager {
                public void scheduleInMyOwnInterestingWay(String deadlineName) {
                }
            }
        """.trimIndent()
        )
        val file = addFile(
            "MyAggregate.java", """      
            import test.MyCommand;
            import test.CustomManager;
            
            @AggregateRoot
            public class MyAggregate {
                @CommandHandler
                public void handle(MyCommand command, CustomManager deadlineManager) {
                    deadlineManager.scheduleInMyOwnInterestingWay("my_special_deadline");
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

    fun `test can find creator of deadline based on the DeadlineManager_cancelSchedule in java`() {
        val file = addFile(
            "MyAggregate.java", """      
            import test.MyCommand;
            import java.time.Instant;
            
            @AggregateRoot
            public class MyAggregate {
                @CommandHandler
                public void handle(MyCommand command, DeadlineManager deadlineManager) {
                    deadlineManager.cancelSchedule("my_special_deadline", "id");
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


    fun `test can find creator of deadline based on the CustomManager_cancel in java`() {
        addFile(
            "CustomManager.java", """
            public class CustomManager implements DeadlineManager {
                public void cancelInMyOwnInterestingWay(String deadlineName) {
                }
            }
        """.trimIndent()
        )
        val file = addFile(
            "MyAggregate.java", """      
            import test.MyCommand;
            import test.CustomManager;
            
            @AggregateRoot
            public class MyAggregate {
                @CommandHandler
                public void handle(MyCommand command, CustomManager deadlineManager) {
                    deadlineManager.cancelInMyOwnInterestingWay("my_special_deadline");
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
