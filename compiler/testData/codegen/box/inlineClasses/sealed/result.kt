// CHECK_BYTECODE_LISTING
// LANGUAGE: -JvmInlineValueClasses, +GenericInlineClassParameter, +SealedInlineClasses
// IGNORE_BACKEND: JVM

sealed inline class Result<out T> {
    inline class Ok<T>(val value: T): Result<T>()
    class Throwable(val error: Throwable): Result<Nothing>()
}

fun box(): String = Result.Ok("OK").value