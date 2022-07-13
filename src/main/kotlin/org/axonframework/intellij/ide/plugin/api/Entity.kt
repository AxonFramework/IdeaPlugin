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

package org.axonframework.intellij.ide.plugin.api

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField

/**
 * Represents an Entity in the Aggregate hierarchy. Can be the Aggregate root (top-level)
 * or an aggregate member.
 * Is used by inspections and line markers.
 */
data class Entity(
    val name: String,
    val clazz: PsiClass,
    val parent: String?,
    val routingKey: String?,
    val routingKeyType: String?,
    val members: List<EntityMember>
)

/**
 * Models the child of an Entity with additional information, such as if it's in a collection.
 * Used by inspections to determine warnings.
 */
data class EntityMember(
    val field: PsiField,
    val fieldName: String,
    val member: Entity,
    val isCollection: Boolean,
    val routingKey: String?,
    val eventForwardingMode: String?
)
