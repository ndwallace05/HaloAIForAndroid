package xyz.haloai.haloai_android_productivity.di

import androidx.activity.result.ActivityResultRegistry
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import xyz.haloai.haloai_android_productivity.services.VoiceTranscriptionService
import xyz.haloai.haloai_android_productivity.services.repository.VoiceTranscriptionRepository
import xyz.haloai.haloai_android_productivity.ui.viewmodel.VoiceTranscriptionViewModel

val voiceTranscriptionModule = module {
    single { VoiceTranscriptionService() }
    single { VoiceTranscriptionRepository(get()) }
    viewModel { (activityResultRegistry: ActivityResultRegistry) -> VoiceTranscriptionViewModel(get(), activityResultRegistry) }
}