package org.axonframework.intellij.ide.plugin.handler;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;

public class HandlerProviderManager {
    private final EventHandlerRepository repository;
    private final Project project;
    private boolean initialized;
    private EventHandlerProvider[] eventHandlerProviders;

    public static HandlerProviderManager getInstance(Project project) {
        return ServiceManager.getService(project, HandlerProviderManager.class);
    }


    public HandlerProviderManager(Project project) {
        this.project = project;
        repository = new DefaultEventHandlerRepository();
    }

    private synchronized void ensureInitialized() {
        if (!initialized) {
            eventHandlerProviders = Extensions.getExtensions(ExtensionPointName
                                                                       .<EventHandlerProvider>create(
                                                                               "org.axonframework.intellij.axonplugin.handlerProvider"));
            for (EventHandlerProvider provider : eventHandlerProviders) {
                provider.scanHandlers(project, GlobalSearchScope.projectScope(project),
                                      new EventHandlerProvider.Registrar() {
                    @Override
                    public void registerHandler(EventHandler eventHandler) {
                        repository.registerHandler(eventHandler);
                    }
                });
            }
            initialized = true;
        }
    }

    public EventHandlerRepository getRepository() {
        ensureInitialized();
        return repository;
    }

    public EventHandler resolveEventHandler(PsiElement psiElement) {
        ensureInitialized();
        for (EventHandlerProvider eventHandlerProvider : eventHandlerProviders) {
            EventHandler handler = eventHandlerProvider.resolve(psiElement);
            if (handler != null) {
                repository.registerHandler(handler);
                return handler;
            }
        }
        return null;
    }

}
