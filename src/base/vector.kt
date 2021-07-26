package raytracing.base

import java.util.*
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

class Vec(val x: Double, val y: Double, val z: Double) {
    operator fun minus(v: Vec): Vec {
        return Vec(x - v.x, y - v.y, z - v.z)
    }

    infix fun dot(v: Vec): Double {
        return x * v.x + y * v.y + z * v.z
    }

    fun cross(v: Vec) : Vec {
        return Vec(this.y * v.z - this.z * v.y,
                this.z * v.x - this.x * v.z,
                this.x * v.y - this.y * v.x);
    }

    operator fun div(s: Int): Vec {
        return Vec(x / s, y / s, z / s)
    }

    operator fun plus(v: Vec): Vec {
        return Vec(x + v.x, y + v.y, z + v.z)
    }

    fun unit(): Vec {
        val c = 1.0 / length()
        return Vec(x * c, y * c, z * c)
    }

    fun length(): Double {
        return sqrt(x * x + y * y + z * z)
    }

    operator fun div(r: Double): Vec {
        return Vec(x / r, y / r, z / r)
    }

    operator fun unaryMinus(): Vec {
        return Vec(-x, -y, -z)
    }

    fun lengthSquared() = x * x + y * y + z * z

    fun nearZero(): Boolean {
        val s = 1e-8;
        return (abs(x) < s) && (abs(y) < s) && (abs(z) < s)
    }

    fun reflect(n: Vec): Vec {
        return this - 2 * this.dot(n) * n;
    }

    fun refract(n: Vec, etai_over_etat: Double): Vec {
        val cos_theta = min((-this).dot(n), 1.0)
        val r_out_perp = etai_over_etat * (this + cos_theta * n);
        val r_out_parallel = -sqrt(abs(1.0 - r_out_perp.lengthSquared())) * n;
        return r_out_perp + r_out_parallel;
    }

}


operator fun Double.times(v: Vec): Vec {
    return Vec(this * v.x, this * v.y, this * v.z)
}

class Ray(val from: Vec, val direction: Vec) {
    fun at(t: Double): Vec {
        return from + t * direction
    }
}

fun randomDouble(r: Random, from: Double, to: Double): Double {
    return from + (to - from) * r.nextDouble()
}

fun randomVec(r: Random, from: Double, to: Double): Vec {
    return Vec(
            randomDouble(r, from, to),
            randomDouble(r, from, to),
            randomDouble(r, from, to)
    )
}

fun randomInUnitSphere(r: Random): Vec {
    while (true) {
        val p = randomVec(r, -1.0, 1.0);
        if (p.lengthSquared() >= 1) continue;
        return p
    }
}

fun randomUnitVector(r: Random): Vec {
    while (true) {
        val p = randomVec(r, -1.0, 1.0);
        val lengthSquared = p.lengthSquared()
        if (lengthSquared >= 1 || lengthSquared < 1e-20) continue;
        return p.unit()
    }
}

fun randomInUnitDisk(r: Random): Vec {
    while (true) {
        val p = Vec(randomDouble(r,-1.0, 1.0), randomDouble(r,-1.0, 1.0), 0.0)
        if (p.lengthSquared() >= 1) continue
        return p
    }
}

fun randomInHemisphere(r: Random, normal: Vec): Vec {
    val inUnitSphere = randomInUnitSphere(r)
    return if (inUnitSphere.dot(normal) > 0.0)
        inUnitSphere
    else
        -inUnitSphere
}