package com.example.a3demo

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class HomePage : AppCompatActivity() {

    private lateinit var selectedImage: ImageView
    private lateinit var pieceCountSeekBar: SeekBar
    private var selectedBitmap: Bitmap? = null
    private var pieceCount = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        selectedImage = findViewById(R.id.selectedImage)
        pieceCountSeekBar = findViewById(R.id.pieceCountSeekBar)

        // Choose Image Button
        findViewById<Button>(R.id.chooseImageButton).setOnClickListener {
            openGallery()
        }

        // SeekBar to adjust the number of pieces
        pieceCountSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                pieceCount = progress + 3  // Pieces range from 3 to 6
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Start Puzzle Button
        findViewById<Button>(R.id.startPuzzleButton).setOnClickListener {
            if (selectedBitmap != null) {
                startPuzzleGame(selectedBitmap!!, pieceCount)
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Open Gallery to choose an image
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    // Handle the image selection result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            selectedBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            selectedImage.setImageBitmap(selectedBitmap)
            selectedImage.background = null
        }
    }

    // Start the puzzle game
    private fun startPuzzleGame(image: Bitmap, pieces: Int) {
        val imageFile = File(cacheDir, "puzzle_image.png")
        FileOutputStream(imageFile).use { out ->
            image.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val intent = Intent(this, PuzzleActivity::class.java).apply {
            putExtra("imageUri", imageFile.absolutePath)
            putExtra("pieces", pieces)
        }
        startActivity(intent)
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1
    }
}
