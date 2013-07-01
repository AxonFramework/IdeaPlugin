package org.axonframework.ide.intellij.inspection;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.impl.source.PsiJavaCodeReferenceElementImpl;

/**
 */
public class ExtractMethodArgumentVisitor extends JavaRecursiveElementVisitor {

    private PsiElement argument;

    @Override
    public void visitElement(PsiElement element) {
        if (element instanceof PsiJavaCodeReferenceElementImpl) {
            PsiElement resolved = ((PsiJavaCodeReferenceElement) element).resolve();
            if (resolved instanceof PsiClass && !((PsiClass) resolved).getName().contains("EventHandler")) {
                argument = resolved;
            }
        }
        super.visitElement(element);
    }

    public PsiElement getArgument() {
        return argument;
    }

    public boolean hasArgument() {
        return argument != null;
    }
}
