package org.axonframework.intellij.ide.plugin.api

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope.allScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import java.util.concurrent.ConcurrentHashMap

enum class MessageHandlerType(
        val messageType: MessageType?,
        val annotationName: String,
) {
    COMMAND(MessageType.COMMAND, "org.axonframework.commandhandling.CommandHandler"),
    EVENT(MessageType.EVENT, "org.axonframework.eventhandling.EventHandler"),
    SAGA(MessageType.EVENT, "org.axonframework.modelling.saga.SagaEventHandler"),
    EVENT_SOURCING(MessageType.EVENT, "org.axonframework.eventsourcing.EventSourcingHandler"),
    QUERY(MessageType.QUERY, "org.axonframework.queryhandling.QueryHandler"),
    COMMAND_INTERCEPTOR(MessageType.COMMAND, "org.axonframework.modelling.command.CommandHandlerInterceptor"),
    ;


    fun findAllRelevantAnnotationClasses(project: Project): List<PsiClass> {
        val ownClass = getClass(project) ?: return emptyList()
        return listOf(ownClass) +
                AnnotatedElementsSearch.searchPsiClasses(ownClass, allScope(project)).findAll()
                        .filter { values().none { type -> type.annotationName == it.qualifiedName } }
    }

    fun displayName(): String {
        return messageType?.displayName ?: "Message"
    }

    private fun getClass(project: Project): PsiClass? {
        return JavaPsiFacade.getInstance(project).findClass(annotationName, allScope(project))
    }

    companion object {
        private val annotationCache: MutableMap<Project, CachedValue<Map<MessageHandlerType, List<PsiClass>>>> = ConcurrentHashMap()

        fun findAnnotationsForProject(project: Project): List<PsiClass> {
            val cache = getCache(project)
            return cache.value.values.flatten()
        }

        fun findAnnotationsGroupedByTypeForProject(project: Project): Map<MessageHandlerType, List<PsiClass>>? {
            val cache = getCache(project)
            return cache.value
        }

        private fun getCache(project: Project) = annotationCache.computeIfAbsent(project) {
            CachedValuesManager.getManager(project).createCachedValue {
                val value = findAnnotations(project)
                CachedValueProvider.Result.create(value, PsiModificationTracker.MODIFICATION_COUNT)
            }
        }

        private fun findAnnotations(project: Project) = values().associateWith { handlerType ->
            handlerType.findAllRelevantAnnotationClasses(project)
        }
    }
}

enum class MessageType(val displayName: String) {
    EVENT("Event"),
    COMMAND("Command"),
    QUERY("Query"),
}
