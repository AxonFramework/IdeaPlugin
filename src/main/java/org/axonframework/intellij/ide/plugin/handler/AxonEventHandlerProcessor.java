package org.axonframework.intellij.ide.plugin.handler;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import org.axonframework.intellij.ide.plugin.publisher.EventPublisher;
import org.axonframework.intellij.ide.plugin.publisher.EventPublisherRepository;
import org.axonframework.intellij.ide.plugin.publisher.ExtractEventPublisherMethodArgumentVisitor;

import java.util.Collection;

public class AxonEventHandlerProcessor implements Processor<PsiFile> {

    private final PsiElement psiElement;
    private EventPublisherRepository publisherRepository;
    private EventHandlerRepository handlerRepository;

    public AxonEventHandlerProcessor(PsiElement psiElement, EventPublisherRepository eventPublisherRepository, EventHandlerRepository eventHandlerRepository) {
        this.psiElement = psiElement;
        this.publisherRepository = eventPublisherRepository;
        this.handlerRepository = eventHandlerRepository;
    }

    @Override
    public boolean process(PsiFile psiFile) {
        Collection<PsiAnnotation> parameterList = PsiTreeUtil.findChildrenOfType(psiFile.getNode().getPsi(),
                PsiAnnotation.class);
        ExtractEventPublisherMethodArgumentVisitor eventPublisherVisitor = new ExtractEventPublisherMethodArgumentVisitor();
        psiElement.accept(eventPublisherVisitor);

        for (PsiAnnotation psiAnnotation : parameterList) {
            if (EventHandlerImpl.isEventHandlerAnnotation(psiAnnotation)) {
                ExtractEventHandlerArgumentVisitor eventHandlerVisitor = new ExtractEventHandlerArgumentVisitor();
                psiAnnotation.getParent().getParent().accept(eventHandlerVisitor);

                EventPublisher eventPublisher = eventPublisherVisitor.getEventPublisher();
                EventHandler eventHandler = eventHandlerVisitor.getEventHandler();
                PsiType type = eventHandler.getHandledType();
                if (eventPublisherVisitor.hasEventPublisher() && eventPublisher.canPublishType(type)) {
                    handlerRepository.addHandlerForType(type, eventHandler);
                    publisherRepository.addPublisherForType(type, eventPublisher);
                }
            }
        }
        return true;
    }

    public EventPublisherRepository getPublisherRepository() {
        return publisherRepository;
    }

    public EventHandlerRepository getHandlerRepository() {
        return handlerRepository;
    }


}
