/**
 * File: io/github/poruru210/methodinsight/analysis/CallGraphBuilder.kt
 * Purpose: Build a call graph starting from a UMethod using the UAST API.
 */
package io.github.poruru210.methodinsight.analysis

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiMethod
import io.github.poruru210.methodinsight.config.PluginConfig
import io.github.poruru210.methodinsight.model.CallEdge
import io.github.poruru210.methodinsight.model.CallGraph
import io.github.poruru210.methodinsight.model.MethodDescriptor
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElement
import org.jetbrains.uast.visitor.AbstractUastVisitor

/**
 * Traverses UAST to construct a call graph that can be reused by other components.
 */
class CallGraphBuilder(
    private val project: Project,
    private val maxDepth: Int = PluginConfig.DEFAULT_MAX_DEPTH
) {
    /**
     * Builds the call graph starting from the provided entry UMethod.
     */
    fun build(entry: UMethod): CallGraphResult {
        val graph = CallGraph()
        val descriptorIndex = mutableMapOf<MethodDescriptor, PsiMethod>()
        val entryPsi = entry.javaPsi ?: entry.sourcePsi as? PsiMethod ?: return CallGraphResult(graph, descriptorIndex)
        val visited = mutableSetOf<String>()
        traverse(entry, entryPsi, 0, visited, graph, descriptorIndex)
        return CallGraphResult(graph, descriptorIndex)
    }

    private fun traverse(
        uMethod: UMethod,
        psiMethod: PsiMethod,
        depth: Int,
        visited: MutableSet<String>,
        graph: CallGraph,
        descriptorIndex: MutableMap<MethodDescriptor, PsiMethod>
    ) {
        if (depth > maxDepth) return
        val descriptor = psiMethod.toDescriptor() ?: return
        descriptorIndex.putIfAbsent(descriptor, psiMethod)
        if (!visited.add(descriptor.displayLabel)) return

        val body = uMethod.uastBody ?: return
        body.accept(object : AbstractUastVisitor() {
            override fun visitCallExpression(node: UCallExpression): Boolean {
                handleCall(node, descriptor, depth, visited, graph, descriptorIndex)
                return super.visitCallExpression(node)
            }
        })
    }

    private fun handleCall(
        call: UCallExpression,
        fromDescriptor: MethodDescriptor,
        depth: Int,
        visited: MutableSet<String>,
        graph: CallGraph,
        descriptorIndex: MutableMap<MethodDescriptor, PsiMethod>
    ) {
        val targetPsi = call.resolve() as? PsiMethod ?: return
        if (!shouldInclude(targetPsi)) return
        val targetDescriptor = targetPsi.toDescriptor() ?: return
        descriptorIndex.putIfAbsent(targetDescriptor, targetPsi)
        val callText = describeCall(call, targetDescriptor)
        graph.addEdge(CallEdge(fromDescriptor, targetDescriptor, callText))

        if (depth + 1 > maxDepth) return
        val targetUMethod = targetPsi.toUElement(UMethod::class.java) ?: return
        traverse(targetUMethod, targetPsi, depth + 1, visited, graph, descriptorIndex)
    }

    private fun PsiMethod.toDescriptor(): MethodDescriptor? {
        val containingClass = containingClass ?: return null
        val className = containingClass.qualifiedName ?: containingClass.name ?: return null
        val signature = parameterList.parameters.joinToString(prefix = "(", postfix = ")") { param ->
            param.type.canonicalText.substringAfterLast('.')
        }
        return MethodDescriptor(className, name, signature)
    }

    private fun describeCall(call: UCallExpression, targetDescriptor: MethodDescriptor): String {
        val arguments = call.valueArguments.joinToString(prefix = "(", postfix = ")") { arg ->
            arg.sourcePsi?.text ?: "?"
        }
        return "${targetDescriptor.methodName}$arguments"
    }

    private fun shouldInclude(method: PsiMethod): Boolean {
        val virtualFile: VirtualFile = method.containingFile?.virtualFile ?: return false
        val index = ProjectFileIndex.getInstance(project)
        return index.isInContent(virtualFile)
    }
}


