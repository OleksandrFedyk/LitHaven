package com.example.firstapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.firstapp.Adapter.EpisodeAdapter
import com.example.firstapp.data.EpisodesDataClass
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


class item_clicked_preview : Fragment() {

    private lateinit var fragmentItemClickedPreviewTitle: TextView
    private lateinit var fragmentItemClickedPreviewCreator: TextView
    private lateinit var fragmentItemClickedPreviewDescription: TextView
    private lateinit var chipGroup: ChipGroup
    private lateinit var likes: TextView
    private var amountOfChapters: TextView? = null
    private var amountOfReads: TextView? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_clicked_preview, container, false)

        // Initialize views
        fragmentItemClickedPreviewTitle = view.findViewById(R.id.fragmentItemClickedPreviewTitle)
        fragmentItemClickedPreviewCreator = view.findViewById(R.id.fragmentItemClickedPreviewCreator)
        fragmentItemClickedPreviewDescription = view.findViewById(R.id.fragmentItemClickedPreviewDescription)
        amountOfChapters = view.findViewById(R.id.AmountOfChapters)
        amountOfReads = view.findViewById(R.id.AmountOfReads)
        chipGroup = view.findViewById(R.id.bookDetailChipGroup)
        likes = view.findViewById(R.id.likes)
        // Get bookId from arguments
        val bookId = arguments?.getString("bookId")
        if (bookId != null) {
            incrementReadCount(bookId)
            loadBookDetails(bookId) // Ваш метод для завантаження деталей книги
        } else {
            Toast.makeText(requireContext(), "Book ID not found", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun loadBookDetails(bookId: String) {
        db.collection("books")
            .document(bookId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val title = document.getString("title") ?: "Unknown Title"
                    val creator = document.getString("authorName") ?: "Unknown Creator"
                    val description = document.getString("description") ?: "No description"
                    val amountOfRead = document.getLong("amountOfReads") ?: 0


                    fragmentItemClickedPreviewTitle.text = title
                    fragmentItemClickedPreviewCreator.text = creator
                    fragmentItemClickedPreviewDescription.text = description
                    amountOfReads?.text = amountOfRead.toString()
                    likes.text = document.getLong("likes").toString()

                    val genres = document.get("genres") as? List<String>
                    if (!genres.isNullOrEmpty()) {
                        chipGroup.removeAllViews()
                        for (genre in genres) {
                            val chip = LayoutInflater.from(requireContext())
                                .inflate(R.layout.chip_item, chipGroup, false) as com.google.android.material.chip.Chip
                            chip.text = genre
                            chipGroup.addView(chip)
                        }
                    } else {
                        Toast.makeText(requireContext(), "No genres found", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(requireContext(), "Book not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load book details: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        db.collection("books")
            .document(bookId)
            .collection("chapters")
            .get()
            .addOnSuccessListener {
                amountOfChapters?.text = it.size().toString()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to load episodes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun incrementReadCount(bookId: String) {
        val db = FirebaseFirestore.getInstance()
        val bookRef = db.collection("books").document(bookId)

        bookRef.update("amountOfReads", FieldValue.increment(1))
            .addOnSuccessListener {
                Log.d("Firestore", "Read count incremented successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error incrementing read count", e)
            }
    }
}