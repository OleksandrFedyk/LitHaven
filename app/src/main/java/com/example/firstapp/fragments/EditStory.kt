package com.example.firstapp.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.example.firstapp.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class editStory : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var editStoryButton: Button
    private lateinit var editStoryImage: ImageView

    private val storage = FirebaseStorage.getInstance()
    private var selectedImageUri: Uri? = null

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            editStoryImage.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_story, container, false)

        editStoryButton = view.findViewById(R.id.editStoryButton)
        val editStoryThemes = view.findViewById<EditText>(R.id.editStoryThemes)
        val editStoryDescription = view.findViewById<EditText>(R.id.editStoryDescription)
        val editStoryTitle = view.findViewById<EditText>(R.id.editStoryTitle)
        editStoryImage = view.findViewById(R.id.editStoryImage)

        editStoryImage.setOnClickListener { selectImageFromGallery() }

        val bookId = arguments?.getString("bookId") ?: return view

        editStoryButton.setOnClickListener {
            val updates = mutableMapOf<String, Any>()

            val title = editStoryTitle.text.toString()
            val description = editStoryDescription.text.toString()

            if (title.isNotBlank()) updates["title"] = title
            if (description.isNotBlank()) updates["description"] = description
            val themes = editStoryThemes.text.toString().split(" ").map { it.trim() }
            if (themes.isNotEmpty()) updates["genres"] = themes

            if (selectedImageUri != null) {
                val imageRef = storage.reference.child("book_covers/${bookId}.jpg")
                imageRef.putFile(selectedImageUri!!)
                    .addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            updates["coverImageUrl"] = uri.toString()
                            updateFirestore(bookId, updates)
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                updateFirestore(bookId, updates)
            }

        }

        return view
    }

    private fun selectImageFromGallery() {
        selectImageLauncher.launch("image/*")
    }

    private fun updateFirestore(bookId: String, updates: Map<String, Any>) {
        if (updates.isNotEmpty()) {
            db.collection("books").document(bookId)
                .update(updates)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Book updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to update book: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "No changes to update", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }
}