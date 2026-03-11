package com.example.drivenui.app.di

import android.content.Context
import com.example.drivenui.app.data.AssetsMicroappFileProvider
import com.example.drivenui.app.data.DirMicroappFileProvider
import com.example.drivenui.app.data.FileRepository
import com.example.drivenui.app.data.FileRepositoryImpl
import com.example.drivenui.app.data.MicroappCollectionApi
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
import com.google.gson.Gson
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
     * Предоставляет Context приложения.
     *
     * @param context ApplicationContext от Hilt
     * @return Context приложения
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

    /**
     * @param context ApplicationContext
     * @return FileRepository
     */
    @Provides
    @Singleton
    fun provideFileRepository(@ApplicationContext context: Context): FileRepository {
        return FileRepositoryImpl(context)
    }

    /**
     * @param context ApplicationContext
     * @return MicroappStorage
     */
    @Provides
    @Singleton
    fun provideMicroappStorage(@ApplicationContext context: Context): MicroappStorage {
        return MicroappStorageImpl(context)
    }

    /**
     * @param client OkHttpClient
     * @param gson Gson
     * @return MicroappCollectionApi
     */
    @Provides
    @Singleton
    fun provideMicroappCollectionApi(
        client: OkHttpClient,
        gson: Gson,
    ): MicroappCollectionApi = MicroappCollectionApi(client, gson)

    /**
     * @param context ApplicationContext
     * @param source Источник микроаппа (ASSETS или FILE_SYSTEM_JSON)
     * @return MicroappFileProvider
     */
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

    /**
     * @param fileRepository Репозиторий файлов
     * @param context Контекст приложения
     * @param microappSource Источник микроаппа
     * @param fileProvider Провайдер файлов микроаппа
     * @param microappStorage Хранилище микроаппов
     * @return FileInteractor
     */
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

    /**
     * @param context Контекст приложения
     * @param client OkHttpClient
     * @param gson Gson
     * @return FileDownloadInteractor
     */
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
        return MicroappSource.FILE_SYSTEM_JSON
    }

    /**
     * Предоставляет OkHttpClient для загрузки файлов.
     *
     * @return OkHttpClient с настроенными таймаутами
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