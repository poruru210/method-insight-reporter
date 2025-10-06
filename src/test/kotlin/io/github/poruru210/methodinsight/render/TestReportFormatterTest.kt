/**
 * ファイル: io/github/poruru210/methodinsight/render/TestReportFormatterTest.kt
 * 目的: テストレポートのJSON整形結果が期待通りであることを検証する。
 * 背景: フォーマッタの仕様を自動テストで固定し、変更検知を容易にするため。
 */
package io.github.poruru210.methodinsight.render

import io.github.poruru210.methodinsight.model.MatchType
import io.github.poruru210.methodinsight.model.MethodDescriptor
import io.github.poruru210.methodinsight.model.TestReference
import io.github.poruru210.methodinsight.model.TestReport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * TestReportFormatterのユニットテスト。
 */
class TestReportFormatterTest {
    @Test
    fun `JSON contains entry point and test details`() {
        val entry = MethodDescriptor("com.example.Service", "handle", "(Request)")
        val report = TestReport(
            entryPoint = entry,
            findings = listOf(
                TestReference(
                    className = "com.example.ServiceTest",
                    methodName = "handle_invokesRepository",
                    framework = "JUnit 5",
                    displayName = "Handles basic scenario",
                    matchType = MatchType.DIRECT,
                    sourceCode = "@Test fun handle_invokesRepository() {}",
                    languageId = "kotlin",
                    matchedMethod = entry
                )
            )
        )
        val formatter = TestReportFormatter()
        val json = formatter.toJson(report)

        assertTrue(json.contains("com.example.Service.handle"), "entry method should be present")
        assertTrue(json.contains("JUnit 5"), "framework should be present")
        assertTrue(json.contains("Handles basic scenario"), "display name should be present")
        assertTrue(json.contains("sourceCode"), "source code field should be present")
        assertTrue(json.contains("languageId\":\"kotlin"), "language id should be present")
        assertTrue(json.contains("matchedMethod\":\"com.example.Service.handle(Request)"), "matched method should be present")
        assertEquals('{', json.first(), "JSON should start with '{'")
        assertEquals('}', json.last(), "JSON should end with '}'")
    }
}


