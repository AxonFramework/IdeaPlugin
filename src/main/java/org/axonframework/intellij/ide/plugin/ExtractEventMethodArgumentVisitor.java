package org.axonframework.intellij.ide.plugin;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiType;

/**
 */
public class ExtractEventMethodArgumentVisitor extends JavaRecursiveElementVisitor {

    private PsiType[] argument = new PsiType[]{};

    @Override
    public void visitParameterList(PsiParameterList list) {
        if (list.getParent().getText().contains("EventHandler")) {
            argument = new PsiType[list.getParametersCount()];
            for (int i = 0; i < list.getParameters().length; i++) {
                PsiParameter psiParameter = list.getParameters()[i];
                argument[i] = psiParameter.getType();
            }
        }
        super.visitParameterList(list);
    }

    public PsiType[] getArguments() {
        return argument;
    }
}
