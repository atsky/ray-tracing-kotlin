package raytracing

import raytracing.base.*
import java.awt.Dimension
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.util.*
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.concurrent.thread


class Drawer(val width: Int, val height: Int) {
    val image: BufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

    var repaint: (() -> Unit)? = null

    fun start() {
        thread(start = true, isDaemon = true) {
            doDraw()
        }
    }

    private fun doDraw() {
        val lookfrom = Vec(15.0, 2.0, 3.0);
        val lookat = Vec(0.0, 0.0, 0.0);
        val vup = Vec(0.0, 1.0, 0.0);
        val dist_to_focus = 15.0;
        val aperture = 0.1;
        val camera = makeCamera(lookfrom, lookat, vup, 20.0, 1.0, aperture, dist_to_focus);

        val material_ground = Lambertian(FColor(0.8, 0.8, 0.0));
        val material_center = Lambertian(FColor(0.7, 0.3, 0.3));
        val material_left = Dielectric(1.5);
        val material_right = Metal(FColor(0.8, 0.6, 0.2), 0.01);


        val objects = mutableListOf(
                Plane(0.0),
                Sphere(Vec(0.0, 1.0, 0.0), 1.0, material_left),
                Sphere(Vec(-4.0, 1.0, 0.0), 1.0, material_center),
                Sphere(Vec(4.0, 1.0, 0.0), 1.0, material_right)
        )

        val random = Random()

        for (a in -11..11) {
            for (b in -11..11) {
                val choose_mat = random.nextDouble()

                val center = Vec(a + 0.9 * random.nextDouble(), 0.15, b + 0.9 * random.nextDouble());

                if ((center - Vec(4.0, 0.2, 0.0)).length() > 0.9) {
                    val sphereMaterial = if (choose_mat < 0.3) {
                        val albedo = FColor.random(random) * FColor.random(random)
                        Lambertian(albedo);
                    } else if (choose_mat < 0.6) {
                        // metal
                        val albedo = FColor.random(random)
                        val fuzz = randomDouble(random, 0.0, 0.5)
                        Metal(albedo, fuzz)
                    } else if (choose_mat < 0.8) {
                        Light(FColor.random(random))
                    } else {
                        Dielectric(1.5);
                    }
                    objects.add(Sphere(center, 0.15, sphereMaterial));
                }
            }
        }

        val w = World(objects)

        val colors = Array<Array<FColor>>(width) { it ->
            Array(height) {
                FColor(0.0, 0.0, 0.0)
            }
        }

        val counts = Array<Array<Int>>(width) { it ->
            Array(height) {
                0
            }
        }

        while (true) {
            for (i in 0 until width) {
                for (j in 0 until height) {
                    val u = (i + random.nextDouble()) / (width - 1);
                    val v = (j + random.nextDouble()) / (height - 1);

                    val r = camera.getRay(random, u, v)

                    colors[i][j] += w.rayColor(Random(), r, 50)
                    counts[i][j] += 1
                }
            }
            synchronized(image) {
                for (i in 0 until width) {
                    for (j in 0 until height) {
                        val color = colors[i][j] / counts[i][j]
                        image.setRGB(i, height - 1 - j, (color).sqrt().intColor())
                    }
                }

            }
            repaint?.invoke()
        }
    }
}

class MainPanel(val drawer: Drawer) : JPanel() {
    init {
        preferredSize = Dimension(drawer.width, drawer.height)

    }

    override fun paint(g: Graphics) {
        super.paint(g)
        synchronized(drawer.image) {
            g.drawImage(drawer.image, 0, 0, null)
        }
    }
}

fun main(args: Array<String>) {
    val frame = JFrame("Ray Tracing")
    val drawer = Drawer(600, 600)
    drawer.start()
    val mainPanel = MainPanel(drawer)
    frame.contentPane = mainPanel
    drawer.repaint = { mainPanel.repaint() }
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.pack()
    frame.isVisible = true
}