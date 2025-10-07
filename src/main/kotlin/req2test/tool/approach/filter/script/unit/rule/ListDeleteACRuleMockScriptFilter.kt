package req2test.tool.approach.filter.script.unit.rule

import req2test.tool.approach.entity.Attribute
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.Rule

class ListDeleteACRuleMockScriptFilter (data: MutableMap<String, Any?>) : ListCreateACRuleMockScriptFilter(data) {
    override fun filterKeyInput(): String {
        return "ListACRules"
    }

    override fun filterName(): String {
        return "ListDeleteACRuleMockScriptFilter"
    }

    override fun filterKeyOutput(): String {
        return "ListDeleteACRuleMockScript"
    }

    override fun getRuleAction(): String {
        return "Delete"
    }

    override fun getInputs(entity: Entity): List<Attribute> {
        return entity.attributes.filter {  it.name.startsWith("id")}
    }

    override fun filterRules(listCreateACRule: List<Rule>): List<Rule> {
        return listCreateACRule.filter { it.ruleType == "endpoint-delete" }
    }
}
