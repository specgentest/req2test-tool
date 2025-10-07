package req2test.tool.approach.filter.script.unit.rule

import req2test.tool.approach.entity.Attribute
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.Rule

class ListGetAllACRuleMockScriptFilter (data: MutableMap<String, Any?>) : ListCreateACRuleMockScriptFilter(data) {
    override fun filterKeyInput(): String {
        return "ListACRules"
    }

    override fun filterName(): String {
        return "ListGetAllACRuleMockScriptFilter"
    }

    override fun filterKeyOutput(): String {
        return "ListGetAllACRuleMockScript"
    }

    override fun getRuleAction(): String {
        return "GetAll"
    }

    override fun getInputs(entity: Entity): List<Attribute> {
        return ArrayList<Attribute>()
    }

    override fun filterRules(listCreateACRule: List<Rule>): List<Rule> {
        return listCreateACRule.filter { it.ruleType == "endpoint-get-all" }
    }
}
