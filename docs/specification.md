# Specification: UAST‑Based Mermaid & Test Finder Plugin

## 1 Purpose

This document specifies a custom IntelliJ IDEA plugin that generates
Mermaid sequence diagrams for a given method and extracts unit tests that
reference the method.  The plugin is inspired by the open‑source
SequencePlugin, which can export sequence diagrams as PlantUML and Mermaid
files【120854760840373†L305-L307】 and has recently added experimental UAST support【120854760840373†L319-L337】.

### Goals

1. **Diagram generation**: Given an entry method in a Java or Kotlin source
   file, build a call graph within the current project (excluding JDK and
   external libraries) and produce a Mermaid `sequenceDiagram` describing
   method calls.  This mirrors the SequencePlugin’s ability to export
   diagrams to Mermaid format【120854760840373†L305-L307】.
2. **Test extraction**: List all JUnit test methods that directly reference
   the entry method or any method reachable in the call graph.  The
   extraction works via static analysis of test sources.
3. **Language neutrality**: Implement analysis using JetBrains’ UAST API so
   that the plugin works across JVM languages.  UAST provides a unified
   abstraction layer over PSI for Java, Kotlin, Scala and Groovy【991582450088256†L32-L40】.  In
   particular, Java and Kotlin have full support, Scala has beta support,
   and Groovy supports declarations only【991582450088256†L32-L40】.

### Non‑Goals

The plugin does **not** perform runtime instrumentation or dynamic
analysis.  It also does not compute code coverage or generate unit test
boilerplate.  Groovy method bodies are not analysed because UAST only
supports Groovy declarations【991582450088256†L32-L40】.

## 2 Functional Requirements

### 2.1 Entry point detection

1. Provide editor actions (e.g. context menu or shortcut) that run when
   the caret is within a method declaration.
2. Convert the selected method’s `PsiElement` into a UAST `UMethod` via
   `toUElement()` or `UastFacade`【991582450088256†L59-L118】.  A fail‑fast conversion should
   be used for performance and predictability【991582450088256†L111-L119】.
3. Use the `javaPsi` property to obtain a Java‑like `PsiMethod`
   representation for reference searching【991582450088256†L136-L166】.

### 2.2 Call graph construction

1. Starting from the entry `UCallExpression` or method body, traverse
   `UCallExpression` nodes using a visitor pattern【991582450088256†L174-L179】.
2. For each call, resolve the target method via `UCallExpression#resolve()`.
3. Exclude calls outside the user’s project (e.g. JDK).  Use the IntelliJ
   file index to check if a file belongs to the source roots.
4. Record call edges to build a directed graph.  Avoid infinite loops by
   tracking visited methods and imposing a depth limit (configurable).

### 2.3 Mermaid sequence diagram generation

1. Represent each class as a participant in the Mermaid diagram.
2. Represent each method call as a message line (`A->>B: method(params)`).
3. Generate a `.mmd` file adjacent to the source file via the Virtual
   File System API.  Provide user feedback via notifications.

### 2.4 Unit test extraction

1. Identify test methods by detecting `@org.junit.jupiter.api.Test`,
   `@ParameterizedTest`, `@org.junit.Test` or `@org.testng.annotations.Test`.
2. Perform a reference search for the entry `PsiMethod` in test source
   roots.  For UAST, convert the `UMethod` to its `javaPsi` when calling
   Java‑PSI APIs【991582450088256†L136-L166】.
3. Optionally compute the call graph closure and repeat the search for
   methods reachable from the entry method.
4. Produce a report (e.g. JSON) listing test class, test method name,
   display name (if present), framework (JUnit 4/5 or TestNG), and a
   summary of calls matched (`direct` vs `closure`).

## 3 Non‑Functional Requirements

1. **Language support**: The plugin must work for Java and Kotlin code.
   Support for Scala is experimental and for Groovy is limited to
   declarations【991582450088256†L32-L40】.
2. **Read‑only analysis**: UAST is a read‑only API【991582450088256†L42-L46】.  The plugin must not
   attempt to modify code through UAST or PSI.
3. **Performance**: Provide configuration for maximum traversal depth and
   restrict analysis to project source roots.
4. **User experience**: The plugin must integrate with the IntelliJ
   action system and display notifications when files are generated.
5. **Export format**: Use the official Mermaid sequence diagram syntax
   documented by the Mermaid specification.  Avoid generating PlantUML or
   images; focus solely on Mermaid in this implementation.

## 4 Open Issues and Limitations

1. **Implementation resolution**: SequencePlugin’s “Smart Interface” feature
   attempts to resolve interface implementations.  In this plugin we will
   implement a basic form by resolving the unique implementation if only
   one exists, otherwise omitting interface calls.  Users may refine this
   later.
2. **Groovy support**: UAST does not support Groovy method bodies【991582450088256†L32-L40】;
   thus calls in Groovy code cannot be analysed.
3. **Mixed languages**: When analysing Kotlin code, the `javaPsi`
   representation may be synthetic and not fully modifiable【991582450088256†L136-L166】.  Care
   should be taken not to rely on modification operations.
4. **Third‑party frameworks**: The plugin does not attempt to interpret
   dependency injection frameworks such as Spring.  Only direct method
   calls are traced.

## 5 References

* JetBrains. “UAST – Unified Abstract Syntax Tree,” IntelliJ Platform
  Plugin SDK.  UAST is an abstraction layer on top of PSI that unifies
  language elements across JVM languages【991582450088256†L6-L10】.  It supports Java and Kotlin
  fully, Scala in beta, and Groovy declarations only【991582450088256†L32-L40】.
* UAST is read‑only and conversion to and from PSI requires
  `UastFacade` or the `toUElement()` extension【991582450088256†L59-L118】.  The `javaPsi` and
  `sourcePsi` properties help obtain Java‑like or physical PSI elements【991582450088256†L124-L166】.
* SequencePlugin’s README notes that version 3.x introduces UAST
  support【120854760840373†L319-L337】 and that diagrams can be exported to Mermaid and PlantUML
  formats【120854760840373†L305-L307】.  These features inform the design of the current
  plugin.