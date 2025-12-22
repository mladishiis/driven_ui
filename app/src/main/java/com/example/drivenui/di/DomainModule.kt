package com.example.drivenui.di

import com.example.drivenui.domain.FileInteractor
import com.example.drivenui.domain.FileInteractorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface DomainModule {

    @Binds
    @Singleton
    fun bindFileInteractor(
        impl: FileInteractorImpl
    ): FileInteractor
}
