package br.udesc.php.approach.filter.ac.rule.type

import br.udesc.php.approach.filter.BaseTest
import req2test.tool.approach.filter.ac.rule.type.GenericEndpointRule
import req2test.tool.approach.filter.ac.rule.type.RuleVariable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GenericEndpointRuleTest: BaseTest() {
    @Test
    fun shouldCreateEndpoint(){
        val rawRule = """
            R1 | endpoint | calculate perimeter of a Triangle
            R1.data | A | %Triangle.A
            R1.data | B | %Triangle.B
            R1.data | C | %Triangle.C
            R1.data | perimeter | Int | ${'$'}perimeter = ${'$'}A + ${'$'}B + ${'$'}C
            R1.input | A, B, C
            R1.output | perimeter
            R1.validation | create %Triangle
        """.trimIndent()
        val rule = GenericEndpointRule(rawRule, entities).createRule()

        assertEquals(4, (rule.attributes["variables"] as List<RuleVariable>).size, "Rule should have 4 data defined")
        assertEquals(3, (rule.attributes["input"] as List<RuleVariable>).size, "Rule should have 3 inputs defined")
        assertEquals(1, (rule.attributes["output"] as List<RuleVariable>).size, "Rule should have 1 output defined")
    }
}