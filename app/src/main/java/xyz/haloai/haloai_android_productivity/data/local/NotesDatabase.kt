package xyz.haloai.haloai_android_productivity.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import xyz.haloai.haloai_android_productivity.data.local.dao.NoteDao
import xyz.haloai.haloai_android_productivity.data.local.entities.Note

@Database(entities = [Note::class], version = 1)
public abstract class NotesDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: NotesDatabase? = null

        fun getDatabase(context: Context): NotesDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotesDatabase::class.java,
                    "notesDb"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}