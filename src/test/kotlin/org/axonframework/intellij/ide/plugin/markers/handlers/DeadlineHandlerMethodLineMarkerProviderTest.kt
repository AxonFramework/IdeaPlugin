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

package org.axonframework.intellij.ide.plugin.markers.handlers

import org.assertj.core.api.Assertions.assertThat
import org.axonframework.intellij.ide.plugin.AbstractAxonFixtureTestCase
import org.axonframework.intellij.ide.plugin.AxonIcons

class DeadlineHandlerMethodLineMarkerProviderTest : AbstractAxonFixtureTestCase() {
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
        assertThat(hasLineMarker(DeadlineHandlerMethodLineMarkerProvider::class.java)).isTrue
        assertThat(getLineMarkerOptions(DeadlineHandlerMethodLineMarkerProvider::class.java)).containsExactly(
            OptionSummary("MyCommand", null, AxonIcons.Publisher)
        )
    }
}
