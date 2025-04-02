package com.example.firstapp

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class CreateNewStory : Fragment() {

    private lateinit var bookCoverImageView: ImageView
    private lateinit var bookTitleInput: EditText
    private lateinit var bookDescriptionInput: EditText
    private lateinit var genresInput: EditText
    private lateinit var saveBookButton: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var selectedImageUri: Uri? = null

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            bookCoverImageView.setImageURI(it) // Встановлюємо обране зображення
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_new_story, container, false)

        bookCoverImageView = view.findViewById(R.id.createNewStoryImage)
        bookTitleInput = view.findViewById(R.id.createNewStoryTitle)
        bookDescriptionInput = view.findViewById(R.id.createNewStoryDescription)
        genresInput = view.findViewById(R.id.createNewStoryThemes)
        saveBookButton = view.findViewById(R.id.createNewStoryFragmentButton)

        bookCoverImageView.setOnClickListener { selectImageFromGallery() }
        saveBookButton.setOnClickListener { saveBook() }
        // Inflate the layout for this fragment

        return view
    }

    private fun selectImageFromGallery() {
        selectImageLauncher.launch("image/*")
    }

    private fun saveBook() {
        val title = bookTitleInput.text.toString().trim()
        val description = bookDescriptionInput.text.toString().trim()
        val genresText = genresInput.text.toString().trim()

        if (title.isBlank() || description.isBlank() || genresText.isBlank()) {
            Toast.makeText(requireContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show()
            return
        }

        val genresList = genresText.split(" ").filter { it.isNotBlank() }
        if (genresList.isEmpty()) {
            Toast.makeText(requireContext(), "Please provide at least one genre!", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Please select a cover image!", Toast.LENGTH_SHORT).show()
            return
        }

        uploadCoverImage(title, description, genresList)
    }


    private fun uploadCoverImage(
        title: String,
        description: String,
        genresList: List<String>
    ) {
        val coverImageRef = storage.reference.child("book_covers/${System.currentTimeMillis()}.jpg")

        val bitmap = (bookCoverImageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        coverImageRef.putBytes(data)
            .addOnSuccessListener {
                coverImageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveBookToFirestore(title, description, uri.toString(), genresList)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun saveBookToFirestore(
        title: String,
        description: String,
        coverImageUrl: String,
        genresList: List<String>
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "You need to be signed in to save a book.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                val authorName = document.getString("accountName") ?: "Unknown Author"
                val bookData = hashMapOf(
                    "title" to title,
                    "description" to description,
                    "coverImageUrl" to coverImageUrl,
                    "genres" to genresList,
                    "authorId" to currentUser.uid,
                    "authorName" to authorName, // Set author name after getting it from Firestore
                    "timestamp" to System.currentTimeMillis(),
                    "chapterCount" to 0,
                )

                // Save the book data to Firestore
                db.collection("books")
                    .add(bookData)
                    .addOnSuccessListener { documentReference ->
                        // After saving book, handle the success here
                        val bookId = documentReference.id
                        Toast.makeText(requireContext(), "Book saved successfully!", Toast.LENGTH_SHORT).show()

                        // Add chapter to the book
//                        addChapterToBook(bookId, titleChapter, textChapter)

                        findNavController().navigateUp() // Navigate back
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to save book: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to fetch author name: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

//    private fun addChapterToBook(bookId: String, chapterTitle: String, chapterText: String) {
//        val db = FirebaseFirestore.getInstance()
//
//        // Створюємо посилання на підколекцію `chapters` книги
//        val chaptersCollectionRef = db.collection("books")
//            .document(bookId)
//            .collection("chapters")
//
//        // Дані глави
//        val chapterData = hashMapOf(
//            "chapterTitle" to chapterTitle,
//            "chapterText" to chapterText,
//            "timestamp" to System.currentTimeMillis()
//        )
//
//        // Додаємо главу до підколекції
//        chaptersCollectionRef.add(chapterData)
//            .addOnSuccessListener {
//                Toast.makeText(requireContext(), "Chapter added successfully!", Toast.LENGTH_SHORT).show()
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(requireContext(), "Failed to add chapter: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }



}