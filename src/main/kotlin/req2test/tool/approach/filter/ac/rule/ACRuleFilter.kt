package req2test.tool.approach.filter.ac.rule

import req2test.tool.approach.core.Filter
import req2test.tool.approach.core.Log
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.Rule
import req2test.tool.approach.filter.ac.rule.type.GeneralException
import req2test.tool.approach.filter.ac.rule.type.RuleFactory

open class ACRuleFilter(data: MutableMap<String, Any?>) : Filter(data, null) {
    override fun filterKeyInput(): String {
        return "RawACRule"
    }
    override fun filterName(): String {
        return "RawACRuleFilter"
    }

    override fun filterKeyOutput(): String {
        return "ACRule"
    }

    private fun extractRule(rawACRule: String): Rule {
        val entities = data["ListEntities"]
        if(entities == null)
            logs.add(Log.createCriticalError("ACRule", "Entities is null"))
        return RuleFactory.createRule(rawACRule, entities as List<Entity>)
    }

    override fun compute(input: Any): Any? {
        val rawACRule = input as String
        var rule: Rule? = null
        try {
            rule = extractRule(rawACRule)
        } catch (ex: GeneralException){
            logs.add(ex.log)
        }
        throwsCriticalErrors()
        return rule
    }
}