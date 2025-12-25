package com.example.drivenui.di

import android.content.Context
import com.example.drivenui.parser.SDUIParserNew
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Предоставляет Context приложения
     */
    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideSDUIParser(context: Context): SDUIParserNew {
        return SDUIParserNew(context)
    }
}