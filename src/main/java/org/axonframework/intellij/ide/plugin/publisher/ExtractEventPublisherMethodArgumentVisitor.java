package org.axonframework.intellij.ide.plugin.publisher;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiType;

/**
 */
public class ExtractEventPublisherMethodArgumentVisitor extends JavaRecursiveElementVisitor {

    private EventPublisher eventPublisher;

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
        PsiType[] expressionTypes = expression.getArgumentList().getExpressionTypes();
        String referenceName = expression.getMethodExpression().getReferenceName();
        if ("apply".equals(referenceName)) {
            eventPublisher = new EventPublisherImpl(expressionTypes[0], expression);
        }
        super.visitMethodCallExpression(expression);
    }

    public boolean hasEventPublisher() {
        return eventPublisher != null;
    }

    public EventPublisher getEventPublisher() {
        return eventPublisher;
    }
}
