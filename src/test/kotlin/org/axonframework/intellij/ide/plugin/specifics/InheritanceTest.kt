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
import org.axonframework.intellij.ide.plugin.util.creatorResolver

class InheritanceTest : AbstractAxonFixtureTestCase() {

    fun `test recognizes creators of message while handler is of generic type`() {
        addFile("myfile.kt", """
            interface MyCommandInterface
            
            data class MyCommand(id: String): MyCommandInterface
            

            @AggregateRoot
            class MyAggregate {
                @CommandHandler
                fun handle(command: MyCommandInterface) {
                    
                }
            }
            
            class CommandDispatcher {
                fun dispatch() {
                     MyCommand("")
                }
            }
        """)

        val creators = project.creatorResolver().getCreatorsForPayload("test.MyCommand")
        assertThat(creators).anyMatch { it.payload == "test.MyCommand" }
    }
}
