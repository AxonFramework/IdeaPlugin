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

enum class AxonDependency(val moduleName: String, val checkVersion: Boolean = true) {
    Core("axon-core"), // Axon 2 only
    Integration("axon-integration"), // Axon 2 only
    SpringMessaging("axon-springmessaging"), // Axon 2 only
    DistributedCommandBus("axon-distributed-commandbus"), // Axon 2 only
    Spring("axon-spring"), // Axon 3 and 4
    SpringAutoconfigure("axon-spring-boot-autoconfigure"), // Axon 3 and 4
    SpringStarter("axon-spring-boot-starter"), // Axon 3 and 4
    Messaging("axon-messaging"), // Axon 4 only
    EventSourcing("axon-eventsourcing"), // Axon 4 only
    Modelling("axon-modelling"), // Axon 4 only
    Configuration("axon-configuration"), // Axon 4 only
    Test("axon-test"), // Axon 2, 3 and 4
    Metrics("axon-metrics"), // Axon 3 and 4
    Legacy("axon-legacy"), // Axon 3 and 4
    Micrometer("axon-micrometer"), // Axon 4 only
    Disruptor("axon-disruptor"), // Axon 4 only
    ServerConnector("axon-server-connector"), // Axon 4 only

    // Extensions, used for reporting during bugs, not for version check
    Mongo("axon-mongo", false),
    Mongo3("axon-mongo3", false),
    Amqp("axon-amqp", false),
    Jgroups("axon-jgroups", false),
    Reactor("axon-reactor", false),
    Kotlin("axon-kotlin", false),
    Kafka("axon-kafka", false),
    Multitenancy("axon-multitenancy", false),
    SpringCloud("axon-springcloud", false),
    Tracing("axon-tracing", false),
    Cdi("axon-cdi", false),
}
