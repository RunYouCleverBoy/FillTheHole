package com.rycbar.fillthehole.runner

import com.rycbar.holefiller.api.ConnectivityMode
import com.rycbar.holefiller.api.HoleFiller
import com.rycbar.holefiller.api.Image
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.CountDownLatch
import javax.imageio.ImageIO

fun main(args: Array<String>) {

    val inputImage: BufferedImage = ImageIO.read(File(args[0]))
    val maskImage: BufferedImage = ImageIO.read(File(args[1]))

    val config = HoleFiller.Configuration(normAdditionCoefficient = 0.01, normExponent = 2f, connectivityMode = ConnectivityMode.Connected8)
    val engine = HoleFiller(config)

    val latch = CountDownLatch(1)
    var image: Image? = null
    engine.heal(inputImage.width, inputImage.height) { x, y ->
        if (maskImage.getRGB(x, y).toGreyScale() <= 0.5f) inputImage.getRGB(x, y).toGreyScale() else config.damagedValueColour
    }.then {
        image = it
        latch.countDown()
    }

    val outImage = BufferedImage(inputImage.width, inputImage.height, BufferedImage.TYPE_INT_RGB)
    for (i in 0 until inputImage.height) {
        for (j in 0 until inputImage.width) {
            outImage.setRGB(i, j, image?.get(j, i)?.times(256)?.toInt() ?: 0)
        }
    }
    latch.await()
}

fun Int.toGreyScale() = (0..3).sumBy { this.shr(it * 8) and 0xFF } / (3f * 256f)
