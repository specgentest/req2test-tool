package req2test.tool.approach.filter.script.unit.rule

import req2test.tool.approach.filter.ac.rule.type.RuleVariable
import req2test.tool.approach.entity.Attribute
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.Rule

class ListUpdateACRuleMockScriptFilter (data: MutableMap<String, Any?>) : ListCreateACRuleMockScriptFilter(data) {
    override fun filterKeyInput(): String {
        return "ListACRules"
    }

    override fun filterName(): String {
        return "ListUpdateACRuleMockScriptFilter"
    }

    override fun filterKeyOutput(): String {
        return "ListUpdateACRuleMockScript"
    }

    override fun getRuleAction(): String {
        return "Update"
    }

    /*
    override fun getInputs(entity: Entity): List<Attribute> {
        if(currentACRule.attributes["input"] != null) {
            val ruleVariable = currentACRule.attributes["input"] as List<RuleVariable>
            return ruleVariable.map { it.attributeReference as Attribute }.filter { (!it.generatedDB and !it.generatedConstructor) or it.name.startsWith("id") }
        }
        else
            return entity.attributes.filter { (!it.generatedDB and !it.generatedConstructor) or it.name.startsWith("id")}

        //return entity.attributes.filter { (!it.generatedDB and !it.generatedConstructor) or it.name.startsWith("id")}
    }*/

    override fun getInputs(entity: Entity): List<Attribute> {
        val attributeListAux: List<Attribute>
        val attributeList = mutableListOf<Attribute>()
        if(currentACRule.attributes["input"] != null) {
            val ruleVariable = currentACRule.attributes["input"] as List<RuleVariable>
            attributeListAux = ruleVariable.map { it.attributeReference as Attribute }.filter { (!it.generatedDB and !it.generatedConstructor) or it.name.startsWith("id") }
        }
        else
            attributeListAux = entity.attributes.filter { (!it.generatedDB and !it.generatedConstructor) or it.name.startsWith("id")}

        attributeListAux.forEach {
            if(it.type.contains("%")){
                val entities = data["ListEntities"] as List<Entity>
                val attrEntityName = it.type.replace("%", "")
                val attrEntity = entities.find { it.name.equals(attrEntityName, true) } as Entity
                val idAttr = attrEntity.attributes.find { it.name.contains("id") } as Attribute
                val idAttrRenamed = idAttr.copy()
                idAttrRenamed.name = idAttrRenamed.name.replace(Regex(attrEntity.name, RegexOption.IGNORE_CASE), it.name.replaceFirstChar { it.uppercase() })
                attributeList.add(idAttrRenamed)
            }
            else
                attributeList.add(it)
        }

        return attributeList
    }


    override fun filterRules(listCreateACRule: List<Rule>): List<Rule> {
        return listCreateACRule.filter { it.ruleType == "endpoint-update" }
    }
}
