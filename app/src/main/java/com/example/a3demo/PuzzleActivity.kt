package com.example.a3demo

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class PuzzleActivity : AppCompatActivity() {

    private lateinit var puzzleRecyclerView: RecyclerView
    private lateinit var originalImageView: ImageView
    private lateinit var backToHomepageButton: Button
    private lateinit var exitGameButton: Button
    private var pieces: Int = 0
    private lateinit var bitmapPieces: List<PuzzlePiece>
    private lateinit var shuffledPieces: MutableList<PuzzlePiece>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle)

        val imagePath = intent.getStringExtra("imageUri")
        val pieceCount = intent.getIntExtra("pieces", 3).coerceAtLeast(1)
        pieces = pieceCount

        if (imagePath == null) {
            Toast.makeText(this, "Image data not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val imageFile = File(imagePath)
        val selectedBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        if (selectedBitmap == null) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        originalImageView = findViewById(R.id.originalImageView)
        originalImageView.setImageBitmap(selectedBitmap)

        puzzleRecyclerView = findViewById(R.id.puzzleRecyclerView)
        puzzleRecyclerView.layoutManager = GridLayoutManager(this, pieceCount)
        bitmapPieces = splitImage(selectedBitmap, pieceCount)

        shuffledPieces = bitmapPieces.shuffled().toMutableList()
        puzzleRecyclerView.adapter = PuzzleAdapter(shuffledPieces)

        val itemTouchHelper = ItemTouchHelper(DragItemTouchHelperCallback())
        itemTouchHelper.attachToRecyclerView(puzzleRecyclerView)

        backToHomepageButton = findViewById(R.id.backToHomepageButton)
        backToHomepageButton.setOnClickListener {
            startActivity(Intent(this, HomePage::class.java))
            finish()
        }

        exitGameButton = findViewById(R.id.exitGameButton)
        exitGameButton.setOnClickListener {
            showExitGameDialog()
        }
    }

    private fun showExitGameDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Exit Game")
            setMessage("Would you like to go back to the homepage or exit the app?")
            setPositiveButton("Homepage") { _, _ ->
                startActivity(Intent(this@PuzzleActivity, HomePage::class.java))
                finish()
            }
            setNegativeButton("Exit") { _, _ ->
                finishAffinity() // Closes the app
            }
            setNeutralButton("Cancel", null)
            show()
        }
    }

    data class PuzzlePiece(val bitmap: Bitmap, val originalPosition: Int)

    private fun splitImage(image: Bitmap, pieces: Int): List<PuzzlePiece> {
        val pieceWidth = image.width / pieces
        val pieceHeight = image.height / pieces
        val pieceList = mutableListOf<PuzzlePiece>()

        for (row in 0 until pieces) {
            for (col in 0 until pieces) {
                val piece = Bitmap.createBitmap(image, col * pieceWidth, row * pieceHeight, pieceWidth, pieceHeight)
                val position = row * pieces + col
                pieceList.add(PuzzlePiece(piece, position))
            }
        }
        return pieceList
    }

    inner class PuzzleAdapter(private val puzzlePieces: List<PuzzlePiece>) :
        RecyclerView.Adapter<PuzzleAdapter.PuzzleViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PuzzleViewHolder {
            val view = LayoutInflater.from(this@PuzzleActivity).inflate(R.layout.puzzle_piece, parent, false)
            return PuzzleViewHolder(view)
        }

        override fun onBindViewHolder(holder: PuzzleViewHolder, position: Int) {
            holder.bind(puzzlePieces[position].bitmap)
            holder.itemView.tag = puzzlePieces[position].originalPosition
        }

        override fun getItemCount(): Int = puzzlePieces.size

        inner class PuzzleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val imageView: ImageView = itemView.findViewById(R.id.pieceImageView)

            fun bind(bitmap: Bitmap) {
                imageView.setImageBitmap(bitmap)
            }
        }
    }

    private inner class DragItemTouchHelperCallback : ItemTouchHelper.Callback() {

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            return makeMovementFlags(dragFlags, 0)
        }

        override fun onMove(recyclerView: RecyclerView, source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val fromPosition = source.adapterPosition
            val toPosition = target.adapterPosition

            shuffledPieces[fromPosition] = shuffledPieces[toPosition].also { shuffledPieces[toPosition] = shuffledPieces[fromPosition] }
            recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)

            checkIfPuzzleSolved()
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

        override fun isLongPressDragEnabled(): Boolean = true
        override fun isItemViewSwipeEnabled(): Boolean = false
    }

    private fun checkIfPuzzleSolved() {
        val isSolved = shuffledPieces.indices.all { i ->
            puzzleRecyclerView.findViewHolderForAdapterPosition(i)?.itemView?.tag == i
        }

        if (isSolved) {
            Toast.makeText(this, "Puzzle Solved!", Toast.LENGTH_LONG).show()
            backToHomepageButton.visibility = View.VISIBLE
        }
    }
}
