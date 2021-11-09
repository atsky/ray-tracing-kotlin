package raytracing.base

import java.lang.Double.max
import java.lang.Double.min
import java.lang.Math.sqrt
import java.util.*

interface Hitable {
    fun hit(ray: Ray, tMin: Double, tMax: Double): HitRecord?
}

class AxisAlignedBoundingBox(
    var minimum: Vec,
    var maximum: Vec
) {

    fun hit(r: Ray, t_min_val: Double, t_max_val: Double): Boolean {
        var t_min = t_min_val
        var t_max = t_max_val

        for (a in 0..2) {
            val time0 = (minimum[a] - r.from[a]) / r.direction[a]
            val time1 = (maximum[a] - r.from[a]) / r.direction[a]
            val t0 = min(time0, time1)
            val t1 = max(time0, time1)
            t_min = max(t0, t_min)
            t_max = min(t1, t_max)
            if (t_max <= t_min)
                return false
        }
        return true
    }

}

class Sphere(val center: Vec, val r: Double, val material: Material) : Hitable {

    override fun hit(ray: Ray, tMin: Double, tMax: Double): HitRecord? {
        val oc = ray.from - center;
        val a = ray.direction dot ray.direction

        val halfB = oc.dot(ray.direction)

        val c = oc.dot(oc) - r * r;

        val discriminant = halfB * halfB - a * c

        if (discriminant < 0) {
            return null
        } else {
            var root = (-halfB - sqrt(discriminant)) / a;
            if (root < tMin || tMax < root) {
                root = (-halfB + sqrt(discriminant)) / a;
                if (root < tMin || tMax < root) {
                    return null
                }
            }

            val p = ray.at(root)
            return makeHit(
                ray,
                p,
                root,
                (p - center) / this.r,
                material
            )
        }
    }
}

class Plane(val y: Double) : Hitable {
    override fun hit(ray: Ray, tMin: Double, tMax: Double): HitRecord? {
        if (ray.from.y < y) {
            return null
        }
        if (ray.direction.y > 0) {
            return null
        }
        val t = (y - ray.from.y) / ray.direction.y

        val p = ray.at(t)

        val p2 = p

        val materialGround = if ((p2.x.toInt() + p2.z.toInt()) % 2 == 0) {
            Metal(FColor(0.8, 0.8, 0.8), 0.3)
        } else {
            Metal(FColor(0.2, 0.2, 0.2), 0.3)
        }

        return HitRecord(
            p,
            Vec(0.0, 1.0, 0.0),
            t,
            materialGround,
            true
        )
    }

}


class HitRecord(
    val p: Vec,
    val normal: Vec,
    val t: Double,
    val material: Material,
    val frontFace: Boolean
) {

    fun scatter(random: Random, r: Ray): Scatter? {
        return material.scatter(random, r, this)
    }

    /*
    fun set_face_normal(const ray& r, const vec3& outward_normal) {
        front_face = dot(r.direction(), outward_normal) < 0;
        normal = front_face ? outward_normal :-outward_normal;
    }
    */
};

fun makeHit(r: Ray, p: Vec, t: Double, outwardNormal: Vec, material: Material): HitRecord {
    val frontFace = r.direction.dot(outwardNormal) < 0
    val normal = if (frontFace) outwardNormal else -outwardNormal

    return HitRecord(
        p,
        normal,
        t,
        material,
        frontFace
    )
}


class World(val objects: List<Hitable>) {
    fun hit(r: Ray, tMin: Double, tMax: Double): HitRecord? {
        var rec: HitRecord? = null
        var closestSoFar = tMax

        for (obj in objects) {
            val tempHit = obj.hit(r, tMin, closestSoFar)
            if (tempHit != null) {
                closestSoFar = tempHit.t
                rec = tempHit
            }
        }

        return rec
    }

    fun rayColor(random: Random, r: Ray, depth: Int): FColor {
        if (depth == 0) {
            return FColor(0.0, 0.0, 0.0)
        }
        val hit = this.hit(r, 0.0001, Double.POSITIVE_INFINITY)
        return if (hit != null) {
            val scatter = hit.scatter(random, r)
            if (scatter != null) {
                val ray = scatter.ray
                if (ray != null) {
                    scatter.color * rayColor(random, ray, depth - 1)
                } else {
                    scatter.color
                }
            } else {
                FColor(0.0, 0.0, 0.0)
            }
        } else {
            val unit_direction = r.direction.unit()
            if (unit_direction.dot(Vec(0.0, 1.0, -1.0).unit()) > 0.9) {
                FColor(1.0, 1.0, 1.0)
            } else {
                FColor(0.1, 0.1, 0.1)
            }
        }
    }
}

class Camera(
    val origin: Vec,
    val lower_left_corner: Vec,
    val horizontal: Vec,
    val vertical: Vec,
    aperture: Double
) {

    val lens_radius = aperture / 2;


    fun getRay(r: Random, u: Double, v: Double): Ray {
        val rd = lens_radius * randomInUnitDisk(r)
        val offset = Vec(u * rd.x, v * rd.y, 0.0)
        return Ray(origin, lower_left_corner + u * horizontal + v * vertical - origin - offset);
    }


}

fun degrees_to_radians(degrees: Double): Double {
    return degrees * Math.PI / 180.0;
}

fun makeCamera(
    lookfrom: Vec,
    lookat: Vec,
    vup: Vec,
    vfov: Double,
    aspect_ratio: Double,
    aperture: Double,
    focus_dist: Double
): Camera {
    val theta = degrees_to_radians(vfov)
    val h = Math.tan(theta / 2)
    val viewport_height = 2.0 * h
    val viewport_width = aspect_ratio * viewport_height

    val w = (lookfrom - lookat).unit()
    val u = vup.cross(w).unit()
    val v = w.cross(u)

    val origin = lookfrom
    val horizontal = focus_dist * viewport_width * u
    val vertical = focus_dist * viewport_height * v
    val lower_left_corner = origin - horizontal / 2 - vertical / 2 - focus_dist * w

    return Camera(origin, lower_left_corner, horizontal, vertical, aperture)
}


fun makeCamera(): Camera {
    val focalLength = 4.0

    val viewport_height = 2.0 * 4
    val viewport_width = viewport_height
    val origin = Vec(0.0, 0.0, 1.0)
    val horizontal = Vec(viewport_width, 0.0, 0.0)
    val vertical = Vec(0.0, viewport_height, 0.0)
    val lower_left_corner = origin - horizontal / 2 - vertical / 2 - Vec(0.0, 0.0, focalLength)

    return Camera(origin, lower_left_corner, horizontal, vertical, 0.1)
}