/**
 * ファイル: io/github/poruru210/methodinsight/model/CallGraph.kt
 * 目的: 呼び出しグラフとそのノード・エッジを表現する純粋データモデルを提供する。
 * 背景: IntelliJ API依存の処理と生成物のレンダリングを分離し、テスト容易性を高める必要がある。
 */
package io.github.poruru210.methodinsight.model

/**
 * メソッドを一意に識別する情報を保持するデータクラス。
 * PSI依存コードから切り離すために文字列表現へ正規化する。
 */
data class MethodDescriptor(
    val className: String,
    val methodName: String,
    val signature: String
) {
    /** Mermaid等での表示用フォーマットを返す。 */
    val displayLabel: String
        get() = "$className.$methodName$signature"
}

/**
 * メソッド呼び出しの方向と補足情報を表す。
 * 呼び出し元 -> 呼び出し先 の1エッジを表現する。
 */
data class CallEdge(
    val from: MethodDescriptor,
    val to: MethodDescriptor,
    val callText: String
)

/**
 * 呼び出しグラフ全体を保持するシンプルなコンテナ。
 * エッジ追加時にノード集合を自動更新する。
 */
class CallGraph {
    private val _edges: MutableList<CallEdge> = mutableListOf()
    private val _methods: LinkedHashSet<MethodDescriptor> = LinkedHashSet()

    val edges: List<CallEdge> get() = _edges
    val methods: Set<MethodDescriptor> get() = _methods

    /**
     * エッジを追記し、関連するメソッドも登録する。
     */
    fun addEdge(edge: CallEdge) {
        _edges += edge
        _methods += edge.from
        _methods += edge.to
    }
}


