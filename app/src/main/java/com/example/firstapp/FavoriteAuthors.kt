package com.example.firstapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapp.Adapter.FavAuthorAdapter
import com.example.firstapp.data.FavAuthor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoriteAuthors : Fragment() {

    private lateinit var favAuthorsFragmentRecycleView: RecyclerView
    private lateinit var adapter: FavAuthorAdapter
    private var favAuthors: ArrayList<FavAuthor> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite_authors, container, false)

        favAuthorsFragmentRecycleView = view.findViewById(R.id.favAuthorsFragmentRecycleView)

        favAuthorsFragmentRecycleView.layoutManager = LinearLayoutManager(requireContext())
        adapter = FavAuthorAdapter(favAuthors){ authorId ->
            val action = FavoriteAuthorsDirections.actionFavoriteAuthorsToFavAuthorFragment(authorId)
            findNavController().navigate(action)
        }
        favAuthorsFragmentRecycleView.adapter = adapter
        loadFavAuthors()




        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    fun loadFavAuthors() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()
        if (userId != null) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val likedAuthors = document.get("likedAuthors") as? List<String>
                        if (!likedAuthors.isNullOrEmpty()) {
                            favAuthors.clear()

                            for (authorId in likedAuthors) {
                                db.collection("users").document(authorId)
                                    .get()
                                    .addOnSuccessListener { authorDoc ->
                                        if (authorDoc.exists()) {
                                            val authorName = authorDoc.getString("accountName") ?: "Unknown"
                                            favAuthors.add(FavAuthor(authorId, authorName))
                                            adapter.notifyDataSetChanged()
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        e.printStackTrace()
                                    }
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }
    }


}