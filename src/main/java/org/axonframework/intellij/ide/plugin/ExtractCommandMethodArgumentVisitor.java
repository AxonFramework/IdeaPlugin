package org.axonframework.intellij.ide.plugin;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiType;

/**
 */
public class ExtractCommandMethodArgumentVisitor extends JavaRecursiveElementVisitor {

    private CommandHandler commandHandler;

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
        PsiType[] expressionTypes = expression.getArgumentList().getExpressionTypes();
        String referenceName = expression.getMethodExpression().getReferenceName();
        if ("apply".equals(referenceName)) {
            commandHandler = new CommandHandler(expressionTypes);
        }
        super.visitMethodCallExpression(expression);
    }

    public boolean hasCommandHandler() {
        return commandHandler != null;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }
}
