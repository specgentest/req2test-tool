package req2test.tool.approach.filter.script.integration.controller

import req2test.tool.approach.core.TestArchitecture
import req2test.tool.approach.entity.Attribute
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.Rule
import req2test.tool.approach.filter.ac.rule.type.RuleVariable

class ListFilterEndpointScriptFilter (data: MutableMap<String, Any?>) : ListCreateEndpointScriptFilter(data) {
    override fun filterKeyInput(): String {
        return "ListACRules"
    }

    override fun filterName(): String {
        return "ListFilterEndpointScriptFilter"
    }

    override fun filterKeyOutput(): String {
        return "ListFilterEndpointScript"
    }

    override fun getRuleAction(): String {
        return "Filter"
    }

    override fun getEndpoint(testArchitecture: TestArchitecture): String? {
        return testArchitecture.endpoints["filter"]
    }

    override fun getEndpointWithValidations(testArchitecture: TestArchitecture): String? {
        return testArchitecture.endpointsWithValidations["filter"]
    }

    override fun getInputs(entity: Entity): List<Attribute> {
        val fields = currentACRule.attributes["fields"] as List<RuleVariable>
        val attrs = fields.map { it.attributeReference!! }
        return attrs
    }

    override fun filterRules(listCreateACRule: List<Rule>): List<Rule> {
        return listCreateACRule.filter { it.ruleType == "endpoint-filter" }
    }
}
