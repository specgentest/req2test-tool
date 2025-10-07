package req2test.tool.approach.filter.ac.rule.type

import req2test.tool.approach.core.Log
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.Rule

abstract class BaseEndpointCreateUpdateRule(rawACRule: String, entities: List<Entity>, totalFields : List<Int>, type: String):
    BaseEndpointCRUDRule(rawACRule, entities, totalFields, type){
    override fun addImplicitInput(){
        val entity = ruleAttributes["entity"] as Entity
        val inputs = entity.attributes.filter { !it.generatedDB and !it.generatedConstructor }
        ruleAttributes["input"] = inputs.map { RuleVariable(it.name, it) }
    }
}
class EndpointCreateRule(rawACRule: String, entities: List<Entity>): BaseEndpointCreateUpdateRule(rawACRule, entities,
    listOf(3,4), "endpoint-create"){

    override fun processLine1(line1: String) {
        super.processLine1(line1)
        //Persistence
        val values = splitRawLine(line1)
        if (values.size == 4) {
            val persistence = values[3].trim()
            if (persistence != "Persistence false")
                throw GeneralException(Log.createCriticalError("RuleType", "Invalid format: $persistence"))
            ruleAttributes["persistence"] = false
        }
    }
}
class EndpointUpdateRule(rawACRule: String, entities: List<Entity>): BaseEndpointCreateUpdateRule(rawACRule, entities, listOf(3), "endpoint-update")
{
    override fun addImplicitInput(){
        val entity = ruleAttributes["entity"] as Entity
        val inputs = entity.attributes.filter { (!it.generatedDB and !it.generatedConstructor) or it.name.startsWith("id") }
        ruleAttributes["input"] = inputs.map { RuleVariable(it.name, it) }
    }
}
class EndpointPartialUpdateRule(rawACRule: String, entities: List<Entity>): BaseEndpointCreateUpdateRule(rawACRule, entities, listOf(3), "endpoint-partial-update"){
    override fun addImplicitInput(){
        val entity = ruleAttributes["entity"] as Entity
        val inputs = entity.attributes.filter { (!it.generatedDB and !it.generatedConstructor) or it.name.startsWith("id") }
        ruleAttributes["input"] = inputs.map { RuleVariable(it.name, it) }
    }
}

class EndpointGetAllRule(rawACRule: String, entities: List<Entity>): BaseEndpointCRUDRule(rawACRule, entities, listOf(3), "endpoint-get-all"){
    override fun createRule(): Rule {
        val lines = rawACRule.split("\n")
        if(lines.size != 1)
            throw GeneralException(Log.createCriticalError("RuleType","Invalid format: number of lines should be 1"))

        processLine1(lines[0])
        addImplicitInput()

        val ruleType = ruleAttributes["type"] as String
        return Rule(rawACRule, ruleType, ruleAttributes, HashSet())
    }
}

//R1 | endpoint-filter | %Employee
//R1.fields | name, email
class EndpointFilterRule(rawACRule: String, entities: List<Entity>): BaseEndpointCRUDRule(rawACRule, entities,
    listOf(3), "endpoint-filter"){
    override fun getLine2FieldName(): String {
        return "fields"
    }
}
class EndpointGetRule(rawACRule: String, entities: List<Entity>): BaseEndpointCRUDRule(rawACRule, entities, listOf(3), "endpoint-get")
class EndpointGetListRule(rawACRule: String, entities: List<Entity>): BaseEndpointCRUDRule(rawACRule, entities, listOf(3), "endpoint-get-list")
class EndpointDeleteRule(rawACRule: String, entities: List<Entity>): BaseEndpointCRUDRule(rawACRule, entities, listOf(3), "endpoint-delete")
