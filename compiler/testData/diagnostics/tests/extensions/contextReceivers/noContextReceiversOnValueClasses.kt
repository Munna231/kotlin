// !LANGUAGE: +InlineClasses, -JvmInlineValueClasses, +ContextReceivers, +ValueClasses
// WITH_STDLIB
// SKIP_TXT
// WORKS_WHEN_VALUE_CLASS

class A

<!VALUE_CLASS_CANNOT_HAVE_CONTEXT_RECEIVERS!>context(A)<!>
OPTIONAL_JVM_INLINE_ANNOTATION
value class B(val x: Int)

<!VALUE_CLASS_CANNOT_HAVE_CONTEXT_RECEIVERS!>context(A)<!>
OPTIONAL_JVM_INLINE_ANNOTATION
value class C(val x: Int, val y: Int)
