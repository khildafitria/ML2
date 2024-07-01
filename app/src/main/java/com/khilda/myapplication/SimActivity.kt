package com.khilda.myapplication

import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class SimActivity : AppCompatActivity() {

    private lateinit var interpreter: Interpreter
    private val mModelPath = "stroke_prediction.tflite"

    private lateinit var resultText: TextView
    private lateinit var gender: EditText
    private lateinit var age: EditText
    private lateinit var hypertension: EditText
    private lateinit var heart_disease: EditText
    private lateinit var ever_married: EditText
    private lateinit var work_type: EditText
    private lateinit var Residence_type: EditText
    private lateinit var avg_glucose_level: EditText
    private lateinit var bmi: EditText
    private lateinit var smoking_status: EditText
    private lateinit var Predict: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sim)

        resultText = findViewById(R.id.txtResult)
        gender = findViewById(R.id.gender)
        age = findViewById(R.id.age)
        hypertension = findViewById(R.id.hypertension)
        heart_disease = findViewById(R.id.heart_disease)
        ever_married = findViewById(R.id.ever_married)
        work_type = findViewById(R.id.work_type)
        Residence_type = findViewById(R.id.residence_type)
        avg_glucose_level = findViewById(R.id.avg_glucose_level)
        bmi = findViewById(R.id.bmi)
        smoking_status = findViewById(R.id.smoking_status)
        Predict = findViewById(R.id.btnCheck)

        Predict.setOnClickListener {
            val result = doInference(
                gender.text.toString(),
                age.text.toString(),
                hypertension.text.toString(),
                heart_disease.text.toString(),
                ever_married.text.toString(),
                work_type.text.toString(),
                work_type.text.toString(),
                Residence_type.text.toString(),
                bmi.text.toString(),
                smoking_status.text.toString()
            )
            runOnUiThread {
                if (result == 0) {
                    resultText.text = "Anda Tidak Memiliki Resiko Struk"
                } else if (result == 1) {
                    resultText.text = "Anda Beresiko Terkena Struk"
                }
            }
        }
        initInterpreter()
    }

    private fun initInterpreter() {
        val options = Interpreter.Options().apply {
            numThreads = 10
            useNNAPI = true
        }
        interpreter = Interpreter(loadModelFile(assets, mModelPath), options)
    }

    private fun doInference(
        input1: String, input2: String, input3: String, input4: String, input5: String,
        input6: String, input7: String, input8: String, input9: String, input10: String
    ): Int {
        val inputVal = FloatArray(10)
        try {
            inputVal[0] = input1.toFloat()
            inputVal[1] = input2.toFloat()
            inputVal[2] = input3.toFloat()
            inputVal[3] = input4.toFloat()
            inputVal[4] = input5.toFloat()
            inputVal[5] = input6.toFloat()
            inputVal[6] = input7.toFloat()
            inputVal[7] = input8.toFloat()
            inputVal[8] = input9.toFloat()
            inputVal[9] = input10.toFloat()
        } catch (e: NumberFormatException) {
            Log.e("Inference Error", "Invalid input format", e)
            return -1
        }

        val inputTensor = arrayOf(inputVal)

        Log.d("Inference Error", "Input values: ${inputVal.joinToString("")}")

        val output = Array(1) { FloatArray(1) }
        interpreter.run(inputTensor, output)

        Log.d("Inference Error", "Output values: ${output[0].joinToString("")}")

        return if (output.isNotEmpty() && output[0].isNotEmpty()) {
            if (output[0][0] > 0.15f) 1 else 0
        } else {
            Log.e("Inference Error", "Output array is empty or valid")
            -1
        }
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}