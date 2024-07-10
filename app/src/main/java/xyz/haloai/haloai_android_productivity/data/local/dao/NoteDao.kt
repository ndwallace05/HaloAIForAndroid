package xyz.haloai.haloai_android_productivity.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import xyz.haloai.haloai_android_productivity.data.local.entities.Note

@Dao
interface NoteDao {
    @Insert
    fun insert(note: Note): Long

    @Query("SELECT * FROM Note")
    fun getAll(): List<Note>

    @Query("SELECT * FROM Note WHERE id = :id")
    fun getById(id: Long): Note

    @Query("DELETE FROM Note")
    fun deleteAll()

    @Query("DELETE FROM Note WHERE id = :id")
    fun deleteById(id: Long)

    @Query("UPDATE Note SET title = :title, content = :content, tag = :tag WHERE id = :id")
    fun updateById(id: Long, title: String, content: String, tag: String)

    @Query("UPDATE Note SET content = :content WHERE id = :id")
    fun updateContentById(id: Long, content: String)
}