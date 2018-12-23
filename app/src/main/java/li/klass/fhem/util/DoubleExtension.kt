package li.klass.fhem.util

fun Double.equalByEpsilon(other: Double, epsilon: Double = 0.001): Boolean =
        Math.abs(this - other) < epsilon