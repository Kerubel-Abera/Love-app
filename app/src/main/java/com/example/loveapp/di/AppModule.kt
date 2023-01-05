package com.example.loveapp.di

import com.example.loveapp.data.AuthRepository
import com.example.loveapp.data.AuthRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Provides
    fun provideFireBaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    fun provideFireStore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    fun provideAuthRepository(impl: AuthRepositoryImpl): AuthRepository = impl
}