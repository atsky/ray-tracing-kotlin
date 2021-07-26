package raytracing.base

import java.awt.Color
import java.util.*
import kotlin.math.roundToInt
import kotlin.math.sqrt

private fun crop(x: Double): Double = when {
    x < 0.0 -> 0.0
    x > 1.0 -> 1.0
    else -> x
}


class FColor(val r: Double, val g: Double, val b: Double) {
    fun intColor(): Int {
        val rc = crop(r)
        val rg = crop(g)
        val rb = crop(b)
        return Color(
                (rc * 255).roundToInt(),
                (rg * 255).roundToInt(),
                (rb * 255).roundToInt()).rgb
    }

    operator fun times(t: Double): FColor {
        return FColor(r * t, g * t, b * t)
    }

    operator fun plus(c: FColor): FColor {
        return FColor(r + c.r, g + c.g, b + c.b)
    }

    fun sqrt(): FColor {
        return FColor(sqrt(r), sqrt(g), sqrt(b))
    }

    operator fun div(i: Int): FColor {
        return FColor(r / i, g / i, b / i)
    }

    operator fun times(c: FColor): FColor {
        return FColor(r * c.r, g * c.g, b * c.b)
    }

    companion object {
        fun random(r: Random): FColor {
            return FColor(r.nextDouble(), r.nextDouble(), r.nextDouble())
        }
    }
}

operator fun Double.times(c: FColor): FColor {
    return FColor(this * c.r, this * c.g, this * c.b)
}