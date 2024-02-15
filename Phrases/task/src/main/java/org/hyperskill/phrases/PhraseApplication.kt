package org.hyperskill.phrases

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration

class PhraseApplication : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "phrases.db"
        )
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }

    fun resetDatabase(context: Context) {
        context.deleteDatabase("phrases.db")
    }
}