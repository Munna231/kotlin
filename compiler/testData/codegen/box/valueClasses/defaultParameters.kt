// CHECK_BYTECODE_LISTING
// WITH_STDLIB
// TARGET_BACKEND: JVM_IR
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

@JvmInline
value class DPoint(val x: Double = 1.0, val y: Double = 2.0) {
    fun f1(a: Int, b: Int = -1, c: DPoint = DPoint(-2.0, -3.0)) = listOf(this, x, y, a, b, c)
}

@JvmInline
value class DSegment(val p1: DPoint = DPoint(3.0, 4.0), val p2: DPoint = DPoint(5.0, 6.0), val n: Int = 7) {
    fun f2(a: Int, b: Int = -1, c: DPoint = DPoint(-2.0, -3.0)) = listOf(this, p1, p2, n, a, b, c)
}

data class Wrapper(val segment: DSegment = DSegment(DPoint(8.0, 9.0), DPoint(10.0, 11.0)), val n: Int = 12) {
    fun f3(a: Int, b: Int = -1, c: DPoint = DPoint(-2.0, -3.0)) = listOf(this, segment, n, a, b, c)
}

fun box(): String {
    require(DPoint() == DPoint(1.0, 2.0)) { "${DPoint()} ${DPoint(1.0, 2.0)}" }
    require(DPoint(3.0) == DPoint(3.0, 2.0)) { "${DPoint()} ${DPoint(3.0, 2.0)}" }
    require(DPoint(x = 3.0) == DPoint(3.0, 2.0)) { "${DPoint()} ${DPoint(3.0, 2.0)}" }
    require(DPoint(y = 3.0) == DPoint(1.0, 3.0)) { "${DPoint()} ${DPoint(1.0, 3.0)}" }
    require(DPoint().f1(4) == listOf(DPoint(1.0, 2.0), 1.0, 2.0, 4, -1, DPoint(-2.0, -3.0))) {
        DPoint().f1(4).toString()
    }
    require(DPoint().f1(4, 1, DPoint(2.0, 3.0)) == listOf(DPoint(1.0, 2.0), 1.0, 2.0, 4, 1, DPoint(2.0, 3.0))) {
        DPoint().f1(4, 1, DPoint(2.0, 3.0)).toString()
    }
    require(DPoint(-1.0, -2.0).f1(4) == listOf(DPoint(-1.0, -2.0), -1.0, -2.0, 4, -1, DPoint(-2.0, -3.0))) {
        DPoint().f1(4).toString()
    }
    require(DPoint(-1.0, -2.0).f1(4, 1, DPoint(2.0, 3.0)) == listOf(DPoint(-1.0, -2.0), -1.0, -2.0, 4, 1, DPoint(2.0, 3.0))) {
        DPoint().f1(4, 1, DPoint(2.0, 3.0)).toString()
    }

    require(DSegment() == DSegment(DPoint(3.0, 4.0), DPoint(5.0, 6.0), 7)) { DSegment().toString() }
    require(DSegment().f2(100) == listOf(DSegment(), DPoint(3.0, 4.0), DPoint(5.0, 6.0), 7, 100, -1, DPoint(-2.0, -3.0))) {
        DSegment().f2(100).toString()
    }
    require(DSegment().f2(100, b = 1) == listOf(DSegment(), DPoint(3.0, 4.0), DPoint(5.0, 6.0), 7, 100, 1, DPoint(-2.0, -3.0))) {
        DSegment().f2(100, b = 1).toString()
    }

    require(Wrapper() == Wrapper(DSegment(DPoint(8.0, 9.0), DPoint(10.0, 11.0)), 12)) { Wrapper().toString() }
    require(Wrapper().f3(100) == listOf(Wrapper(), DSegment(DPoint(8.0, 9.0), DPoint(10.0, 11.0), 7), 12, 100, -1, DPoint(-2.0, -3.0))) {
        Wrapper().f3(100).toString()
    }
    require(Wrapper().f3(100, b = 1) == listOf(Wrapper(), DSegment(DPoint(8.0, 9.0), DPoint(10.0, 11.0), 7), 12, 100, 1, DPoint(-2.0, -3.0))) {
        Wrapper().f3(100, b = 1).toString()
    }

    return "OK"
}
