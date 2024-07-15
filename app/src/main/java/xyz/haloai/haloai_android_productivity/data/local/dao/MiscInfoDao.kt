package xyz.haloai.haloai_android_productivity.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import xyz.haloai.haloai_android_productivity.data.local.entities.KVPair

@Dao
interface MiscInfoDao {
    @Insert
    fun insert(miscInfo: KVPair): Long

    @Query("SELECT * FROM KVPair")
    fun getAll(): List<KVPair>

    @Query("SELECT * FROM KVPair WHERE k = :key")
    fun getByKey(key: String): KVPair

    @Query("UPDATE KVPair SET v = :value WHERE k = :key")
    fun updateByKey(key: String, value: String)

    @Query("DELETE FROM KVPair")
    fun deleteAll()

    @Query("DELETE FROM KVPair WHERE k = :key")
    fun deleteByKey(key: String)
}