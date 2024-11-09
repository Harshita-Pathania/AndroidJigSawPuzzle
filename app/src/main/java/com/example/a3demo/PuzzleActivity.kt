package com.example.a3demo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class PuzzleActivity : AppCompatActivity() {

    private lateinit var puzzleRecyclerView: RecyclerView
    private var pieces: Int = 0
    private lateinit var bitmapPieces: List<Bitmap>
    private lateinit var shuffledPieces: MutableList<Bitmap>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle)

        val imagePath = intent.getStringExtra("imageUri")
        if (imagePath == null) {
            Toast.makeText(this, "Image data not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val pieceCount = intent.getIntExtra("pieces", 3).coerceAtLeast(1)
        pieces = pieceCount

        val imageFile = File(imagePath)
        val selectedBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)

        if (selectedBitmap == null) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Setup RecyclerView
        puzzleRecyclerView = findViewById(R.id.puzzleRecyclerView)
        puzzleRecyclerView.layoutManager = GridLayoutManager(this, pieceCount)
        bitmapPieces = splitImage(selectedBitmap, pieceCount)

        // Shuffle pieces
        shuffledPieces = bitmapPieces.shuffled().toMutableList()
        puzzleRecyclerView.adapter = PuzzleAdapter(shuffledPieces)

        // Set up drag-and-drop with ItemTouchHelper
        val itemTouchHelper = ItemTouchHelper(DragItemTouchHelperCallback())
        itemTouchHelper.attachToRecyclerView(puzzleRecyclerView)
    }

    private fun splitImage(image: Bitmap, pieces: Int): List<Bitmap> {
        val pieceWidth = image.width / pieces
        val pieceHeight = image.height / pieces
        val pieceList = mutableListOf<Bitmap>()

        for (row in 0 until pieces) {
            for (col in 0 until pieces) {
                val piece = Bitmap.createBitmap(image, col * pieceWidth, row * pieceHeight, pieceWidth, pieceHeight)
                pieceList.add(piece)
            }
        }
        return pieceList
    }

    // RecyclerView Adapter to display puzzle pieces
    inner class PuzzleAdapter(private val puzzlePieces: List<Bitmap>) :
        RecyclerView.Adapter<PuzzleAdapter.PuzzleViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PuzzleViewHolder {
            val view = LayoutInflater.from(this@PuzzleActivity).inflate(R.layout.puzzle_piece, parent, false)
            return PuzzleViewHolder(view)
        }

        override fun onBindViewHolder(holder: PuzzleViewHolder, position: Int) {
            holder.bind(puzzlePieces[position])
        }

        override fun getItemCount(): Int = puzzlePieces.size

        inner class PuzzleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val imageView: ImageView = itemView.findViewById(R.id.pieceImageView)

            fun bind(bitmap: Bitmap) {
                imageView.setImageBitmap(bitmap)
            }
        }
    }

    // ItemTouchHelper for drag-and-drop logic
    private inner class DragItemTouchHelperCallback : ItemTouchHelper.Callback() {

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            return makeMovementFlags(dragFlags, 0)
        }

        override fun onMove(recyclerView: RecyclerView, source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val fromPosition = source.adapterPosition
            val toPosition = target.adapterPosition
            // Swap puzzle pieces
            val temp = shuffledPieces[fromPosition]
            shuffledPieces[fromPosition] = shuffledPieces[toPosition]
            shuffledPieces[toPosition] = temp

            recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)
            checkIfPuzzleSolved()  // Check if puzzle is solved
            return true
        }


        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // No swipe actions needed
        }

        override fun isLongPressDragEnabled(): Boolean = true
        override fun isItemViewSwipeEnabled(): Boolean = false
    }

    private fun checkIfPuzzleSolved() {
        var isSolved = true
        for (i in shuffledPieces.indices) {
            val bitmap1 = shuffledPieces[i]
            val bitmap2 = bitmapPieces[i]
            if (!bitmap1.isSameAs(bitmap2)) {
                isSolved = false
                break
            }
        }

        if (isSolved) {
            Toast.makeText(this, "Puzzle Solved!", Toast.LENGTH_LONG).show()
        }
    }

    private fun Bitmap.isSameAs(other: Bitmap): Boolean {
        if (this.width != other.width || this.height != other.height) {
            return false
        }

        for (x in 0 until width) {
            for (y in 0 until height) {
                if (this.getPixel(x, y) != other.getPixel(x, y)) {
                    return false
                }
            }
        }
        return true
    }


}
