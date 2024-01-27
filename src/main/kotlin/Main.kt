import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import javax.imageio.ImageIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

fun main() {
    val executionTime = measureTimeMillis {
        runBlocking {
            val folderPath =
                "/Users/damianlesniok/Documents/studia/sem4/obliczenia_równoległe/empty-photos/src/main/photos"

            val imageFiles = File(folderPath).listFiles { file ->
                file.isFile && file.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif", "bmp")
            }

            if (imageFiles != null && imageFiles.isNotEmpty()) {

                val numberOfThreads = 5

                val imageQueue: BlockingQueue<File> = LinkedBlockingQueue()
                imageQueue.addAll(imageFiles.toList())

                val jobs = List(numberOfThreads) {
                    launch(Dispatchers.Default) {
                        while (true) {
                            val imageFile = imageQueue.poll() ?: break

                            val originalImage = ImageIO.read(imageFile)

                            if (originalImage == null) {
                                println("Nie udało się załadować obrazu: ${imageFile.absolutePath}")
                                continue
                            }

                            processImage(originalImage)

                            val outputImagePath = imageFile.absolutePath.replaceBeforeLast("/", "src/main/modifiedPhotos")
                            val outputFile = File(outputImagePath)
                            outputFile.parentFile.mkdirs() // Create directories if they don't exist

                            ImageIO.write(originalImage, "jpg", outputFile)

                            println("Wątek ${Thread.currentThread().id} zakończył przetwarzanie dla obrazu: ${imageFile.absolutePath}")
                        }
                    }
                }

                jobs.forEach { it.join() }

                println("Wszystkie wątki zakończyły przetwarzanie.")
            } else {
                println("Brak plików obrazów w folderze.")
            }
        }
    }

    println("Całkowity czas wykonania skryptu: $executionTime ms")
}

fun processImage(image: BufferedImage) {
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {

            val rgb = image.getRGB(x, y)
            val invertedRgb = invertColor(rgb)
            image.setRGB(x, y, invertedRgb)
        }
    }
}

fun invertColor(rgb: Int): Int {
    val red = 255 - (rgb shr 16) and 0xFF
    val green = 255 - (rgb shr 8) and 0xFF
    val blue = 255 - (rgb and 0xFF)

    return (red shl 16) or (green shl 8) or blue
}
