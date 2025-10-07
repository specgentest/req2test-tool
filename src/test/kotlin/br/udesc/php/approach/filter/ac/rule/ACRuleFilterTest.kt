package br.udesc.php.approach.filter.ac.rule

import br.udesc.php.approach.filter.BaseTest
import req2test.tool.approach.entity.Rule
import req2test.tool.approach.filter.ac.rule.ACRuleFilter
import req2test.tool.approach.filter.ac.rule.type.RuleVariable
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ACRuleFilterTest: BaseTest() {

    @Test
    fun shouldExecuteACRuleFilter() {
        val rawACRule = """
            R1 | endpoint | calculate perimeter of a Triangle
            R1.data | A | Double | max 10.0; min 8.0
            R1.data | B | %Triangle
            R1.data | C | %Triangle.C; min 5
            R1.data | perimeter | Int | generated: ${'$'}perimeter = ${'$'}A + ${'$'}B + ${'$'}C; greater than 10
            R1.input | A, B, C
            R1.output | perimeter
            R1.validation | create %Triangle
            R1.validation | ${'$'}A should be greater than 10
            R1.validation | ${'$'}B should be greater than 20
            R1.validation | ${'$'}C should be greater than 30
        """.trimIndent()
        data["RawACRule"] = rawACRule
        val filterRule = ACRuleFilter(data)

        val result = filterRule.execute()

        val rule = data["ACRule"] as Rule
        assertTrue(result, "Execution successful")
        assertEquals("endpoint", rule.ruleType, "Rule type: endpoint")
        assertEquals(3, (rule.attributes["input"] as List<RuleVariable>).size, "Rule type: endpoint")
    }
}