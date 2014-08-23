package org.axonframework.intellij.ide.plugin.annotator;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import org.axonframework.intellij.ide.plugin.handler.EventHandler;
import org.axonframework.intellij.ide.plugin.publisher.EventPublisher;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

class PsiEventHandlerWrapper implements PsiElement, Comparable<PsiEventHandlerWrapper> {
    private final PsiElement elementForAnnotation;
    private final EventHandler eventHandler;
    private final EventPublisher publisher;

    public PsiEventHandlerWrapper(PsiElement elementForAnnotation, EventHandler eventHandler, EventPublisher publisher) {
        this.eventHandler = eventHandler;
        this.elementForAnnotation = elementForAnnotation;
        this.publisher = publisher;
    }

    @NotNull
    @Override
    public Project getProject() throws PsiInvalidElementAccessException {
        return elementForAnnotation.getProject();
    }

    @NotNull
    @Override
    public Language getLanguage() {
        return elementForAnnotation.getLanguage();
    }

    @Override
    public PsiManager getManager() {
        return elementForAnnotation.getManager();
    }

    @NotNull
    @Override
    public PsiElement[] getChildren() {
        return elementForAnnotation.getChildren();
    }

    @Override
    public PsiElement getParent() {
        return elementForAnnotation.getParent();
    }

    @Override
    public PsiElement getFirstChild() {
        return elementForAnnotation.getFirstChild();
    }

    @Override
    public PsiElement getLastChild() {
        return elementForAnnotation.getLastChild();
    }

    @Override
    public PsiElement getNextSibling() {
        return elementForAnnotation.getNextSibling();
    }

    @Override
    public PsiElement getPrevSibling() {
        return elementForAnnotation.getPrevSibling();
    }

    @Override
    public PsiFile getContainingFile() throws PsiInvalidElementAccessException {
        return elementForAnnotation.getContainingFile();
    }

    @Override
    public TextRange getTextRange() {
        return elementForAnnotation.getTextRange();
    }

    @Override
    public int getStartOffsetInParent() {
        return elementForAnnotation.getStartOffsetInParent();
    }

    @Override
    public int getTextLength() {
        return elementForAnnotation.getTextLength();
    }

    @Nullable
    @Override
    public PsiElement findElementAt(int offset) {
        return elementForAnnotation.findElementAt(offset);
    }

    @Nullable
    @Override
    public PsiReference findReferenceAt(int offset) {
        return elementForAnnotation.findReferenceAt(offset);
    }

    @Override
    public int getTextOffset() {
        return elementForAnnotation.getTextOffset();
    }

    @NonNls
    @Override
    public String getText() {
        return elementForAnnotation.getText();
    }

    @NotNull
    @Override
    public char[] textToCharArray() {
        return elementForAnnotation.textToCharArray();
    }

    @Override
    public PsiElement getNavigationElement() {
        return elementForAnnotation.getNavigationElement();
    }

    @Override
    public PsiElement getOriginalElement() {
        return elementForAnnotation.getOriginalElement();
    }

    @Override
    public boolean textMatches(@NotNull @NonNls CharSequence text) {
        return elementForAnnotation.textMatches(text);
    }

    @Override
    public boolean textMatches(@NotNull PsiElement element) {
        return elementForAnnotation.textMatches(element);
    }

    @Override
    public boolean textContains(char c) {
        return elementForAnnotation.textContains(c);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        elementForAnnotation.accept(visitor);
    }

    @Override
    public void acceptChildren(@NotNull PsiElementVisitor visitor) {
        elementForAnnotation.acceptChildren(visitor);
    }

    @Override
    public PsiElement copy() {
        return elementForAnnotation.copy();
    }

    @Override
    public PsiElement add(@NotNull PsiElement element) throws IncorrectOperationException {
        return elementForAnnotation.add(element);
    }

    @Override
    public PsiElement addBefore(@NotNull PsiElement element, @Nullable PsiElement anchor) throws IncorrectOperationException {
        return elementForAnnotation.addBefore(element, anchor);
    }

    @Override
    public PsiElement addAfter(@NotNull PsiElement element, @Nullable PsiElement anchor) throws IncorrectOperationException {
        return elementForAnnotation.addAfter(element, anchor);
    }

    @Override
    public void checkAdd(@NotNull PsiElement element) throws IncorrectOperationException {
    }

    @Override
    public PsiElement addRange(PsiElement first, PsiElement last) throws IncorrectOperationException {
        return elementForAnnotation.addRange(first, last);
    }

    @Override
    public PsiElement addRangeBefore(@NotNull PsiElement first, @NotNull PsiElement last, PsiElement anchor) throws IncorrectOperationException {
        return elementForAnnotation.addRangeBefore(first, last, anchor);
    }

    @Override
    public PsiElement addRangeAfter(PsiElement first, PsiElement last, PsiElement anchor) throws IncorrectOperationException {
        return elementForAnnotation.addRangeAfter(first, last, anchor);
    }

    @Override
    public void delete() throws IncorrectOperationException {
        elementForAnnotation.delete();
    }

    @Override
    public void checkDelete() throws IncorrectOperationException {
    }

    @Override
    public void deleteChildRange(PsiElement first, PsiElement last) throws IncorrectOperationException {
        elementForAnnotation.deleteChildRange(first, last);
    }

    @Override
    public PsiElement replace(@NotNull PsiElement newElement) throws IncorrectOperationException {
        return elementForAnnotation.replace(newElement);
    }

    @Override
    public boolean isValid() {
        return elementForAnnotation.isValid();
    }

    @Override
    public boolean isWritable() {
        return elementForAnnotation.isWritable();
    }

    @Nullable
    @Override
    public PsiReference getReference() {
        return elementForAnnotation.getReference();
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        return elementForAnnotation.getReferences();
    }

    @Nullable
    @Override
    public <T> T getCopyableUserData(Key<T> key) {
        return elementForAnnotation.getCopyableUserData(key);
    }

    @Override
    public <T> void putCopyableUserData(Key<T> key, @Nullable T value) {
        elementForAnnotation.putCopyableUserData(key, value);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, @Nullable PsiElement lastParent, @NotNull PsiElement place) {
        return elementForAnnotation.processDeclarations(processor, state, lastParent, place);
    }

    @Nullable
    @Override
    public PsiElement getContext() {
        return elementForAnnotation.getContext();
    }

    @Override
    public boolean isPhysical() {
        return elementForAnnotation.isPhysical();
    }

    @NotNull
    @Override
    public GlobalSearchScope getResolveScope() {
        return elementForAnnotation.getResolveScope();
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
        return elementForAnnotation.getUseScope();
    }

    @Override
    public ASTNode getNode() {
        return elementForAnnotation.getNode();
    }

    @Override
    public boolean isEquivalentTo(PsiElement another) {
        return elementForAnnotation.isEquivalentTo(another);
    }

    @Override
    public Icon getIcon(@IconFlags int flags) {
        return elementForAnnotation.getIcon(flags);
    }

    @Nullable
    @Override
    public <T> T getUserData(@NotNull Key<T> key) {
        return elementForAnnotation.getUserData(key);
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        elementForAnnotation.putUserData(key, value);
    }

    public EventHandler getEventHandler() {
        return eventHandler;
    }

    public EventPublisher getPublisher() {
        return publisher;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PsiEventHandlerWrapper that = (PsiEventHandlerWrapper) o;

        return elementForAnnotation.equals(that.elementForAnnotation);
    }

    @Override
    public int hashCode() {
        return elementForAnnotation.hashCode();
    }

    @Override
    public String toString() {
        return "PsiEventHandlerWrapper{" +
                "elementForAnnotation=" + elementForAnnotation +
                ", eventHandler=" + eventHandler +
                ", publisher=" + publisher +
                '}';
    }

    @Override
    public int compareTo(@NotNull PsiEventHandlerWrapper psiEventHandlerWrapper) {
        if (psiEventHandlerWrapper.eventHandler.isInternalEvent() == this.eventHandler.isInternalEvent()) {
            return psiEventHandlerWrapper.eventHandler.toString().compareTo(this.eventHandler.toString());
        }
        return -1;
    }
}
