/**
 * ファイル: io/github/poruru210/methodinsight/render/MermaidRendererTest.kt
 * 目的: MermaidRendererが正しいシーケンス図テキストを出力することを検証する。
 * 背景: 生成済みMarkdownのMermaidブロックが破綻しないよう回帰テストを維持する必要がある。
 */
package io.github.poruru210.methodinsight.render

import io.github.poruru210.methodinsight.model.CallEdge
import io.github.poruru210.methodinsight.model.CallGraph
import io.github.poruru210.methodinsight.model.MethodDescriptor
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class MermaidRendererTest {
    @Test
    fun `participants and messages include call numbering`() {
        val entry = MethodDescriptor("com.example.Service", "handle", "(Request)")
        val repo = MethodDescriptor("com.example.Repository", "load", "(String)")
        val graph = CallGraph().apply {
            addEdge(CallEdge(entry, repo, "load(key)"))
        }
        val renderer = MermaidRenderer()
        val numbering = linkedMapOf(repo to 1)
        val diagram = renderer.render(graph, entry, numbering)

        assertEquals("sequenceDiagram", diagram.lines().first())
        assertContains(diagram, "participant Service")
        assertContains(diagram, "participant Repository")
        assertContains(diagram, "Service->>Repository: #35;1 load(key)")
    }

    @Test
    fun `duplicate participant declarations are suppressed`() {
        val entry = MethodDescriptor("com.example.Service", "handle", "(Request)")
        val helper = MethodDescriptor("com.example.Service", "helper", "(String)")
        val repo = MethodDescriptor("com.example.Repository", "load", "(String)")
        val graph = CallGraph().apply {
            addEdge(CallEdge(entry, helper, "helper(arg)"))
            addEdge(CallEdge(helper, repo, "load(arg)"))
        }
        val renderer = MermaidRenderer()
        val numbering = linkedMapOf(helper to 1, repo to 2)
        val diagram = renderer.render(graph, entry, numbering)

        val serviceParticipants = diagram.lines().filter { it.startsWith("    participant Service") }
        assertEquals(1, serviceParticipants.size, "参加者宣言はクラス単位で1回だけ出力する")
        assertContains(diagram, "Service->>Service: #35;1 helper(arg)")
        assertContains(diagram, "Service->>Repository: #35;2 load(arg)")
    }
}


