package com.example.drivenui.di

import android.content.Context
import com.example.drivenui.data.FileRepository
import com.example.drivenui.data.FileRepositoryImpl
import com.example.drivenui.domain.FileDownloadInteractor
import com.example.drivenui.domain.FileDownloadInteractorImpl
import com.example.drivenui.domain.FileInteractor
import com.example.drivenui.domain.FileInteractorImpl
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
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
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

    @Provides
    @Singleton
    fun provideFileRepository(@ApplicationContext context: Context): FileRepository {
        return FileRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideFileInteractor(
        fileRepository: FileRepository,
        @ApplicationContext context: Context,
        sdUiParser: SDUIParser
    ): FileInteractor {
        return FileInteractorImpl(fileRepository, context)
    }

    /**
     * Предоставляет FileDownloadInteractor
     */
    @Provides
    @Singleton
    fun provideFileDownloadInteractor(
        @ApplicationContext context: Context,
        client: OkHttpClient
    ): FileDownloadInteractor {
        return FileDownloadInteractorImpl(context, client)
    }

    /**
     * Предоставляет OkHttpClient для загрузки файлов
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}