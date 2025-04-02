package com.example.firstapp.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapp.Adapter.RecycleViewLikedLibraryAdapter
import com.example.firstapp.R
import com.example.firstapp.data.LickedLibraryRecycleViewDataClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class LibraryHistoryFragment : Fragment(), com.example.firstapp.interfaces.Filterable {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecycleViewLikedLibraryAdapter
    private var visitedBooks: ArrayList<LickedLibraryRecycleViewDataClass> = arrayListOf()
    private val allBooks = arrayListOf<LickedLibraryRecycleViewDataClass>()
    private val filteredBooks = arrayListOf<LickedLibraryRecycleViewDataClass>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_library_history, container, false)

        recyclerView = view.findViewById(R.id.HistoryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = RecycleViewLikedLibraryAdapter(filteredBooks) { bookId ->
            val action = LibraryHistoryFragmentDirections.actionLibraryHistoryFragmentToRecycleViewItemClicked(bookId)
            findNavController().navigate(action)
        }
        recyclerView.adapter = adapter

        loadVisitedBooks()
        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadVisitedBooks() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        if (userId != null) {
            db.collection("users").document(userId).collection("visitedBooks")
                .orderBy("visitedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val visitedDocs = querySnapshot.documents
                    val bookIds = visitedDocs.map { it.id }

                    if (bookIds.isEmpty()) {
                        visitedBooks.clear()
                        adapter.notifyDataSetChanged()
                        return@addOnSuccessListener
                    }

                    db.collection("books")
                        .whereIn(FieldPath.documentId(), bookIds) // Масив `bookId`
                        .get()
                        .addOnSuccessListener { booksSnapshot ->
                            allBooks.clear()
                            visitedBooks.clear()

                            booksSnapshot.documents.forEach { bookDoc ->
                                val bookId = bookDoc.id
                                val title = bookDoc.getString("title") ?: "Unknown Title"
                                val coverImageUrl = bookDoc.getString("coverImageUrl") ?: ""

                                val book = LickedLibraryRecycleViewDataClass(
                                    bookId = bookId,
                                    title = title,
                                    coverImageUrl = coverImageUrl
                                )

                                allBooks.add(book)
                                visitedBooks.add(book)
                            }

                            visitedBooks.sortByDescending { book ->
                                visitedDocs.find { it.id == book.bookId }?.getLong("visitedAt") ?: 0L
                            }

                            filteredBooks.clear()
                            filteredBooks.addAll(visitedBooks)
                            adapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to load books: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to load visited books: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun filter(query: String?) {
        filteredBooks.clear()
        if (query.isNullOrEmpty()) {
            filteredBooks.addAll(visitedBooks)
        } else {
            filteredBooks.addAll(visitedBooks.filter { it.title.contains(query, ignoreCase = true) })
        }
        adapter.notifyDataSetChanged()
    }
}

