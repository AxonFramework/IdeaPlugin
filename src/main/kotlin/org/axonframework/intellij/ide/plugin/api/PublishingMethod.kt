package org.axonframework.intellij.ide.plugin.api

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope

enum class PublishingMethod(
        val containingClass: String,
        val method: String,
        val payloadArgumentName: String,
) {
    LIFECYCLE_APPLY(containingClass = "org.axonframework.modelling.command.AggregateLifecycle", method = "apply", payloadArgumentName = "payload"),
    EVENT_BUS_PUBLISH(containingClass = "org.axonframework.eventhandling.EventBus", method = "publish", payloadArgumentName = "events"),
    COMMAND_BUS_DISPATCH(containingClass = "org.axonframework.commandhandling.CommandBus", method = "dispatch", payloadArgumentName = "command"),
    COMMAND_GATEWAY_SEND(containingClass = "org.axonframework.commandhandling.gateway.CommandGateway", method = "send", payloadArgumentName = "command"),
    COMMAND_GATEWAY_SEND_AND_WAIT(containingClass = "org.axonframework.commandhandling.gateway.CommandGateway", method = "sendAndWait", payloadArgumentName = "command"),
    QUERY_GATEWAY_QUERY(containingClass = "org.axonframework.queryhandling.QueryGateway", method = "query", payloadArgumentName = "query"),
    // TODO:  Add support for scatter/gather and subscription queries
    ;

    fun getMethod(project: Project): PsiMethod? {
        return JavaPsiFacade.getInstance(project).findClass(containingClass, GlobalSearchScope.allScope(project))
                ?.methods
                ?.firstOrNull { m -> m.name == method }
                ?.takeIf { it.hasArgumentWithName(payloadArgumentName) }
    }

    private fun PsiMethod.hasArgumentWithName(argumentName: String): Boolean {
        return parameters?.any { argumentName == it.name } == true
    }
}
