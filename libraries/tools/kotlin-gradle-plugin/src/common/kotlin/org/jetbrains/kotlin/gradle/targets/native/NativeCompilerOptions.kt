/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.native

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.CompilerCommonOptions
import org.jetbrains.kotlin.gradle.dsl.CompilerCommonOptionsDefault
import org.jetbrains.kotlin.gradle.plugin.HasCompilerOptions
import org.jetbrains.kotlin.gradle.plugin.runOnceAfterEvaluated
import org.jetbrains.kotlin.gradle.plugin.sources.applyLanguageSettingsToCompilerOptions
import org.jetbrains.kotlin.project.model.LanguageSettings
import org.jetbrains.kotlin.tooling.core.UnsafeApi
import java.util.*

internal class NativeCompilerOptions
/**
 * Unsafe constructor: Not providing languageSettings requires to call 'applyLanguageSettingsToCompilerOptions'  manually!
 */
@UnsafeApi constructor(
    project: Project,
    languageSettings: Optional<LanguageSettings>
) : HasCompilerOptions<CompilerCommonOptions> {

    @OptIn(UnsafeApi::class)
    constructor(project: Project, languageSettings: LanguageSettings) : this(
        project, Optional.of(languageSettings)
    )

    override val options: CompilerCommonOptions = project.objects
        .newInstance(CompilerCommonOptionsDefault::class.java)
        .apply {
            useK2.finalizeValue()

            if (languageSettings.isPresent) {
                @OptIn(UnsafeApi::class)
                applyLanguageSettingsToCompilerOptions(project, languageSettings.get(), this)
            }
        }

    companion object {
        /**
         * Only call when no languageSettings were provided in the constructor!
         */
        @UnsafeApi
        fun applyLanguageSettingsToCompilerOptions(project: Project, languageSettings: LanguageSettings, options: CompilerCommonOptions) {
            project.runOnceAfterEvaluated("apply Kotlin native properties from language settings") {
                applyLanguageSettingsToCompilerOptions(languageSettings, options)
            }
        }
    }
}
