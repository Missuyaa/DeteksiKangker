package com.dicoding.asclepius.view

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityResultBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private lateinit var classifier: ImageClassifierHelper
    private lateinit var viewModel: ResultViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menggunakan View Binding
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi ImageClassifierHelper
        classifier = ImageClassifierHelper(this)

        // Inisialisasi ViewModel
        viewModel = ViewModelProvider(this).get(ResultViewModel::class.java)

        // Cek apakah ada URI gambar yang sudah disimpan di ViewModel
        Log.d("ResultActivity", "imageUri sebelum rotasi: ${viewModel.imageUri}")
        viewModel.imageUri =
            viewModel.imageUri ?: intent.getStringExtra("imageUri")?.let { Uri.parse(it) }

        // Tampilkan gambar di ImageView dan jalankan analisis jika ada URI gambar
        viewModel.imageUri?.let {
            val bitmap = uriToBitmap(it)
            bitmap?.let { bmp ->
                binding.resultImage.setImageBitmap(bmp)
                analyzeImage(bmp)
            } ?: run {
                binding.resultText.text = getString(R.string.error_processing_image)
            }
        } ?: run {
            binding.resultText.text = getString(R.string.error_processing_image)
            Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    }

    private fun analyzeImage(bitmap: Bitmap) {
        try {
            // Jalankan prediksi menggunakan ImageClassifierHelper
            val results = classifier.classifyStaticImage(bitmap)
            Log.d("Prediction", "Results: ${results.joinToString(", ")}") // Log hasil model untuk melihat probabilitas masing-masing label

            // Asumsi: results[0] adalah Non Cancer dan results[1] adalah Cancer
            val nonCancerProbability = results[0]
            val cancerProbability = results[1]

            // Gunakan ambang batas 60% untuk menentukan Cancer atau Non Cancer
            val threshold = 0.60 // Ambang batas 60%
            val confidence = (cancerProbability * 100).toInt() // Konversi ke persentase untuk tampilan

            // Tentukan teks prediksi berdasarkan ambang batas
            val predictionText = if (cancerProbability >= threshold) {
                "Cancer $confidence%"
            } else {
                "Non Cancer $confidence%"
            }

            binding.resultText.text = predictionText
            Log.d("Prediction", "Label: ${if (cancerProbability >= threshold) "Cancer" else "Non Cancer"}, Confidence: $confidence%")

        } catch (e: Exception) {
            // Menangani kesalahan jika terjadi selama proses analisis
            Log.e("PredictionError", "Terjadi kesalahan saat menganalisis gambar", e)

            // Tampilkan pesan kesalahan pada UI
            binding.resultText.text = getString(R.string.error_processing_image)
            Toast.makeText(this, "Terjadi kesalahan dalam memproses gambar. Silakan coba lagi.", Toast.LENGTH_LONG).show()
        }
    }


}
