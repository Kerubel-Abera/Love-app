package com.example.loveapp.data

data class Request(
    val email: String,
    val name: String
) {
    constructor(): this("", "")
}