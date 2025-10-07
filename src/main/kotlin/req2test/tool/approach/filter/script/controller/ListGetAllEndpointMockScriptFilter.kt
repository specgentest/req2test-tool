package req2test.tool.approach.filter.script.controller

import req2test.tool.approach.core.TestArchitecture
import req2test.tool.approach.entity.Attribute
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.Rule

class ListGetAllEndpointMockScriptFilter (data: MutableMap<String, Any?>) : ListCreateEndpointMockScriptFilter(data) {
    override fun filterKeyInput(): String {
        return "ListACRules"
    }

    override fun filterName(): String {
        return "ListGetAllEndpointMockScriptFilter"
    }

    override fun filterKeyOutput(): String {
        return "ListGetAllEndpointMockScript"
    }

    override fun getRuleAction(): String {
        return "GetAll"
    }

    override fun getEndpoint(testArchitecture: TestArchitecture): String? {
        return testArchitecture.endpoints["retrieve all"]
    }

    override fun getEndpointWithValidations(testArchitecture: TestArchitecture): String? {
        return testArchitecture.endpointsWithValidations["retrieve all"]
    }

    override fun getInputs(entity: Entity): List<Attribute> {
        return ArrayList<Attribute>()
    }

    override fun filterRules(listCreateACRule: List<Rule>): List<Rule> {
        return listCreateACRule.filter { it.ruleType == "endpoint-get-all" }
    }
}
