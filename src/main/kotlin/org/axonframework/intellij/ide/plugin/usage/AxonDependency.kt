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

package org.axonframework.intellij.ide.plugin.usage

enum class AxonDependency(
    val groupId: String,
    val artifactId: String,
    val checkVersion: Boolean = true
) {
    Core("org.axonframework", "axon-core"), // Axon 2 only
    Integration("org.axonframework", "axon-integration"), // Axon 2 only
    SpringMessaging("org.axonframework", "axon-springmessaging"), // Axon 2 only
    DistributedCommandBus("org.axonframework", "axon-distributed-commandbus"), // Axon 2 only
    Spring("org.axonframework", "axon-spring"), // Axon 3 and 4
    SpringAutoconfigure("org.axonframework", "axon-spring-boot-autoconfigure"), // Axon 3 and 4
    SpringStarter("org.axonframework", "axon-spring-boot-starter"), // Axon 3 and 4
    Messaging("org.axonframework", "axon-messaging"), // Axon 4 only
    EventSourcing("org.axonframework", "axon-eventsourcing"), // Axon 4 only
    Modelling("org.axonframework", "axon-modelling"), // Axon 4 only
    Configuration("org.axonframework", "axon-configuration"), // Axon 4 only
    Test("org.axonframework", "axon-test"), // Axon 2, 3 and 4
    Metrics("org.axonframework", "axon-metrics"), // Axon 3 and 4
    Legacy("org.axonframework", "axon-legacy"), // Axon 3 and 4
    Micrometer("org.axonframework", "axon-micrometer"), // Axon 4 only
    Disruptor("org.axonframework", "axon-disruptor"), // Axon 4 only
    ServerConnector("org.axonframework", "axon-server-connector"), // Axon 4 only

    // Extensions, used for reporting during bugs, not for version check
    Mongo("org.axonframework.extensions.mongo", "axon-mongo", false),
    Amqp("org.axonframework", "axon-amqp", false),
    Jgroups("org.axonframework.extensions.jgroups", "axon-jgroups", false),
    Reactor("org.axonframework.extensions.reactor", "axon-reactor", false),
    Kotlin("org.axonframework.extensions.kotlin", "axon-kotlin", false),
    Kafka("org.axonframework.extensions.kafka", "axon-kafka", false),
    Multitenancy("org.axonframework.extensions.multitenancy", "axon-multitenancy", false),
    SpringCloud("org.axonframework.extensions.springcloud", "axon-springcloud", false),
    Tracing("org.axonframework.extensions.tracing", "axon-tracing", false),
    Cdi("org.axonframework.extensions.cdi", "axon-cdi", false),
    ;

    val moduleName: String
        get() = "$groupId:$artifactId"
}
