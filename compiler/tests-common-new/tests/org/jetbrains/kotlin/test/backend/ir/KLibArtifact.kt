/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.backend.ir

import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.backend.jvm.JvmIrCodegenFactory
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.backend.js.KotlinFileSerializedData
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.test.model.KLibKinds
import org.jetbrains.kotlin.test.model.ResultingArtifact

sealed class KLibArtifact : ResultingArtifact.KLibArtifactBase<KLibArtifact>() {
    override val kind = KLibKinds.KLib

    abstract val irModuleFragment: IrModuleFragment

    data class JsKLibArtifact(
        override val irModuleFragment: IrModuleFragment,
        val sourceFiles: List<KtFile>,
        val bindingContext: BindingContext,
        val icData: List<KotlinFileSerializedData>,
        val expectDescriptorToSymbol: MutableMap<DeclarationDescriptor, IrSymbol>,
    ) : KLibArtifact()

    data class JvmIrKLibArtifact(
        val state: GenerationState,
        val codegenFactory: JvmIrCodegenFactory,
        val backendInput: JvmIrCodegenFactory.JvmIrBackendInput,
        val sourceFiles: List<KtSourceFile>
    ) : KLibArtifact() {
        override val irModuleFragment: IrModuleFragment
            get() = backendInput.irModuleFragment
    }
}