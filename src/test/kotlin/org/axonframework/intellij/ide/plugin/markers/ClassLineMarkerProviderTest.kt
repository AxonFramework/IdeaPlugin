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

import org.assertj.core.api.Assertions
import org.axonframework.intellij.ide.plugin.AbstractAxonFixtureTestCase
import org.axonframework.intellij.ide.plugin.AxonIcons

class ClassLineMarkerProviderTest : AbstractAxonFixtureTestCase() {

    fun `test shows line marker when is used in a command`() {
        addFile(
            "MyAggregate.kt", """
            data class MyCommand(@TargetAggregateIdentifier id: String)<caret>
            
            @AggregateRoot
            class MyAggregate {
                @CommandHandler
                fun handle(command: MyCommand) {
                    
                }
            }
        """.trimIndent(), open = true
        )
        Assertions.assertThat(hasLineMarker(ClassLineMarkerProvider::class.java)).isTrue
        Assertions.assertThat(getLineMarkerOptions(ClassLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("MyCommand", "MyAggregate", AxonIcons.Handler)
        )
    }


    fun `test shows line marker options of publishing location`() {
        addFile(
            "MyAggregate.kt", """
            data class MyCommand(@TargetAggregateIdentifier id: String)<caret>
            
            @AggregateRoot
            class MyAggregate {
                @CommandHandler
                fun handle(command: MyCommand) {
                    
                }
            }
            
            class MyPublisher() {
                fun publish() {
                    MyCommand("")
                }
            }
        """.trimIndent(), open = true
        )
        Assertions.assertThat(hasLineMarker(ClassLineMarkerProvider::class.java)).isTrue
        Assertions.assertThat(getLineMarkerOptions(ClassLineMarkerProvider::class.java)).contains(
            OptionSummary("MyPublisher.publish", null, AxonIcons.Publisher)
        )
    }

    fun `test shows line marker on aggregate of hierarchy`() {
        addFile(
            "MyAggregate.kt", """
            
            class MyAggregateMemberSingle {}
            class MyAggregateMemberList {
                @AggregateMember
                private lateinit var singleMember: MyAggregateMemberSingle
            }
            class MyAggregateMemberMap {}
            
            @AggregateRoot
            class MyAggregate {<caret>
                @AggregateMember
                private lateinit var singleMember: MyAggregateMemberSingle
                
                @AggregateMember
                private lateinit var listMembers: List<MyAggregateMemberList>
                
                @AggregateMember
                private lateinit var mapMembers: List<MyAggregateMemberMap>
            }
        """.trimIndent(), open = true
        )
        Assertions.assertThat(hasLineMarker(ClassLineMarkerProvider::class.java)).isTrue
        Assertions.assertThat(getLineMarkerOptions(ClassLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("MyAggregate", null, AxonIcons.Axon),
            OptionSummary("- MyAggregateMemberSingle", null, AxonIcons.Axon),
            OptionSummary("- MyAggregateMemberList", null, AxonIcons.Axon),
            OptionSummary("-- MyAggregateMemberSingle", null, AxonIcons.Axon),
            OptionSummary("- MyAggregateMemberMap", null, AxonIcons.Axon),
        )
    }

    fun `test shows line marker on child of hierarchy`() {
        addFile(
            "MyAggregate.kt", """
            
            class MyAggregateMemberSingle {}
            class MyAggregateMemberList {}<caret>
            class MyAggregateMemberMap {}
            
            @AggregateRoot
            class MyAggregate {
                @AggregateMember
                private lateinit var singleMember: MyAggregateMemberSingle
                
                @AggregateMember
                private lateinit var listMembers: List<MyAggregateMemberList>
                
                @AggregateMember
                private lateinit var mapMembers: List<MyAggregateMemberMap>
            }
        """.trimIndent(), open = true
        )
        Assertions.assertThat(hasLineMarker(ClassLineMarkerProvider::class.java)).isTrue
        Assertions.assertThat(getLineMarkerOptions(ClassLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("MyAggregate", null, AxonIcons.Axon),
            OptionSummary("- MyAggregateMemberSingle", null, AxonIcons.Axon),
            OptionSummary("- MyAggregateMemberList", null, AxonIcons.Axon),
            OptionSummary("- MyAggregateMemberMap", null, AxonIcons.Axon),
        )
    }
}
