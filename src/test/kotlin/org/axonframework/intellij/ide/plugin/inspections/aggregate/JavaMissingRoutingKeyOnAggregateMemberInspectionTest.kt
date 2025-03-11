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
import org.assertj.core.api.Assertions
import org.axonframework.intellij.ide.plugin.AbstractAxonFixtureTestCase

class JavaMissingRoutingKeyOnAggregateMemberInspectionTest : AbstractAxonFixtureTestCase() {
    fun `test will show problem when key is missing`() {
        addFile(
            "MyCommand.java", """
            class MyCommand {}
        """.trimIndent()
        )

        addFile(
            "MyEntity.java", """
            import test.MyCommand;
            
            class MyEntity {
              @EntityId
              private String myEntityId;
              
              @CommandHandler
              public void handle(MyCommand command) {}
            }
        """.trimIndent(), open = true
        )

        addFile(
            "MyAggregate.java", """
            import test.MyEntity;
            import java.util.List;
            
            @AggregateRoot
            class MyAggregate {
              @AggregateMember
              List<MyEntity> entities;
        """.trimIndent()
        )

        myFixture.enableInspections(JavaMissingRoutingKeyOnAggregateMemberInspection())
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).anyMatch {
            it.text == "handle" && it.description.contains("The payload requires a myEntityId property or getter")
        }
    }

    fun `test will not show problem if has no handler annotation`() {
        addFile(
            "MyCommand.java", """
            class MyCommand {}
        """.trimIndent()
        )

        addFile(
            "MyEntity.java", """
            import test.MyCommand;
            
            class MyEntity {
              @EntityId
              private String myEntityId;
              
              public void handle(MyCommand command) {}
            }
        """.trimIndent(), open = true
        )

        addFile(
            "MyAggregate.java", """
            import test.MyEntity;
            import java.util.List;
            
            @AggregateRoot
            class MyAggregate {
              @AggregateMember
              List<MyEntity> entities;
        """.trimIndent()
        )

        myFixture.enableInspections(JavaMissingRoutingKeyOnAggregateMemberInspection())
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).noneMatch {
            it.text == "handle" && it.description.contains("The payload requires a myEntityId property or getter")
        }
    }

    fun `test will not show problem when key is present`() {
        addFile(
            "MyCommand.java", """
            class MyCommand {
                private String myEntityId;
            }
        """.trimIndent()
        )

        addFile(
            "MyEntity.java", """
            import test.MyCommand;
            
            class MyEntity {
              @EntityId
              private String myEntityId;
              
              @CommandHandler
              public void handle(MyCommand command) {}
            }
        """.trimIndent(), open = true
        )

        addFile(
            "MyAggregate.java", """
            import test.MyEntity;
            import java.util.List;
            
            @AggregateRoot
            class MyAggregate {
              @AggregateMember
              List<MyEntity> entities;
        """.trimIndent()
        )

        myFixture.enableInspections(JavaMissingRoutingKeyOnAggregateMemberInspection())
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).noneMatch {
            it.text == "handle" && it.description.contains("The payload requires a myEntityId property or getter")
        }
    }

    fun `test will not show problem when key is present with getter`() {
        addFile(
            "MyCommand.java", """
            class MyCommand {
                public String getMyEntityId() {
                return ""
                };
            }
        """.trimIndent()
        )

        addFile(
            "MyEntity.java", """
            import test.MyCommand;
            
            class MyEntity {
              @EntityId
              private String myEntityId;
              
              @CommandHandler
              public void handle(MyCommand command) {}
            }
        """.trimIndent(), open = true
        )

        addFile(
            "MyAggregate.java", """
            import test.MyEntity;
            import java.util.List;
            
            @AggregateRoot
            class MyAggregate {
              @AggregateMember
              List<MyEntity> entities;
        """.trimIndent()
        )

        myFixture.enableInspections(JavaMissingRoutingKeyOnAggregateMemberInspection())
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).noneMatch {
            it.text == "handle" && it.description.contains("The payload requires a myEntityId property or getter")
        }
    }

    fun `test will show error when is wrong key in entity id annotation`() {
        addFile(
            "MyCommand.java", """
            class MyCommand {
                private String myEntityId;
            }
        """.trimIndent()
        )

        addFile(
            "MyEntity.java", """
            import test.MyCommand;
            
            class MyEntity {
              @EntityId(routingKey="wrongKey")
              private String myEntityId;
              
              @CommandHandler
              public void handle(MyCommand command) {}
            }
        """.trimIndent(), open = true
        )

        addFile(
            "MyAggregate.java", """
            import test.MyEntity;
            import java.util.List;
            
            @AggregateRoot
            class MyAggregate {
              @AggregateMember
              List<MyEntity> entities;
        """.trimIndent()
        )

        myFixture.enableInspections(JavaMissingRoutingKeyOnAggregateMemberInspection())
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).anyMatch {
            it.text == "handle" && it.description.contains("The payload requires a wrongKey property or getter")
        }
    }

    fun `test will show error when is wrong key in aggregate member annotation`() {
        addFile(
            "MyCommand.java", """
            class MyCommand {
                private String myEntityId;
            }
        """.trimIndent()
        )

        addFile(
            "MyEntity.java", """
            import test.MyCommand;
            
            class MyEntity {
              @EntityId(routingKey="myEntityId")
              private String myEntityId;
              
              @CommandHandler
              public void handle(MyCommand command) {}
            }
        """.trimIndent(), open = true
        )

        addFile(
            "MyAggregate.java", """
            import test.MyEntity;
            import java.util.List;
            
            @AggregateRoot
            class MyAggregate {
              @AggregateMember(routingKey="wrongKey")
              List<MyEntity> entities;
        """.trimIndent()
        )

        myFixture.enableInspections(JavaMissingRoutingKeyOnAggregateMemberInspection())
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).anyMatch {
            it.text == "handle" && it.description.contains("The payload requires a wrongKey property or getter")
        }
    }

    fun `test will not show error when is right key in aggregate member annotation`() {
        addFile(
            "MyCommand.java", """
            class MyCommand {
                private String myEntityId;
            }
        """.trimIndent()
        )

        addFile(
            "MyEntity.java", """
            import test.MyCommand;
            
            class MyEntity {
              @EntityId(routingKey="wrongId")
              private String wrongId;
              
              @CommandHandler
              public void handle(MyCommand command) {}
            }
        """.trimIndent(), open = true
        )

        addFile(
            "MyAggregate.java", """
            import test.MyEntity;
            import java.util.List;
            
            @AggregateRoot
            class MyAggregate {
              @AggregateMember(routingKey="myEntityId")
              List<MyEntity> entities;
        """.trimIndent()
        )

        myFixture.enableInspections(JavaMissingRoutingKeyOnAggregateMemberInspection())
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).noneMatch {
            it.text == "handle" && it.description.contains("The payload requires a myEntityId property or getter")
        }
    }

    fun `test will not show error when routingKey in entityId annotation is correct`() {
        addFile(
            "MyCommand.java", """
            class MyCommand {
                private String myEntityId;
            }
        """.trimIndent()
        )

        addFile(
            "MyEntity.java", """
            import test.MyCommand;
            
            class MyEntity {
              @EntityId(routingKey="myEntityId")
              private String wrongId;
              
              @CommandHandler
              public void handle(MyCommand command) {}
            }
        """.trimIndent(), open = true
        )

        addFile(
            "MyAggregate.java", """
            import test.MyEntity;
            import java.util.List;
            
            @AggregateRoot
            class MyAggregate {
              @AggregateMember
              List<MyEntity> entities;
        """.trimIndent()
        )

        myFixture.enableInspections(JavaMissingRoutingKeyOnAggregateMemberInspection())
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).noneMatch {
            it.text == "handle" && it.description.contains("The payload requires a myEntityId property or getter")
        }
    }

    fun `test will not show problem when key is a getter`() {
        addFile(
            "MyCommand.java", """
            class MyCommand {
                public String getMyEntityId() {
                    return "BLA";
                }
            }
        """.trimIndent()
        )

        addFile(
            "MyEntity.java", """
            import test.MyCommand;
            
            class MyEntity {
              @EntityId
              private String myEntityId;
              
              @CommandHandler
              public void handle(MyCommand command) {}
            }
        """.trimIndent(), open = true
        )

        addFile(
            "MyAggregate.java", """
            import test.MyEntity;
            import java.util.List;
            
            @AggregateRoot
            class MyAggregate {
              @AggregateMember
              List<MyEntity> entities;
        """.trimIndent()
        )

        myFixture.enableInspections(JavaMissingRoutingKeyOnAggregateMemberInspection())
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).noneMatch {
            it.text == "handle" && it.description.contains("The payload requires a myEntityId property or getter")
        }
    }

    fun `test will not show problem when is not in collection`() {
        addFile(
            "MyCommand.java", """
            class MyCommand {
                public String getMyEntityId() {
                    return "BLA";
                }
            }
        """.trimIndent()
        )

        addFile(
            "MyEntity.java", """
            import test.MyCommand;
            
            class MyEntity {
              @EntityId
              private String myEntityId;
              
              @CommandHandler
              public void handle(MyCommand command) {}
            }
        """.trimIndent(), open = true
        )

        addFile(
            "MyAggregate.java", """
            import test.MyEntity;
            import java.util.List;
            
            @AggregateRoot
            class MyAggregate {
              @AggregateMember(routingKey="wrongKey")
              MyEntity entity;
        """.trimIndent()
        )

        myFixture.enableInspections(JavaMissingRoutingKeyOnAggregateMemberInspection())
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).noneMatch {
            it.text == "handle" && it.description.contains("The payload requires a myEntityId property or getter")
        }
    }


    fun `test will not show problem when is event sourcing handler without matching forward mode`() {
        addFile(
            "MyCommand.java", """
            class MyEvent {
            }
        """.trimIndent()
        )

        addFile(
            "MyEntity.java", """
            import test.MyEvent;
            
            class MyEntity {
              @EntityId
              private String myEntityId;
              
              @EventSourcingHandler
              public void handle(MyEvent event) {}
            }
        """.trimIndent(), open = true
        )

        addFile(
            "MyAggregate.java", """
            import test.MyEntity;
            import java.util.List;
            
            @AggregateRoot
            class MyAggregate {
              @AggregateMember
              List<MyEntity> entities;
        """.trimIndent()
        )

        myFixture.enableInspections(JavaMissingRoutingKeyOnAggregateMemberInspection())
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).noneMatch {
            it.text == "handle" && it.description.contains("The payload requires a myEntityId property or getter")
        }
    }


    fun `test will show problem when is event sourcing handler and has ForwardMatchingInstances`() {
        addFile(
            "MyCommand.java", """
            class MyEvent {
            }
        """.trimIndent()
        )

        addFile(
            "MyEntity.java", """
            import test.MyEvent;
            
            class MyEntity {
              @EntityId
              private String myEntityId;
              
              @EventSourcingHandler
              public void handle(MyEvent event) {}
            }
        """.trimIndent(), open = true
        )

        addFile(
            "MyAggregate.java", """
            import test.MyEntity;
            import java.util.List;
            import org.axonframework.modelling.command.ForwardMatchingInstances;
            
            @AggregateRoot
            class MyAggregate {
              @AggregateMember(routingKey="someKey", eventForwardingMode=ForwardMatchingInstances.class)
              List<MyEntity> entities;
        """.trimIndent()
        )

        myFixture.enableInspections(JavaMissingRoutingKeyOnAggregateMemberInspection())
        val highlights = myFixture.doHighlighting(HighlightSeverity.WARNING)
        Assertions.assertThat(highlights).anyMatch {
            it.text == "handle" && it.description.contains("The payload requires a someKey property or getter")
        }
    }
}
