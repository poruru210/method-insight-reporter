/**
 * File: io/github/poruru210/methodinsight/analysis/CallGraphResult.kt
 * Purpose: Hold the call graph alongside a lookup from MethodDescriptor to PsiMethod.
 */
package io.github.poruru210.methodinsight.analysis

import com.intellij.psi.PsiMethod
import io.github.poruru210.methodinsight.model.CallGraph
import io.github.poruru210.methodinsight.model.MethodDescriptor

/**
 * Stores the call graph and a descriptor-to-PsiMethod index for follow-up searches.
 */
data class CallGraphResult(
    val graph: CallGraph,
    val descriptorIndex: Map<MethodDescriptor, PsiMethod>
)


