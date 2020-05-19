package com.rycbar.fillthehole.runner

import com.rycbar.holefiller.api.ConnectivityMode
import com.rycbar.holefiller.api.HoleFiller
import com.rycbar.holefiller.api.Image
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.CountDownLatch
import javax.imageio.ImageIO
import kotlin.math.abs
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

    val inputFile = when (val inputFiles = argsParser.inputFiles) {
        is ArgsParser.InputFiles.FileAndHexColour -> File(inputFiles.path)
        is ArgsParser.InputFiles.FileAndMask -> File(inputFiles.path)
        null -> return // Not supposed to occur. Validation is done beforehand
    }

    val config = HoleFiller.Configuration(normAdditionCoefficient = 0.01, normExponent = 2f, connectivityMode = ConnectivityMode.Connected8)
    val healerInput: HealerInput = when (val inputArguments = argsParser.inputFiles) {
        is ArgsParser.InputFiles.FileAndMask -> paramsWithMask(inputArguments, config)
        is ArgsParser.InputFiles.FileAndHexColour -> paramsWithColour(inputArguments, config)
        else -> {
            // Should not get here, ever
            print("Syntax error")
            return
        }
    }

    val image: Image? = heal(healerInput, config)
    val outImage = PixelManipulators.convertToRgb(healerInput.width, healerInput.height, image)

    ImageIO.write(outImage, inputFile.extension, inputFile.run { File(this.parent, "${nameWithoutExtension}.out.$extension") })
}

data class HealerInput(val width: Int, val height: Int, val conversionLambda: (x: Int, y: Int) -> Float)

private fun paramsWithColour(inputArguments: ArgsParser.InputFiles.FileAndHexColour, config: HoleFiller.Configuration): HealerInput {
    val inputImage: BufferedImage = ImageIO.read(File(inputArguments.path))
    return HealerInput(inputImage.width, inputImage.height) { x, y ->
        val rawPixel = inputImage.getRGB(x, y)
        val damagedColour = inputArguments.colour
        val redDiff = abs(rawPixel.red - damagedColour.red)
        val greenDiff = abs(rawPixel.green - damagedColour.green)
        val blueDiff = abs(rawPixel.blue - damagedColour.blue)
        if (maxOf(redDiff, greenDiff, blueDiff) > 16) {
            rawPixel.toGreyScale()
        } else {
            config.damagedValueColour
        }
    }
}

private fun paramsWithMask(inputArguments: ArgsParser.InputFiles.FileAndMask, config: HoleFiller.Configuration): HealerInput {
    val inputImage: BufferedImage = ImageIO.read(File(inputArguments.path))
    val maskImage: BufferedImage = ImageIO.read(File(inputArguments.maskPath))

    val converterLambda = { x: Int, y: Int ->
        if (maskImage.getRGB(x, y).toGreyScale() <= 0.5f) inputImage.getRGB(x, y).toGreyScale() else config.damagedValueColour
    }

    return HealerInput(inputImage.width, inputImage.height, converterLambda)
}


private fun heal(healerInput: HealerInput, config: HoleFiller.Configuration): Image? {
    val engine = HoleFiller(config)
    val latch = CountDownLatch(1)
    val refTime = System.currentTimeMillis()
    var image: Image? = null
    engine.heal(healerInput.width, healerInput.height, healerInput.conversionLambda) {
        println("Healed the image in ${(System.currentTimeMillis() - refTime) / 1000f}s")
        image = it
        latch.countDown()
    }

    latch.await()
    return image
}



