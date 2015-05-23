package org.axonframework.intellij.ide.plugin.publisher;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;

public class PublisherProviderManager {

    private final PublisherRepository repository;
    private final Project project;
    private boolean initialized;
    private PublisherProvider[] eventPublisherProviders;

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
                                                                       .<PublisherProvider>create(
                                                                               "org.axonframework.intellij.axonplugin.publisherProvider"));
            for (PublisherProvider provider : eventPublisherProviders) {
                provider.scanPublishers(project, GlobalSearchScope.projectScope(project),
                                        new PublisherProvider.Registrar() {
                                            @Override
                                            public void registerPublisher(Publisher eventPublisher) {
                                                repository.registerPublisher(eventPublisher);
                                            }
                                        });
            }
            initialized = true;
        }
    }

    public PublisherRepository getRepository() {
        return repository;
    }

    public Publisher resolveEventPublisher(PsiElement psiElement) {
        for (PublisherProvider eventPublisherProvider : eventPublisherProviders) {
            Publisher publisher = eventPublisherProvider.resolve(psiElement);
            if (publisher != null && psiElement.isValid()) {
                repository.registerPublisher(publisher);
                return publisher;
            }
        }
        return null;
    }
}
