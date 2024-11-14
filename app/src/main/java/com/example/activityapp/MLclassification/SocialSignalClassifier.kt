package com.example.activityapp.MLclassification

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.example.activityapp.logging.ActivityLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class SocialSignalClassifier(context: Context, private val activityLogger: ActivityLogger, private val windowSize: Int = 200 ) {
    private val interpreter: Interpreter
    private val buffer = mutableListOf<FloatArray>()


    // Define period (seconds) between classification results
    private val classification_period = 1
    // This will be the amount of buffer readings removed after every classification is made
    private val bufferReadingsToRemove = classification_period * 25

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        try {
            val modelFile = loadModelFile(context.assets, "social_signal_model_2.tflite")
            interpreter = Interpreter(modelFile)
        } catch (e: Exception) {
            throw RuntimeException("Error initializing TensorFlow Lite interpreter: ${e.message}")
        }
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    fun addSensorData(x: Float, y: Float, z: Float): String? {
        buffer.add(floatArrayOf(x, y, z))
        if (buffer.size == windowSize) {
            val result = classify()
            buffer.subList(0, bufferReadingsToRemove).clear()  // Determine how often a classification should be made
            result?.let {
                scope.launch {
                    activityLogger.logSocialSignal(it, durationInSeconds = classification_period)
                }
            }
            return result
        }
        return null
    }

    private fun classify(): String {
        if (buffer.any { it.size != 3 }) {
            throw IllegalArgumentException("Each entry in buffer must contain exactly 3 elements (x, y, z).")
        }

        // Input is 2D array. Each row is a collection of x, y and z values
        val input = Array(1){Array(buffer.size) { i -> buffer[i] }}

        // Stores output predictions of model. Stores model's confidence score for each class
        val output = Array(1) { FloatArray(4) }

        // Run inference
        interpreter.run(input, output)

        // Finds max value in output[0] and returns corresponding index. This index represents the predicted activity class
        // If no maximum is found, defaults to -1 (indicates unknown result)
        val activityIndex = output[0].withIndex().maxByOrNull { it.value }?.index ?: -1

        // Map index to activity Name
        return when (activityIndex) {
            0 -> "breathingNormal"
            1 -> "coughing"
            2 -> "hyperventilating"
            3 -> "other"
            else -> "Unknown"
        }
    }
}