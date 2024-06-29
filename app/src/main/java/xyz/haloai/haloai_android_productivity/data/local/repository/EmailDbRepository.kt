package xyz.haloai.haloai_android_productivity.data.local.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.haloai.haloai_android_productivity.data.local.dao.EmailAccountDao
import xyz.haloai.haloai_android_productivity.data.local.entities.EmailAccount
import xyz.haloai.haloai_android_productivity.data.local.entities.enumEmailType

class EmailDbRepository(private val emailDao: EmailAccountDao) {

    suspend fun getAllEmailIds(): List<String> = withContext(Dispatchers.IO) {

        var accounts = emailDao.getAllAccounts()
        return@withContext accounts.map { it.email }
    }

    suspend fun getAccountsByType(type: enumEmailType): List<EmailAccount> = withContext(Dispatchers.IO) {
        return@withContext emailDao.getAccountsByType(type)
    }

    suspend fun insert(email: EmailAccount) = withContext(Dispatchers.IO) {
        emailDao.insertAccount(email)
    }

    suspend fun updateCalendarIds(emailId: String, calendarIds: List<String>) = withContext(Dispatchers.IO) {
        emailDao.updateCalendarIds(emailId, calendarIds)
    }

    suspend fun deleteById(emailId: String) = withContext(Dispatchers.IO) {
        emailDao.deleteByEmail(emailId)
    }

    suspend fun updateIsActive(emailId: String, isActive: Boolean) = withContext(Dispatchers.IO) {
        emailDao.updateIsActive(emailId, isActive)
    }
}