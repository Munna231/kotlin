/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl.factory

import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.targets.native.NativeCompilerOptions
import org.jetbrains.kotlin.tooling.core.UnsafeApi
import java.util.*

internal object NativeCompilerOptionsFactory : KotlinCompilationImplFactory.CompilerOptionsFactory {

    @OptIn(UnsafeApi::class) // We are handling it properly!
    override fun create(target: KotlinTarget, compilationName: String): KotlinCompilationImplFactory.CompilerOptionsFactory.Options {
        val compilerOptions = NativeCompilerOptions(target.project, Optional.empty())
        target.compilations.matching { it.name == compilationName }.all { compilation ->
            NativeCompilerOptions.applyLanguageSettingsToCompilerOptions(
                target.project, compilation.defaultSourceSet.languageSettings, compilerOptions.options
            )
        }

        val kotlinOptions = object : KotlinCommonOptions {
            override val options get() = compilerOptions.options
        }

        return KotlinCompilationImplFactory.CompilerOptionsFactory.Options(compilerOptions, kotlinOptions)
    }
}