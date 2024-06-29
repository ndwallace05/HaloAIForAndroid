package xyz.haloai.haloai_android_productivity.data.ui.viewmodel

import android.accounts.AccountManager
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import xyz.haloai.haloai_android_productivity.data.local.entities.EmailAccount
import xyz.haloai.haloai_android_productivity.data.local.entities.enumEmailType
import xyz.haloai.haloai_android_productivity.data.local.repository.EmailDbRepository

class EmailDbViewModel(private val repository: EmailDbRepository) : ViewModel() {

    private val _isAuthCompleted = MutableLiveData<Boolean>()
    val isAuthCompleted: LiveData<Boolean> get() = _isAuthCompleted
    suspend fun allEmailIds(): List<String> {
        return repository.getAllEmailIds()
    }


    fun insert(emailId: String, calendarIds: List<String>, type: enumEmailType) = viewModelScope.launch {
        val currentTime = System.currentTimeMillis()
        val emailAccToAdd = EmailAccount(
            email = emailId,
            emailType = type,
            isActive = true,
            calendarIds = calendarIds,
            lastCalendarUpdateDateTime = currentTime
        )
        repository.insert(emailAccToAdd)
    }

    fun onConsentGranted() {
        viewModelScope.launch {
            // repository.fetchCalendarEvents()
            _isAuthCompleted.postValue(true)
        }
    }

    suspend fun getGoogleAccountsAdded(): List<String> {
        return repository.getAccountsByType(enumEmailType.GMAIL).map { it.email }
    }

    fun addMicrosoftAccount(email: String, token: String, isActive: Boolean) = viewModelScope.launch {
        // repository.addMicrosoftAccount(email, token, isActive)
    }

    fun getAllGoogleAccountsOnDevice(context: Context): List<String> {
        val accountManager = AccountManager.get(context)
        val accounts = accountManager.getAccountsByType("com.google")
        val emailList = mutableListOf<String>()
        for (account in accounts) {
            emailList.add(account.name)
        }
        return emailList
    }

    fun addGoogleAccountFromOnDeviceGoogleAccounts(context: Context, emailId: String) {


    }

    fun updateCalendarIds(emailId: String, calendarIds: List<String>) = viewModelScope.launch {
        repository.updateCalendarIds(emailId, calendarIds)
    }

    fun deleteById(emailId: String) = viewModelScope.launch {
        repository.deleteById(emailId)
    }

    fun updateIsActive(emailId: String, isActive: Boolean) = viewModelScope.launch {
        repository.updateIsActive(emailId, isActive)
    }
}

class ContentViewModelFactory(private val repository: EmailDbRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmailDbViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmailDbViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}