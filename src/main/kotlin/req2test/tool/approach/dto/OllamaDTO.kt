package req2test.tool.approach.dto

import kotlinx.serialization.Serializable

@Serializable
data class OllamaMessageDTO(
    val message: OllamaMessageContentDTO,
)

@Serializable
data class OllamaMessageContentDTO(
    val content: String
)