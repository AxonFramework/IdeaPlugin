package org.axonframework.intellij.ide.plugin;

import com.intellij.psi.PsiFile;
import com.intellij.util.Processor;
import org.axonframework.intellij.ide.plugin.handler.EventHandlerRepository;

public interface AxonEventProcessor extends Processor<PsiFile> {

    EventHandlerRepository getHandlerRepository();
}
