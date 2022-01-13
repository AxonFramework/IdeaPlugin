package org.axonframework.intellij.ide.plugin.api

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope.allScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch

enum class MessageHandlerType(
        val messageType: MessageType?,
        val annotationName: String,
) {
    COMMAND(MessageType.COMMAND, "org.axonframework.commandhandling.CommandHandler"),
    EVENT(MessageType.EVENT, "org.axonframework.eventhandling.EventHandler"),
    EVENT_SOURCING(MessageType.EVENT, "org.axonframework.eventsourcing.EventSourcingHandler"),
    QUERY(MessageType.QUERY, "org.axonframework.queryhandling.QueryHandler"),
    INTERCEPTOR(null, "org.axonframework.messaging.interceptors.MessageHandlerInterceptor"),
    ;

    fun getClass(project: Project): PsiClass? {
        return JavaPsiFacade.getInstance(project).findClass(annotationName, allScope(project))
    }

    fun findAllRelevantAnnotationClasses(project: Project): List<PsiClass> {
        val ownClass = getClass(project) ?: return emptyList()
        return listOf(ownClass) +
                AnnotatedElementsSearch.searchPsiClasses(ownClass, allScope(project)).findAll()
                        .filter { values().none { type -> type.annotationName == it.qualifiedName } }
    }

    fun displayName(): String {
        return messageType?.displayName ?: "Message"
    }
}

enum class MessageType(val displayName: String) {
    EVENT("Event"),
    COMMAND("Command"),
    QUERY("Query"),
    GENERIC("Message"),
}
