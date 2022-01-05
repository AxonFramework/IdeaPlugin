package org.axonframework.intellij.ide.plugin.publisher;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import jnr.ffi.annotations.In;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class PublisherProviderManager {
    private static final ExtensionPointName<PublisherProvider> PUBLISHER_EP = ExtensionPointName.create("org.axonframework.intellij.axonplugin.publisherProvider");

    private final PublisherRepository repository;
    private final Project project;
    private final PublisherProvider[] eventPublisherProviders;

    public static PublisherProviderManager getInstance(Project project) {
        return ServiceManager.getService(project, PublisherProviderManager.class);
    }


    public PublisherProviderManager(Project project) {
        this.project = project;
        repository = new DefaultEventPublisherRepository();
        eventPublisherProviders = PUBLISHER_EP.getExtensions();
    }

    public synchronized void initialize() {
        for (PublisherProvider provider : eventPublisherProviders) {
            provider.scanPublishers(project, GlobalSearchScope.projectScope(project), repository::registerPublisher);
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
