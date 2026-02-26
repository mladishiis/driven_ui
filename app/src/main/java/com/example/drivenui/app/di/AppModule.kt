package com.example.drivenui.app.di

import android.content.Context
import com.example.drivenui.app.data.AssetsMicroappFileProvider
import com.example.drivenui.app.data.DirMicroappFileProvider
import com.example.drivenui.app.data.FileRepository
import com.example.drivenui.app.data.FileRepositoryImpl
import com.example.drivenui.app.data.MicroappStorageImpl
import com.example.drivenui.app.domain.FileDownloadInteractor
import com.example.drivenui.app.domain.FileDownloadInteractorImpl
import com.example.drivenui.app.domain.FileInteractor
import com.example.drivenui.app.domain.FileInteractorImpl
import com.example.drivenui.app.domain.MicroappFileProvider
import com.example.drivenui.app.domain.MicroappSource
import com.example.drivenui.app.domain.MicroappStorage
import com.example.drivenui.engine.context.ContextManager
import com.example.drivenui.engine.context.IContextManager
import com.example.drivenui.engine.generative_screen.action.DefaultExternalDeeplinkHandler
import com.example.drivenui.engine.generative_screen.action.ExternalDeeplinkHandler
import com.example.drivenui.engine.generative_screen.widget.IWidgetValueProvider
import com.example.drivenui.engine.generative_screen.widget.WidgetValueProvider
import com.example.drivenui.engine.parser.SDUIParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.google.gson.Gson
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
    fun provideSDUIParser(): SDUIParser {
        return SDUIParser()
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
    fun provideMicroappStorage(@ApplicationContext context: Context): MicroappStorage {
        return MicroappStorageImpl(context)
    }

    @Provides
    @Singleton
    fun provideMicroappFileProvider(
        @ApplicationContext context: Context,
        source: MicroappSource,
    ): MicroappFileProvider {
        return if (source == MicroappSource.ASSETS) {
            AssetsMicroappFileProvider(context)
        } else {
            DirMicroappFileProvider(context)
        }
    }

    @Provides
    @Singleton
    fun provideFileInteractor(
        fileRepository: FileRepository,
        @ApplicationContext context: Context,
        microappSource: MicroappSource,
        fileProvider: MicroappFileProvider,
        microappStorage: MicroappStorage,
    ): FileInteractor =
        FileInteractorImpl(
            fileRepository = fileRepository,
            context = context,
            source = microappSource,
            fileProvider = fileProvider,
            microappStorage = microappStorage,
        )

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideFileDownloadInteractor(
        @ApplicationContext context: Context,
        client: OkHttpClient,
        gson: Gson,
    ): FileDownloadInteractor =
        FileDownloadInteractorImpl(context, client, gson)

    @Provides
    @Singleton
    fun provideMicroappSource(): MicroappSource {
        // ASSETS | FILE_SYSTEM (архив напрямую) | FILE_SYSTEM_JSON (архив в JSON base64)
        return MicroappSource.FILE_SYSTEM
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