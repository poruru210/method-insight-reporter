/**
 * ファイル: io/github/poruru210/methodinsight/render/MarkdownReportRenderer.kt
 * 目的: Mermaid図とテスト結果をMarkdown形式の最終レポートへまとめる。
 * 背景: IDE内で閲覧するレポートを一貫した構成で出力する必要がある。
 */
package io.github.poruru210.methodinsight.render

import io.github.poruru210.methodinsight.model.MatchType
import io.github.poruru210.methodinsight.model.MethodDescriptor
import io.github.poruru210.methodinsight.model.TestReference
import io.github.poruru210.methodinsight.model.TestReport

/** Markdownレポートを組み立てるレンダラ。 */
class MarkdownReportRenderer {
    fun render(
        entry: MethodDescriptor,
        mermaid: String,
        report: TestReport,
        numbering: MutableMap<MethodDescriptor, Int>
    ): String {
        val builder = StringBuilder()
        builder.appendLine("# Sequence Report: ${entry.displayLabel}")
        builder.appendLine()
        appendOverview(builder, entry, report)
        builder.appendLine("## Sequence Diagram")
        builder.appendLine("---")
        builder.appendLine("```mermaid")
        builder.appendLine(mermaid.trim())
        builder.appendLine("```")
        builder.appendLine()
        builder.appendLine("## Tests")
        builder.appendLine("---")
        val hasSections = appendSections(builder, report.findings, numbering)
        if (!hasSections) {
            builder.appendLine("No matching tests were found.")
        }
        return builder.toString()
    }

    private fun appendOverview(
        builder: StringBuilder,
        entry: MethodDescriptor,
        report: TestReport
    ) {
        // レポート先頭に概況をまとめて読み手が全体像を把握しやすくする
        builder.appendLine("## Overview")
        val frameworks = report.findings
            .map { it.framework }
            .filter { it.isNotBlank() }
            .toSet()
            .sorted()
        val packageName = entry.className.substringBeforeLast('.', entry.className)
        val frameworksText = if (frameworks.isEmpty()) "-" else frameworks.joinToString(", ")
        val totalTests = report.findings.size
        builder.appendLine("- Package: $packageName")
        builder.appendLine("- Entry method: ${entry.displayLabel}")
        builder.appendLine("- Frameworks: $frameworksText")
        builder.appendLine("- Tests: total $totalTests")
        builder.appendLine()
    }

    private fun appendSections(
        builder: StringBuilder,
        findings: List<TestReference>,
        numbering: MutableMap<MethodDescriptor, Int>
    ): Boolean {
        var appended = false
        val grouped = LinkedHashMap<MethodDescriptor, MutableList<TestReference>>()
        numbering.entries.sortedBy { it.value }.forEach { (descriptor, _) ->
            grouped[descriptor] = mutableListOf()
        }
        findings.mapNotNull { it.matchedMethod }
            .filterNot { grouped.containsKey(it) }
            .forEach { grouped[it] = mutableListOf() }
        val unassigned = mutableListOf<TestReference>()

        findings.forEach { ref ->
            val method = ref.matchedMethod
            if (method != null && grouped.containsKey(method)) {
                grouped.getValue(method).add(ref)
            } else {
                unassigned.add(ref)
            }
        }

        grouped.entries
            .sortedBy { numbering[it.key] ?: Int.MAX_VALUE }
            .forEach { (method, tests) ->
                val numberedTests = tests.filter { it.sourceCode != null }
                if (numberedTests.isNotEmpty()) {
                    appendSection(builder, method, numbering, numberedTests)
                    appended = true
                }
            }

        val leftover = unassigned.filter { it.sourceCode != null }
        if (leftover.isNotEmpty()) {
            appendSection(builder, null, numbering, leftover)
            appended = true
        }
        return appended
    }

    private fun appendSection(
        builder: StringBuilder,
        method: MethodDescriptor?,
        numbering: MutableMap<MethodDescriptor, Int>,
        tests: List<TestReference>
    ) {
        val number = method?.let { numbering[it] ?: assignNumber(it, numbering) }
        val simpleLabel = method?.simpleDisplay()
        val header = when {
            simpleLabel != null && number != null -> "## #$number $simpleLabel"
            simpleLabel != null -> "## Call $simpleLabel"
            else -> "## Call (unresolved method)"
        }
        builder.appendLine(header)
        method?.className?.let { builder.appendLine("- Declared in: $it") }
        builder.appendLine()
        tests.forEachIndexed { index, ref ->
            val simpleMethod = ref.methodName
            val simpleClass = ref.className.substringAfterLast('.')
            builder.appendLine("#### ${index + 1}. $simpleClass.$simpleMethod")
            builder.appendLine()
            builder.appendLine("- Display name: ${ref.displayName ?: "-"}")
            builder.appendLine()
            val code = ref.sourceCode ?: return@forEachIndexed
            val language = ref.languageId?.takeIf { it.isNotBlank() } ?: "text"
            builder.appendLine("<details>")
            builder.appendLine("<summary>Show source</summary>")
            builder.appendLine()
            builder.append("```").append(language).appendLine()
            builder.appendLine(code.trim())
            builder.appendLine("```")
            builder.appendLine()
            builder.appendLine("</details>")
            builder.appendLine()
        }
    }

    private fun assignNumber(method: MethodDescriptor, numbering: MutableMap<MethodDescriptor, Int>): Int {
        val next = (numbering.values.maxOrNull() ?: 0) + 1
        numbering[method] = next
        return next
    }

    private fun MethodDescriptor.simpleDisplay(): String {
        val simpleClass = className.substringAfterLast('.')
        return "$simpleClass.$methodName$signature"
    }
}


