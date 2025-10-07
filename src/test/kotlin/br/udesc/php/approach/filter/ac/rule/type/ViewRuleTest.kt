package br.udesc.php.approach.filter.ac.rule.type

import br.udesc.php.approach.filter.BaseTest
import req2test.tool.approach.filter.ac.rule.type.RuleAction
import req2test.tool.approach.filter.ac.rule.type.ViewRule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

//R# | view | A | number
//R#.action | click | R1, R2
//R.validation | validation
class ViewRuleTest: BaseTest() {

    @Test
    fun shouldCreateViewRule1Line(){
        val rawRule = """
            R1 | view | A | number
        """.trimIndent()

        val rule = ViewRule(rawRule, entities).createRule()

        Assertions.assertEquals("view", rule.ruleType, "Type: view")
        Assertions.assertEquals("A", rule.attributes["variable"], "Variable: A")
        Assertions.assertEquals("number", rule.attributes["field"], "Field type: number")
    }

    //R# | view | btnEnter | button
//R#.action | click | R1, R2
    @Test
    fun shouldCreateViewRuleWithAction(){
        val rawRule = """
            R1 | view | btnSave | button
            R1.action | click | R1, R2
        """.trimIndent()

        val rule = ViewRule(rawRule, entities).createRule()

        Assertions.assertEquals("view", rule.ruleType, "Type: view")
        Assertions.assertEquals("btnSave", rule.attributes["variable"], "Variable: btnSave")
        Assertions.assertEquals("button", rule.attributes["field"], "Field Type: button")
        Assertions.assertEquals(1, (rule.attributes["actions"] as List<RuleAction>).size, "Actions: 1")
        Assertions.assertEquals("click", (rule.attributes["actions"] as List<RuleAction>)[0].eventName, "Actions: 1")
    }

    @Test
    fun shouldCreateViewRuleValidations(){
        val rawRule = """
            R1 | view | A | number
            R1.validation | greater than 0
            R1.validation | max value is 100
        """.trimIndent()

        val rule = ViewRule(rawRule, entities).createRule()

        Assertions.assertEquals("view", rule.ruleType, "Type: view")
        Assertions.assertEquals("A", rule.attributes["variable"], "Variable: A")
        Assertions.assertEquals("number", rule.attributes["field"], "Field type: number")
        Assertions.assertEquals(2, (rule.attributes["validations"] as List<String>).size, "Validations: 2")

    }
}