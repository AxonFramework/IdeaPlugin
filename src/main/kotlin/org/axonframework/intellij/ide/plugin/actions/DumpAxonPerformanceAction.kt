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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.intellij.json.JsonLanguage
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.testFramework.LightVirtualFile
import org.axonframework.intellij.ide.plugin.util.PerformanceRegistry

/**
 * Action that can be invoked using the "Dump Axon Plugin Performance" in IntelliJ.
 * It will open a new window with a JSON file (virtual, not on disk) that can be sent with a bug report.
 *
 * The JSON contains performance metrics based on the PerformanceRegistry
 *
 * Does not contain sensitive data
 *
 * @see org.axonframework.intellij.ide.plugin.util.PerformanceRegistry
 */
class DumpAxonPerformanceAction : AnAction() {
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val dump = ObjectMapper().findAndRegisterModules().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(PerformanceRegistry.getMeasurements())
        FileEditorManager.getInstance(project).openEditor(
                OpenFileDescriptor(project, LightVirtualFile("axon-performance.json", JsonLanguage.INSTANCE, dump)),
                true
        )
    }
}
