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

package org.axonframework.intellij.ide.plugin.markers.handlers

import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement

class ValidatingLazyValue<T : Any>(
    private val element: PsiElement,
    private val supplier: (PsiElement) -> List<T>,
): NotNullLazyValue<List<T>>() {
    override fun compute(): List<T> {
        if(element.isValid) {
            return supplier.invoke(element)
        }
        return emptyList()
    }
}