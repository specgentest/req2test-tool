package req2test.tool.approach.filter.script.unit.rule

import req2test.tool.approach.entity.Attribute
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.Rule
import req2test.tool.approach.filter.ac.rule.type.RuleVariable

class ListFilterACRuleMockScriptFilter (data: MutableMap<String, Any?>) : ListCreateACRuleMockScriptFilter(data) {
    override fun filterKeyInput(): String {
        return "ListACRules"
    }

    override fun filterName(): String {
        return "ListFilterACRuleMockScriptFilter"
    }

    override fun filterKeyOutput(): String {
        return "ListFilterACRuleMockScript"
    }

    override fun getRuleAction(): String {
        return "Filter"
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
