package com.rycbar.fillthehole.runner

import java.lang.Exception
import java.util.*

class ArgsParser {
    enum class Mode(val token: String){ Mask("mask"), ImageAndColour("col") }
    var inputFiles: InputFiles? = null
    fun parse(args: Array<String>) : ArgsParser {
        val iterator = args.iterator()
        val item = iterator.nextOrThrow("Missing arguments")
        inputFiles = when (item.toLowerCase(Locale.ENGLISH)) {
            Mode.ImageAndColour.token -> InputFiles.FileAndHexColour(iterator.nextOrThrow("Missing source file"), iterator.nextHexOrThrow("Missing damaged pixel colour"))
            Mode.Mask.token -> InputFiles.FileAndMask(iterator.nextOrThrow("Missing source file"), iterator.nextOrThrow("Missing Mask"))
            else -> InputFiles.FileAndMask(iterator.nextOrThrow("Missing source file"), iterator.nextOrThrow("Missing Mask"))
        }

        return this
    }

    sealed class InputFiles {
        data class FileAndHexColour(val path: String, val colour: Int) : InputFiles()
        data class FileAndMask(val path: String, val maskPath: String) : InputFiles()
    }

    class ParserException(message: String): IllegalArgumentException(message)
    private fun Iterator<String>.nextOrThrow(message: String) = if (hasNext()) next() else throw ParserException(message)
    private fun Iterator<String>.nextHexOrThrow(message: String) = try {
        next().replace("#", "").replace("x", "", ignoreCase = true).toInt(16)
    } catch (exception: Exception) { throw ParserException(message) }
}