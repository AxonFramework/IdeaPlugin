package org.axonframework.intellij.ide.plugin

import org.assertj.core.api.Assertions.assertThat
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver

/**
 * Test recursive annotations, such as ProcessingGroup and AggregateRoot
 */
class RecursiveAnnotationTest : AbstractAxonFixtureTestCase() {
    fun `test can handle ProcessingGroup annotation recursively - one deep in kotlin`() {
        addKotlinFile("myfile.kt", """            
            @ProcessingGroup("processor-a")
            @Target(AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
            @Retention(AnnotationRetention.RUNTIME)
            annotation class MyProcessingGroup
            
            data class SomeEvent(val data: String)
            
            @MyProcessingGroup
            class LevelOneProcessingGroupProcessor {
                @EventHandler
                fun on(event: SomeEvent) {
                    
                }
            }

        """)
        val handlerResolver = myFixture.project.getService(MessageHandlerResolver::class.java)

        val handlers = handlerResolver.findAllHandlers()
        assertThat(handlers).anyMatch { it.payloadFullyQualifiedName == "test.SomeEvent" && it.renderContainerText() == "processor-a" }
    }

    fun `test can handle ProcessingGroup annotation recursively - four deep in kotlin`() {
        addKotlinFile("myfile.kt", """
            package test
            
            import org.axonframework.config.ProcessingGroup
            import org.axonframework.eventhandling.EventHandler
            
            @ProcessingGroup("processor-b")
            @Target(AnnotationTarget.CLASS)
            @Retention(AnnotationRetention.RUNTIME)
            annotation class MyProcessingGroupA
            
            @MyProcessingGroupA
            @Target(AnnotationTarget.CLASS)
            @Retention(AnnotationRetention.RUNTIME)
            annotation class MyProcessingGroupB
            
            @MyProcessingGroupB
            @Target(AnnotationTarget.CLASS)
            @Retention(AnnotationRetention.RUNTIME)
            annotation class MyProcessingGroupC
            
            @MyProcessingGroupC
            @Target(AnnotationTarget.CLASS)
            @Retention(AnnotationRetention.RUNTIME)
            annotation class MyProcessingGroupD
            
            data class SomeEvent(val data: String)
            
            @MyProcessingGroupD
            class LevelOneProcessingGroupProcessor {
                @EventHandler
                fun on(event: SomeEvent) {
                    
                }
            }

        """)
        val handlerResolver = myFixture.project.getService(MessageHandlerResolver::class.java)

        val handlers = handlerResolver.findAllHandlers()
        assertThat(handlers).anyMatch { it.payloadFullyQualifiedName == "test.SomeEvent" && it.renderContainerText() == "processor-b" }
    }
}
