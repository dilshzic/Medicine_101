package com.algorithmx.medmate.di

import android.content.Context
import androidx.room.Room
import com.algorithmx.medicine101.data.AppDatabase
import com.algorithmx.medicine101.data.NoteDao
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "medmate_db"
        )
        .addMigrations(AppDatabase.MIGRATION_1_2)
        .fallbackToDestructiveMigration() // Keep as a safety net during dev
        .build()
    }

    @Provides
    fun provideNoteDao(db: AppDatabase): NoteDao {
        return db.noteDao()
    }
}