package req2test.tool.approach.filter.datatest.rule

import req2test.tool.approach.core.Filter
import req2test.tool.approach.entity.ListDataTest
import req2test.tool.approach.entity.Rule

class ListRuleValidationDataTestFilter (data: MutableMap<String, Any?>, processor: Any) : Filter(data, processor) {
    override fun filterKeyInput(): String {
        return "ListEntitiesDataTest"
    }

    override fun filterName(): String {
        return "ListRuleValidationDataTestFilter"
    }

    override fun filterKeyOutput(): String {
        return "MapListRuleValidationDataTest"
    }

    override fun compute(input: Any): Any? {
        val listEntitiesDataTest = input as List<ListDataTest>
        var mapRulesDataTest = HashMap<Rule, List<ListDataTest>>()
        if (processor != null) {
            listEntitiesDataTest.forEach {
                data["EntityDataTest"] = it
                val filter = RuleValidationDataTestFilter(data, processor)
                if(filter.execute()){
                    val listRuleValidationDataTest = data["ListRuleValidationDataTest"] as List<ListDataTest>
                    val groupedListRuleValidationDataTest: Map<Rule, List<ListDataTest>> = listRuleValidationDataTest.groupBy { it.reference as Rule }
                    mapRulesDataTest = groupedListRuleValidationDataTest as HashMap<Rule, List<ListDataTest>>
                }
            }
        }
        return mapRulesDataTest
    }
}
