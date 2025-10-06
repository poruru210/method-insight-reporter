/**
 * ファイル: io/github/poruru210/methodinsight/render/MarkdownReportRendererTest.kt
 * 目的: MarkdownReportRendererが所定の構造でレポートを出力するか検証する。
 * 背景: call番号や概要セクションのフォーマットが崩れないよう回帰テストを保持する。
 */
package io.github.poruru210.methodinsight.render

import io.github.poruru210.methodinsight.model.MatchType
import io.github.poruru210.methodinsight.model.MethodDescriptor
import io.github.poruru210.methodinsight.model.TestReference
import io.github.poruru210.methodinsight.model.TestReport
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class MarkdownReportRendererTest {
    @Test
    fun `sections are grouped by call number`() {
        val entry = MethodDescriptor("com.example.Service", "handle", "(Request)")
        val repo = MethodDescriptor("com.example.Repository", "load", "(String)")
        val report = TestReport(
            entryPoint = entry,
            findings = listOf(
                TestReference(
                    className = "com.example.ServiceTest",
                    methodName = "handle_invokesRepository",
                    framework = "JUnit 5",
                    displayName = null,
                    matchType = MatchType.DIRECT,
                    sourceCode = "@Test\nfun handle_invokesRepository() { }",
                    languageId = "kotlin",
                    matchedMethod = entry
                ),
                TestReference(
                    className = "com.example.ServiceTest",
                    methodName = "load_invokesRepository",
                    framework = "JUnit 5",
                    displayName = null,
                    matchType = MatchType.CLOSURE,
                    sourceCode = "@Test\nfun load_invokesRepository() { }",
                    languageId = "kotlin",
                    matchedMethod = repo
                )
            )
        )
        val renderer = MarkdownReportRenderer()
        val numbering = linkedMapOf(repo to 1)
        val markdown = renderer.render(entry, "sequenceDiagram\nparticipant A", report, numbering)

        assertContains(markdown, "## Overview")
        assertContains(markdown, "- Package: com.example")
        assertContains(markdown, "- Frameworks: JUnit 5")
        assertContains(markdown, "- Tests: total 2")
        assertContains(markdown, "## #1 Repository.load(String)")
        assertContains(markdown, "- Declared in: com.example.Repository")
        assertContains(markdown, "#### 1. ServiceTest.load_invokesRepository")
        assertTrue(markdown.contains("<summary>Show source</summary>"))
        assertContains(markdown, "## #2 Service.handle(Request)")
        assertContains(markdown, "- Declared in: com.example.Service")
        assertContains(markdown, "#### 1. ServiceTest.handle_invokesRepository")
        assertFalse(markdown.contains("- Framework:"))
        assertFalse(markdown.contains("- Match type:"))
    }

    @Test
    fun `empty test list shows message`() {
        val entry = MethodDescriptor("com.example.Service", "handle", "(Request)")
        val renderer = MarkdownReportRenderer()
        val markdown = renderer.render(entry, "sequenceDiagram", TestReport(entry, emptyList()), mutableMapOf())

        assertContains(markdown, "## Overview")
        assertContains(markdown, "- Tests: total 0")
        assertContains(markdown, "No matching tests were found.")
    }
}


