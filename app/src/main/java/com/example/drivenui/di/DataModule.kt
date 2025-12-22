package com.example.drivenui.di

import com.example.drivenui.data.FileRepository
import com.example.drivenui.data.FileRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface DataModule {

    @Binds
    @Singleton
    fun bindFileRepository(
        impl: FileRepositoryImpl
    ): FileRepository
}
