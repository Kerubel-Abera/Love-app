package com.example.loveapp.data

sealed class Resource<out Result> {
    data class Success<out Result>(val result: Result): Resource<Result>()
    data class Failure(val exception: Exception): Resource<Nothing>()
    object Loading: Resource<Nothing>()
}