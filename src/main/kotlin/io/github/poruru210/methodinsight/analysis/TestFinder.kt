/**
 * File: io/github/poruru210/methodinsight/analysis/TestFinder.kt
 * Purpose: Locate tests referencing the entry method or its reachable calls.
 */
package io.github.poruru210.methodinsight.analysis

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.TestSourcesFilter
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import io.github.poruru210.methodinsight.model.MatchType
import io.github.poruru210.methodinsight.model.MethodDescriptor
import io.github.poruru210.methodinsight.model.TestReference
import io.github.poruru210.methodinsight.model.TestReport
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.getContainingUClass
import org.jetbrains.uast.toUElement
import java.util.LinkedHashMap
import kotlin.collections.buildSet

/**
 * Performs static analysis to map tests back to the call graph.
 */
class TestFinder(private val project: Project) {
    fun findTests(entry: MethodDescriptor, graphResult: CallGraphResult): TestReport {
        val entryPsi = graphResult.descriptorIndex[entry] ?: return TestReport(entry, emptyList())
        val collected = LinkedHashMap<String, TestReference>()

        searchReferences(entryPsi, entry, MatchType.DIRECT, collected)
        graphResult.descriptorIndex
            .filterKeys { it != entry }
            .forEach { (descriptor, psi) ->
                searchReferences(psi, descriptor, MatchType.CLOSURE, collected)
            }

        return TestReport(entry, collected.values.toList())
    }

    private fun searchReferences(
        target: PsiMethod,
        matchedDescriptor: MethodDescriptor?,
        matchType: MatchType,
        sink: MutableMap<String, TestReference>
    ) {
        val scope = GlobalSearchScope.projectScope(project)
        ReferencesSearch.search(target, scope).forEach { ref ->
            val psiMethod = PsiTreeUtil.getParentOfType(ref.element, PsiMethod::class.java)
            val ktFunction = PsiTreeUtil.getParentOfType(ref.element, KtNamedFunction::class.java)
            val uMethod = when {
                psiMethod != null -> psiMethod.toUElement(UMethod::class.java)
                ktFunction != null -> ktFunction.toUElement(UMethod::class.java)
                else -> null
            }
            val container = psiMethod ?: uMethod?.javaPsi ?: return@forEach
            if (!isInTestSources(container)) return@forEach
            val framework = detectFramework(uMethod, container) ?: return@forEach
            val key = buildKey(container)
            val displayName = extractDisplayName(uMethod)
            val reference = TestReference(
                className = resolveClassName(uMethod, container),
                methodName = container.name,
                framework = framework,
                displayName = displayName,
                matchType = matchType,
                sourceCode = extractSourceCode(uMethod, container),
                languageId = extractLanguageId(uMethod, container),
                matchedMethod = matchedDescriptor
            )
            val existing = sink[key]
            if (existing == null || (existing.matchType == MatchType.CLOSURE && matchType == MatchType.DIRECT)) {
                sink[key] = reference
            }
        }
    }

    private fun isInTestSources(method: PsiMethod): Boolean {
        val file = method.containingFile?.virtualFile ?: return false
        return TestSourcesFilter.isTestSources(file, project)
    }

    private fun detectFramework(uMethod: UMethod?, psiMethod: PsiMethod): String? {
        val annotationNames = buildSet {
            uMethod?.annotations?.mapNotNullTo(this) { it.qualifiedName }
            psiMethod.annotations.mapNotNullTo(this) { it.qualifiedName }
        }
        return when {
            annotationNames.contains("org.testng.annotations.Test") -> "TestNG"
            annotationNames.any { it == "org.junit.jupiter.api.Test" || it == "org.junit.jupiter.params.ParameterizedTest" } -> "JUnit 5"
            annotationNames.contains("org.junit.Test") -> "JUnit 4"
            else -> null
        }
    }

    private fun extractDisplayName(uMethod: UMethod?): String? {
        val displayAnnotation = uMethod?.annotations?.firstOrNull { it.qualifiedName == "org.junit.jupiter.api.DisplayName" }
        val literal = displayAnnotation?.findAttributeValue("value") ?: return null
        return literal.toString().trim('"')
    }

    private fun resolveClassName(uMethod: UMethod?, psiMethod: PsiMethod): String {
        val uClass = uMethod?.getContainingUClass()
        return uClass?.qualifiedName
            ?: psiMethod.containingClass?.qualifiedName
            ?: psiMethod.containingClass?.name
            ?: ""
    }

    private fun buildKey(method: PsiMethod): String {
        val className = resolveClassName(null, method)
        return "$className#${method.name}"
    }

    private fun extractSourceCode(uMethod: UMethod?, psiMethod: PsiMethod): String? {
        val sourcePsi = uMethod?.sourcePsi
        return when {
            sourcePsi != null -> sourcePsi.text
            else -> psiMethod.text
        }
    }

    private fun extractLanguageId(uMethod: UMethod?, psiMethod: PsiMethod): String? {
        val sourcePsi = uMethod?.sourcePsi
        return when {
            sourcePsi != null -> sourcePsi.language.id.lowercase()
            else -> psiMethod.language.id.lowercase()
        }
    }
}


