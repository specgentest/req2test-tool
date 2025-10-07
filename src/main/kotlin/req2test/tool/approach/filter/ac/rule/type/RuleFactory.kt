package req2test.tool.approach.filter.ac.rule.type

import req2test.tool.approach.core.Log
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.Rule

class RuleFactory {
    companion object {
        fun createRule(rawACRule: String, entities: List<Entity>): Rule {
            val values = rawACRule.split("|").map { it.trim() }
            if (values.size < 3) {
                throw GeneralException(Log.createCriticalError("ACRule", "Invalid format: $rawACRule"))
            }
            return when (val type = values[1]) {
                "endpoint" -> GenericEndpointRule(rawACRule, entities).createRule()
                "endpoint-create" -> EndpointCreateRule(rawACRule, entities).createRule()
                "endpoint-get" -> EndpointGetRule(rawACRule, entities).createRule()
                "endpoint-get-list" -> EndpointGetListRule(rawACRule, entities).createRule()
                "endpoint-get-all" -> EndpointGetAllRule(rawACRule, entities).createRule()
                "endpoint-update" -> EndpointUpdateRule(rawACRule, entities).createRule()
                "endpoint-partial-update" -> EndpointPartialUpdateRule(rawACRule, entities).createRule()
                "endpoint-delete" -> EndpointDeleteRule(rawACRule, entities).createRule()
                "endpoint-filter" -> EndpointFilterRule(rawACRule, entities).createRule()
                "view" -> ViewRule(rawACRule, entities).createRule()
                else -> throw GeneralException(Log.createCriticalError("ACRule", "Rule type not found: $type"))
            }
        }
    }
}