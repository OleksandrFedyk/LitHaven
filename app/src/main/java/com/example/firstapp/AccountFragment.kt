package com.example.firstapp


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firstapp.Adapter.SearchAdapter
import com.example.firstapp.fragments.RecycleViewItemClickedArgs
import com.google.firebase.firestore.FirebaseFirestore
import com.example.firstapp.data.SearchBook
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue


class AccountFragment: Fragment() {

    val args: AccountFragmentArgs by navArgs()
    private lateinit var recycleViewAccountFragment: RecyclerView
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val db = FirebaseFirestore.getInstance()
    private lateinit var likeAuthorButton: ImageButton
    private lateinit var likesTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        recycleViewAccountFragment = view.findViewById(R.id.accountFragmentRecycleView)
        likeAuthorButton = view.findViewById(R.id.likeAuthorButton)
        recycleViewAccountFragment.layoutManager = LinearLayoutManager(requireContext())
        loadBooks { books ->
            val adapter = SearchAdapter(books) { bookId ->
                // Обробка кліку на елемент RecyclerView
                navigateToBookDetails(bookId)
            }
            recycleViewAccountFragment.adapter = adapter
        }

        val authorId = args.authorId
        Log.d("AccountFragment", "Navigated with authorId: $authorId")
        checkAuthorLiked(authorId)
        loadAuthorInfo(authorId)
        showAuthorLikes(authorId)
        likeAuthorButton.setOnClickListener {
            ensureAuthorExists(authorId) {
                likeAuthor(authorId)
            }
        }

        return view
    }

    private fun loadAuthorInfo(authorId: String) {
        val db = FirebaseFirestore.getInstance()
        Log.d("AccountFragment", "Loading author info for authorId: $authorId")

        db.collection("users")
            .whereEqualTo("userId", authorId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val document = querySnapshot.documents.firstOrNull()
                if (document != null) {
                    val authorName = document.getString("accountName") ?: "Unknown Name"
                    val authorEmail = document.getString("email") ?: "Unknown Email"
                    Log.d("AccountFragment", "Author found: $authorName, $authorEmail")
                    view?.findViewById<TextView>(R.id.authorName)?.text = authorName
                    view?.findViewById<TextView>(R.id.authorEmail)?.text = authorEmail
                } else {
                    Log.d("AccountFragment", "No document matches the authorId")
                    view?.findViewById<TextView>(R.id.authorName)?.text = "Author not found"
                }
            }
            .addOnFailureListener { e ->
                Log.e("AccountFragment", "Error fetching document: ${e.message}", e)
                view?.findViewById<TextView>(R.id.authorName)?.text = "Error loading author: ${e.message}"
            }
    }

    private fun loadBooks(onBooksLoaded: (List<SearchBook>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val authorId = args.authorId

        db.collection("books")
            .whereEqualTo("authorId", authorId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val books = querySnapshot.documents.mapNotNull { document ->
                    val id = document.id
                    val title = document.getString("title") ?: "No Title"
                    val description = document.getString("description") ?: "No Description"
                    val coverImageUrl = document.getString("coverImageUrl") ?: ""
                    val genres = document.get("genres") as? List<String> ?: emptyList()

                    SearchBook(id, title, description, coverImageUrl, genres)
                }
                onBooksLoaded(books)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }

    }
    private fun navigateToBookDetails(bookId: String) {
        val action = AccountFragmentDirections.actionAccountFragmentToRecycleViewItemClicked(bookId)
        findNavController().navigate(action)
    }

    private fun likeAuthor(authorId: String) {
        if (userId != null) {
            val userDocRef = db.collection("users").document(userId)
            val authorDocRef = db.collection("authors").document(authorId)

            userDocRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val likedAuthors = document.get("likedAuthors") as? List<String> ?: emptyList()

                    if (likedAuthors.contains(authorId)) {
                        userDocRef.update("likedAuthors", FieldValue.arrayRemove(authorId))
                            .addOnSuccessListener {
                                authorDocRef.update("likes", FieldValue.increment(-1))
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Author unliked", Toast.LENGTH_SHORT).show()
                                        likeAuthorButton.setImageResource(R.drawable.baseline_thumb_up_off_alt_24)
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Failed to update likes: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Failed to unlike author: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        userDocRef.update("likedAuthors", FieldValue.arrayUnion(authorId))
                            .addOnSuccessListener {
                                authorDocRef.update("likes", FieldValue.increment(1))
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Author liked", Toast.LENGTH_SHORT).show()
                                        likeAuthorButton.setImageResource(R.drawable.baseline_thumb_up_24)
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Failed to update likes: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Failed to like author: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(context, "User document not found", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Please log in to like authors", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAuthorLiked(authorId: String) {
        if (userId != null) {
            val userDocRef = db.collection("users").document(userId)

            userDocRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val likedAuthors = document.get("likedAuthors") as? List<String> ?: emptyList()

                    if (likedAuthors.contains(authorId)) {
                        likeAuthorButton.setImageResource(R.drawable.baseline_thumb_up_24)
                    } else {
                        likeAuthorButton.setImageResource(R.drawable.baseline_thumb_up_off_alt_24)
                    }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun ensureAuthorExists(authorId: String, callback: () -> Unit) {
        val authorDocRef = db.collection("authors").document(authorId)

        authorDocRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val authorData = hashMapOf(
                    "likes" to 0
                )
                authorDocRef.set(authorData).addOnSuccessListener {
                    callback()
                }.addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to create author: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                callback()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Failed to check author: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAuthorLikes(authorId: String, callback: (Int) -> Unit) {
        val authorDocRef = db.collection("authors").document(authorId)

        authorDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val likes = document.getLong("likes")?.toInt() ?: 0
                callback(likes)
            } else {
                callback(0)
            }
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Failed to get likes: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun showAuthorLikes(authorId: String) {

        getAuthorLikes(authorId) { likes ->
            likesTextView = view?.findViewById(R.id.likesTextView)!!
            likesTextView.text = "$likes"
        }
    }

}

