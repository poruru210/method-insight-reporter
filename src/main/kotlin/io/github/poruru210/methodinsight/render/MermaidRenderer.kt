/**
 * ファイル: io/github/poruru210/methodinsight/render/MermaidRenderer.kt
 * 目的: 呼び出しグラフからMermaidシーケンス図テキストを生成する。
 * 背景: IDE内で生成するレポートのMermaidブロックに整形済みテキストを供給する必要がある。
 */
package io.github.poruru210.methodinsight.render

import io.github.poruru210.methodinsight.model.CallEdge
import io.github.poruru210.methodinsight.model.CallGraph
import io.github.poruru210.methodinsight.model.MethodDescriptor
import java.util.LinkedHashMap
import java.util.LinkedHashSet

/** Mermaidシーケンス図用テキストを生成する。 */
class MermaidRenderer {
    fun render(
        graph: CallGraph,
        entry: MethodDescriptor,
        numbering: Map<MethodDescriptor, Int>
    ): String {
        val aliases = buildAliasMap(graph, entry)
        val builder = StringBuilder().apply { appendLine("sequenceDiagram") }

        appendParticipants(builder, aliases)
        graph.edges.forEach { edge ->
            appendEdge(builder, edge, aliases, numbering)
        }

        return builder.toString()
    }

    private fun appendParticipants(
        builder: StringBuilder,
        aliases: Map<MethodDescriptor, String>
    ) {
        val declared = LinkedHashSet<String>()
        aliases.values.forEach { alias ->
            if (declared.add(alias)) {
                // Mermaidではエイリアス名でメッセージを描画するため、表示名と合わせておく
                builder.append("    participant ")
                    .append(alias)
                    .appendLine()
            }
        }
    }

    private fun buildAliasMap(
        graph: CallGraph,
        entry: MethodDescriptor
    ): LinkedHashMap<MethodDescriptor, String> {
        val mapping = LinkedHashMap<MethodDescriptor, String>()
        val aliasByClass = mutableMapOf<String, String>()
        val aliasCounts = mutableMapOf<String, Int>()

        fun aliasFor(className: String): String {
            aliasByClass[className]?.let { return it }
            val base = className.substringAfterLast('.')
                .takeIf { it.isNotBlank() }
                ?: className.replace('.', '_')
            val sanitized = sanitizeAlias(base)
            val count = aliasCounts.getOrDefault(sanitized, 0)
            aliasCounts[sanitized] = count + 1
            val alias = if (count == 0) sanitized else "${sanitized}_${count + 1}"
            aliasByClass[className] = alias
            return alias
        }

        mapping[entry] = aliasFor(entry.className)
        graph.methods
            .filterNot { it == entry }
            .sortedWith(compareBy({ it.className }, { it.methodName }))
            .forEach { descriptor ->
                mapping[descriptor] = aliasFor(descriptor.className)
            }
        return mapping
    }

    private fun appendEdge(
        builder: StringBuilder,
        edge: CallEdge,
        aliases: Map<MethodDescriptor, String>,
        numbering: Map<MethodDescriptor, Int>
    ) {
        val fromAlias = aliases[edge.from] ?: aliases.getValue(edge.from)
        val toAlias = aliases[edge.to] ?: aliases.getValue(edge.to)
        val callNumber = numbering[edge.to]?.let { "#35;$it " } ?: ""
        builder.append("    ")
            .append(fromAlias)
            .append("->>")
            .append(toAlias)
            .append(": ")
            .append(callNumber)
            .append(edge.callText)
            .appendLine()
    }

    private fun sanitizeAlias(raw: String): String {
        val sanitized = raw.replace(Regex("[^A-Za-z0-9_]"), "_")
        return if (sanitized.isBlank()) "Participant" else sanitized
    }
}


