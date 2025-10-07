package req2test.tool.approach.entity

import req2test.tool.approach.core.TestArchitecture
import req2test.tool.approach.processor.adapter.ChatGPTMessage
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

enum class Level(val value: String) {
    UNIT("Unit"),
    INTEGRATION("Integration"),
    SYSTEM("System")
}

enum class Technique(val value: String) {
    BLACKBOX("Black Box"),
    WHITEBOX("White Box"),
    GRAYBOX("Gray Box")
}

enum class InputTechnique(val value: String) {
    EQUIVALENCE_PARTITIONING("Equivalence Partitioning"),
    BOUNDARY_VALUE_EVALUATION("Boundary Value Evaluation"),
    BASED_ON_USE_CASE("Based on use case"),
    USE_CASE_PATH_COVERAGE("Use Case Path Coverage"),
    USE_CASE_TESTING("Use Case Testing"),
    DECISION_TABLE("Decision Table"),
    CAUSE_EFFECT_GRAPH ("Cause Effect Graph"),
    CONDITION_AND_DECISION_COVERAGE ("Condition/Decision Coverage"),
    CALL_COVERAGE ("Call Coverage"),
    CONTROL_FLOW_TESTING ("Control Flow Testing")
}
class Script(val level: Level, val objective: String, val location: String, val preconditions: List<String>,
             val steps: List<String>, val technique: Technique, val inputTechniques: List<InputTechnique>, val inputs: List<Attribute>, val reference: Any) {
    val testCases = ArrayList<TestCase>()
    val testCasesAttributeGenerated = ArrayList<TestCase>()
    var allDataTest: String? = null
    val automatedTestCases = ArrayList<String>()
    var testArchitecture: TestArchitecture? = null
    var inputAllAttributes: List<Attribute>? = null
    override fun toString(): String {
        return "Script(level=$level, objective='$objective', location='$location', preconditions=$preconditions, steps=$steps, technique=$technique, inputTechniques=$inputTechniques, inputs=$inputs, reference=$reference, allDataTest=$allDataTest)"
    }


}

data class TestCase(val dataTest: DataTest, val script: Script, val expectedResult: String, val expectedResultReference: Any? = null){
    var preconditions = ArrayList<String>()
    var steps = ArrayList<String>()
    var technique: Technique = script.technique
    var testName: String? = null

    init {
        if(script.level == Level.UNIT)
            totalUnit++
        if(script.level == Level.INTEGRATION)
            totalIntegration++
    }
    companion object {
        var totalUnit:Int = 0
        var totalIntegration:Int = 0

        fun countUnitTestsFromString(classContent: String, level: Level) {
            val count = classContent.lines()
                .map { it.trimStart() }
                .count { it.startsWith("@Test") }
            println("Total unitário era: $totalUnit")
            println("Total integracao era: $totalIntegration")
            if(level == Level.UNIT)
                totalUnit +=count
            if(level == Level.INTEGRATION)
                totalIntegration +=count

            println("Total unitário é: $totalUnit")
            println("Total integracao é: $totalIntegration")
        }
    }
}


data class DataTest(val input: Map<String, Any?>?, val isValidInput: Boolean, val errorMessage: String?, var className: String){
    fun convertToJson(): String {
        val mapper = jacksonObjectMapper()
        return mapper.writeValueAsString(this)
    }
}


data class ListDataTest(val dataTest: List<DataTest>, @JsonIgnore var reference: Any?=null){
    var llmMessageHistory: MutableList<ChatGPTMessage> = mutableListOf<ChatGPTMessage>()
    var varDictionaryAttributes = HashMap<String, String>()
    var varDictionaryEntities = HashMap<String, String>()
    var entitiesAllVariables = HashMap<String, Map<String, String>>()

    fun convertToJson(): String {
        val mapper = jacksonObjectMapper()
        return mapper.writeValueAsString(this)
    }
}

data class TestPlan(val scripts: List<Script>){
}