package req2test.tool.approach.filter.datatest.rule

import req2test.tool.approach.core.Filter
import req2test.tool.approach.entity.ListDataTest

class ListRuleDataTestFilter (data: MutableMap<String, Any?>, processor: Any) : Filter(data, processor) {
    override fun filterKeyInput(): String {
        return "ListEntities"
    }

    override fun filterName(): String {
        return "ListRuleDataTestFilter"
    }

    override fun filterKeyOutput(): String {
        return "ListRuleDataTest"
    }

    override fun compute(input: Any): Any? {
        var listEntitiesDataTest = data["ListEntitiesDataTest"] as List<ListDataTest>
        val rulesDataTest = ArrayList<ListDataTest>()
        if (processor != null) {
            listEntitiesDataTest.forEach {
                data["EntityDataTest"] = it
                val filter = RuleDataTestFilter(data, processor)
                if(filter.execute()){
                    val ruleDataTest = data["RuleDataTest"] as ListDataTest
                    ruleDataTest.reference = it
                    rulesDataTest.add(ruleDataTest)
                }

            }
        }
        return rulesDataTest
    }
}
