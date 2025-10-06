/**
 * File: io/github/poruru210/methodinsight/actions/SequenceReportAction.kt
 * Purpose: Build the call graph and render a single Markdown report.
 */
package io.github.poruru210.methodinsight.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import io.github.poruru210.methodinsight.analysis.CallGraphBuilder
import io.github.poruru210.methodinsight.analysis.CallGraphResult
import io.github.poruru210.methodinsight.analysis.TestFinder
import io.github.poruru210.methodinsight.config.PluginConfig
import io.github.poruru210.methodinsight.model.MethodDescriptor
import io.github.poruru210.methodinsight.render.MarkdownReportRenderer
import io.github.poruru210.methodinsight.render.MermaidRenderer
import io.github.poruru210.methodinsight.util.Notifier
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElement
import java.nio.charset.StandardCharsets

class SequenceReportAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: run {
            Notifier.error(null, "Project is not available")
            return
        }
        val psiFile = event.getData(CommonDataKeys.PSI_FILE) ?: run {
            Notifier.error(project, "Unable to obtain PSI file")
            return
        }
        val editor = event.getData(CommonDataKeys.EDITOR) ?: run {
            Notifier.error(project, "Unable to obtain editor context")
            return
        }
        val offset = editor.caretModel.offset
        val method = locateMethod(psiFile, offset) ?: run {
            Notifier.error(project, "Place the caret inside a method before running the report")
            return
        }
        val entryPsi = method.javaPsi ?: method.sourcePsi as? PsiMethod ?: run {
            Notifier.error(project, "Unable to resolve the entry PsiMethod")
            return
        }

        val builder = CallGraphBuilder(project)
        val result = builder.build(method)
        val entryDescriptor = findDescriptorFor(entryPsi, result) ?: run {
            Notifier.error(project, "Failed to resolve the call graph entry")
            return
        }

        val numbering = buildNumbering(result)
        val mermaid = MermaidRenderer().render(result.graph, entryDescriptor, numbering)
        val report = TestFinder(project).findTests(entryDescriptor, result)
        val markdown = MarkdownReportRenderer().render(entryDescriptor, mermaid, report, numbering)

        val targetDir = psiFile.virtualFile?.parent ?: run {
            Notifier.error(project, "Unable to determine the output directory")
            return
        }
        val markdownFile = buildMarkdownFileName(entryDescriptor)

        val success = writeFile(project, targetDir, markdownFile, markdown)
        if (success) {
            Notifier.info(project, "Sequence report generated: $markdownFile")
        } else {
            Notifier.error(project, "Failed to write the sequence report")
        }
    }

    private fun locateMethod(file: PsiFile, offset: Int): UMethod? {
        val element = file.findElementAt(offset) ?: return null
        val psiMethod = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java)
        if (psiMethod != null) {
            return psiMethod.toUElement(UMethod::class.java)
        }
        val ktFunction = PsiTreeUtil.getParentOfType(element, KtNamedFunction::class.java)
        return ktFunction?.toUElement(UMethod::class.java)
    }

    private fun findDescriptorFor(entryPsi: PsiMethod, result: CallGraphResult): MethodDescriptor? {
        return result.descriptorIndex.entries.firstOrNull { it.value.isEquivalentTo(entryPsi) }?.key
    }

    private fun buildNumbering(result: CallGraphResult): LinkedHashMap<MethodDescriptor, Int> {
        val numbering = LinkedHashMap<MethodDescriptor, Int>()
        var counter = 1
        result.graph.edges.forEach { edge ->
            if (!numbering.containsKey(edge.to)) {
                numbering[edge.to] = counter++
            }
        }
        return numbering
    }

    private fun buildMarkdownFileName(descriptor: MethodDescriptor): String {
        return "${descriptor.methodName}.${PluginConfig.MARKDOWN_REPORT_SUFFIX}.${PluginConfig.MARKDOWN_REPORT_EXTENSION}"
    }

    private fun writeFile(
        project: com.intellij.openapi.project.Project,
        directory: VirtualFile,
        fileName: String,
        content: String
    ): Boolean {
        return try {
            WriteCommandAction.runWriteCommandAction(project) {
                val file = directory.findChild(fileName) ?: directory.createChildData(this, fileName)
                file.setBinaryContent(content.toByteArray(StandardCharsets.UTF_8))
            }
            true
        } catch (ex: Exception) {
            false
        }
    }
}


