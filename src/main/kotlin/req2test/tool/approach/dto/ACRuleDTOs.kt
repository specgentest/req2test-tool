package req2test.tool.approach.dto

import kotlinx.serialization.Serializable

@Serializable
data class ACRuleSemanticDTO(
    val conceptuallySound: Boolean,
    val wellFormedFormula: Boolean,
    val conceptuallySoundErrors: List<String>,
    val wellFormedFormulaErrors: List<String>
)
