package com.retrocollector.app.core.data.datasource

import com.retrocollector.app.settings.domain.model.AiProvider
import com.retrocollector.app.settings.domain.model.AppSettings

class AiDataSourceFactory(
    val geminiDataSource: GeminiRemoteDataSource = GeminiRemoteDataSource(),
    val claudeDataSource: AnthropicRemoteDataSource = AnthropicRemoteDataSource(),
    val openAiDataSource: OpenAiCompatibleRemoteDataSource = OpenAiCompatibleRemoteDataSource(AiProvider.OPENAI_COMPATIBLE),
    val localOllamaDataSource: OpenAiCompatibleRemoteDataSource = OpenAiCompatibleRemoteDataSource(AiProvider.LOCAL_OLLAMA)
) {
    fun getDataSource(provider: AiProvider): IAiRemoteDataSource {
        return when (provider) {
            AiProvider.GEMINI -> geminiDataSource
            AiProvider.CLAUDE -> claudeDataSource
            AiProvider.OPENAI_COMPATIBLE -> openAiDataSource
            AiProvider.LOCAL_OLLAMA -> localOllamaDataSource
        }
    }

    data class ResolvedAiConfig(
        val provider: AiProvider,
        val apiKey: String,
        val model: String,
        val baseUrl: String?
    )

    fun resolveConfig(settings: AppSettings): ResolvedAiConfig {
        return when (settings.aiProvider) {
            AiProvider.GEMINI -> ResolvedAiConfig(
                provider = AiProvider.GEMINI,
                apiKey = settings.geminiApiKey,
                model = settings.geminiModel.ifBlank { "gemini-3.7-flash" },
                baseUrl = null
            )
            AiProvider.CLAUDE -> ResolvedAiConfig(
                provider = AiProvider.CLAUDE,
                apiKey = settings.claudeApiKey,
                model = settings.claudeModel.ifBlank { "claude-3-7-sonnet-20250219" },
                baseUrl = null
            )
            AiProvider.OPENAI_COMPATIBLE -> ResolvedAiConfig(
                provider = AiProvider.OPENAI_COMPATIBLE,
                apiKey = settings.openAiApiKey,
                model = settings.openAiModel.ifBlank { "gpt-4o" },
                baseUrl = settings.openAiBaseUrl.ifBlank { "https://api.openai.com/v1" }
            )
            AiProvider.LOCAL_OLLAMA -> ResolvedAiConfig(
                provider = AiProvider.LOCAL_OLLAMA,
                apiKey = "",
                model = settings.localAiModel.ifBlank { "llama3.2-vision" },
                baseUrl = settings.localAiBaseUrl.ifBlank { "http://localhost:11434/v1" }
            )
        }
    }
}
