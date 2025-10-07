package req2test.tool.approach.filter.ac.rule

import req2test.tool.approach.core.Filter
import req2test.tool.approach.core.Log
import req2test.tool.approach.entity.Rule
import req2test.tool.approach.processor.generator.file.TestPlanGenerator

open class ListACRulesFilter(data: MutableMap<String, Any?>) : Filter(data, null) {
    override fun filterKeyInput(): String {
        return "ListRawACRules"
    }
    override fun filterName(): String {
        return "ListACRulesFilter"
    }

    override fun filterKeyOutput(): String {
        return "ListACRules"
    }

    override fun isMandatoryFilter(): Boolean {
        return false
    }

    fun separateRawRules(listRawACRules: String): List<String> {
        val lines = listRawACRules.split("\n").map { it.trim() }
        val listRawRules = ArrayList<String>()
        var rawRule = ""
        var ruleId: String? = null
        for(line in lines){
            if(line.trim() != "") {
                if (rawRule == "") {
                    rawRule += line
                    ruleId = line.split("|")[0].trim()
                } else {
                    val lineId = line.split("|")[0].split(".")[0].trim()
                    if (ruleId == lineId) {
                        rawRule += "\n" + line
                    } else {
                        listRawRules.add(rawRule)
                        rawRule = line
                        ruleId = lineId
                    }
                }
            }
        }
        listRawRules.add(rawRule)
        return listRawRules
    }

    override fun compute(input: Any): Any? {
        val rawACRules = input as String
        val listRawRules = separateRawRules(rawACRules)
        val rules = ArrayList<Rule>()
        for(rawRule in listRawRules){
            data["RawACRule"] = rawRule
            val filterRule = ACRuleFilter(data)
            filterRule.execute()
            val rule = data["ACRule"] as Rule
            val id = rule.attributes["id"]
            if(rules.any { it.attributes["id"] == id}){
                logs.add(Log.createCriticalError("ListACRule", "Rule $id already exists"))
            }
            else {
                rules.add(rule)
            }
        }

        //Verify dependencies
        rules.forEach { it ->
            it.dependencies.forEach {it2 ->
                if(it2 !in rules.map { it.attributes["id"]}){
                    logs.add(Log.createCriticalError("ListACRule", "Dependency Rule $it2 not found"))
                }
            }

            //verify variable with same name
            if(it.ruleType == "view"){
                val variableDuplicated = rules.any { it3 ->
                    (it3.attributes["variable"] == it.attributes["variable"]) and (it != it3) }
                if(variableDuplicated)
                    logs.add(Log.createCriticalError("ListACRule", "Duplicated view variable ${it.attributes["variable"]}"))
            }
        }

        //ruleAttributes["variable"]
        throwsCriticalErrors()
        rules.forEach { r ->
            r.validated = true
        }
        TestPlanGenerator.createFile(rules.toString(), "outputArtifacts/", "rules.txt")
        return rules
    }
}