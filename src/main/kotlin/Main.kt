package org.example

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.filter.GrayscaleFilter
import com.sksamuel.scrimage.nio.JpegWriter
import com.sksamuel.scrimage.pixels.Pixel
import java.io.File
import kotlin.math.exp
import kotlin.math.pow

private fun gaussianFunction(x: Int, y: Int, standardDeviation: Double): Double {
    val coefficient = 1 / (Math.PI * 2 * standardDeviation.pow(2.0))
    val exponent = exp(-((x * x + y * y) / (2 * standardDeviation.pow(2.0))))
    return coefficient * exponent
}

private fun createGaussianKernel(kernelSize: Int = 3, standardDeviation: Double = 0.2): Array<Array<Double>> {
    val kernel = Array(kernelSize) { Array(kernelSize) { 0.0 } }

    val center = (kernelSize - 1) / 2
    for (x in 0 until kernelSize) {
        for (y in 0 until kernelSize) {
            kernel[x][y] = gaussianFunction(x - center, y - center, standardDeviation)
        }
    }

    val totalSum = kernel.sumOf { row -> row.sum() }
    for (x in 0 until kernelSize) {
        for (y in 0 until kernelSize) {
            kernel[x][y] /= totalSum
        }
    }
    return kernel
}

fun applyGaussianBlur(image: ImmutableImage, kernelSize: Int = 3, standardDeviation: Double = 0.2): ImmutableImage? {
    val gaussianKernel = createGaussianKernel(kernelSize, standardDeviation)
    val blurredImage = image.copy()

    val offsetX = kernelSize / 2
    val offsetY = kernelSize / 2
    for (row in offsetY until blurredImage.height - offsetY) {
        for (col in offsetX until blurredImage.width - offsetX) {
            var redSum = 0.0
            var greenSum = 0.0
            var blueSum = 0.0

            for (kx in -offsetX..offsetX) {
                for (ky in -offsetY..offsetY) {
                    val pixelX = (col + kx).coerceIn(0, blurredImage.width - 1)
                    val pixelY = (row + ky).coerceIn(0, blurredImage.height - 1)

                    val pixelColor = blurredImage.pixel(pixelX, pixelY)
                    val weight = gaussianKernel[kx + offsetX][ky + offsetY]

                    redSum += pixelColor.red() * weight
                    greenSum += pixelColor.green() * weight
                    blueSum += pixelColor.blue() * weight
                }
            }
            blurredImage.setPixel(
                Pixel(
                    col,
                    row,
                    redSum.toInt().coerceIn(0, 255),
                    greenSum.toInt().coerceIn(0, 255),
                    blueSum.toInt().coerceIn(0, 255),
                    1
                )
            )
        }
    }
    return blurredImage
}

fun blurImage() {
    val imageFile = File("src/main/resources/cat.jpg")
    val inputImage = ImmutableImage.loader().fromFile(imageFile)
    val jpegWriter = JpegWriter()
    val kernelSize = 15
    val standardDeviation = 5.0
    val blurredImage = applyGaussianBlur(inputImage, kernelSize, standardDeviation)
    blurredImage?.output(jpegWriter, File("src/main/resources/cat-blur.jpg"))
}

fun main(args: Array<String>) {
    blurImage()
}
