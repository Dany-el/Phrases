package org.hyperskill.phrases

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PhraseDao {
    @Insert
    fun insert(vararg phrase: Phrase)

    @Delete
    fun delete(phrase: Phrase)

    @Query("SELECT * FROM phrases")
    fun getAll(): List<Phrase>

    @Query("SELECT * FROM phrases WHERE phrase = :phrase")
    fun get(phrase: String): List<Phrase>
}
