package com.example.firstapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.firstapp.Adapter.ViewPagerAdapter
import com.example.firstapp.R
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class RecycleViewItemClicked : Fragment() {

    private val args by navArgs<RecycleViewItemClickedArgs>()
    private lateinit var coverImageView: ImageView
    private lateinit var bookImageLike: ImageButton
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val db = FirebaseFirestore.getInstance()
    private lateinit var fragmentItemClickedPreviewTitle: TextView
    private lateinit var fragmentItemClickedPreviewCreator: TextView
    private lateinit var fragmentItemClickedPreviewDescription: TextView
    private lateinit var chipGroup: ChipGroup
    private lateinit var likes: TextView
    private var amountOfChapters: TextView? = null
    private var amountOfReads: TextView? = null
    private lateinit var episodeButton: Button
    private var authorId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recycle_view_item_clicked, container, false)
        val bookId = args.bookId
        coverImageView = view.findViewById(R.id.fragmentRecycleViewItemClickedImage)
        bookImageLike = view.findViewById(R.id.imageButtonLike)
        fragmentItemClickedPreviewTitle = view.findViewById(R.id.fragmentItemClickedPreviewTitle)
        fragmentItemClickedPreviewCreator = view.findViewById(R.id.fragmentItemClickedPreviewCreator)
        fragmentItemClickedPreviewDescription = view.findViewById(R.id.fragmentItemClickedPreviewDescription)
        amountOfChapters = view.findViewById(R.id.AmountOfChapters)
        amountOfReads = view.findViewById(R.id.AmountOfReads)
        chipGroup = view.findViewById(R.id.bookDetailChipGroup)
        likes = view.findViewById(R.id.likes)

        fragmentItemClickedPreviewCreator.setOnClickListener {
            if (!authorId.isNullOrEmpty()) {
                val action = RecycleViewItemClickedDirections
                    .actionRecycleViewItemClickedAuthorToAuthorPage(authorId!!)
                findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "Author not found", Toast.LENGTH_SHORT).show()
            }
        }

        incrementReadCount(bookId)
        loadBookDetails(bookId)
        checkBookLiked(bookId)
        addBookToHistory(bookId)
        bookImageLike.setOnClickListener { likeBook(bookId) }
        episodeButton = view.findViewById(R.id.episodesButton)
        episodeButton.setOnClickListener {
            val action = RecycleViewItemClickedDirections.actionRecycleViewItemClickedToItemClickedEpisodes(bookId)
            findNavController().navigate(action)
        }
        return view
    }

    private fun loadBookDetails(bookId: String) {
        db.collection("books")
            .document(bookId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val coverImageUrl = document.getString("coverImageUrl") ?: ""
                    val title = document.getString("title") ?: "Unknown Title"
                    val creator = document.getString("authorName") ?: "Unknown Creator"
                    val description = document.getString("description") ?: "No description"
                    val amountOfRead = document.getLong("amountOfReads") ?: 0
                    authorId = document.getString("authorId")

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

                    Glide.with(requireContext())
                        .load(coverImageUrl)
                        .placeholder(R.drawable.books_book_svgrepo_com)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(coverImageView)

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

    private fun checkBookLiked(bookId: String) {
        if (userId != null) {
            val userDocRef = db.collection("users").document(userId)

            userDocRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val likedBooks = document.get("likedBooks") as? List<String> ?: emptyList()

                    if (likedBooks.contains(bookId)){
                    bookImageLike.setImageResource(R.drawable.baseline_thumb_up_24)
                    } else {
                       bookImageLike.setImageResource(R.drawable.baseline_thumb_up_off_alt_24)
                    }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun likeBook(bookId: String) {
        if (userId != null) {
            val userDocRef = db.collection("users").document(userId)
            val bookDocRef = db.collection("books").document(bookId)


            userDocRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val likedBooks = document.get("likedBooks") as? List<String> ?: emptyList()

                    if (likedBooks.contains(bookId)) {
                        userDocRef.update("likedBooks", FieldValue.arrayRemove(bookId))
                            .addOnSuccessListener {
                                // Зменшуємо кількість лайків книги
                                bookDocRef.update("likes", FieldValue.increment(-1))
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Book removed from Liked", Toast.LENGTH_SHORT).show()
                                        bookImageLike.setImageResource(R.drawable.baseline_thumb_up_off_alt_24)
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Failed to update likes: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Failed to unlike book: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {

                        // Якщо книги немає в списку, додаємо її
                        userDocRef.update("likedBooks", FieldValue.arrayUnion(bookId))
                            .addOnSuccessListener {
                                // Збільшуємо кількість лайків книги
                                bookDocRef.update("likes", FieldValue.increment(1))
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Book added to Liked", Toast.LENGTH_SHORT).show()
                                        bookImageLike.setImageResource(R.drawable.baseline_thumb_up_24)
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Failed to update likes: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Failed to like book: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(context, "User document not found", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Please log in to like books", Toast.LENGTH_SHORT).show()
        }
    }


    fun addBookToHistory(bookId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        if (userId != null) {
            val visitedBook = hashMapOf(
                "bookId" to bookId,
                "visitedAt" to System.currentTimeMillis()
            )

            db.collection("users").document(userId).collection("visitedBooks").document(bookId)
                .set(visitedBook)
                .addOnSuccessListener {

                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
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

