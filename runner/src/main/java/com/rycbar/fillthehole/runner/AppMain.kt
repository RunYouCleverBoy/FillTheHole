package com.rycbar.fillthehole.runner

import com.rycbar.holefiller.api.ConnectivityMode
import com.rycbar.holefiller.api.HoleFiller
import com.rycbar.holefiller.api.Image
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.CountDownLatch
import javax.imageio.ImageIO
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val argsParser: ArgsParser = try {
        ArgsParser().parse(args)
    } catch (e: ArgsParser.ParserException) {
        print("Syntax error\n")
        print("Missing input: ${e.message}\n")
        print("Syntax: ${ArgsParser.Mode.Mask.token} <Input file> <Input mask file: Anything brighter than 50% is considered a damaged pixel>")
        print("Syntax: ${ArgsParser.Mode.Mask.token} <Input file> <Hex colour to designate defected pixel>")
        exitProcess(-1)
    }

    val config = HoleFiller.Configuration(normAdditionCoefficient = 0.01, normExponent = 2f, connectivityMode = ConnectivityMode.Connected8)
    val engine = HoleFiller(config)

    data class HealerInput(val width: Int, val height: Int, val inputFileName: String, val conversionLambda: (x: Int, y: Int) -> Float)

    val healerInput: HealerInput = when (val inputArguments = argsParser.inputFiles) {
        is ArgsParser.InputFiles.FileAndMask -> {
            val inputImage: BufferedImage = ImageIO.read(File(inputArguments.path))
            val maskImage: BufferedImage = ImageIO.read(File(inputArguments.maskPath))

            val converterLambda = { x:Int, y: Int ->
                if (maskImage.getRGB(x, y).toGreyScale() <= 0.5f) inputImage.getRGB(x, y).toGreyScale() else config.damagedValueColour
            }

            HealerInput(inputImage.width, inputImage.height, inputArguments.path, converterLambda)
        }
        is ArgsParser.InputFiles.FileAndHexColour -> {
            val inputImage: BufferedImage = ImageIO.read(File(inputArguments.path))
            HealerInput(inputImage.width, inputImage.height, inputArguments.path) { x, y ->
                inputImage.getRGB(x, y).let { if (it != inputArguments.colour) it.toGreyScale() else config.damagedValueColour }
            }
        }
        else -> {
            // Should not get here, ever
            print("Syntax error")
            return
        }
    }

    val latch = CountDownLatch(1)
    var image: Image? = null
    engine.heal(healerInput.width, healerInput.height, healerInput.conversionLambda) .then {
        image = it
        latch.countDown()
    }

    latch.await()
    val outImage = BufferedImage(healerInput.width, healerInput.height, BufferedImage.TYPE_INT_RGB)
    for (y in 0 until healerInput.height) {
        for (x in 0 until healerInput.width) {
            outImage.setRGB(y, x, image?.get(x, y)?.times(256)?.toInt() ?: 0)
        }
    }

    val inputFile = File(healerInput.inputFileName)
    ImageIO.write(outImage, inputFile.extension, inputFile.apply { File(this.parent, "${nameWithoutExtension}.out.$extension") })
}

fun Int.toGreyScale() : Float = ((this and 0xFF) + ((this shr 8) and 0xFF) + ((this shr 16) and 0xFF)).toFloat()/(3f*256f)
