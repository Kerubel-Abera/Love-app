package com.example.loveapp.data

data class Request(
    val email: String,
    val name: String,
    val date: List<Int>?
) {
    constructor() : this("", "", null)
}