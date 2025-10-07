package req2test.tool.approach.filter.datatest.genericrule

import req2test.tool.approach.core.Filter
import req2test.tool.approach.entity.ListDataTest
import req2test.tool.approach.entity.Rule

class ListGenericRulesDataTestFilter (data: MutableMap<String, Any?>, processor: Any) : Filter(data, processor) {
    override fun filterKeyInput(): String {
        return "ListACRules"
    }

    override fun filterName(): String {
        return "ListEntitiesDataTestFilter"
    }

    override fun filterKeyOutput(): String {
        return "ListGenericRulesDataTest"
    }

    override fun compute(input: Any): Any? {
        //val entities = input as List<Entity>
        var listACRules = input as List<Rule>
        listACRules = listACRules.filter { it.ruleType == "endpoint" }
        val genericRulesDataTest = ArrayList<ListDataTest>()
        if (processor != null) {
            listACRules.forEach {
                data["ACRule"] = it
                val filter = GenericRuleDataTestFilter(data, processor)
                if(filter.execute()){
                    val entityDataTest = data["GenericRuleDataTest"] as ListDataTest
                    entityDataTest.reference = it
                    genericRulesDataTest.add(entityDataTest)
                }

            }
        }
        return genericRulesDataTest
    }
}
