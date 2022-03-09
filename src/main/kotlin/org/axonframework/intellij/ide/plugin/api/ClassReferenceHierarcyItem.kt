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

package org.axonframework.intellij.ide.plugin.api

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import org.axonframework.intellij.ide.plugin.AxonIcons
import javax.swing.Icon

/**
 * Represents a class, which can also be in a hierarchy. If it has a parent that references it, supply a PsiField.
 * The depth determines how many hyphens are shown in front of the name
 */
class ClassReferenceHierarcyItem(
    private val clazz: PsiClass,
    private val field: PsiField?,
    override val element: PsiElement = field ?: clazz,
    val depth: Int
) : PsiElementWrapper {

    override fun getIcon(): Icon {
        return AxonIcons.Axon
    }

    override fun renderText(): String {
        if (depth == 0) {
            return clazz.name!!
        }
        return 1.rangeTo(depth).joinToString(separator = "") { "-" } + " ${clazz.name}"
    }

    override fun getSortKey(): String {
        return "" // Disable sort to keep the list stable
    }
}
