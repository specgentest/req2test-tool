package req2test.tool.approach.dto

import kotlinx.serialization.Serializable

@Serializable
data class AIAnswer(val role: String, val action: String, val purpose: String)

@Serializable
data class WellFormedDTO(
    val actors: List<String>,
    val actions: List<String>,
    val purposes: List<String>,
    val totalActors: Int,
    val totalActions: Int,
    val totalPurposes: Int,
    val wellFormed: Boolean
)

@Serializable
data class CorrectTemplateDTO(
     val correctTemplate: Boolean
)

@Serializable
data class AtomicAndMinimaDTO(
    val atomic: Boolean,
    val minimal: Boolean,
    val atomicErrors: List<String>,
    val minimalErrors: List<String>
)


@Serializable
data class SemanticDTO(
    val conceptuallySound: Boolean,
    val problemOriented: Boolean,
    val unambiguous: Boolean,
    val estimatable: Boolean,
    val conceptuallySoundErrors: List<String>,
    val problemOrientedErrors: List<String>,
    val unambiguousErrors: List<String>,
    val estimatableErrors: List<String>
)


@Serializable
data class ConflictFreeDTO(
    val conflictFree: Boolean,
    var conflictUS: List<String>
)

@Serializable
data class ConflictEvaluationDTO(
    val evaluation: String
)