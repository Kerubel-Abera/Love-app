package com.example.loveapp.data

interface RequestCallbacks {
    fun onAccept(request: Request)
    fun onDecline(request: Request)
}
