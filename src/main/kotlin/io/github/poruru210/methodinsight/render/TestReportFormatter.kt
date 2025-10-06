/**
 * ファイル: io/github/poruru210/methodinsight/render/TestReportFormatter.kt
 * 目的: テスト探索結果をJSON文字列へ安全に整形する。
 * 背景: 追加依存なしで出力形式を統一し、単体テストで検証しやすくするため。
 */
package io.github.poruru210.methodinsight.render

import io.github.poruru210.methodinsight.model.TestReference
import io.github.poruru210.methodinsight.model.TestReport

/**
 * シンプルなJSON整形器。必要最小限のエスケープのみを実装する。
 */
class TestReportFormatter {
    fun toJson(report: TestReport): String {
        val builder = StringBuilder()
        builder.append('{')
        builder.append("\"entryPoint\":")
        builder.append('"').append(escape(report.entryPoint.displayLabel)).append('"')
        builder.append(',')
        builder.append("\"findings\":[")
        report.findings.forEachIndexed { index, ref ->
            if (index > 0) builder.append(',')
            appendFinding(builder, ref)
        }
        builder.append(']')
        builder.append('}')
        return builder.toString()
    }

    private fun appendFinding(builder: StringBuilder, ref: TestReference) {
        builder.append('{')
        builder.append("\"className\":\"").append(escape(ref.className)).append('"')
        builder.append(',')
        builder.append("\"methodName\":\"").append(escape(ref.methodName)).append('"')
        builder.append(',')
        builder.append("\"framework\":\"").append(escape(ref.framework)).append('"')
        builder.append(',')
        builder.append("\"displayName\":")
        if (ref.displayName != null) {
            builder.append('"').append(escape(ref.displayName)).append('"')
        } else {
            builder.append("null")
        }
        builder.append(',')
        builder.append("\"matchType\":\"").append(ref.matchType.name).append('"')
        builder.append(',')
        builder.append("\"sourceCode\":")
        if (ref.sourceCode != null) {
            builder.append('"').append(escape(ref.sourceCode)).append('"')
        } else {
            builder.append("null")
        }
        builder.append(',')
        builder.append("\"languageId\":")
        if (ref.languageId != null) {
            builder.append('"').append(escape(ref.languageId)).append('"')
        } else {
            builder.append("null")
        }
        builder.append(',')
        builder.append("\"matchedMethod\":")
        if (ref.matchedMethod != null) {
            builder.append('"').append(escape(ref.matchedMethod.displayLabel)).append('"')
        } else {
            builder.append("null")
        }
        builder.append('}')
    }

    private fun escape(value: String): String = buildString {
        value.forEach { ch ->
            when (ch) {
                '\\' -> append("\\\\")
                '\"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(ch)
            }
        }
    }
}


