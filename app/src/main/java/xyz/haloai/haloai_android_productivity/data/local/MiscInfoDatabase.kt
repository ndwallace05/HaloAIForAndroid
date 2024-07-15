package xyz.haloai.haloai_android_productivity.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import xyz.haloai.haloai_android_productivity.data.local.dao.MiscInfoDao
import xyz.haloai.haloai_android_productivity.data.local.entities.KVPair

@Database(entities = [KVPair::class], version = 1)
public abstract class MiscInfoDatabase : RoomDatabase() {
    abstract fun miscInfoDao(): MiscInfoDao

    companion object {
        @Volatile
        private var INSTANCE: MiscInfoDatabase? = null

        fun getDatabase(context: Context): MiscInfoDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MiscInfoDatabase::class.java,
                    "miscInfoDb"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}