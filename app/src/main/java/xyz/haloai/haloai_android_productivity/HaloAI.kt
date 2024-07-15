package xyz.haloai.haloai_android_productivity

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.work.Configuration
import com.azure.identity.ClientSecretCredentialBuilder
import com.azure.security.keyvault.secrets.SecretClientBuilder
import com.azure.security.keyvault.secrets.models.KeyVaultSecret
import io.github.cdimascio.dotenv.dotenv
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import xyz.haloai.haloai_android_productivity.di.assistantModeFunctionsModule
import xyz.haloai.haloai_android_productivity.di.emailDbModule
import xyz.haloai.haloai_android_productivity.di.gmailModule
import xyz.haloai.haloai_android_productivity.di.ltGoalsDbModule
import xyz.haloai.haloai_android_productivity.di.microsoftGraphModule
import xyz.haloai.haloai_android_productivity.di.miscInfoDbModule
import xyz.haloai.haloai_android_productivity.di.notesDbModule
import xyz.haloai.haloai_android_productivity.di.openAIModule
import xyz.haloai.haloai_android_productivity.di.productivityFeedModule
import xyz.haloai.haloai_android_productivity.di.productivityFeedOptionsModule
import xyz.haloai.haloai_android_productivity.di.scheduleDbModule
import xyz.haloai.haloai_android_productivity.di.textExtractionFromImageModule
import xyz.haloai.haloai_android_productivity.di.workManagerModule
import xyz.haloai.haloai_android_productivity.workers.scheduleCalendarUpdateWork
import xyz.haloai.haloai_android_productivity.workers.scheduleEmailCheckWork
import xyz.haloai.haloai_android_productivity.workers.scheduleSuggestedTasksWork
import xyz.haloai.haloai_android_productivity.workers.workerFactory.CalendarUpdateWorkerFactory
import xyz.haloai.haloai_android_productivity.workers.workerFactory.CombinedWorkerFactory
import xyz.haloai.haloai_android_productivity.workers.workerFactory.EmailCheckWorkerFactory
import xyz.haloai.haloai_android_productivity.workers.workerFactory.SuggestedTasksWorkerFactory

class HaloAI: Application(), Configuration.Provider, KoinComponent {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@HaloAI)
            modules(emailDbModule, scheduleDbModule, gmailModule, openAIModule,
                assistantModeFunctionsModule, microsoftGraphModule, notesDbModule,
                ltGoalsDbModule, productivityFeedModule, productivityFeedOptionsModule,
                miscInfoDbModule, workManagerModule, textExtractionFromImageModule)
        }

        // Schedule the workers
        applicationContext.scheduleEmailCheckWork()
        applicationContext.scheduleCalendarUpdateWork()
        applicationContext.scheduleSuggestedTasksWork()

        // Get Azure and openAI keys from Azure Key Vault

        val dotenv = dotenv {
            directory = "/assets"
            filename = "env" // instead of 'env', use 'env'
        }

        val clientId = dotenv["AZURE_CLIENT_ID"] ?: throw IllegalArgumentException("Missing AZURE_CLIENT_ID")
        val clientSecret = dotenv["AZURE_CLIENT_SECRET"] ?: throw IllegalArgumentException("Missing AZURE_CLIENT_SECRET")
        val tenantId = dotenv["AZURE_TENANT_ID"] ?: throw IllegalArgumentException("Missing AZURE_TENANT_ID")
        val vaultUrl = "https://keysforapp.vault.azure.net/"

        val clientSecretCredential = ClientSecretCredentialBuilder()
            .clientId(clientId)
            .clientSecret(clientSecret)
            .tenantId(tenantId)
            .build()

        val secretClient = SecretClientBuilder()
            .vaultUrl(vaultUrl)
            .credential(clientSecretCredential)
            .buildClient()

        val secretName = "OpenAI"
        val secret: KeyVaultSecret = secretClient.getSecret(secretName)
        openAI_API_KEY = secret.value

        // Check if permission to access screenshots is granted (READ_MEDIA_IMAGES)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Check if permission to access screenshots is granted (READ_MEDIA_IMAGES)
            val permission = android.Manifest.permission.READ_MEDIA_IMAGES
            val permissionStatus = applicationContext.checkSelfPermission(permission)
            if (permissionStatus == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Request permission
                startScreenshotObserverService()
            }
        }

    }

    companion object {
        var openAI_API_KEY: String = ""
    }

    private fun startScreenshotObserverService() {
        val intent = Intent(this, ScreenshotObserverService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    // This is where we provide the worker factory to WorkManager
    override val workManagerConfiguration: Configuration
        get() {
            val emailCheckWorkerFactory: EmailCheckWorkerFactory by inject()
            val calendarUpdateWorkerFactory: CalendarUpdateWorkerFactory by inject()
            val suggestedTasksWorkerFactory: SuggestedTasksWorkerFactory by inject()
            return Configuration.Builder()
                .setWorkerFactory(CombinedWorkerFactory(
                    emailCheckWorkerFactory,
                    calendarUpdateWorkerFactory,
                    suggestedTasksWorkerFactory
                ))
                .build()
        }
}