package org.axonframework.intellij.ide.plugin.search

import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType

data class HandlerSearchResult(
        val method: PsiMethod,
        val payload: PsiType
)
