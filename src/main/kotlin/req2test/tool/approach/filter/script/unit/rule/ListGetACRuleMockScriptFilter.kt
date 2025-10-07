package req2test.tool.approach.filter.script.unit.rule

import req2test.tool.approach.filter.ac.rule.type.RuleVariable
import req2test.tool.approach.entity.Attribute
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.Rule

class ListGetACRuleMockScriptFilter (data: MutableMap<String, Any?>) : ListCreateACRuleMockScriptFilter(data) {
    override fun filterKeyInput(): String {
        return "ListACRules"
    }

    override fun filterName(): String {
        return "ListGetACRuleMockScriptFilter"
    }

    override fun filterKeyOutput(): String {
        return "ListGetACRuleMockScript"
    }

    override fun getRuleAction(): String {
        return "Get"
    }

    override fun getInputs(entity: Entity): List<Attribute> {
        if(currentACRule.attributes["urlinput"] == null) {
            return entity.attributes.filter { it.name.startsWith("id") }
        }
        val inputs = currentACRule.attributes["urlinput"] as List<RuleVariable>
        return inputs.map { it.attributeReference!! }
    }



    override fun filterRules(listCreateACRule: List<Rule>): List<Rule> {
        return listCreateACRule.filter { it.ruleType == "endpoint-get" }
    }
}
