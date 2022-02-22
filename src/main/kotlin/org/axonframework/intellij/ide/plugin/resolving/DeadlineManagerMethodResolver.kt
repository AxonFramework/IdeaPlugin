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

package org.axonframework.intellij.ide.plugin.resolving

import com.intellij.ProjectTopics
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootEvent
import com.intellij.openapi.roots.ModuleRootListener
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import org.axonframework.intellij.ide.plugin.util.PerformanceRegistry
import org.axonframework.intellij.ide.plugin.util.allScope
import org.axonframework.intellij.ide.plugin.util.axonScope
import org.axonframework.intellij.ide.plugin.util.createCachedValue
import org.axonframework.intellij.ide.plugin.util.javaFacade


/**
 * Responsible for managing (and caching) information regarding Deadline methods.
 *
 * The thing with deadlines is that the user can create their own implementation. For example, in our own projects
 * we created a deadline manager with a scheduleCronjob function, and the signature does not match that of Axon's
 * default.
 * This method resolves all allowed possible method calls. For it to take class into account:
 * - It has to be an implementor of the `DeadlineManager` interface
 * - the name has to start with `schedule` or `cancel`
 * - There is a String parameter
 * The first String parameter is registered as the deadline name.
 *
 * Methods that cancel deadlines are also seen as message creators, to link all related actions to the deadline handler.
 *
 * We differentiate between managers found in libraries, which are scanned once every library change.
 * We do this by listening the PROJECT_ROOTS topic of IntelliJ.
 *
 * Note that the scan mechanism for libraries and project files is the same, but only differs in scope. This makes
 * a great impact in performance (I measured 7 ms with axonScope(), 170ms with allScope()).
 *
 * Every PSI change we scan for deadline manager implementations in the project scope. This way we can support
 * all use cases.
 */
class DeadlineManagerMethodResolver(val project: Project) {
    private val libraryCache = LibraryDeadlineCache()
    private val deadlineManagerClass = project.createCachedValue {
        project.javaFacade().findClass("org.axonframework.deadline.DeadlineManager", project.allScope())
    }
    private val deadlineScheduleCache = project.createCachedValue {
        PerformanceRegistry.measure("DeadlineManagerResolver.computeSchedule") { computeDeadlineScheduleMethods() }
    }
    private val cancelCache = project.createCachedValue {
        PerformanceRegistry.measure("DeadlineManagerResolver.computeCancel") { computeDeadlineCancelMethods() }
    }

    fun getAllReferencedMethods(): List<PsiMethod> = deadlineScheduleCache.value + cancelCache.value
    fun getAllScheduleMethods(): List<PsiMethod> = deadlineScheduleCache.value
    fun getAllCancelMethods(): List<PsiMethod> = cancelCache.value

    /**
     * Computes the actual annotations, based on the ones found in libraries.
     *
     * @return the annotations in the library cache and any specific in the current source code (axonScope)
     */
    private fun computeDeadlineScheduleMethods(): List<PsiMethod> {
        val libAnnotations = libraryCache.getScheduleMethods()
        return (libAnnotations + scanForSchedulers(project.axonScope()))
            .distinct()
    }

    private fun computeDeadlineCancelMethods(): List<PsiMethod> {
        val libAnnotations = libraryCache.getCancelMethods()
        return (libAnnotations + scanForCancelMethods(project.axonScope()))
            .distinct()
    }

    private fun scanForSchedulers(scope: GlobalSearchScope): List<PsiMethod> = scanForMethodsStartingWith(scope, "schedule")
    private fun scanForCancelMethods(scope: GlobalSearchScope): List<PsiMethod> = scanForMethodsStartingWith(scope, "cancel")

    private fun scanForMethodsStartingWith(scope: GlobalSearchScope, text: String): List<PsiMethod> {
        val deadlineManager = deadlineManagerClass.value ?: return emptyList()
        val inheritors = ClassInheritorsSearch.search(deadlineManager, scope, true)
        return (listOf(deadlineManager) + inheritors)
            .flatMap { it.methods.toList() }
            .filter { it.name.startsWith(text, ignoreCase = true) }
            .filter { getDeadlineParameterIndex(it) != null }

    }

    fun getDeadlineParameterIndex(method: PsiMethod): Int? {
        val matchingParam = method.parameterList.parameters
            .firstOrNull { p -> p.type.canonicalText == "java.lang.String" }
            ?: return null
        return method.parameterList.getParameterIndex(matchingParam)
    }

    /**
     * Private cache for library annotations, following the same lifecycle as the AnnotationResolver itself.
     * Encapsulates inner workings such as the invalidation of the cache based on the message bus.
     */
    private inner class LibraryDeadlineCache {
        // Set to false if libraries are updated
        private var libraryInitialized: Boolean = false
        private var scheduleMethods: List<PsiMethod> = listOf()
        private var cancelMethods: List<PsiMethod> = listOf()

        init {
            // Listen to root changes, and invalidate library scanning
            project.messageBus.connect().subscribe(ProjectTopics.PROJECT_ROOTS, object : ModuleRootListener {
                override fun rootsChanged(event: ModuleRootEvent) {
                    libraryInitialized = false
                }
            })
        }


        /**
         * Get all annotations in the library cache. If the cache is out-of-date, executes a scan.
         */
        fun getScheduleMethods(): List<PsiMethod> = retrieveWithInitialization { scheduleMethods }
        fun getCancelMethods(): List<PsiMethod> = retrieveWithInitialization { cancelMethods }

        private fun <T> retrieveWithInitialization(block: () -> T): T {
            if (!libraryInitialized) {
                updateLibraryAnnotations()
            }
            return block.invoke()
        }

        private fun updateLibraryAnnotations() =
            PerformanceRegistry.measure("DeadlineManagerResolver.libraryManagers") {
                scheduleMethods = scanForSchedulers(project.allScope())
                cancelMethods = scanForCancelMethods(project.allScope())
                libraryInitialized = true
            }
    }
}
