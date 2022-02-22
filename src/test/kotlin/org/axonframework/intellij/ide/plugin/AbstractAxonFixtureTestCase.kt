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

package org.axonframework.intellij.ide.plugin

import com.intellij.codeInsight.daemon.GutterMark
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.impl.LineMarkersPass
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.java.LanguageLevel
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import com.intellij.util.PathUtil
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.intellij.ide.plugin.markers.WrappedGoToRelatedItem
import org.axonframework.modelling.command.AggregateMember
import org.jetbrains.kotlin.utils.addToStdlib.firstNotNullResult
import javax.swing.Icon

/**
 * Mother test class which all Axon tests should inherit from. Does the following:
 * - Adds Axon libraries
 * - Configures the correct Mock JDK
 * - Adds the addFile method which adds a java or kotlin file
 * - The addFile method adds a lot of imports automatically
 * - Adds utility methods to retrieve line markers
 *
 *
 */
abstract class AbstractAxonFixtureTestCase : LightJavaCodeInsightFixtureTestCase() {
    override fun setUp() {
        super.setUp()

        PsiTestUtil.addLibrary(module, PathUtil.getJarPathForClass(ProcessingGroup::class.java))
        PsiTestUtil.addLibrary(module, PathUtil.getJarPathForClass(EventHandler::class.java))
        PsiTestUtil.addLibrary(module, PathUtil.getJarPathForClass(AggregateMember::class.java))
        PsiTestUtil.addLibrary(module, PathUtil.getJarPathForClass(EventSourcingHandler::class.java))

        /* Mock the Instant class, or some tests won't run */
        addFile(
            "Instant.java", """
            public class Instant {
                public static Instant now() {
                    return null;
                }
            }
        """.trimIndent(), "java.time"
        )
    }

    override fun getTestDataPath(): String {
        return "src/test/testData/"
    }

    override fun getProjectDescriptor(): LightProjectDescriptor {
        return object : ProjectDescriptor(LanguageLevel.JDK_11) {
            override fun getSdk(): Sdk {
                return IdeaTestUtil.createMockJdk("java 11", "jdk/mockJDK-11")
            }
        }
    }

    /**
     * Automatically added to source files in testcases, for convenience
     */
    private val autoImports = listOf(
        "org.axonframework.config.ProcessingGroup",
        "org.axonframework.eventhandling.EventHandler",
        "org.axonframework.eventsourcing.EventSourcingHandler",
        "org.axonframework.modelling.command.AggregateLifecycle",
        "org.axonframework.modelling.command.AggregateRoot",
        "org.axonframework.commandhandling.CommandHandler",
        "org.axonframework.commandhandling.RoutingKey",
        "org.axonframework.modelling.command.CommandHandlerInterceptor",
        "org.axonframework.modelling.command.TargetAggregateIdentifier",
        "org.axonframework.modelling.command.AggregateIdentifier",
        "org.axonframework.modelling.command.EntityId",
        "org.axonframework.modelling.command.AggregateMember",
        "org.axonframework.queryhandling.QueryHandler",
        "org.axonframework.modelling.saga.SagaEventHandler",
        "org.axonframework.deadline.annotation.DeadlineHandler",
        "org.axonframework.deadline.DeadlineManager",
        "java.time.Instant",
    )

    /**
     * Adds a file to the project with given name/path and content.
     * You can skipp importing any Axon annotations defined in autoImports, they are added automagically.
     *
     * @see autoImports
     */
    fun addFile(name: String, text: String, pckg: String = "test", open: Boolean = false): VirtualFile {
        val newLineChar = if (name.endsWith(".java")) ";" else ""
        var content = ""
        content += "package $pckg$newLineChar\n\n"
        autoImports.forEach { content += "import $it$newLineChar\n" }
        content += ("\n" + text)
        // Save <caret> position
        val caretPosition = content.indexOf("<caret>")
        content = content.replace("<caret>", "")
        // Add file and optionally open it
        val file = myFixture.addFileToProject(name, content).virtualFile
        if (open) {
            // Move cursor to <caret> position. For some reason, the IDEA test won't do it,
            // while it is documented it should. Just do it ourselves.
            myFixture.openFileInEditor(file)
            if (caretPosition != -1) {
                myFixture.editor.caretModel.moveToOffset(caretPosition)
            }
        }
        return file
    }

    /**
     * Retrieve marker options on the current line. You can set the caret position using `<caret>` in the files.
     * Will throw if no line marker exist, please first check that with `hasLineMarker`
     */
    fun getLineMarkerOptions(
        clazz: Class<out LineMarkerProvider>
    ): List<OptionSummary> {
        val gutters = myFixture.findGuttersAtCaret()
        val marker = gutters.firstNotNullResult { getHandlerMethodMakerProviders(it, clazz) }
            ?: throw IllegalStateException("No gutter found")
        val items = marker.createGotoRelatedItems() as List<WrappedGoToRelatedItem>
        return items.map {
            OptionSummary(it.wrapper.renderText(), it.wrapper.renderContainerText(), it.wrapper.getIcon())
        }
    }

    fun areNoLineMarkers(clazz: Class<out LineMarkerProvider>): Boolean {
        val gutters = myFixture.findGuttersAtCaret()
        return gutters.all { getHandlerMethodMakerProviders(it, clazz) == null }
    }

    fun hasLineMarker(clazz: Class<out LineMarkerProvider>): Boolean {
        val gutters = myFixture.findGuttersAtCaret()
        return gutters.any { getHandlerMethodMakerProviders(it, clazz) != null }
    }

    private fun getHandlerMethodMakerProviders(
        gutter: GutterMark,
        clazz: Class<out LineMarkerProvider>
    ): RelatedItemLineMarkerInfo<*>? {
        val renderer = gutter as? LineMarkerInfo.LineMarkerGutterIconRenderer<*> ?: return null
        val element = renderer.lineMarkerInfo.element!!
        val project = element.project
        val provider = LineMarkersPass.getMarkerProviders(element.containingFile.language, project)
            .filterIsInstance(clazz)
            .firstOrNull() ?: return null

        return provider.getLineMarkerInfo(element) as RelatedItemLineMarkerInfo?
    }

    data class OptionSummary(
        val text: String,
        val containerText: String?,
        val icon: Icon
    )
}
