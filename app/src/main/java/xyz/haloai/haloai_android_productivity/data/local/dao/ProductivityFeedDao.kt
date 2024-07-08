package xyz.haloai.haloai_android_productivity.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import xyz.haloai.haloai_android_productivity.data.local.entities.FeedCard

@Dao
interface ProductivityFeedDao {

    @Insert
    fun insert(feedCard: FeedCard): Long

    @Query("SELECT * FROM productivityFeed ORDER BY importanceScore DESC")
    fun getAll(): List<FeedCard>

    @Query("SELECT * FROM productivityFeed WHERE id = :id")
    fun getById(id: Long): FeedCard

    @Query("DELETE FROM productivityFeed")
    fun deleteAll()

    @Query("DELETE FROM productivityFeed WHERE id = :id")
    fun deleteById(id: Long)

    @Query("UPDATE productivityFeed SET isCompleted = :isCompleted WHERE id = :id")
    fun markFeedCardAsCompleted(id: Long, isCompleted: Boolean = true)

    @Query("UPDATE productivityFeed SET importanceScore = :importanceScore WHERE id = :id")
    fun updateImportanceScore(id: Long, importanceScore: Int)

    @Query("UPDATE productivityFeed SET deadline = :deadline WHERE id = :id")
    fun updateDeadline(id: Long, deadline: Long)

    @Query("UPDATE productivityFeed SET imgBase64 = :imgBase64 WHERE id = :id")
    fun updateImgBase64(id: Long, imgBase64: String)

    @Query("UPDATE productivityFeed SET primaryActionType = :primaryActionType WHERE id = :id")
    fun updatePrimaryActionType(id: Long, primaryActionType: Int)


}