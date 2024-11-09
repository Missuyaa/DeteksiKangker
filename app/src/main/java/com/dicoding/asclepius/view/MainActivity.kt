package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pulihkan URI gambar jika ada
        currentImageUri = savedInstanceState?.getString("currentImageUri")?.let { Uri.parse(it) }
        showImage() // Tampilkan kembali gambar jika ada

        // Setup button listeners
        binding.galleryButton.setOnClickListener {
            startGallery()
        }

        binding.analyzeButton.setOnClickListener {
            analyzeImage()
        }
    }

    // Menyimpan URI gambar saat orientasi berubah
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currentImageUri?.let {
            outState.putString("currentImageUri", it.toString())
        }
    }

    private val REQUEST_GALLERY = 100  // Request code untuk galeri

    private fun startGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                currentImageUri = uri
                showImage()  // Tampilkan gambar setelah dipilih
            } ?: run {
                showToast("Gagal memilih gambar.")
            }
        }
    }

    private fun showImage() {
        currentImageUri?.let { uri ->
            binding.previewImageView.setImageURI(uri)
        } ?: showToast("Tidak ada gambar untuk ditampilkan.")
    }

    private fun analyzeImage() {
        if (currentImageUri != null) {
            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("imageUri", currentImageUri.toString()) // Mengirim URI gambar
            startActivity(intent)
        } else {
            showToast("Pilih gambar terlebih dahulu.")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
