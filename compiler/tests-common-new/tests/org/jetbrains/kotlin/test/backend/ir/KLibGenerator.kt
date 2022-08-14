/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.backend.ir

import org.jetbrains.kotlin.test.model.*
import org.jetbrains.kotlin.test.services.TestServices

class KLibGenerator(
    val testServices: TestServices,
) : AbstractTestFacade<IrBackendInput, KLibArtifact>() {
    override val inputKind = BackendKinds.IrBackend
    override val outputKind = KLibKinds.KLib

    override fun shouldRunAnalysis(module: TestModule): Boolean {
        return true
    }

    override fun transform(module: TestModule, inputArtifact: IrBackendInput): KLibArtifact? {
        return when (inputArtifact) {
            is IrBackendInput.JsIrBackendInput -> TODO()
            is IrBackendInput.JvmIrBackendInput -> KLibArtifact.JvmIrKLibArtifact(
                inputArtifact.state,
                inputArtifact.codegenFactory,
                inputArtifact.backendInput,
                inputArtifact.sourceFiles
            )
        }
    }
}