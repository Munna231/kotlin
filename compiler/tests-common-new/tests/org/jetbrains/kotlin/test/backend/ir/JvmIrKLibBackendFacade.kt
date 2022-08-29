/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.backend.ir

import org.jetbrains.kotlin.KtPsiSourceFile
import org.jetbrains.kotlin.backend.common.BackendException
import org.jetbrains.kotlin.backend.common.IrActualizer
import org.jetbrains.kotlin.backend.jvm.MultifileFacadeFileEntry
import org.jetbrains.kotlin.backend.jvm.lower.getFileClassInfoFromIrFile
import org.jetbrains.kotlin.fileClasses.JvmFileClassUtil
import org.jetbrains.kotlin.ir.PsiIrFileEntry
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.util.NaiveSourceBasedFileEntryImpl
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.backend.classic.JavaCompilerFacade
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives
import org.jetbrains.kotlin.test.model.*
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.compilerConfigurationProvider
import org.jetbrains.kotlin.test.services.dependencyProvider

class JvmIrKLibBackendFacade(
    val testServices: TestServices,
) : AbstractTestFacade<KLibArtifact, BinaryArtifacts.Jvm>() {
    private val javaCompilerFacade = JavaCompilerFacade(testServices)

    override val inputKind = KLibKinds.KLib
    override val outputKind = ArtifactKinds.Jvm

    override fun shouldRunAnalysis(module: TestModule): Boolean {
        val incomingDependencies = testServices.dependencyProvider.getIncomingDependencies(module)
        return incomingDependencies.none { it.relation == DependencyRelation.DependsOnDependency }
    }

    override fun transform(module: TestModule, inputArtifact: KLibArtifact): BinaryArtifacts.Jvm? {
        require(inputArtifact is KLibArtifact.JvmIrKLibArtifact) {
            "JvmIrKLibBackendFacade expects KLibArtifact.JvmIrKLibArtifact as input"
        }

        val dependencyProvider = testServices.dependencyProvider
        val dependencies = module.dependsOnDependencies.map { dependency ->
            val testModule = dependencyProvider.getTestModule(dependency.moduleName)
            val artifact = dependencyProvider.getArtifact(testModule, KLibKinds.KLib)
            artifact.irModuleFragment
        }
        IrActualizer.actualize(inputArtifact.irModuleFragment, dependencies)

        val state = inputArtifact.state
        try {
            inputArtifact.codegenFactory.generateModule(state, inputArtifact.backendInput)
        } catch (e: BackendException) {
            if (CodegenTestDirectives.IGNORE_ERRORS in module.directives) {
                return null
            }
            throw e
        }
        state.factory.done()
        val configuration = testServices.compilerConfigurationProvider.getCompilerConfiguration(module)
        javaCompilerFacade.compileJavaFiles(module, configuration, state.factory)

        fun sourceFileInfos(irFile: IrFile, allowNestedMultifileFacades: Boolean): List<SourceFileInfo> =
            when (val fileEntry = irFile.fileEntry) {
                is PsiIrFileEntry -> {
                    listOf(
                        SourceFileInfo(
                            KtPsiSourceFile(fileEntry.psiFile),
                            JvmFileClassUtil.getFileClassInfoNoResolve(fileEntry.psiFile as KtFile)
                        )
                    )
                }
                is NaiveSourceBasedFileEntryImpl -> {
                    val sourceFile = inputArtifact.sourceFiles.find { it.path == fileEntry.name }
                    if (sourceFile == null) emptyList() // synthetic files, like CoroutineHelpers.kt, are ignored here
                    else listOf(SourceFileInfo(sourceFile, getFileClassInfoFromIrFile(irFile, sourceFile.name)))
                }
                is MultifileFacadeFileEntry -> {
                    if (!allowNestedMultifileFacades) error("nested multi-file facades are not allowed")
                    else fileEntry.partFiles.flatMap { sourceFileInfos(it, allowNestedMultifileFacades = false) }
                }
                else -> {
                    error("unknown kind of file entry: $fileEntry")
                }
            }

        return BinaryArtifacts.Jvm(
            state.factory,
            inputArtifact.irModuleFragment.files.flatMap {
                sourceFileInfos(it, allowNestedMultifileFacades = true)
            }
        )
    }
}