package xyz.haloai.haloai_android_productivity

import android.app.Application
import com.azure.identity.ClientSecretCredentialBuilder
import com.azure.security.keyvault.secrets.SecretClientBuilder
import com.azure.security.keyvault.secrets.models.KeyVaultSecret
import io.github.cdimascio.dotenv.dotenv
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import xyz.haloai.haloai_android_productivity.di.assistantModeFunctionsModule
import xyz.haloai.haloai_android_productivity.di.emailDbModule
import xyz.haloai.haloai_android_productivity.di.gmailModule
import xyz.haloai.haloai_android_productivity.di.microsoftGraphModule
import xyz.haloai.haloai_android_productivity.di.openAIModule
import xyz.haloai.haloai_android_productivity.di.scheduleDbModule

class HaloAI: Application()  {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@HaloAI)
            modules(emailDbModule, scheduleDbModule, gmailModule, openAIModule,
                assistantModeFunctionsModule, microsoftGraphModule)
        }

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
    }

    companion object {
        var openAI_API_KEY: String = ""
    }
}