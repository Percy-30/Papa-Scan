package com.example.imagerecognitionapp.utils

import android.R
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import com.example.imagerecognitionapp.data.model.RecognitionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.Tensor
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject

/**
 * Clase que ayuda a utilizar el modelo de TensorFlow Lite.
 */
class TensorFlowHelper @Inject constructor(@ApplicationContext private val context: Context) {
    private lateinit var interpreter: Interpreter
    private lateinit var inputTensor: Tensor
    private lateinit var outputTensor: Tensor
    private val model: Interpreter
    private val inputSize = 250  // Tamaño de entrada esperado por el modelo

    private var error: String? = null

    private val classNames: List<String> by lazy { loadLabels() }

    companion object {
        private const val NUM_CLASSES = 4 // Ajusta este valor según el número de clases en tu modelo
        private const val THRESHOLD = 0.5f // Ajusta este valor según tus necesidades de detección
    }

    init {
        //loadModel()
        //val model = loadModelFile(context, "modelo_cnn.tflite")
        //val modelFile = FileUtil.loadMappedFile(context, "model_mobilenet.tflite")
        val modelFile = FileUtil.loadMappedFile(context, "ml/model_mobilenet.tflite")
        model = Interpreter(modelFile)
    }

    fun getInputShape(): IntArray {
        return inputTensor.shape()
    }

    private fun loadLabels(): List<String> {
        return context.assets.open("labels.txt").bufferedReader().useLines { lines ->
            lines.toList()
        }
    }

    private fun loadModel() {
        if (!::interpreter.isInitialized) {
            val model = loadModelFile(context, "model_mobilenet.tflite")
            interpreter = Interpreter(model)

            inputTensor = interpreter.getInputTensor(0)
            outputTensor = interpreter.getOutputTensor(0)

            val outputShape = outputTensor.shape()
            if (outputShape.size != 2 || outputShape[1] != NUM_CLASSES) {
                throw RuntimeException("La forma del tensor de salida no es la esperada. Forma: ${outputShape.joinToString()}")
            }
        }
    }

    private fun preprocessBitmap(bitmap: Bitmap): ByteBuffer {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3).apply {
            order(ByteOrder.nativeOrder())
            rewind()
        }

        for (y in 0 until inputSize) {
            for (x in 0 until inputSize) {
                val pixel = scaledBitmap.getPixel(x, y)
                byteBuffer.putFloat(Color.red(pixel) / 255.0f)
                byteBuffer.putFloat(Color.green(pixel) / 255.0f)
                byteBuffer.putFloat(Color.blue(pixel) / 255.0f)
            }
        }
        return byteBuffer
    }



    fun runInference(bitmap: Bitmap): FloatArray {
        // Preprocesar la imagen
        val inputImage = preprocessBitmap(bitmap)

        // Salida del modelo
        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 4), DataType.FLOAT32)  // Asegúrate de que la forma coincida con el modelo
        //val outputBuffer = TensorBuffer.createFixedSize(outputTensor.shape(), DataType.FLOAT32)  // Asegúrate de que la forma coincida con el modelo

        // Ejecutar la inferencia
        model.run(inputImage, outputBuffer.buffer.rewind())

        // Obtener el arreglo de resultados
        return outputBuffer.floatArray
    }

    suspend fun runInferenceWithUnknownDetection(file: File): Pair<Boolean, RecognitionResult> {
        val bitmap = BitmapFactory.decodeFile(file.path) ?: return Pair(true, RecognitionResult("No identificado", 0f))
        //val inputBuffer = preprocessImage(bitmap)
        val outputBuffer = TensorBuffer.createFixedSize(outputTensor.shape(), DataType.FLOAT32)

        // interpreter.run(inputBuffer.buffer, outputBuffer.buffer.rewind())

        val results = outputBuffer.floatArray
        val maxIndex = results.indices.maxByOrNull { results[it] } ?: 0

        return if (results[maxIndex] > THRESHOLD) {
            Pair(false, mapResultToRecognition(results, maxIndex))
        } else {
            Pair(true, RecognitionResult("No identificado", 0f))
        }
    }

    private fun mapResultToRecognition(results: FloatArray, classIndex: Int): RecognitionResult {
        return if (classIndex in results.indices) {
            RecognitionResult(
                classNames[classIndex],
                results[classIndex]
            )
        } else {
            RecognitionResult("No identificado", 0f)
        }
    }

    private fun getOutputSize(): Int {
        // Devuelve el tamaño de la salida basado en las etiquetas
        // Puedes ajustar este valor manualmente o inferirlo del modelo
        return 3  // Número de clases del modelo (ajustar según el modelo)
    }

    fun getError(): String? {
        return error
    }

    /*private fun loadModelFile(context: Context, filename: String): ByteBuffer {
        return try {
            context.assets.open(filename).use { inputStream ->
                val buffer = ByteBuffer.allocateDirect(inputStream.available())
                buffer.order(ByteOrder.nativeOrder())
                inputStream.read(buffer.array())
                //buffer.rewind() // Prepare the buffer for reading
                buffer
            }
            /*val fileDescriptor = context.assets.openFd(filename)
            val inputStream = fileDescriptor.createInputStream()
            val fileChannel = inputStream.channel
            val size = fileChannel.size()
            val buffer = ByteBuffer.allocateDirect(size.toInt())
            buffer.order(ByteOrder.nativeOrder())
            fileChannel.read(buffer)
            fileDescriptor.close()
            buffer.rewind()
            buffer*/
        } catch (e: IOException) {
            throw RuntimeException("Error al cargar el modelo $filename: ${e.message}", e)
        }
    }*/

    private fun loadModelFile(context: Context, filename: String): ByteBuffer {
        try {
            val fileDescriptor = context.assets.openFd(filename)
            val inputStream = fileDescriptor.createInputStream()
            val fileChannel = inputStream.channel
            val size = fileChannel.size()
            val buffer = ByteBuffer.allocateDirect(size.toInt())
            buffer.order(ByteOrder.nativeOrder())
            fileChannel.read(buffer)
            fileDescriptor.close()
            buffer.rewind()
            return buffer
        } catch (e: Exception) {
            throw RuntimeException("Error al cargar el modelo $filename: ${e.message}", e)
        }
    }

    fun close() {
        if (::interpreter.isInitialized) {
            interpreter.close()
        } else {
            Log.w("TensorFlowHelper", "Interpreter no inicializado antes de cerrar.")
        }
    }

    /*fun close() {
        interpreter.close()
    }*/
}