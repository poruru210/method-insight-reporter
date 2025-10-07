# Method Insight Reporter

Method Insight Reporter is an IntelliJ IDEA plugin that leverages the JetBrains Unified Abstract Syntax Tree (UAST) API to analyse method invocation relationships and test coverage, then emits a Markdown report for the currently selected method. Each report includes a Mermaid sequence diagram alongside a list of relevant methods, making it easy to validate complex call flows and document behaviour. The project is maintained publicly as `method-insight-reporter`.

## Features

- **Generate sequence reports**  
  Run *Generate Sequence Report* from the editor context menu to inspect the call chain. The plugin walks the UAST, collects call sites and referenced tests, and writes the result to `<method>.sequence-report.md`. The Markdown contains a Mermaid diagram and summarised test matrices so you can review impact and coverage at a glance.
- **Background feedback**  
  Progress and diagnostics appear in the `Method Insight Reporter` notification group so you can continue working while the report is prepared.

## Repository Layout

- `docs/specification.md` - Functional goals, JVM support matrix, and design assumptions.
- `docs/planning.md` - Planning artefacts gathered during the initial design phase.
- `build.gradle.kts` - Gradle build configuration targeting IntelliJ IDEA 2024.3 with Java and Kotlin toolchains.
- `src/main/kotlin/io/github/poruru210/methodinsight` - Kotlin sources that implement report generation, rendering, and IntelliJ actions.
- `src/main/resources/META-INF/plugin.xml` - Plugin metadata, action registrations, and notification group definitions.
- `src/test/kotlin/io/github/poruru210/methodinsight/render` - Unit tests covering Mermaid rendering, JSON formatting, and Markdown output.

## Getting Started

1. Open the project in IntelliJ IDEA and install the IntelliJ Platform Plugin SDK.
2. Run `./gradlew build` (or `gradlew.bat build` on Windows) to compile the plugin and execute all tests.
3. Run `./gradlew verifyPlugin` to validate the plugin against the configured IDE builds with the IntelliJ Plugin Verifier.
4. Run `./gradlew runIde` to launch a sandbox IDE session with the plugin enabled.
5. Inside the sandbox IDE, open a source file, choose a method of interest, and invoke **Generate Sequence Report** from the context menu.

Each report writes a Markdown file named `<method>.sequence-report.md` alongside the source file. Mermaid diagrams can be previewed immediately, while the associated test list helps confirm that the method remains adequately covered.

## CI / CD

The **Build Plugin** GitHub Actions workflow runs whenever a Git tag is pushed. It executes `./gradlew --no-daemon test`, `verifyPlugin`, and `buildPlugin`, then uploads the resulting ZIP archive as a workflow artifact. Because Gradle commands run on GitHub-hosted runners, rerun the job if transient network issues occur while downloading IntelliJ artifacts or dependencies.

## References

- JetBrains UAST documentation (Java, Kotlin, Scala, Groovy PSI traversal guidance).
- SequencePlugin README, which inspired the Mermaid output format and UAST-based analysis patterns.
