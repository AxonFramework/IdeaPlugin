package org.axonframework.intellij.ide.plugin.handler;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;

public class HandlerProviderManager {
    private final HandlerRepository repository;
    private final Project project;
    private boolean initialized;
    private HandlerProvider[] eventHandlerProviders;

    public static HandlerProviderManager getInstance(Project project) {
        final HandlerProviderManager manager = ServiceManager.getService(project, HandlerProviderManager.class);
        manager.ensureInitialized();
        return manager;
    }

    public HandlerProviderManager(Project project) {
        this.project = project;
        repository = new DefaultEventHandlerRepository();
    }

    private synchronized void ensureInitialized() {
        if (!initialized) {
            eventHandlerProviders = Extensions.getExtensions(ExtensionPointName
                                                                       .<HandlerProvider>create(
                                                                               "org.axonframework.intellij.axonplugin.handlerProvider"));
            for (HandlerProvider provider : eventHandlerProviders) {
                provider.scanHandlers(project, GlobalSearchScope.projectScope(project),
                                      new HandlerProvider.Registrar() {
                    @Override
                    public void registerHandler(Handler eventHandler) {
                        repository.registerHandler(eventHandler);
                    }
                });
            }
            initialized = true;
        }
    }

    public HandlerRepository getRepository() {
        return repository;
    }

    public Handler resolveEventHandler(PsiElement psiElement) {
        for (HandlerProvider eventHandlerProvider : eventHandlerProviders) {
            Handler handler = eventHandlerProvider.resolve(psiElement, project);
            if (handler != null && psiElement.isValid()) {
                repository.registerHandler(handler);
                return handler;
            }
        }
        return null;
    }

}
