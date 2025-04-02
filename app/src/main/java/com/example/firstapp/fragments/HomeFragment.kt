package com.example.firstapp.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.example.firstapp.R
import com.example.firstapp.Adapter.RecycleViewAdapter
import com.example.firstapp.Adapter.RecycleViewHomeItem1Adapter
import com.example.firstapp.data.RecycleViewDataClassItem1
import com.example.firstapp.data.RecycleViewItemData
import com.google.android.gms.tasks.Tasks
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Calendar

class HomeFragment : Fragment() {

    private lateinit var recyclerViewFinishedStories: RecyclerView
    private lateinit var recyclerView2: RecyclerView
    private lateinit var recyclerViewHugeStories: RecyclerView
    private lateinit var recyclerViewTopPicks: RecyclerView
    private lateinit var recycleViewAdapterForFinishedStories: RecycleViewAdapter
    private lateinit var recycleViewAdapterForBooksYouWillLike: RecycleViewHomeItem1Adapter
    private lateinit var recycleViewAdapterForTopPicks: RecycleViewAdapter
    private lateinit var recycleViewAdapterForHugeStories: RecycleViewHomeItem1Adapter
    private lateinit var recyclerVieMostLikes: RecyclerView
    private lateinit var recyclerViewForMostPopular: RecyclerView
    private lateinit var recycleViewAdapterForMostLikes: RecycleViewHomeItem1Adapter
    private lateinit var recycleViewAdapterForMostPopular: RecycleViewHomeItem1Adapter
    private lateinit var userAccName: TextView
    private lateinit var logOut: ImageButton
    private lateinit var auth: FirebaseAuth
    private var db = FirebaseFirestore.getInstance()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUser == null) {
            val intent = Intent(requireContext(), Login::class.java)
            startActivity(intent)
        }

        recyclerViewFinishedStories = view.findViewById(R.id.recyclerView)
        recyclerViewFinishedStories.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        recyclerView2 = view.findViewById(R.id.recyclerView2)
        recyclerView2.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        recyclerVieMostLikes = view.findViewById(R.id.recyclerView3)
        recyclerVieMostLikes.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        recyclerViewTopPicks = view.findViewById(R.id.recycleView4)
        recyclerViewTopPicks.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        recyclerViewHugeStories = view.findViewById(R.id.recycleView5)
        recyclerViewHugeStories.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        recyclerViewForMostPopular = view.findViewById(R.id.recycleView6)
        recyclerViewForMostPopular.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        loadBooksForFinishedStories()
        loadBooksWeThinkYouWillEnjoy()
        loadBookTheMostLikes()
        loadBooksForTopPicks()
        loadBooksHugeStories()
        loadBooksForMostPopular()

        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerViewFinishedStories)
        snapHelper.attachToRecyclerView(recyclerViewTopPicks)

        return view
    }

    private fun loadBooksForFinishedStories(){
        val finishedStories = ArrayList<RecycleViewItemData>()
        db.collection("books")
            .whereEqualTo("finished", true)
            .get()
            .addOnSuccessListener { document ->
                for (document in document) {
                    val title = document.getString("title")
                    val description = document.getString("description")
                    val coverImageUrl = document.getString("coverImageUrl")
                    val bookId = document.id
                    if (title != null && description != null && coverImageUrl != null) {
                        finishedStories.add(RecycleViewItemData(title, description, coverImageUrl, bookId))
                    }
                }

                recycleViewAdapterForFinishedStories = RecycleViewAdapter(finishedStories, onItemClick = { bookId ->
                    val action = HomeFragmentDirections.actionHomeFragmentToRecycleViewItemClicked(bookId)
                    findNavController().navigate(action)
                })
                recyclerViewFinishedStories.adapter = recycleViewAdapterForFinishedStories

            }
            .addOnFailureListener { exception ->
                Log.e("HomeFragment", "Error fetching finished books: ${exception.message}")
            }
    }

    private fun loadBooksWeThinkYouWillEnjoy() {
        val weThinkYouWillEnjoy = ArrayList<RecycleViewDataClassItem1>()
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUser != null) {
            db.collection("users")
                .document(currentUser)
                .get()
                .addOnSuccessListener { documents ->
                    val likedAuthorIds = (documents.get("likedAuthors") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    Log.d("HomeFragment", "Liked Author IDs: $likedAuthorIds")

                    if (likedAuthorIds.isNotEmpty()) {
                        db.collection("books")
                            .whereIn("authorId", likedAuthorIds)
                            .get()
                            .addOnSuccessListener { books ->
                                for (book in books) {
                                    val title = book.getString("title")
                                    val imageUrl = book.getString("coverImageUrl")
                                    val bookId = book.id
                                    if (title != null && imageUrl != null) {
                                        weThinkYouWillEnjoy.add(
                                            RecycleViewDataClassItem1(
                                                title,
                                                imageUrl,
                                                bookId
                                            )
                                        )
                                    }
                                }

                                if (weThinkYouWillEnjoy.isNotEmpty()) {
                                    recycleViewAdapterForBooksYouWillLike = RecycleViewHomeItem1Adapter(weThinkYouWillEnjoy,
                                        onItemClick = { bookId ->
                                            val action =
                                                HomeFragmentDirections.actionHomeFragmentToRecycleViewItemClicked(bookId)
                                            findNavController().navigate(action)
                                        })
                                    recyclerView2.adapter = recycleViewAdapterForBooksYouWillLike
                                } else {
                                    Log.d("HomeFragment", "No books found for liked authors.")
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("HomeFragment", "Error fetching books: ${exception.message}")
                            }
                    } else {
                        Log.d("HomeFragment", "No liked authors found.")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("HomeFragment", "Error fetching user data: ${exception.message}")
                }
        }
    }

    private fun loadBookTheMostLikes(){
        val db = FirebaseFirestore.getInstance()
        val mostLikedList = ArrayList<RecycleViewDataClassItem1>()

        db.collection("books")
            .orderBy("likes", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { books ->
                for (book in books){
                    val title = book.getString("title")
                    val imageUrl = book.getString("coverImageUrl")
                    val bookId = book.id
                    if (title != null && imageUrl != null){
                        mostLikedList.add(RecycleViewDataClassItem1(title, imageUrl, bookId))
                    }
                }
                val limitedList = mostLikedList.take(7)
                recycleViewAdapterForMostLikes = RecycleViewHomeItem1Adapter(limitedList as ArrayList<RecycleViewDataClassItem1>, onItemClick = { bookId ->
                    val action = HomeFragmentDirections.actionHomeFragmentToRecycleViewItemClicked(bookId)
                    findNavController().navigate(action)
                })
                recyclerVieMostLikes.adapter = recycleViewAdapterForMostLikes
            }

    }

    private fun loadBooksForTopPicks(){
        val db = FirebaseFirestore.getInstance()
        val topPicksList = ArrayList<RecycleViewItemData>()
        db.collection("books")
            .get()
            .addOnSuccessListener { books ->
                for (book in books){
                    val title = book.getString("title")
                    val description = book.getString("description")
                    val coverImageUrl = book.getString("coverImageUrl")
                    val bookId = book.id
                    if (title != null && description != null && coverImageUrl != null){
                        topPicksList.add(RecycleViewItemData(title, description, coverImageUrl, bookId))
                    }
                }
                val limitedAndShuffledList = topPicksList.take(5).shuffled()
                recycleViewAdapterForTopPicks = RecycleViewAdapter(limitedAndShuffledList as ArrayList<RecycleViewItemData>, onItemClick = { bookId ->
                    val action = HomeFragmentDirections.actionHomeFragmentToRecycleViewItemClicked(bookId)
                    findNavController().navigate(action)
                })
                recyclerViewTopPicks.adapter = recycleViewAdapterForTopPicks

            }

    }

    private fun loadBooksHugeStories() {
        val db = FirebaseFirestore.getInstance()
        val hugeStoriesList = ArrayList<RecycleViewDataClassItem1>()

        db.collection("books")
            .orderBy("chapterCount", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { books ->
                for (book in books) {
                    val title = book.getString("title")
                    val imageUrl = book.getString("coverImageUrl")
                    val bookId = book.id

                    if (title != null && imageUrl != null) {
                        hugeStoriesList.add(RecycleViewDataClassItem1(title, imageUrl, bookId))
                    }
                }
                val limitedList = hugeStoriesList.take(7)
                recycleViewAdapterForHugeStories = RecycleViewHomeItem1Adapter(
                    limitedList as ArrayList<RecycleViewDataClassItem1>,
                    onItemClick = { bookId ->
                        val action = HomeFragmentDirections.actionHomeFragmentToRecycleViewItemClicked(bookId)
                        findNavController().navigate(action)
                    }
                )
                recyclerViewHugeStories.adapter = recycleViewAdapterForHugeStories
            }
            .addOnFailureListener { exception ->
                Log.e("HomeFragment", "Error fetching books: ${exception.message}")
            }
    }

    private fun loadBooksForMostPopular() {
        val db = FirebaseFirestore.getInstance()
        val mostPopularList = ArrayList<RecycleViewDataClassItem1>()
        db.collection("books")
            .orderBy("amountOfReads", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { books ->
                for (book in books) {
                    val title = book.getString("title")
                    val imageUrl = book.getString("coverImageUrl")
                    val bookId = book.id
                    if (title != null && imageUrl != null) {
                        mostPopularList.add(RecycleViewDataClassItem1(title, imageUrl, bookId))
                    }
                }
                val limitedList = mostPopularList.take(7)
                recycleViewAdapterForMostPopular = RecycleViewHomeItem1Adapter(
                    limitedList as ArrayList<RecycleViewDataClassItem1>,
                    onItemClick = { bookId ->
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToRecycleViewItemClicked(bookId)
                        findNavController().navigate(action) })

                recyclerViewForMostPopular.adapter = recycleViewAdapterForMostPopular
            }

    }


}