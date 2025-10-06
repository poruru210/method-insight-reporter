# Method Insight Reporter

JetBrains の Unified Abstract Syntax Tree (UAST) API を活用し、カーソル位置のメソッドに対する呼び出し関係とテスト参照を調査して Markdown 形式のシーケンスレポートを生成する IntelliJ IDEA 向けプラグインです。レポートには Mermaid によるコールグラフと、到達可能なメソッドを参照するテストケースがまとめて記載されます。GitHub 上では `method-insight-reporter` リポジトリとして管理することを想定しています。

## 主な機能

- **シーケンスレポート生成**: エディタのコンテキストメニューから *Generate Sequence Report* を実行すると、コールグラフの解析・参照テストの特定・`<method>.sequence-report.md` の書き出しまで自動で行います。生成された Markdown には Mermaid ダイアグラムと、メタデータ付きのテストセクション（折りたたみ可能）が含まれます。
- **通知によるフィードバック**: 処理状況や完了結果は通知グループ `Method Insight Reporter` に表示されます。

## リポジトリ構成

- `docs/specification.md` — プラグインの目的、サポートする JVM 言語、設計上の制約をまとめた仕様ドキュメント。
- `docs/planning.md` — 実装のための主要マイルストーン。
- `build.gradle.kts` — IntelliJ IDEA 2024.3（Java/Kotlin バンドル込み）をターゲットにした Gradle 設定。
- `src/main/kotlin/io/github/poruru210/methodinsight` — 解析・レポート生成・ユーティリティ・アクション定義などの Kotlin ソースコード。
- `src/main/resources/META-INF/plugin.xml` — アクションや通知グループを登録するプラグイン記述子。
- `src/test/kotlin/io/github/poruru210/methodinsight/render` — Mermaid レンダリング、JSON 整形、Markdown 出力の単体テスト。

## 使い方

1. IntelliJ Platform Plugin SDK を有効化した IntelliJ IDEA で本プロジェクトを開きます。
2. `./gradlew build`（Windows の場合は `gradlew.bat build`）でコンパイルと単体テストを実行します。
3. `./gradlew verifyPlugin` で設定済みの IDE ビルドに対する IntelliJ Plugin Verifier を実行します。
4. `./gradlew runIde` を実行し、サンドボックス IDE を起動します。
5. 対象メソッドにカーソルを置き、コンテキストメニューから **Generate Sequence Report** を選択します。

生成されたレポートはソースファイルと同じディレクトリに `<method>.sequence-report.md` として作成され、Mermaid ダイアグラムとテスト一覧（各呼び出し単位で折りたたみ可能なコードブロック付き）が含まれます。

## CI / CD

GitHub Actions ワークフロー **Build Plugin** は `main` ブランチへの push および pull request をトリガーに自動実行され、`./gradlew --no-daemon test` → `verifyPlugin` → `buildPlugin` を順番に実行して成果物をアーティファクトとして保存します。ローカルで失敗した場合は同じ Gradle コマンドを順に実行し、状況の再現と修正を行ってください。

## 参考資料

- JetBrains UAST ドキュメント（Java / Kotlin / Scala[β] / Groovy[宣言部] をカバーする読み取り専用 PSI 抽象化）。
- SequencePlugin README — Mermaid 出力や UAST ベースのジェネレーターへの移行について記載された参考プロジェクト。

## Git Versioning

This project uses the [Palantir Git-Version Gradle Plugin](https://github.com/palantir/gradle-git-version) to derive the plugin version from Git metadata. When running `./gradlew buildPlugin`, the generated distribution ZIP and `plugin.xml` will use the output of `git describe --tags --always --first-parent`. CI fetches the full history (`fetch-depth: 0`) so tags are available during the build.
