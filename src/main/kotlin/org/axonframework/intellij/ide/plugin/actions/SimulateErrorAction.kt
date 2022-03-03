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

package org.axonframework.intellij.ide.plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ReadAction
import org.axonframework.intellij.ide.plugin.AxonIcons

/**
 * Action that can be invoked using the "Simulate exception" action.
 * Only enabled on system property `axon.simulate` to true.
 */
class SimulateErrorAction : AnAction(AxonIcons.Axon) {
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = System.getProperty("axon.simulate") == "true"
    }

    override fun actionPerformed(e: AnActionEvent) {
        ReadAction.run<Exception> {
            throw IllegalStateException("This is a test exception!")
        }
    }
}
