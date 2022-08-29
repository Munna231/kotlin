/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common

import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.IdSignature

object IrActualizer {
    fun actualize(platformFragment: IrModuleFragment, commonFragments: List<IrModuleFragment>) {
        val expectMembers = collectExpectMembers(commonFragments)
        val actualMembers = findActualMembers(platformFragment, expectMembers)
        linkExpectToActual(actualMembers, commonFragments)
        mergeIrFragments(platformFragment, commonFragments)
    }

    private fun collectExpectMembers(commonFragments: List<IrModuleFragment>): Set<IdSignature> {
        val expectMembers = mutableSetOf<IdSignature>()

        fun collectExpectMembers(declaration: IrDeclaration) {
            if (declaration is IrClass) {
                if (declaration.origin == IrDeclarationOrigin.FILE_CLASS) {
                    for (subDeclaration in declaration.declarations) {
                        collectExpectMembers(subDeclaration)
                    }
                }
            }

            val signature = declaration.symbol.signature as? IdSignature.CommonSignature ?: return

            if (declaration is IrFunction && declaration.isExpect || declaration is IrProperty && declaration.isExpect) {
                expectMembers.add(IdSignature.CommonSignature(signature.packageFqName, signature.declarationFqName, signature.id, 0))
            }
        }

        for (commonFragment in commonFragments) {
            for (file in commonFragment.files) {
                for (declaration in file.declarations) {
                    collectExpectMembers(declaration)
                }
            }
        }
        return expectMembers
    }

    private fun findActualMembers(platformFragment: IrModuleFragment, expectMembers: Set<IdSignature>): Map<IdSignature, IrSymbol> {
        val actualMembers = mutableMapOf<IdSignature, IrSymbol>()
        for (file in platformFragment.files) {
            for (declaration in file.declarations) {
                if (declaration is IrFunction || declaration is IrProperty) {
                    val signature = declaration.symbol.signature ?: continue
                    if (expectMembers.contains(signature)) {
                        actualMembers[signature] = declaration.symbol
                    }
                }
            }
        }
        return actualMembers
    }

    private fun linkExpectToActual(actualMembers: Map<IdSignature, IrSymbol>, commonFragments: List<IrModuleFragment>) {
        val actualizer = IrActualizerTransformer(actualMembers)
        for (commonFragment in commonFragments) {
            commonFragment.transform(actualizer, null)
        }
    }

    private fun mergeIrFragments(platformFragment: IrModuleFragment, commonFragments: List<IrModuleFragment>) {
        platformFragment.files.addAll(0, commonFragments.flatMap { it.files })
    }
}