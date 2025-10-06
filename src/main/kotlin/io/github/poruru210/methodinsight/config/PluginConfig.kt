/**
 * File: io/github/poruru210/methodinsight/config/PluginConfig.kt
 * Purpose: Centralise configurable constants shared by the plugin.
 */
package io.github.poruru210.methodinsight.config

/**
 * Singleton holding configuration defaults so they can be tweaked in one place.
 */
object PluginConfig {
    /** Default maximum traversal depth when building the call graph. */
    const val DEFAULT_MAX_DEPTH: Int = 5

    /** File extension used for the Mermaid diagram. */
    const val MERMAID_FILE_EXTENSION: String = "mmd"

    /** Suffix appended to the generated Mermaid file name. */
    const val MERMAID_FILE_SUFFIX: String = "sequence"

    /** File extension used for the raw test report (JSON). */
    const val TEST_REPORT_EXTENSION: String = "tests.json"

    /** File extension used for the Markdown report. */
    const val MARKDOWN_REPORT_EXTENSION: String = "md"

    /** Suffix appended to the Markdown report file name. */
    const val MARKDOWN_REPORT_SUFFIX: String = "sequence-report"

    /** Notification group identifier registered in plugin.xml. */
    const val NOTIFICATION_GROUP_ID: String = "Method Insight Reporter"
}


