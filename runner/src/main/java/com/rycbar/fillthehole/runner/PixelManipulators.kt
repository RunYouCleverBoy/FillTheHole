package com.rycbar.fillthehole.runner

import com.rycbar.holefiller.api.Image
import java.awt.image.BufferedImage

object PixelManipulators {
    fun convertToRgb(width: Int, height: Int, image: Image?): BufferedImage {
        val outImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until height) {
            for (x in 0 until width) {
                outImage.setRGB(x, y, image?.get(x, y)?.toRgb() ?: 0)
            }
        }
        return outImage
    }
}

fun Int.toGreyScale() : Float = (red + green + blue) / (3f * 256f)
fun Float.toRgb() : Int = (this * 256f).toInt().let { it.shl(16) or it.shl(8) or it }
inline val Int.red get() = ((this shr 16) and 0xFF)
inline val Int.green get() = ((this shr 8) and 0xFF)
inline val Int.blue get() = ((this shr 0) and 0xFF)