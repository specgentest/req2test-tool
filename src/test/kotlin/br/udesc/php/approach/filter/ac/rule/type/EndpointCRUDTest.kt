package br.udesc.php.approach.filter.ac.rule.type

import req2test.tool.approach.entity.Entity
import br.udesc.php.approach.filter.BaseTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import req2test.tool.approach.filter.ac.rule.type.*

class EndpointCreateRuleTest: BaseTest() {

    @Test
    fun shouldCreateRuleEndpointCreate1LinePersistenceFalse(){
        val rawRule = """
            R1 | endpoint-create | %Triangle | Persistence false
        """.trimIndent()

        val rule = EndpointCreateRule(rawRule, entities).createRule()

        assertEquals("endpoint-create", rule.ruleType, "Type: endpoint-create")
        assertEquals("Triangle", (rule.attributes["entity"] as Entity).name, "Entity: Triangle")
        assertEquals(false, rule.attributes["persistence"], "Persistence: false")
    }

    @Test
    fun shouldCreateRuleEndpointCreate2Lines(){

        val rawRule = """
            R1 | endpoint-create | %Triangle
            R1.input | A, B, C
        """.trimIndent()

        val rule = EndpointCreateRule(rawRule, entities).createRule()

        assertEquals("endpoint-create", rule.ruleType, "Type: endpoint-create")
        assertEquals("Triangle", (rule.attributes["entity"] as Entity).name, "Entity: Triangle")
        assertEquals(null, rule.attributes["persistence"], "Persistence: null")
    }

    @Test
    fun shouldCreateRuleEndpoint1Line(){

        val rawRule = """
            R1 | endpoint-create | %Triangle
        """.trimIndent()

        val rule = EndpointCreateRule(rawRule, entities).createRule()

        println("input: " + rule.attributes["input"])

        assertEquals("endpoint-create", rule.ruleType, "Type: endpoint-create")
        assertEquals("Triangle",(rule.attributes["entity"] as Entity).name, "Entity: Triangle")
        assertEquals(null, rule.attributes["persistence"], "Persistence: null")
    }

    @Test
    fun shouldCreateRuleEndpoint1LineWithImplicitInput(){

        val rawRule = """
            R1 | endpoint-create | %Customer
        """.trimIndent()

        val rule = EndpointCreateRule(rawRule, entities).createRule()

        println("input: " + rule.attributes["input"])

        assertEquals("endpoint-create", rule.ruleType, "Type: endpoint-create")
        assertEquals("Customer",(rule.attributes["entity"] as Entity).name, "Entity: Triangle")
        assertEquals(listOf("fullName", "CNH", "CNHExpirationDate", "birthDate", "dependentCustomer"),
            (rule.attributes["input"] as List<RuleVariable>).map {it.name}, "Input of Customer")
    }

    @Test
    fun shouldCreateRuleEndpointUpdate(){
        val rawRule = """
            R1 | endpoint-update | Triangle
        """.trimIndent()

        val rule = EndpointUpdateRule(rawRule, entities).createRule()

        assertEquals("endpoint-update", rule.ruleType, "Type: endpoint-update")
        assertEquals("Triangle", (rule.attributes["entity"] as Entity).name, "Entity: Triangle")
        assertEquals(3, (rule.attributes["input"] as List<RuleVariable>).size, "Input: 3 fields")

    }

    @Test
    fun shouldCreateRuleEndpointUpdate2Lines(){
        val rawRule = """
            R1 | endpoint-update | Triangle
            R1.input | A
        """.trimIndent()

        val rule = EndpointUpdateRule(rawRule, entities).createRule()

        assertEquals("endpoint-update", rule.ruleType, "Type: endpoint-update")
        assertEquals("Triangle", (rule.attributes["entity"] as Entity).name, "Entity: Triangle")
        assertEquals(1, (rule.attributes["input"] as List<RuleVariable>).size, "Input: 1 field")

    }

    @Test
    fun shouldFailCreateRuleEndpointUpdate(){
        val rawRule = """
            R1 | endpoint-update | Triangle2
        """.trimIndent()

        val exception = assertThrows<GeneralException> {
            EndpointUpdateRule(rawRule, entities).createRule()
        }

        assertTrue(exception.message!!.contains("Entity Triangle2 not found"), "Type: endpoint-update")

    }

    @Test
    fun shouldCreateRuleEndpointDelete(){
        val rawRule = """
            R1 | endpoint-delete | Triangle
        """.trimIndent()

        val rule = EndpointDeleteRule(rawRule, entities).createRule()

        assertEquals("endpoint-delete", rule.ruleType, "Type: endpoint-delete")
        assertEquals("Triangle", (rule.attributes["entity"] as Entity).name, "Entity: Triangle")
        assertEquals(3, (rule.attributes["input"] as List<RuleVariable>).size, "Input: 3 fields")
    }

    @Test
    fun shouldCreateRuleEndpointDelete2Lines(){
        val rawRule = """
            R1 | endpoint-delete | Triangle
            R1.input | A
        """.trimIndent()

        val rule = EndpointDeleteRule(rawRule, entities).createRule()

        assertEquals("endpoint-delete", rule.ruleType, "Type: endpoint-delete")
        assertEquals("Triangle", (rule.attributes["entity"] as Entity).name, "Entity: Triangle")
        assertEquals(1, (rule.attributes["input"] as List<RuleVariable>).size, "Input: 1 field")
    }

    @Test
    fun shouldCreateRuleEndpointGet(){
        val rawRule = """
            R1 | endpoint-get | Triangle
        """.trimIndent()

        val rule = EndpointGetRule(rawRule, entities).createRule()

        assertEquals("endpoint-get", rule.ruleType, "Type: endpoint-get")
        assertEquals("Triangle", (rule.attributes["entity"] as Entity).name, "Entity: Triangle")
        assertEquals(3, (rule.attributes["input"] as List<RuleVariable>).size, "Input: 3 fields")
    }

    @Test
    fun shouldCreateRuleEndpointGetAll(){
        val rawRule = """
            R1 | endpoint-get-all | Triangle
        """.trimIndent()

        val rule = EndpointGetAllRule(rawRule, entities).createRule()

        assertEquals("endpoint-get-all", rule.ruleType, "Type: endpoint-get-all")
        assertEquals("Triangle", (rule.attributes["entity"] as Entity).name, "Entity: Triangle")
        assertEquals(3, (rule.attributes["input"] as List<RuleVariable>).size, "Input: 3 fields")
    }

    @Test
    fun shouldCreateRuleEndpointGetList(){
        val rawRule = """
            R1 | endpoint-get-list | Triangle
        """.trimIndent()

        val rule = EndpointGetListRule(rawRule, entities).createRule()

        assertEquals("endpoint-get-list", rule.ruleType, "Type: endpoint-get-list")
        assertEquals("Triangle", (rule.attributes["entity"] as Entity).name, "Entity: Triangle")
        assertEquals(3, (rule.attributes["input"] as List<RuleVariable>).size, "Input: 3 fields")
    }

    @Test
    fun shouldCreateRuleEndpointFilter(){
        val rawRule = """
            R1 | endpoint-filter | Triangle
        """.trimIndent()

        val rule = EndpointFilterRule(rawRule, entities).createRule()

        assertEquals("endpoint-filter", rule.ruleType, "Type: endpoint-filter")
        assertEquals("Triangle", (rule.attributes["entity"] as Entity).name, "Entity: Triangle")
        println(rule.attributes["fields"])
        assertEquals(3, (rule.attributes["fields"] as List<RuleVariable>).size, "Fields: 3 fields")
    }

    @Test
    fun shouldCreateRuleEndpointFilter2Lines(){
        val rawRule = """
            R1 | endpoint-filter | Triangle
            R1.fields | A, B, C
        """.trimIndent()

        val rule = EndpointFilterRule(rawRule, entities).createRule()

        assertEquals("endpoint-filter", rule.ruleType, "Type: endpoint-filter")
        assertEquals("Triangle", (rule.attributes["entity"] as Entity).name, "Entity: Triangle")
        println(rule.attributes["fields"])
        assertEquals(3, (rule.attributes["fields"] as List<RuleVariable>).size, "Fields: 3 fields")
    }

}