package org.axonframework.intellij.ide.plugin.publisher;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;

public class PublisherProviderManager {

    private final EventPublisherRepository repository;
    private final Project project;
    private boolean initialized;
    private EventPublisherProvider[] eventPublisherProviders;

    public static PublisherProviderManager getInstance(Project project) {
        final PublisherProviderManager manager = ServiceManager.getService(project, PublisherProviderManager.class);
        manager.ensureInitialized();
        return manager;
    }

    public PublisherProviderManager(Project project) {
        this.project = project;
        repository = new DefaultEventPublisherRepository();
    }

    private synchronized void ensureInitialized() {
        if (!initialized) {
            eventPublisherProviders = Extensions.getExtensions(ExtensionPointName
                                                                       .<EventPublisherProvider>create(
                                                                               "org.axonframework.intellij.axonplugin.publisherProvider"));
            for (EventPublisherProvider provider : eventPublisherProviders) {
                provider.scanPublishers(project, GlobalSearchScope.projectScope(project),
                                        new EventPublisherProvider.Registrar() {
                                            @Override
                                            public void registerPublisher(EventPublisher eventPublisher) {
                                                repository.registerPublisher(eventPublisher);
                                            }
                                        });
            }
            initialized = true;
        }
    }

    public EventPublisherRepository getRepository() {
        ensureInitialized();
        return repository;
    }

    public EventPublisher resolveEventPublisher(PsiElement psiElement) {
        ensureInitialized();
        for (EventPublisherProvider eventPublisherProvider : eventPublisherProviders) {
            EventPublisher publisher = eventPublisherProvider.resolve(psiElement);
            if (publisher != null) {
                repository.registerPublisher(publisher);
                return publisher;
            }
        }
        return null;
    }
}
