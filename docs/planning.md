# Planning: UAST‑Based Mermaid & Test Finder Plugin

This document outlines a high‑level plan for implementing the plugin
described in the specification.  The goal is to deliver a minimal yet
functional IntelliJ IDEA plugin that leverages UAST to generate Mermaid
diagrams and collect unit tests.

## 1 Assumptions

* The project is a fork or new module separate from the existing
  SequencePlugin.  It will reuse concepts such as call graph traversal and
  export but will implement them from scratch.
* Development will be performed in Kotlin and built with the IntelliJ
  Platform Gradle Plugin version 2.x.
* The primary focus is on Java and Kotlin; Scala and Groovy support are
  nice‑to‑have if time permits.

## 2 Milestones

### Milestone 1: Project setup

1. Initialise the Gradle project with `build.gradle.kts` configured for
   IntelliJ plugin development.  Add dependencies on `com.intellij.java`
   and optionally `org.jetbrains.kotlin` plugins.
2. Create a minimal `plugin.xml` with the plugin ID, name and actions.
3. Set up a `src/main/kotlin` package structure (e.g.
   `io.github.poruru210.methodinsight`) for source files.
4. Define actions in `plugin.xml` for “Generate Mermaid” and “Find Unit
   Tests” and bind them to empty Kotlin classes implementing `AnAction`.

### Milestone 2: UAST call graph generation

1. Implement a `CallGraphBuilder` that accepts a `UMethod` and performs
   depth‑limited traversal of `UCallExpression` nodes.  Use UAST
   visitors【991582450088256†L174-L179】 to walk the AST and `resolve()` to
   determine the target methods.
2. Filter out calls outside project source roots.
3. Build an in‑memory directed graph representation (e.g. adjacency list).
4. Add a basic `MermaidGenerator` that converts the call graph to
   Mermaid syntax and writes a `.mmd` file using Virtual File System APIs.

### Milestone 3: Unit test discovery

1. Implement a `TestFinder` that uses the Psi search API to find
   references to a given `PsiMethod` within test source roots.
2. Identify JUnit or TestNG tests via annotations.  For each test
   containing a reference, record the class and method name along with the
   call location.
3. Provide a simple JSON serialization of the discovered tests and save
   it near the source file.
4. Optionally implement closure search: build the call graph closure and
   search for tests referencing any method within this set.

### Milestone 4: IntelliJ UI integration

1. Populate the `AnAction` classes with logic to obtain the caret
   position, convert to UAST (`UElement#toUElement()`【991582450088256†L59-L118】) and
   invoke the appropriate builder.
2. Display notifications upon generation and open the generated files.
3. Add settings panel (optional) to configure depth limits or exclude
   certain packages.

### Milestone 5: Polish and documentation

1. Write user documentation detailing how to use the plugin, referencing
   UAST’s capabilities and limitations (e.g. Groovy support【991582450088256†L32-L40】).
2. Add comprehensive comments and KDoc to code for maintainability.
3. Test the plugin on sample projects using Java and Kotlin to verify
   diagram correctness and test discovery accuracy.

## 3 Risks and Mitigations

| Risk                                   | Mitigation                                               |
|----------------------------------------|-----------------------------------------------------------|
| UAST visitor performance on large files | Impose a depth limit and track visited methods to avoid  |
|                                        | endless loops; allow configuration via settings.        |
| Multiple implementations for interfaces | Initially skip ambiguous interface calls or ask the user |
|                                        | to pick; optionally implement the SequencePlugin’s Smart |
|                                        | Interface strategy later.                                |
| Incomplete support for Scala and Groovy | Clearly document limitations【991582450088256†L32-L40】 and treat support for |
|                                        | these languages as experimental.                         |
| IDE API changes across versions        | Target a stable IntelliJ version (e.g. 2024.3) and test  |
|                                        | the plugin during upgrade cycles.                        |

## 4 Next Steps

The repository currently contains only documentation and a project skeleton.
Implementation will proceed by following the milestones above.  The
SequencePlugin’s source code and README provide examples of UAST usage and
Mermaid export【120854760840373†L305-L337】, which can inform the development effort.

