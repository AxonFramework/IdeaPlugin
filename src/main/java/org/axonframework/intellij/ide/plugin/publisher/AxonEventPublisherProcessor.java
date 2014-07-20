package org.axonframework.intellij.ide.plugin.publisher;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import org.axonframework.intellij.ide.plugin.handler.*;

import java.util.Collection;

public class AxonEventPublisherProcessor implements Processor<PsiFile> {

    private final PsiElement psiElement;
    private EventPublisherRepository publisherRepository;
    private EventHandlerRepository handlerRepository;

    public AxonEventPublisherProcessor(PsiElement psiElement, EventPublisherRepository eventPublisherRepository, EventHandlerRepository eventHandlerRepository) {
        this.psiElement = psiElement;
        this.publisherRepository = eventPublisherRepository;
        this.handlerRepository = eventHandlerRepository;
    }

    @Override
    public boolean process(PsiFile psiFile) {
        Collection<PsiMethodCallExpression> psiExpressions = PsiTreeUtil.findChildrenOfType(psiFile.getNode().getPsi(),
                PsiMethodCallExpression.class);
        ExtractEventHandlerArgumentVisitor eventHandlerVisitor = new ExtractEventHandlerArgumentVisitor();
        psiElement.getParent().getParent().accept(eventHandlerVisitor);

        for (PsiExpression psiExpression : psiExpressions) {
            ExtractEventPublisherMethodArgumentVisitor eventPublisherVisitor = new ExtractEventPublisherMethodArgumentVisitor();
            psiExpression.accept(eventPublisherVisitor);

            EventPublisher eventPublisher = eventPublisherVisitor.getEventPublisher();
            EventHandler eventHandler = eventHandlerVisitor.getEventHandler();
            System.out.println("handler: " + eventHandler + " publisher: " + eventPublisher);
            if (eventHandler != null && eventPublisher != null) {

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
