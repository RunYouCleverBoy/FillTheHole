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
        println("Syntax error\n")
        println("Missing input: ${e.message}\n")
        println("Syntax: ${ArgsParser.Mode.Mask.token} <Input file> <Input mask file: Defective pixels should be painted in any (r+g+b)/3 < 10%. Others are painted black. Transparency is ignored>")
        println("Syntax: ${ArgsParser.Mode.Mask.token} <Input file> <Hex colour to designate defected pixel>")
        exitProcess(-1)
    }

    val inputFile = try {
        when (val inputFiles = argsParser.inputFiles) {
            is ArgsParser.InputFiles.FileAndHexColour -> File(inputFiles.path)
            is ArgsParser.InputFiles.FileAndMask -> File(inputFiles.path)
            null -> exitProcess(-1) // Not supposed to occur. Validation is done beforehand
        }
    } catch (exception: Exception) {
        print("Failed to open files")
        exitProcess(-1)
    }

    val config = HoleFiller.Configuration(normAdditionCoefficient = 0.01, normExponent = 2f, connectivityMode = ConnectivityMode.Connected8)
    val pixelTranslationDescriptor: PixelTranslationDescriptor = when (val inputArguments = argsParser.inputFiles) {
        is ArgsParser.InputFiles.FileAndMask -> openFilesWithMask(inputArguments, config)
        is ArgsParser.InputFiles.FileAndHexColour -> openFileWithColour(inputArguments, config)
        else -> {
            // Should not get here, ever
            print("Syntax error")
            exitProcess(-1)
        }
    }

    val image: Image? = heal(pixelTranslationDescriptor, config)
    val outImage = PixelManipulators.convertToRgb(pixelTranslationDescriptor.width, pixelTranslationDescriptor.height, image)

    ImageIO.write(outImage, inputFile.extension, inputFile.run { File(this.parent, "${nameWithoutExtension}.out.$extension") })
}

data class PixelTranslationDescriptor(val width: Int, val height: Int, val conversionLambda: (x: Int, y: Int) -> Float)

private fun openFileWithColour(inputArguments: ArgsParser.InputFiles.FileAndHexColour, config: HoleFiller.Configuration): PixelTranslationDescriptor {
    val inputImage: BufferedImage = ImageIO.read(File(inputArguments.path))
    return PixelTranslationDescriptor(inputImage.width, inputImage.height) { x, y ->
        val rawPixel = inputImage.getRGB(x, y)
        val damagedColour = inputArguments.colour
        val redDiff = abs(rawPixel.red - damagedColour.red)
        val greenDiff = abs(rawPixel.green - damagedColour.green)
        val blueDiff = abs(rawPixel.blue - damagedColour.blue)
        if (maxOf(redDiff, greenDiff, blueDiff) > 16) { // Antialias may make things ugly
            rawPixel.toGreyScale()
        } else {
            config.damagedValueColour
        }
    }
}

private fun openFilesWithMask(inputArguments: ArgsParser.InputFiles.FileAndMask, config: HoleFiller.Configuration): PixelTranslationDescriptor {
    val inputImage: BufferedImage = ImageIO.read(File(inputArguments.path))
    val maskImage: BufferedImage = ImageIO.read(File(inputArguments.maskPath))

    // Transparent pixels should be painted black
    val converterLambda = { x: Int, y: Int ->
        if (maskImage.getRGB(x, y).toGreyScale() <= 0.1f) inputImage.getRGB(x, y).toGreyScale() else config.damagedValueColour
    }

    return PixelTranslationDescriptor(inputImage.width, inputImage.height, converterLambda)
}


private fun heal(pixelTranslationDescriptor: PixelTranslationDescriptor, config: HoleFiller.Configuration): Image? {
    val engine = HoleFiller(config)
    val latch = CountDownLatch(1)
    val refTime = System.currentTimeMillis()
    var image: Image? = null
    engine.heal(pixelTranslationDescriptor.width, pixelTranslationDescriptor.height, pixelTranslationDescriptor.conversionLambda) {
        println("Healed the image in ${(System.currentTimeMillis() - refTime) / 1000f}s")
        image = it
        latch.countDown()
    }

    latch.await()
    return image
}



