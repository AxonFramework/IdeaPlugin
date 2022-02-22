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

package org.axonframework.intellij.ide.plugin.handlers.searchers

import org.assertj.core.api.Assertions
import org.axonframework.intellij.ide.plugin.AbstractAxonFixtureTestCase
import org.axonframework.intellij.ide.plugin.resolving.handlers.types.AggregateConstructor
import org.axonframework.intellij.ide.plugin.util.handlerResolver

class AggregateConstructorSearcherTest : AbstractAxonFixtureTestCase() {

    fun `test can resolve aggregate constructor without command handler annotation with aggregate as payload`() {
        addFile(
            "MyAggregate.kt", """
            data class MyCommand(@TargetAggregateIdentifier id: String)
            
            @AggregateRoot
            class MyAggregate {
                constructor(command: MyCommand) {
                    
                }
            }
        """.trimIndent()
        )
        val handlers = project.handlerResolver().findAllHandlers()
            .filterIsInstance<AggregateConstructor>()
        Assertions.assertThat(handlers).anyMatch {
            it.payload == "test.MyAggregate" && it.element.name == "MyAggregate"
        }
    }

    fun `test will not resolve default constructor`() {
        addFile(
            "MyAggregate.kt", """
            data class MyCommand(@TargetAggregateIdentifier id: String)
            
            @AggregateRoot
            class MyAggregate {
                constructor() {
                    
                }
            }
        """.trimIndent()
        )
        val handlers = project.handlerResolver().findAllHandlers()
            .filterIsInstance<AggregateConstructor>()
        Assertions.assertThat(handlers).isEmpty()
    }
}
