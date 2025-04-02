package com.example.firstapp.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.R.id.search_mag_icon
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapp.Adapter.SearchAdapter
import com.example.firstapp.R
import com.example.firstapp.data.SearchBook
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.widget.SearchView



class SearchFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchAdapter
    private lateinit var chipGroup: ChipGroup
    private val allBooks: MutableList<SearchBook> = mutableListOf() // Усі книги
    private val filteredBooks: MutableList<SearchBook> = mutableListOf() // Відфільтровані книги

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView = view.findViewById(R.id.recyclerSearchView)
        chipGroup = view.findViewById(R.id.genreChipGroup)
        val searchView = view.findViewById<SearchView>(R.id.search_view)
        val searchIcon = searchView.findViewById<ImageView>(search_mag_icon)
        searchIcon.setColorFilter(Color.YELLOW)
        val textView = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        textView.setTextColor(Color.WHITE)
        textView.setHintTextColor(Color.GRAY)

        val closeButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        closeButton.setColorFilter(Color.RED)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = SearchAdapter(filteredBooks) { bookId ->
            val action = SearchFragmentDirections.actionSearchFragmentToRecycleViewItemClicked(bookId)
            findNavController().navigate(action)
        }
        recyclerView.adapter = adapter


        setupSearchView(searchView) // Налаштування для SearchView
        setupChipGroup() // Налаштування для ChipGroup
        loadBooks() // Завантаження книг з Firestore
        return view
    }

    private fun setupChipGroup() {
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val selectedGenres = checkedIds.mapNotNull { id ->
                group.findViewById<Chip>(id)?.text?.toString()
            }
            filterBooksByGenres(selectedGenres)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadBooks() {
            val db = FirebaseFirestore.getInstance()

            db.collection("books")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    allBooks.clear()
                    allBooks.addAll(querySnapshot.documents.map { document ->
                        SearchBook(
                            id = document.id,
                            title = document.getString("title") ?: "Untitled",
                            description = document.getString("description") ?: "No description",
                            coverImageUrl = document.getString("coverImageUrl") ?: "",
                            genres = document.get("genres") as? List<String> ?: emptyList()
                        )
                    })

                        filteredBooks.clear()
                        filteredBooks.addAll(allBooks)
                        adapter.notifyDataSetChanged()

                }
                .addOnFailureListener { exception ->
                        Toast.makeText(
                            requireContext(),
                            "Failed to load books: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()

                }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterBooksByGenres(selectedGenres: List<String>) {
        if (selectedGenres.isEmpty()) {
            filteredBooks.clear()
            filteredBooks.addAll(allBooks)
        } else {
            filteredBooks.clear()
            filteredBooks.addAll(allBooks.filter { book ->
                selectedGenres.all { genre -> genre in book.genres }
            })
        }
        if (filteredBooks.isEmpty()) {
            Toast.makeText(requireContext(), "No books match the selected genres", Toast.LENGTH_SHORT).show()
        }
        Log.d("SearchFragment", "Books after filtering by query: ${filteredBooks.size}")
        adapter.notifyDataSetChanged()
    }

    private fun setupSearchView(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText?.lowercase() ?: ""
                filterBooksByQuery(query)
                return true
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterBooksByQuery(query: String) {
        Log.d("SearchFragment", "Query: $query")

        val filteredList = if (query.isBlank()) {
            allBooks
        } else {
            allBooks.filter { book ->
                book.title.lowercase().contains(query) ||
                        book.description.lowercase().contains(query)
            }
        }

        if (filteredBooks != filteredList) {
            filteredBooks.clear()
            filteredBooks.addAll(filteredList)
            Log.d("SearchFragment", "Books after filtering by query: ${filteredBooks.size}")
            adapter.notifyDataSetChanged()
        }
    }

}