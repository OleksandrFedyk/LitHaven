package com.example.firstapp.data

data class SearchBook(
    val id: String,
    val title: String,
    val description: String,
    val coverImageUrl: String,
    val genres: List<String>
)
