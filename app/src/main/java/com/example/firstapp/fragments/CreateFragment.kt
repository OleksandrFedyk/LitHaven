package com.example.firstapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapp.Adapter.CreateFragmentRecycleViewAdapter
import com.example.firstapp.R
import com.example.firstapp.data.CreateRecycleViewDataClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        recyclerView = view.findViewById<RecyclerView>(R.id.recycleViewCreateFragmentRecycleView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadBooksFromFirestore()

        val createNewStory = view.findViewById<Button>(R.id.createNewStoryButton)
        createNewStory.setOnClickListener {
            findNavController().navigate(R.id.nav_create_new_story)
        }

        return view
    }

    private fun loadBooksFromFirestore() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please log in to see your books.", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserId = currentUser.uid
        val bookList = mutableListOf<CreateRecycleViewDataClass>()

        db.collection("books")
            .whereEqualTo("authorId", currentUserId) // Фільтруємо книги за ID автора
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val id = document.id // ID книги
                    val title = document.getString("title") ?: "Unknown Title"
                    val coverImageUrl = document.getString("coverImageUrl") ?: ""
                    val finished = document.getBoolean("finished") ?: false

                    bookList.add(CreateRecycleViewDataClass(id, coverImageUrl, title, finished))
                }

                val adapter = CreateFragmentRecycleViewAdapter(
                    bookList,
                    onItemClick = { bookId ->
                        val action = CreateFragmentDirections.actionCreateFragmentToRecycleViewItemClicked(bookId)
                        findNavController().navigate(action)
                    },
                    onAddChapterClick = { bookId ->
                        val action = CreateFragmentDirections.actionCreateFragmentToAddChapterButton(bookId)
                        findNavController().navigate(action)
                    },
                    onEditStoryClick = { bookId ->
                        val action = CreateFragmentDirections.actionCreateFragmentToEditStory(bookId)
                        findNavController().navigate(action)
                    },
                    onFinishStoryClick = { bookId ->
                        finishStory(bookId)
                        loadBooksFromFirestore()
                    }
                )

                recyclerView.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Failed to load books: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun finishStory(bookId: String) {
        db.collection("books").document(bookId)
            .update("finished", true) // Додаємо або оновлюємо поле `finished`
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Story marked as finished.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to finish story: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}