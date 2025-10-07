package req2test.tool.approach.filter.script.controller

import req2test.tool.approach.core.TestArchitecture
import req2test.tool.approach.entity.Attribute
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.Rule

class ListDeleteEndpointMockScriptFilter (data: MutableMap<String, Any?>) : ListCreateEndpointMockScriptFilter(data) {
    override fun filterKeyInput(): String {
        return "ListACRules"
    }

    override fun filterName(): String {
        return "ListDeleteEndpointMockScriptFilter"
    }

    override fun filterKeyOutput(): String {
        return "ListDeleteEndpointMockScript"
    }

    override fun getRuleAction(): String {
        return "Delete"
    }

    override fun getEndpoint(testArchitecture: TestArchitecture): String? {
        return testArchitecture.endpoints["delete"]
    }

    override fun getEndpointWithValidations(testArchitecture: TestArchitecture): String? {
        return testArchitecture.endpointsWithValidations["delete"]
    }

    override fun getInputs(entity: Entity): List<Attribute> {
        return entity.attributes.filter { it.name.startsWith("id") }
    }

    override fun filterRules(listCreateACRule: List<Rule>): List<Rule> {
        return listCreateACRule.filter { it.ruleType == "endpoint-delete" }
    }
}
