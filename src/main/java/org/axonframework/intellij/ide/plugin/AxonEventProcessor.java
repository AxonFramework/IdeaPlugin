package org.axonframework.intellij.ide.plugin;

import com.intellij.psi.PsiFile;
import com.intellij.util.Processor;
import org.axonframework.intellij.ide.plugin.handler.EventHandlerRepository;
import org.axonframework.intellij.ide.plugin.publisher.EventPublisherRepository;

public interface AxonEventProcessor extends Processor<PsiFile> {

    EventPublisherRepository getPublisherRepository();

    EventHandlerRepository getHandlerRepository();
}
