package xyz.haloai.haloai_android_productivity.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import xyz.haloai.haloai_android_productivity.data.local.entities.LTGoal

@Dao
interface LTGoalDao {

    @Insert
    fun insertGoal(goal: LTGoal): Long

    @Query("SELECT * FROM ltGoalsDb")
    fun getAll(): List<LTGoal>

    @Query("SELECT * FROM ltGoalsDb WHERE id = :id")
    fun getById(id: Long): LTGoal

    @Query("DELETE FROM ltGoalsDb")
    fun deleteAll()

    @Query("DELETE FROM ltGoalsDb WHERE id = :id")
    fun deleteById(id: Long)

    @Query("UPDATE ltGoalsDb SET title = :title, userContext = :userContext WHERE id = :id")
    fun updateById(id: Long, title: String, userContext: String)

    @Query("UPDATE ltGoalsDb SET userContext = :userContext WHERE id = :id")
    fun updateContentById(id: Long, userContext: String)

    @Query("UPDATE ltGoalsDb SET isActive = :isActive WHERE id = :id")
    fun updateIsActive(id: Long, isActive: Boolean)

    @Query("UPDATE ltGoalsDb SET deadline = :deadline WHERE id = :id")
    fun updateDeadline(id: Long, deadline: Long)

}