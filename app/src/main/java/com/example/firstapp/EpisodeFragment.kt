package com.example.firstapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.example.firstapp.fragments.RecycleViewItemClickedArgs
import com.google.firebase.firestore.FirebaseFirestore

class EpisodeFragment : Fragment() {

    private val args: EpisodeFragmentArgs by navArgs()

    private val db = FirebaseFirestore.getInstance()

    private lateinit var episodeTitle: TextView
    private lateinit var episodeText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_episode, container, false)



        episodeTitle = view.findViewById(R.id.episodeFragmentTitle)
        episodeText = view.findViewById(R.id.episodeFragmentText)

        val chapterID = args.chapterID
        val bookId = args.bookId
        Log.d("EpisodeFragment", "Chapter ID: $chapterID")
        Log.d("EpisodeFragment", "Chapter ID from args: $chapterID")

        FirebaseFirestore.getInstance().collection("books")
            .document(bookId)
            .collection("chapters")
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("EpisodeFragment", "Collection data: ${querySnapshot.documents}")
            }
            .addOnFailureListener { e ->
                Log.e("EpisodeFragment", "Error getting documents: ", e)
            }

        FirebaseFirestore.getInstance().clearPersistence()
        loadEpisodeData(chapterID, bookId)



        return view
    }

    private fun loadEpisodeData(chapterID: String, bookId: String) {
        Log.d("EpisodeFragment", "Loading data for chapter ID: $chapterID")

        db.collection("books")
            .document(bookId)
            .collection("chapters")
            .document(chapterID)
            .get()
            .addOnSuccessListener { document ->
                Log.d("EpisodeFragment", "Document data: ${document.data}")
                if (document != null && document.exists()) {
                    val title = document.getString("title") ?: "No Title"
                    val text = document.getString("text") ?: "No Text"

                    episodeTitle.text = title
                    episodeText.text = text
                } else {
                    Toast.makeText(requireContext(), "Chapter not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}