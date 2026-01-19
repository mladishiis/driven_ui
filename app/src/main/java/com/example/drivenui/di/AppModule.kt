package com.example.drivenui.di

import android.content.Context
import com.example.drivenui.engine.context.ContextManager
import com.example.drivenui.engine.context.IContextManager
import com.example.drivenui.engine.generative_screen.action.DefaultExternalDeeplinkHandler
import com.example.drivenui.engine.generative_screen.action.ExternalDeeplinkHandler
import com.example.drivenui.engine.generative_screen.widget.IWidgetValueProvider
import com.example.drivenui.engine.generative_screen.widget.WidgetValueProvider
import com.example.drivenui.parser.SDUIParser
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
    fun provideSDUIParser(context: Context): SDUIParser {
        return SDUIParser(context)
    }

    @Provides
    @Singleton
    fun provideExternalDeeplinkHandler(
        defaultHandler: DefaultExternalDeeplinkHandler
    ): ExternalDeeplinkHandler = defaultHandler

    @Provides
    @Singleton
    fun provideContextManager(contextManager: ContextManager): IContextManager {
        return contextManager
    }

    @Provides
    @Singleton
    fun provideWidgetValueProvider(widgetValueProvider: WidgetValueProvider): IWidgetValueProvider {
        return widgetValueProvider
    }
}