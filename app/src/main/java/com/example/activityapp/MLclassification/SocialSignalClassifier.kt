package com.example.activityapp.MLclassification

import android.content.Context
import android.content.res.AssetManager
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class SocialSignalClassifier(context: Context, private val windowSize: Int = 300) {
    private val interpreter: Interpreter
    private val buffer = mutableListOf<FloatArray>()

    init {
        val modelFile = loadModelFile(context.assets, "social_signal_model.tflite")
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


        // Manually flatten the buffer
        val input = FloatArray(buffer.size * 3) // Assuming each FloatArray in buffer has 3 elements (x, y, z)
        var index = 0
        for (array in buffer) {
            for (value in array) {
                input[index++] = value
            }
        }

        val output = Array(1) { FloatArray(4) }  // Adjust based on model output classes
        interpreter.run(input, output)
        val signalIndex = output[0].withIndex().maxByOrNull { it.value }?.index ?: -1
        return when (signalIndex) {
            0 -> "breathingNormal"
            1 -> "coughing"
            2 -> "hyperventilating"
            3 -> "other"
            else -> "Unknown"
        }
    }
}