package com.example.kampai.di

import android.content.Context
import androidx.room.Room
import com.example.kampai.data.local.AppDatabase
import com.example.kampai.data.local.dao.CustomCardDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "kampai_database"
        ).fallbackToDestructiveMigration() // Esto evita crasheos si cambias la DB en el futuro
            .build()
    }

    @Provides
    @Singleton
    fun provideCustomCardDao(database: AppDatabase): CustomCardDao {
        return database.customCardDao()
    }
}