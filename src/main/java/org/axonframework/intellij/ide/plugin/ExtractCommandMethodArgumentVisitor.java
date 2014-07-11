package org.axonframework.intellij.ide.plugin;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiType;

/**
 */
public class ExtractCommandMethodArgumentVisitor extends JavaRecursiveElementVisitor {

    private EventPublisher eventPublisher;

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
        PsiType[] expressionTypes = expression.getArgumentList().getExpressionTypes();
        String referenceName = expression.getMethodExpression().getReferenceName();
        if ("apply".equals(referenceName)) {
            eventPublisher = new EventPublisher(expressionTypes, expression);
        }
        super.visitMethodCallExpression(expression);
    }

    public boolean hasCommandHandler() {
        return eventPublisher != null;
    }

    public EventPublisher getEventPublisher() {
        return eventPublisher;
    }
}
