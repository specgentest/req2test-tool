package req2test.tool.approach.filter.script.controller

import req2test.tool.approach.core.TestArchitecture
import req2test.tool.approach.entity.Attribute
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.Rule
import req2test.tool.approach.filter.ac.rule.type.RuleVariable

class ListGetEndpointMockScriptFilter (data: MutableMap<String, Any?>) : ListCreateEndpointMockScriptFilter(data) {

    override fun filterKeyInput(): String {
        return "ListACRules"
    }

    override fun filterName(): String {
        return "ListGetEndpointMockScriptFilter"
    }

    override fun filterKeyOutput(): String {
        return "ListGetEndpointMockScript"
    }

    override fun getRuleAction(): String {
        return "Get"
    }

    override fun getEndpoint(testArchitecture: TestArchitecture): String? {
        return testArchitecture.endpoints["retrieve"]
    }

    override fun getEndpointWithValidations(testArchitecture: TestArchitecture): String? {
        return testArchitecture.endpointsWithValidations["retrieve"]
    }

    override fun getInputs(entity: Entity): List<Attribute> {
        if(currentACRule.attributes["urlinput"] == null) {
            return entity.attributes.filter { it.name.startsWith("id") }
//            currentACRule.attributes["urlinput"] = entity.attributes.filter { it.name.startsWith("id") }
//            return currentACRule.attributes["urlinput"] as List<Attribute>
        }
        val inputs = currentACRule.attributes["urlinput"] as List<RuleVariable>
        return inputs.map { it.attributeReference!! }
//        return entity.attributes.filter { it.name.startsWith("id") }
    }

    override fun filterRules(listCreateACRule: List<Rule>): List<Rule> {
        return listCreateACRule.filter { it.ruleType == "endpoint-get" }
    }
}
