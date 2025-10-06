/**
 * File: io/github/poruru210/methodinsight/model/TestReference.kt
 * Purpose: Data structures describing test references and their metadata.
 */
package io.github.poruru210.methodinsight.model

/**
 * Information captured for a single test method.
 */
data class TestReference(
    val className: String,
    val methodName: String,
    val framework: String,
    val displayName: String?,
    val matchType: MatchType,
    val sourceCode: String?,
    val languageId: String?,
    val matchedMethod: MethodDescriptor?
)

/**
 * Indicates how the test references the entry point.
 */
enum class MatchType {
    DIRECT,
    CLOSURE
}

/**
 * Container for the overall test discovery result.
 */
data class TestReport(
    val entryPoint: MethodDescriptor,
    val findings: List<TestReference>
)


