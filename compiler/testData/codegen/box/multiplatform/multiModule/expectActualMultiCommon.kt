// TARGET_BACKEND: JVM
// !LANGUAGE: +MultiPlatformProjects

// MODULE: common1
// TARGET_PLATFORM: Common
// FILE: common1.kt

expect fun f1(): String

fun g1() = f1()

// MODULE: common2
// TARGET_PLATFORM: Common
// FILE: common2.kt

expect fun f2(): String

fun g2() = f2()

// MODULE: jvm()()(common1, common2)
// TARGET_PLATFORM: JVM
// FILE: main.kt
actual fun f1(): String = "O"
actual fun f2(): String = "K"

fun box() = g1() + g2()