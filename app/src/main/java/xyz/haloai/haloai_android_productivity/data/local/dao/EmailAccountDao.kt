package xyz.haloai.haloai_android_productivity.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import xyz.haloai.haloai_android_productivity.data.local.entities.EmailAccount
import xyz.haloai.haloai_android_productivity.data.local.entities.enumEmailType


@Dao
interface EmailAccountDao {
    @Insert
    fun insertAccount(userAccount: EmailAccount): Long

    @Query("SELECT * FROM emailDb")
    fun getAllAccounts(): List<EmailAccount>

    @Query("SELECT * FROM emailDb WHERE emailType = :type")
    fun getAccountsByType(type: enumEmailType): List<EmailAccount>

    @Query("SELECT * FROM emailDb WHERE email = :email")
    fun getAccountByEmail(email: String): EmailAccount

    @Query("DELETE FROM emailDb")
    fun deleteAllAccounts()

    @Query("DELETE FROM emailDb WHERE email = :email")
    fun deleteByEmail(email: String)

    @Query("UPDATE emailDb SET lastCalendarUpdateDateTime = :lastCalendarUpdateDateTime WHERE email = :email")
    fun updateLastCalendarUpdateDateTime(email: String, lastCalendarUpdateDateTime: Long)

    @Query("UPDATE emailDb SET calendarIds = :calendarIds WHERE email = :email")
    fun updateCalendarIds(email: String, calendarIds: List<String>)

    @Query("SELECT * FROM emailDb WHERE isActive")
    fun getActiveAccounts(): List<EmailAccount>

    @Query("SELECT * FROM emailDb WHERE not isActive")
    fun getInactiveAccounts(): List<EmailAccount>

    @Query("UPDATE emailDb SET isActive = :isActive WHERE email = :email")
    fun updateIsActive(email: String, isActive: Boolean)
}