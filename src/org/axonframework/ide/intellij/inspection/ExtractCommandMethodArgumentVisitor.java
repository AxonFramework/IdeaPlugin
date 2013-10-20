package org.axonframework.ide.intellij.inspection;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiType;

/**
 */
public class ExtractCommandMethodArgumentVisitor extends JavaRecursiveElementVisitor {

    private PsiType[] arguments;

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
        PsiType[] expressionTypes = expression.getArgumentList().getExpressionTypes();
        String referenceName = expression.getMethodExpression().getReferenceName();
        if ("apply".equals(referenceName)) {
            arguments = expressionTypes;
        }
        super.visitMethodCallExpression(expression);
    }

    public boolean commandCanHandleArguments(PsiType[] eventHandlerArgument) {
        if (arguments == null || eventHandlerArgument == null || eventHandlerArgument.length != arguments.length) {
            return false;
        }
        for (int i = 0; i < eventHandlerArgument.length; i++) {
            if(eventHandlerArgument[i] == null || arguments[i] == null ||
                    !eventHandlerArgument[i].isAssignableFrom(arguments[i])) {
                return false;
            }
        }
        return true;
    }

}
