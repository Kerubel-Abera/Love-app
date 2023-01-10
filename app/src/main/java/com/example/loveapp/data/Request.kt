package com.example.loveapp.data

import java.sql.Date

data class Request(
    val email: String,
    val name: String,
    val year: Int?,
    val month: Int?,
    val day: Int?
) {
    constructor(): this("", "", null, null, null)
}