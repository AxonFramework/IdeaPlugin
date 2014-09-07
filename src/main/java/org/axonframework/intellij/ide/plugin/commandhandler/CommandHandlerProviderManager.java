package org.axonframework.intellij.ide.plugin.commandhandler;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;

public class CommandHandlerProviderManager {
    private final DefaultCommandHandlerRepository repository;
    private final Project project;
    private boolean initialized;
    private CommandHandlerProvider[] eventHandlerProviders;

    public static CommandHandlerProviderManager getInstance(Project project) {
        final CommandHandlerProviderManager manager = ServiceManager.getService(project, CommandHandlerProviderManager.class);
        manager.ensureInitialized();
        return manager;
    }

    public CommandHandlerProviderManager(Project project) {
        this.project = project;
        repository = new DefaultCommandHandlerRepository();
    }

    private synchronized void ensureInitialized() {
        if (!initialized) {
            eventHandlerProviders = Extensions.getExtensions(ExtensionPointName
                                                                       .<CommandHandlerProvider>create(
                                                                               "org.axonframework.intellij.axonplugin.handlerProvider"));
            for (CommandHandlerProvider provider : eventHandlerProviders) {
                provider.scanHandlers(project, GlobalSearchScope.projectScope(project),
                                      new CommandHandlerProvider.Registrar() {
                    @Override
                    public void registerHandler(CommandHandler eventHandler) {
                        repository.registerHandler(eventHandler);
                    }
                });
            }
            initialized = true;
        }
    }

    public CommandHandlerRepository getRepository() {
        return repository;
    }

    public CommandHandler resolveEventHandler(PsiElement psiElement) {
        for (CommandHandlerProvider eventHandlerProvider : eventHandlerProviders) {
            CommandHandler handler = eventHandlerProvider.resolve(psiElement);
            if (handler != null && psiElement.isValid()) {
                repository.registerHandler(handler);
                return handler;
            }
        }
        return null;
    }

}
