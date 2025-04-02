package com.example.firstapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


class add_chapter_button : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var chapterTitleEditText: EditText
    private lateinit var chapterTextEditText: EditText
    private lateinit var bookId: String
    private lateinit var saveChapterButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_chapter_button, container, false)

        db = FirebaseFirestore.getInstance()
        chapterTitleEditText = view.findViewById(R.id.addChapterTitleEditText)
        chapterTextEditText = view.findViewById(R.id.addChapterTextEditText)
        saveChapterButton = view.findViewById(R.id.saveChapterButton)

        val bookId = arguments?.getString("bookId") ?: return view

        val saveButton = view.findViewById<Button>(R.id.saveChapterButton)
        saveButton.setOnClickListener {
            saveChapter(bookId)
        }

        return view
    }

    private fun saveChapter(bookId: String) {
        val title = chapterTitleEditText.text.toString().trim()
        val text = chapterTextEditText.text.toString().trim()

        if (title.isEmpty() || text.isEmpty()) {
            Toast.makeText(context, "Title and text are required", Toast.LENGTH_SHORT).show()
            return
        }

        val chapterRef = db.collection("books").document(bookId).collection("chapters").document()
        val chapterID = chapterRef.id

        val chapterData = hashMapOf(
            "bookId" to bookId,
            "title" to title,
            "text" to text,
            "timestamp" to System.currentTimeMillis(),
            "chapterID" to chapterID
        )

        chapterRef.set(chapterData)
            .addOnSuccessListener {
                db.collection("books")
                    .document(bookId)
                    .update("chapterCount", FieldValue.increment(1))
                    .addOnSuccessListener {
                        Toast.makeText(context, "Chapter added successfully", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()

                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Chapter added but failed to update count: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to add chapter: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}