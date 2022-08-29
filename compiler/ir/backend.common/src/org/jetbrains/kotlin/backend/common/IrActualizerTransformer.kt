/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common

import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

class IrActualizerTransformer(val actualMembers: Map<IdSignature, IrSymbol>) : IrElementTransformerVoid() {
    override fun visitCall(expression: IrCall): IrExpression {
        val keySignature = when (val signature = expression.symbol.signature) {
            is IdSignature.AccessorSignature -> {
                val propertySignature = signature.propertySignature as? IdSignature.CommonSignature
                if (propertySignature != null) {
                    IdSignature.CommonSignature(
                        propertySignature.packageFqName,
                        propertySignature.declarationFqName, propertySignature.id, 0
                    )
                } else {
                    null
                }
            }
            is IdSignature.CommonSignature -> {
                if (expression.symbol.owner.isExpect)
                    IdSignature.CommonSignature(signature.packageFqName, signature.declarationFqName, signature.id, 0)
                else
                    signature
            }
            else -> {
                null
            }
        }
        if (keySignature != null) {
            val actualMember = actualMembers[keySignature]
            val actualSymbol = if (actualMember is IrSimpleFunctionSymbol) {
                actualMember
            } else if (actualMember is IrPropertySymbol) {
                val owner = actualMember.owner
                if (expression.origin == IrStatementOrigin.GET_PROPERTY) {
                    owner.getter!!.symbol
                } else {
                    owner.setter!!.symbol
                }
            } else {
                null
            }
            if (actualSymbol != null) {
                val result = IrCallImpl(
                    expression.startOffset,
                    expression.endOffset,
                    expression.type,
                    actualSymbol,
                    expression.typeArgumentsCount,
                    expression.valueArgumentsCount,
                    expression.origin,
                    expression.superQualifierSymbol
                )
                result.contextReceiversCount = expression.contextReceiversCount
                result.extensionReceiver = expression.extensionReceiver?.let { visitExpression(it) }
                result.dispatchReceiver = expression.dispatchReceiver?.let { visitExpression(it) }
                for (index in 0 until expression.valueArgumentsCount) {
                    val valueArgument = expression.getValueArgument(index)?.let { visitExpression(it) }
                    result.putValueArgument(index, valueArgument)
                }
                for (index in 0 until expression.typeArgumentsCount) {
                    val typeArgument = expression.getTypeArgument(index)
                    result.putTypeArgument(index, typeArgument)
                }
                return result
            }
        }
        return super.visitCall(expression)
    }
}