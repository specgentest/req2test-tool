package br.udesc.php.approach.filter.ac.rule

import br.udesc.php.approach.filter.BaseTest
import req2test.tool.approach.core.FilterError
import req2test.tool.approach.entity.Rule
import req2test.tool.approach.filter.ac.rule.ListACRulesFilter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ListACRulesTest: BaseTest() {

    @Test
    fun shouldSeparateRawRules(){
        val rawACRule1 = """
            R1 | endpoint | calculate perimeter of a Triangle
            
            
            R1.data | A | %Triangle.A
            R1.data | B | %Triangle.B
            R1.data | C | %Triangle.C
            R1.data | perimeter | Int | ${'$'}perimeter = ${'$'}A + ${'$'}B + ${'$'}C
            R1.input | A, B, C
            R1.output | perimeter
            R1.validation | create %Triangle
        """.trimIndent()

        val rawACRule2 = """
            R2 | endpoint-create | Triangle
        """.trimIndent()

        val rawACRule3 = """
            R3 | endpoint-update | Triangle
        """.trimIndent()

        val rawACRule4 = """
            R4 | view | A | number
            R4.validation | greater than 0
            R4.validation | max value is 100
        """.trimIndent()

        val rawRules = rawACRule1 + "\n" + rawACRule2 + "\n" + rawACRule3 + "\n" + rawACRule4

        val filterListRule = ListACRulesFilter(data)
        val rules = filterListRule.separateRawRules(rawRules)
        rules.forEach{println(it+"\n")}
        assertEquals(4, rules.size, "Should have 4 rules")
    }

    @Test
    fun shouldExecuteListACRulesFilter2(){
        val rawRules = """
            R1 | endpoint-get | Customer

            R2 | endpoint-create | Reservation

       		R3 | endpoint | verify CNH expiration     
            R3.data | CNHExpirationDate | String | not null
            R3.data | reservationDate | String | not null
            R3.data | isCNHValid | Boolean | ${'$'}isCNHValid = ${'$'}reservationDate as Date < ${'$'}CNHExpirationDate as Date
            R3.input | CNHExpirationDate, reservationDate
            R3.output | isCNHValid
            R3.validation | ${'$'}CNHExpirationDate is a valid date
            R3.validation | ${'$'}reservationDate is a valid date
        """.trimIndent()

        data["ListRawACRules"] = rawRules
        val filterListRule = ListACRulesFilter(data)
        filterListRule.execute()
        val rules = data["ListACRules"] as List<Rule>
        rules.forEach{println(it.ruleType + "\n")}
        assertEquals(3, rules.size, "Should have 6 rules")
    }

    @Test
    fun shouldExecuteListACRulesFilter(){
        val rawRules = """
            R1 | endpoint | calculate perimeter of a Triangle     
            R1.data | A | %Triangle.A
            R1.data | B | %Triangle.B
            R1.data | C | %Triangle.C
            R1.data | perimeter | Int | ${'$'}perimeter = ${'$'}A + ${'$'}B + ${'$'}C
            R1.input | A, B, C
            R1.output | perimeter
            R1.validation | create %Triangle
       
            R2 | endpoint-create | Triangle
       
            R3 | endpoint-update | Triangle
       
            R4 | view | A | number
            R4.validation | greater than 0
            R4.validation | max value is 100
       
            R5 | view | B | number
            R5.validation | greater than 0
            R5.validation | max value is 100
        
            R6 | view | btnSave | button
            R6.action | click | R4, R5
        """.trimIndent()

        data["ListRawACRules"] = rawRules
        val filterListRule = ListACRulesFilter(data)
        filterListRule.execute()
        val rules = data["ListACRules"] as List<Rule>
        rules.forEach{println(it.ruleType + "\n")}
        assertEquals(6, rules.size, "Should have 6 rules")
        assertEquals(2, rules[5].dependencies.size, "Should have 2 dependencies")
    }

    @Test
    fun shouldFailExecuteListACRulesWithWrongDependency(){
        val rawRules = """
            R1 | view | btnSave | button
            R1.action | click | R4, R5
        """.trimIndent()

        data["ListRawACRules"] = rawRules
        val filterListRule = ListACRulesFilter(data)
        val exception = assertThrows<FilterError>() {
            filterListRule.execute()
        }
        assertTrue(exception.message!!.contains("Dependency Rule R4 not found"), "Dependency Rule R4 not found")
        assertTrue(exception.message!!.contains("Dependency Rule R5 not found"), "Dependency Rule R5 not found")

    }

    @Test
    fun shouldFailExecuteListACRulesIfRuleAlreadyExists(){
        val rawRules = """
            R1 | view | btnSave1 | button
            
            R2 | view | btnSave2 | button
            
            R1 | view | btnSave3 | button
        """.trimIndent()

        data["ListRawACRules"] = rawRules
        val filterListRule = ListACRulesFilter(data)
        val exception = assertThrows<FilterError>() {
            filterListRule.execute()
        }

        assertTrue(exception.message!!.contains("already exists"), "already exists")
    }

    @Test
    fun shouldFailExecuteListACRulesIfDuplicatedVariables(){
        val rawRules = """
            R1 | view | btnSave | button
            
            R2 | view | btnSave | button
        """.trimIndent()

        data["ListRawACRules"] = rawRules
        val filterListRule = ListACRulesFilter(data)
        val exception = assertThrows<FilterError>() {
            filterListRule.execute()
        }
        assertTrue(exception.message!!.contains("Duplicated view variable"), "Duplicated view variable")
    }

}