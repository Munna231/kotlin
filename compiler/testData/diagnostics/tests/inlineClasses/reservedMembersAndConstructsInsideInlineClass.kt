// FIR_IDENTICAL
// !LANGUAGE: +InlineClasses, -JvmInlineValueClasses, +CustomEqualsInValueClasses
// !DIAGNOSTICS: -UNUSED_PARAMETER

inline class IC1(val x: Any) {
    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>box<!>() {}
    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>box<!>(x: Any) {}

    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>unbox<!>() {}
    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>unbox<!>(x: Any) {}

    override fun <!INEFFICIENT_EQUALS_OVERRIDING_IN_VALUE_CLASS!>equals<!>(other: Any?): Boolean = true
    override fun hashCode(): Int = 0
}

inline class IC2(val x: Any) {
    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>box<!>(x: Any) {}
    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>box<!>(): Any = TODO()

    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>unbox<!>(x: Any) {}
    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>unbox<!>(): Any = TODO()

    fun equals(my: Any, other: Any): Boolean = true
    fun hashCode(a: Any): Int = 0
}

inline class IC3(val x: Any) {
    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>box<!>(x: Any): Any = TODO()
    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>unbox<!>(x: Any): Any = TODO()

    fun equals(): Boolean = true
}

interface WithBox {
    fun box(): String
}

inline class IC4(val s: String) : WithBox {
    override fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>box<!>(): String = ""
}

inline class IC5(val a: String) {
    constructor(i: Int) : this(i.toString()) {
        TODO("something")
    }
}

inline class IC6(val a: String) {
    fun <!TYPE_PARAMETER_ON_TYPED_VALUE_CLASS_EQUALS!><T><!> equals(other: IC6): Boolean = true
}

inline class IC7<T>(val a: String) {
    fun equals(other: IC7<*>): Boolean = true
}

inline class IC8<T>(val a: String) {
    fun equals(other: <!TYPE_ARGUMENT_ON_TYPED_VALUE_CLASS_EQUALS!>IC8<T><!>): Boolean = true
}

inline class IC9<T>(val a: String) {
    fun equals(other: <!TYPE_ARGUMENT_ON_TYPED_VALUE_CLASS_EQUALS!>IC9<String><!>): Boolean = true
}
