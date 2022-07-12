/*
 *  Copyright (c) (2010-2022). Axon Framework
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

import org.axonframework.intellij.ide.plugin.AbstractAxonFixtureTestCase
import org.axonframework.intellij.ide.plugin.util.aggregateResolver

class ExceptionCasesTests : AbstractAxonFixtureTestCase() {
    /**
     * Tests whether the containing code does not cause a ClassCastException in the AggregateResolver
     */
    fun `test handles wildcard type in immediate class type`() {
        addFile(
            "MyAggregate.java", """
            import java.util.List;
            
            @AggregateRoot
            class MyAggregate {
                @AggregateMember
                private List<?> entities;
            }
        """.trimIndent()
        )

        project.aggregateResolver().getEntityMembersByName("text.MyAggregate")
    }
}
