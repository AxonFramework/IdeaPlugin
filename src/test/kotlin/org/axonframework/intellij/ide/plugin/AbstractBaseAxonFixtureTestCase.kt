/*
 *  Copyright (c) 2022-2026. Axon Framework
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import org.axonframework.intellij.ide.plugin.api.PsiElementWrapper
import javax.swing.Icon

/**
 * Base class for all Axon Framework tests. Contains shared helper methods for
 * testing line markers.
 *
 * Subclasses should override setUp() to add appropriate Axon version libraries
 * and implement addFile() with version-specific auto-imports.
 */
abstract class AbstractBaseAxonFixtureTestCase : LightJavaCodeInsightFixtureTestCase() {

    data class OptionSummary(val text: String?, val containerText: String?, val icon: Icon?)

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

    fun getLineMarkerOptions(
        clazz: Class<out LineMarkerProvider>
    ): List<OptionSummary> {
        val gutters = myFixture.findGuttersAtCaret()
        val marker = gutters.firstNotNullOf { getHandlerMethodMakerProviders(it, clazz) }
        val items = marker.createGotoRelatedItems()
        return items
            .map { it.element }
            .filterIsInstance<PsiElementWrapper>().map {
                OptionSummary(it.renderText(), it.renderContainerText(), it.getIcon())
            }
    }

    fun areNoLineMarkers(clazz: Class<out LineMarkerProvider>): Boolean {
        val gutters = myFixture.findGuttersAtCaret()
        return gutters.all { getHandlerMethodMakerProviders(it, clazz) == null }
    }

    fun hasLineMarker(clazz: Class<out LineMarkerProvider>): Boolean {
        return !areNoLineMarkers(clazz)
    }
}
