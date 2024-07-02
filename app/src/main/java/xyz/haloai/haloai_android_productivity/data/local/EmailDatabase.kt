package xyz.haloai.haloai_android_productivity.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import xyz.haloai.haloai_android_productivity.data.local.dao.EmailAccountDao
import xyz.haloai.haloai_android_productivity.data.local.entities.EmailAccount
import xyz.haloai.haloai_android_productivity.data.local.typeConverters.ListConverter
import xyz.haloai.haloai_android_productivity.data.local.typeConverters.emailTypeConverter

@TypeConverters(ListConverter::class, emailTypeConverter::class)
@Database(entities = [EmailAccount::class], version = 1)
public abstract class EmailDatabase : RoomDatabase() {
    abstract fun emailAccountDao(): EmailAccountDao

    companion object {
        @Volatile
        private var INSTANCE: EmailDatabase? = null

        fun getDatabase(context: Context): EmailDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EmailDatabase::class.java,
                    "emailsDb"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}