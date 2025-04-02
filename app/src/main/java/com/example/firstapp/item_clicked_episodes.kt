package com.example.firstapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapp.Adapter.EpisodeAdapter
import com.example.firstapp.data.EpisodesDataClass
import com.google.firebase.firestore.FirebaseFirestore

class item_clicked_episodes : Fragment() {

    private lateinit var recycleViewEpisodes: RecyclerView
    private lateinit var adapter: EpisodeAdapter
    private val db = FirebaseFirestore.getInstance()
    private var episodeList = mutableListOf<EpisodesDataClass>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_clicked_episodes, container, false)
        recycleViewEpisodes = view.findViewById(R.id.recycle_view_episodes)
        recycleViewEpisodes.layoutManager = LinearLayoutManager(context)

        adapter = EpisodeAdapter(episodeList) { episodeID, bookId ->
            if (episodeID != null) {
                Log.d("EpisodeID", "Clicked episode ID: $episodeID")
                Log.d("BookID", "Clicked book ID: $bookId")
                val action = item_clicked_episodesDirections
                    .actionItemClickedEpisodesToEpisodeDetailsFragment(episodeID, bookId)
                findNavController().navigate(action)
            } else {
                Toast.makeText(context, "Invalid episode ID", Toast.LENGTH_SHORT).show()
            }
        }
        recycleViewEpisodes.adapter = adapter

        val bookId = arguments?.getString("bookId")
        if (bookId != null) {
            loadEpisodes(bookId)
        } else {
            Toast.makeText(requireContext(), "Book ID not found", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadEpisodes(bookId: String) {
        Log.d("Firestore", "Loading chapters for book ID: $bookId")

        db.collection("books")
            .document(bookId)
            .collection("chapters")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Toast.makeText(context, "No chapters found for this book", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val newEpisodeList = mutableListOf<EpisodesDataClass>()
                var chapterNumber = 1

                for (document in snapshot) {
                    val title = document.getString("title") ?: "No Title"
                    val id = document.id
                    val numberedTitle = "$chapterNumber"
                    newEpisodeList.add(EpisodesDataClass(bookId, title, id, numberedTitle))
                    chapterNumber++
                }

                episodeList.clear()
                episodeList.addAll(newEpisodeList)
                adapter.notifyDataSetChanged()

                Log.d("Firestore", "Loaded ${episodeList.size} chapters successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to load chapters: ${e.message}", e)
                Toast.makeText(context, "Failed to load episodes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
