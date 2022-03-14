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

package org.axonframework.intellij.ide.plugin.inspections.aggregate


const val aggregateIdStaticDescription =
    """Inspects Java aggregate classes to check whether they have a field that the framework can use to identify it."""

const val aggregateIdDescription =
    """<html>Axon Framework requires a field annotated with @AggregateIdentifier to be able identify the aggregate.
Please add such a field to the aggregate. Also see <a href="https://docs.axoniq.io/reference-guide/axon-framework/axon-framework-commands/modeling/aggregate"> the reference guide</a> for more information.
</html>"""

const val emptyConstructorStaticDescription =
    """Inspects aggregate classes to check whether it has an empty constructor. This is required for Axon Framework.
"""

const val emptyConstructorDescription =
    """<html>Axon Framework requires an empty constructor to construct the aggregate instance before replaying the events on it.
Also see <a href="https://docs.axoniq.io/reference-guide/axon-framework/axon-framework-commands/modeling/aggregate"> the reference guide</a> for more information.
</html>"""

const val missingEntityIdStaticDescription =
    """Inspects aggregate classes to check whether entities have a field or method with an @EntityId annotation. This is required for Axon Framework.
"""

const val missingEntityIdDescription =
    """<html>Axon Framework requires a field or method annotated with @EntityId in aggregate members of collection types to be able to route messages to it.
Also see <a href=\"https://docs.axoniq.io/reference-guide/axon-framework/axon-framework-commands/modeling/multi-entity-aggregates\"> the reference guide</a> for more information.
</html>"""

const val missingRoutingKeyStaticDescription =
    """Inspects Java aggregate classes to check messages targeted at entities have their routingKey in the payload."""

const val missingRoutingKeyDescription =
    """<html>${missingRoutingKeyStaticDescription}</html>"""
