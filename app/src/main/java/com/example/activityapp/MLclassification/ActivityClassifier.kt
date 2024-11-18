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

class ActivityClassifier(context: Context, private val activityLogger: ActivityLogger, private val windowSize: Int = 100) { /// CHANGE WINDOW SIZE
    // Loads activity_model.tflite and performs inference
    private val interpreter: Interpreter
    // Creates empty list of FloatArray elements. Each FloatArray element holds the x, y and z values of sensor readings
    private val buffer = mutableListOf<FloatArray>()

    // Define period (seconds) between making a classification
    private val classification_period = 2
    // This will be the amount of buffer readings removed after every classification is made
    private val bufferReadingsToRemove = classification_period * 25

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        try {
            val modelFile = loadModelFile(context.assets, "activity_model_2.tflite")
            interpreter = Interpreter(modelFile)
        } catch (e: Exception) {
            throw RuntimeException("Error initializing TensorFlow Lite interpreter: ${e.message}")
        }
    }


    // Load tflite model from assets and return it as MappedByteBuffer.
    // AssetManager: Allows access to app assets (i.e. non-code files like model files)
    // MappedByteBuffer: Represents the model file mapped into memory
    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        // Opens the model file and returns an AssetFileDescriptor
        val fileDescriptor = assetManager.openFd(modelPath)
        // Allows app to read data directly from the tflite file
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        // Maps model into memory in read-only mode.
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    // Collects and processes accelerometer data (x, y and z values). Is called in LiveDataActivity.kt
    // String? = Function returns nullable string, may be string or null (if theres not enough data to classify yet)
    fun addSensorData(x: Float, y: Float, z: Float): String? {
        // Adds the new accelerometer data (x, y and z) to the buffer as a Float array containing these values.
        // Adds a single set of three values (x, y and z) to the buffer
        buffer.add(floatArrayOf(x, y, z))
        Log.d("ActivityClassifier", "Buffer size is ${buffer.size}")
        // Check if buffer has reached or exceeded the specified windowSize
        // WindowSize defines how many data points are needed before classification can occur
        if (buffer.size == windowSize) {
            // classify() function called and returns a String representing and activity
            val result = classify()
            Log.d("ActivityClassifier", "Buffer size ${buffer.size} is now the same as window size ${windowSize}. Making classification")
            // Removes the oldest data point from buffer to keep the list size constant
            // Could reduce computational load by increasing the number of data points removed
            buffer.subList(0, bufferReadingsToRemove).clear() // Determine how often a classification should be made
            Log.d("ActivityClassifier", "Removing elements from buffer, buffer size is now ${buffer.size}")
            // Return the classification result.
            result?.let {
                scope.launch {
                    activityLogger.logActivity(it, durationInSeconds = classification_period)
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
        val input = Array(1) {Array(buffer.size) { i -> buffer[i] }}

        // Stores output predictions of model. Stores model's confidence score for each class
        val output = Array(1) { FloatArray(11) }

        // Run inference
        interpreter.run(input, output)

        // Finds max value in output[0] and returns corresponding index. This index represents the predicted activity class
        // If no maximum is found, defaults to -1 (indicates unknown result)
        val activityIndex = output[0].withIndex().maxByOrNull { it.value }?.index ?: -1

        // Map index to activity Name
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