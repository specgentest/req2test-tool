package req2test.tool.approach.filter.script.controller

import req2test.tool.approach.core.TestArchitecture
import req2test.tool.approach.entity.Attribute
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.Rule
import req2test.tool.approach.filter.ac.rule.type.RuleVariable

class ListUpdateEndpointMockScriptFilter (data: MutableMap<String, Any?>) : ListCreateEndpointMockScriptFilter(data) {
    override fun filterKeyInput(): String {
        return "ListACRules"
    }

    override fun filterName(): String {
        return "ListUpdateEndpointScriptFilter"
    }

    override fun filterKeyOutput(): String {
        return "ListUpdateEndpointMockScript"
    }

    override fun getRuleAction(): String {
        return "Update"
    }

    override fun getEndpoint(testArchitecture: TestArchitecture): String? {
        return testArchitecture.endpoints["update"]
    }

    override fun getEndpointWithValidations(testArchitecture: TestArchitecture): String? {
        return testArchitecture.endpointsWithValidations["update"]
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
    }
    */


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
            attr ->
            if(attr.type.contains("%")){
                val entities = data["ListEntities"] as List<Entity>
                val attrEntityName = attr.type.replace("%", "")
                val attrEntity = entities.find { it.name.equals(attrEntityName, true) } as Entity

                var idAttr = attrEntity.attributes.find { it.name.contains("id") } as Attribute

                if(currentACRule.attributes["urlinput"] != null){
                    val inputs = currentACRule.attributes["urlinput"] as List<RuleVariable>
//                    val currentInputUrl = inputs.map { it.attributeReference!! }.find { attr.name == it.name }
                    var listCurrentInputUrl = inputs.map { it.attributeReference!! }
//                    currentInputUrl = currentInputUrl.find { attr.name == it.name }

                    val currentInputUrl = attrEntity.attributes.intersect(listCurrentInputUrl).first()

//                    var currentInputUrl = listCurrentInputUrl.find { it.name == attr.name }
//                    currentInputUrl = listCurrentInputUrl.find { it.name == attr.name }
//                    idAttr = attrEntity.attributes.find { it.name.equals(currentInputUrl?.name) } as Attribute
                    idAttr = currentInputUrl!!
                }

                val idAttrRenamed = idAttr.copy()
                idAttrRenamed.name = idAttrRenamed.name.replace(Regex(attrEntity.name, RegexOption.IGNORE_CASE), attr.name.replaceFirstChar { it.uppercase() })
                attributeList.add(idAttrRenamed)
            }
            else
                attributeList.add(attr)
        }

        return attributeList
    }

    override fun filterRules(listCreateACRule: List<Rule>): List<Rule> {
        return listCreateACRule.filter { it.ruleType == "endpoint-update" }
    }
}
