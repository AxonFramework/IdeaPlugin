package org.axonframework.intellij.ide.plugin

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiElement


private val AxonIconIn = IconLoader.getIcon("/icons/axon_into.png", AxonLineMarkerProvider::class.java) // 16x16
private val AxonIconOut = IconLoader.getIcon("/icons/axon_publish.png", AxonLineMarkerProvider::class.java) // 16x16

class AxonLineMarkerProvider: LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        TODO("Not yet implemented")
    }
}
