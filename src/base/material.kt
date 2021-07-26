package raytracing.base

import java.lang.Math.pow
import java.util.*
import kotlin.math.min
import kotlin.math.sqrt

class Scatter(val ray: Ray?, val color: FColor)

interface Material {
    fun scatter(random: Random, rIn: Ray, hitRecord: HitRecord): Scatter?
};


class Lambertian(val albedo: FColor) : Material {

    override fun scatter(random: Random, rIn: Ray, rec: HitRecord): Scatter? {
        var scatterDirection = randomInHemisphere(random, rec.normal)
        if (scatterDirection.nearZero()) {
            scatterDirection = rec.normal;
        }
        val scattered = Ray(rec.p, scatterDirection)
        return Scatter(scattered, albedo)
    }
};

class Light(val albedo: FColor) : Material {

    override fun scatter(random: Random, rIn: Ray, rec: HitRecord): Scatter? {
        return Scatter(null, albedo)
    }
};

class Metal(val albedo: FColor, val fuzz: Double = 0.0) : Material {
    override fun scatter(random: Random, rIn: Ray, rec: HitRecord): Scatter? {
        val reflected = rIn.direction.unit().reflect(rec.normal)
        val scattered = Ray(rec.p, reflected + fuzz * randomInUnitSphere(random))

        return if (scattered.direction.dot(rec.normal) > 0) {
            Scatter(scattered, albedo)
        } else {
            null
        }
    }

}

class Dielectric(val ir: Double) : Material {

    override fun scatter(random: Random, rIn: Ray, rec: HitRecord): Scatter? {
        val refraction_ratio = if (rec.frontFace) (1.0/ir) else ir

        val unitDirection = rIn.direction.unit()
        val cos_theta = min(-unitDirection.dot(rec.normal), 1.0);
        val sin_theta = sqrt(1.0 - cos_theta*cos_theta);

        val cannot_refract = refraction_ratio * sin_theta > 1.0;

        val direction = if (cannot_refract || reflectance(cos_theta, refraction_ratio) > random.nextDouble())
            unitDirection.reflect(rec.normal);
        else
            unitDirection.refract(rec.normal, refraction_ratio)

        val scattered = Ray(rec.p, direction);
        return Scatter(scattered, FColor(1.0, 1.0, 1.0));
    }


    private fun reflectance(cosine : Double, ref_idx : Double): Double {
        var r0 = (1-ref_idx) / (1+ref_idx)
        r0 = r0*r0
        return r0 + (1-r0)*pow((1 - cosine), 5.0)
    }
};