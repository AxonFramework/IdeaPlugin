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

package org.axonframework.intellij.ide.plugin.deadline

import org.assertj.core.api.Assertions.assertThat
import org.axonframework.intellij.ide.plugin.AbstractAxonFixtureTestCase
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.markers.handlers.DeadlineHandlerMethodLineMarkerProvider
import org.axonframework.intellij.ide.plugin.markers.publishers.DeadlinePublisherLineMarkerProvider

class DeadlineLineMarkerTests : AbstractAxonFixtureTestCase() {
    fun `test creates correct handler line marker`() {
        addFile(
            "MyAggregate.kt", """      
            import java.time.Instant
            
            class MyCommand
            
            @AggregateRoot
            class MyAggregate {
                @CommandHandler
                fun handle(command: MyCommand, deadlineManager: DeadlineManager) {
                    deadlineManager.schedule(Instant.now(), "my_special_deadline")
                }
                
                @DeadlineHandler<caret>("my_special_deadline")
                fun handle() {
                }
            }1
        """.trimIndent(), open = true
        )
        val options = getLineMarkers(DeadlineHandlerMethodLineMarkerProvider::class.java)
        assertThat(options).anyMatch {
            it.text == "MyCommand" && it.icon == AxonIcons.Model
        }
    }

    fun `test creates correct publisher line marker`() {
        addFile(
            "MyAggregate.kt", """      
            import java.time.Instant
            
            class MyCommand
            
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
        val optionsForPublisher = getLineMarkers(DeadlinePublisherLineMarkerProvider::class.java)
        assertThat(optionsForPublisher).anyMatch {
            it.text == "my_special_deadline" && it.icon == AxonIcons.DeadlineHandler
        }
    }
}
