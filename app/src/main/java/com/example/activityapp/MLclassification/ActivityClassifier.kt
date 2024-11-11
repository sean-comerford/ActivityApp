package com.example.activityapp.MLclassification
import android.content.Context
import android.content.res.AssetManager
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ActivityClassifier(context: Context, private val windowSize: Int = 300) { /// CHANGE WINDOW SIZE
    private val interpreter: Interpreter
    private val buffer = mutableListOf<FloatArray>()

    init {
        val modelFile = loadModelFile(context.assets, "activity_model_2.tflite")
        interpreter = Interpreter(modelFile)
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    fun addSensorData(x: Float, y: Float, z: Float): String? {
        buffer.add(floatArrayOf(x, y, z))
        if (buffer.size >= windowSize) {
            val result = classify()
            buffer.removeAt(0)  // Keep buffer size constant
            return result
        }
        return null
    }

    private fun classify(): String {
        // Flatten the buffer into a 1D array as input for the model
        //val input = buffer.flatten().toFloatArray()

        // Manually flatten the buffer CHANGE NOT NEEDED
        val input = FloatArray(buffer.size * 3) // Assuming each FloatArray in buffer has 3 elements (x, y, z)
        var index = 0
        for (array in buffer) {
            for (value in array) {
                input[index++] = value
            }
        }


        // Adjust output array size based on your model's output shape
        val output = Array(1) { FloatArray(10) }  // Here, 3 is the number of classes; adjust if needed

        // Run inference
        interpreter.run(input, output)

        // Find the index of the maximum value in the output array
        val activityIndex = output[0].withIndex().maxByOrNull { it.value }?.index ?: -1

        // Return the classification label based on the index
        return when (activityIndex) {
            0 -> "ascending"
            1 -> "shuffleWalking"
            2 -> "sittingStanding"
            3 -> "miscMovement"
            4 -> "normalWalking"
            5 -> "lyingBack"
            6 -> "lyingLeft"
            7 -> "lyingRight"
            8 -> "lyingStomach"
            9 -> "descending"
            10 -> "running"
            else -> "Unknown"
        }
    }
}
