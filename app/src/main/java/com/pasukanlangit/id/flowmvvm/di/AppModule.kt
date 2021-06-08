package com.pasukanlangit.id.flowmvvm.di

import android.app.Application
import androidx.room.Room
import com.pasukanlangit.id.flowmvvm.data.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        app: Application,
        callback: TaskDatabase.Callback
    ) = Room.databaseBuilder(app, TaskDatabase::class.java, "task_database")
            //when the version is upgrade just drop old table and build new
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .build()

    @Provides
    //automatic singleton because this function inside class db which singleton
    fun provideTaskDao(db: TaskDatabase) = db.taskDao()

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope