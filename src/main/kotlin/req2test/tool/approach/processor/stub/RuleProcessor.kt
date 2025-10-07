package req2test.tool.approach.processor.stub

import req2test.tool.approach.dto.ACRuleSemanticDTO
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

interface ValidateWellFormedRule {
    fun isRuleSemantic(rawRule: String): ACRuleSemanticDTO
}
interface RuleProcessor: ValidateWellFormedRule

class RuleProcessorStub: RuleProcessor {
    override fun isRuleSemantic(rawRule: String): ACRuleSemanticDTO {
        var json = """
            {
              "conceptuallySound": true,
              "wellFormedFormula": true,
              "conceptuallySoundErrors": [],
              "wellFormedFormulaErrors": []
            }""".trimIndent()

        if(System.getenv("INPUT_WITH_ERRORS") == "TRUE") {
            json = """{
                "conceptuallySound": false,
                "wellFormedFormula": false,
                "conceptuallySoundErrors": ["The rule does not express a clear rule."],
                "wellFormedFormulaErrors": ["The formula does not contain any condition or calculation operators."]
            }""".trimIndent()
        }

        return Json.decodeFromString(json) as ACRuleSemanticDTO
    }

    fun isFormulaWellFormed(formula: String): String {
        return ""
    }
}