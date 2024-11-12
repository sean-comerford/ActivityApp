package com.example.activityapp.MLclassification

import android.content.Context
import android.content.res.AssetManager
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ActivityClassifier(context: Context, private val windowSize: Int = 250) { // Set window size to 250
    // Loads activity_model.tflite and performs inference
    private val interpreter: Interpreter
    // Creates empty list of FloatArray elements. Each FloatArray element holds the x, y, and z values of sensor readings
    private val buffer = mutableListOf<FloatArray>()

    init {
        try {
            val modelFile = loadModelFile(context.assets, "activity_model_2.tflite")
            interpreter = Interpreter(modelFile)
            interpreter.allocateTensors() // Ensure tensor allocation
        } catch (e: Exception) {
            throw RuntimeException("Error initializing TensorFlow Lite interpreter: ${e.message}")
        }
    }

    // Load tflite model from assets and return it as MappedByteBuffer
    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    // Collects and processes accelerometer data (x, y, and z values). Is called in LiveDataActivity.kt
    fun addSensorData(x: Float, y: Float, z: Float): String? {
        // Adds the new accelerometer data (x, y, z) to the buffer
        buffer.add(floatArrayOf(x, y, z))

        // Check if buffer has reached the specified windowSize
        if (buffer.size == windowSize) {
            val result = classify()
            buffer.removeAt(0) // Keep buffer size constant by removing the oldest data point
            return result
        }
        return null
    }

    private fun classify(): String {
        if (buffer.any { it.size != 3 }) {
            throw IllegalArgumentException("Each entry in buffer must contain exactly 3 elements (x, y, z).")
        }

        // Prepare input as a 3D array with shape (1, 250, 3)
        val input = Array(1) { Array(buffer.size) { FloatArray(3) } }
        for (i in buffer.indices) {
            input[0][i] = buffer[i]
        }

        // Prepare output array to hold predictions
        val output = Array(1) { FloatArray(11) }

        // Run inference
        interpreter.run(input, output)

        // Find the index with the maximum confidence score in output[0]
        val activityIndex = output[0].withIndex().maxByOrNull { it.value }?.index ?: -1

        // Map index to activity name
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
