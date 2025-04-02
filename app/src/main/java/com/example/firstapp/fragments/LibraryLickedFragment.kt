package com.example.firstapp.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.appcompat.R.id.search_mag_icon
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapp.Adapter.RecycleViewLikedLibraryAdapter
import com.example.firstapp.R
import com.example.firstapp.data.LickedLibraryRecycleViewDataClass
import com.example.firstapp.interfaces.Filterable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class LibraryLickedFragment : Fragment(), Filterable {

    private lateinit var recycleView: RecyclerView
    private lateinit var adapter: RecycleViewLikedLibraryAdapter
    private lateinit var searchView: SearchView
    private var lickedBooks: ArrayList<LickedLibraryRecycleViewDataClass> = arrayListOf()
    private val allBooks = arrayListOf<LickedLibraryRecycleViewDataClass>() // Усі книги
    private val filteredBooks = arrayListOf<LickedLibraryRecycleViewDataClass>() // Відфільтровані книги

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_library_licked, container, false)

        searchView = view.findViewById(R.id.searchView3)
        val searchIcon = searchView.findViewById<ImageView>(search_mag_icon)
        searchIcon.setColorFilter(Color.YELLOW)
        val textView = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        textView.setTextColor(Color.WHITE)
        textView.setHintTextColor(Color.GRAY)

        val closeButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        closeButton.setColorFilter(Color.RED)
        setupSearchView()

        recycleView = view.findViewById(R.id.LickedRecyclerView)
        recycleView.layoutManager = LinearLayoutManager(requireContext())
        adapter = RecycleViewLikedLibraryAdapter(filteredBooks){ bookId ->
            val action = LibraryLickedFragmentDirections.actionLibraryLickedFragmentToRecycleViewItemClicked(bookId)
            findNavController().navigate(action)
        }
        recycleView.adapter = adapter

        loadLickedBooks()
        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadLickedBooks() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val likedBooksIds = document.get("likedBooks") as? List<String> ?: emptyList()
                    if (likedBooksIds.isNotEmpty()) {
                        db.collection("books")
                            .whereIn(FieldPath.documentId(), likedBooksIds)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                lickedBooks.clear()
                                lickedBooks.addAll(querySnapshot.documents.map { doc ->
                                    LickedLibraryRecycleViewDataClass(
                                        bookId = doc.id,
                                        title = doc.getString("title") ?: "Unknown Title",
                                        coverImageUrl = doc.getString("coverImageUrl") ?: ""
                                    )
                                })
                                filteredBooks.clear()
                                filteredBooks.addAll(lickedBooks)
                                adapter.notifyDataSetChanged()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    "Failed to load liked books: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(context, "No liked books found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to load user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filter(query) // Викликаємо фільтрацію на підставі введеного запиту
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText) // Викликаємо фільтрацію при кожній зміні тексту
                return true
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun filter(query: String?) {
        filteredBooks.clear()
        if (query.isNullOrEmpty()) {
            filteredBooks.addAll(lickedBooks) // Якщо немає тексту, показуємо всі книги
        } else {
            // Фільтруємо книги, які містять пошуковий текст
            filteredBooks.addAll(lickedBooks.filter { it.title.contains(query, ignoreCase = true) })
        }
        adapter.notifyDataSetChanged() // Оновлюємо RecyclerView після фільтрації
    }
}
