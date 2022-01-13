package org.axonframework.intellij.ide.plugin.window

import com.intellij.codeInsight.navigation.NavigationUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.source.PsiClassReferenceType
import org.axonframework.intellij.ide.plugin.api.MessageHandler
import org.axonframework.intellij.ide.plugin.api.MessageType
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.MutableTreeNode

class MessageOverviewToolWindow(val toolWindow: ToolWindow, val project: Project) {
    private lateinit var myContent: JPanel
    private lateinit var scrollPane: JScrollPane
    private lateinit var items: JTree
    private lateinit var refreshButton: JButton

    init {
        refreshButton.addActionListener {
            refreshData()
        }
        items.addTreeSelectionListener {
            val lastPathComponent = it.path?.lastPathComponent
            if (lastPathComponent !is DefaultMutableTreeNode) {
                return@addTreeSelectionListener
            }
            val wrapper = lastPathComponent.userObject as TreeValueWrapper
            NavigationUtil.activateFileWithPsiElement(wrapper.element)
        }
    }

    data class TreeValueWrapper(
            val element: PsiMethod
    ) {
        override fun toString(): String {
            if (element.isConstructor) {
                return element.name
            }
            val parameterListString = element.parameters.joinToString(separator = ", ") { it.type.toString().split(":").last() }
            return "${element.containingClass?.name}.${element.name}($parameterListString)"
        }
    }


    fun refreshData() {
        val searcher = project.getService(MessageHandlerResolver::class.java)
        val handlers = searcher.findAllMessageHandlers()


        val root = items.model.root as DefaultMutableTreeNode
        root.userObject = "Axon Framework"
        root.removeAllChildren()
        root.add(createTypeTree("Commands", MessageType.COMMAND, handlers))
        root.add(createTypeTree("Events", MessageType.EVENT, handlers))
        root.add(createTypeTree("Query", MessageType.QUERY, handlers))
        root.add(createTypeTree("Other", MessageType.GENERIC, handlers))

        SwingUtilities.updateComponentTreeUI(items);

    }

    private fun createTypeTree(name: String, messageType: MessageType, handlers: List<MessageHandler>): MutableTreeNode? {

        val node = DefaultMutableTreeNode(name)
        handlers.asSequence()
                .filter { it.messageType.messageType == messageType }
                .map { it.payloadType }
                .distinct()
                .filterIsInstance<PsiClassReferenceType>()
                .sortedBy { it.toString() }.toList()
                .forEach { type ->
                    val constructor = type.resolve()!!.constructors.getOrNull(0) ?: return@forEach
                    val subNode = DefaultMutableTreeNode(TreeValueWrapper(constructor))
                    handlers
                            .filter { it.payloadType.isAssignableFrom(type) }
                            .forEach { handler ->
                                subNode.add(DefaultMutableTreeNode(TreeValueWrapper(handler.element)))
                            }

                    node.add(subNode)
                }
        return node
    }

    fun getContent(): JPanel {
        return myContent;
    }
}
