package com.example.firstapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapp.Adapter.NotificationAdapter
import com.example.firstapp.R
import com.example.firstapp.data.notificationDataClass
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class NotificationFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private lateinit var notificationList: ArrayList<notificationDataClass>
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var notificationRecycleView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        notificationRecycleView = view.findViewById(R.id.notificationRecycleView)
        notificationList = ArrayList()
        notificationAdapter = NotificationAdapter(notificationList)
        notificationRecycleView.layoutManager = LinearLayoutManager(requireContext())
        notificationRecycleView.adapter = notificationAdapter

        auth = FirebaseAuth.getInstance()
        fetchLikedBooks()

        return view
    }

    private fun fetchLikedBooks() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val likedBooks = document.get("likedBooks") as? List<String> ?: emptyList()

                if (likedBooks.isEmpty()) {
                    Toast.makeText(context, "No liked books found.", Toast.LENGTH_SHORT).show()
                } else {
                    listenForNewChapters(likedBooks)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to fetch liked books: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun listenForNewChapters(likedBookIds: List<String>) {
        if (likedBookIds.isEmpty()) {
            Toast.makeText(context, "No liked books found", Toast.LENGTH_SHORT).show()
            return
        }

        db.collectionGroup("chapters")
            .whereIn("bookId", likedBookIds)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(context, "Error fetching chapters: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    for (doc in snapshot.documentChanges) {
                        if (doc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                            val bookId = doc.document.getString("bookId") ?: "Unknown"
                            val chapterTitle = doc.document.getString("title") ?: "No Title"
                            val timestamp = doc.document.getLong("timestamp") ?: 0L
                            val userId = auth.currentUser?.uid ?: return@addSnapshotListener

                            // Отримуємо назву книги
                            getLikedTimestamp(bookId, userId) { likedTimestamp ->
                                if (likedTimestamp != null && timestamp > likedTimestamp) {
                                    // Якщо розділ був доданий після того, як книга була лайкнута
                                    fetchBookTitle(bookId) { bookTitle ->
                                        val notification = notificationDataClass(
                                            bookId = bookId,
                                            title = "New Chapter in $bookTitle",
                                            description = "Title: $chapterTitle",
                                            timestamp = timestamp
                                        )
                                        addNotification(notification)
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }

    private fun getLikedTimestamp(bookId: String, userId: String, onResult: (Long?) -> Unit) {
        val userDocRef = db.collection("users").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val likedBooks = document.get("likedBooks") as? List<Map<String, Any>> ?: emptyList()

                // Знайти книгу в списку likedBooks і отримати її timestamp
                val likedBook = likedBooks.find { it["bookId"] == bookId }
                val timestamp = likedBook?.get("timestamp") as? Long

                onResult(timestamp) // Передаємо timestamp в onResult
            } else {
                onResult(null) // Якщо користувача не знайдено, передаємо null
            }
        }.addOnFailureListener { e ->
            onResult(null) // Якщо сталася помилка, передаємо null
        }
    }

    private fun fetchBookTitle(bookId: String, callback: (String) -> Unit) {
        db.collection("books").document(bookId).get()
            .addOnSuccessListener { document ->
                val bookTitle = document.getString("title") ?: "Unknown Book"
                callback(bookTitle)
            }
            .addOnFailureListener {
                callback("Unknown Book")
            }
    }

    private fun addNotification(notification: notificationDataClass) {
        notificationList.add(0, notification) // Додаємо сповіщення на початок списку
        notificationAdapter.notifyItemInserted(0) // Оновлюємо адаптер
        notificationRecycleView.scrollToPosition(0) // Скролимо до нового сповіщення
    }
}
