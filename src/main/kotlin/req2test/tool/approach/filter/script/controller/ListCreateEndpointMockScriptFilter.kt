package req2test.tool.approach.filter.script.controller

import req2test.tool.approach.core.Filter
import req2test.tool.approach.core.TestArchitecture
import req2test.tool.approach.filter.ac.rule.type.RuleVariable
import req2test.tool.approach.processor.generator.file.TestPlanGenerator
import req2test.tool.approach.entity.*

open class ListCreateEndpointMockScriptFilter (data: MutableMap<String, Any?>) : Filter(data, null) {
    lateinit var listACRules: List<Rule>
    lateinit var currentACRule: Rule

    override fun filterKeyInput(): String {
        return "ListACRules"
    }

    override fun isMandatoryFilter(): Boolean {
        return false
    }

    open fun getRuleAction(): String {
        return "Create"
    }
    override fun filterName(): String {
        return "ListCreateEndpointMockScriptFilter"
    }

    override fun filterKeyOutput(): String {
        return "ListCreateEndpointMockScript"
    }

    protected open fun getCompleteInputs(entity: Entity): List<Attribute> {
        if(currentACRule.attributes["input"] != null) {
            val ruleVariable = currentACRule.attributes["input"] as List<RuleVariable>
            return ruleVariable.map { it.attributeReference as Attribute }
                .filter { !it.generatedDB and !it.generatedConstructor }
        }
        else
            return entity.attributes.filter { !it.generatedDB and !it.generatedConstructor }
    }

    protected open fun getInputs(entity: Entity): List<Attribute> {
        var attributeListAux = mutableListOf<Attribute>()
        var attributeList = mutableListOf<Attribute>()
        if(currentACRule.attributes["input"] != null) {
            val ruleVariable = currentACRule.attributes["input"] as List<RuleVariable>
            attributeListAux = ruleVariable.map { it.attributeReference as Attribute }
                .filter { !it.generatedDB and !it.generatedConstructor }.toMutableList()
        }
        else
            attributeListAux = entity.attributes.filter { !it.generatedDB and !it.generatedConstructor }.toMutableList()

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

    protected open fun getInputTechniques(): List<InputTechnique>{
        val inputTechniques = ArrayList<InputTechnique>()
        inputTechniques.add(InputTechnique.CONDITION_AND_DECISION_COVERAGE)
        return inputTechniques
    }

    fun createSteps(inputs: List<Attribute>, testArchitecture: TestArchitecture, endpoint: String?): List<String> {
        val steps = ArrayList<String>()
        steps.addAll(inputs.map { "Inform ${it.name}" })
        steps.add("call endpoint $endpoint mapped by ${testArchitecture.controllerLayerMethod} in ${testArchitecture.controllerLayerClass} class")
        return steps
    }

    open fun getEndpoint(testArchitecture: TestArchitecture): String? {
        return testArchitecture.endpoints["create"]
    }

    open fun getEndpointWithValidations(testArchitecture: TestArchitecture): String? {
        return testArchitecture.endpointsWithValidations["create"]
    }

    fun printScript(id: Int, script: Script, entity: Entity): String{
        var textScript = ""
        textScript += "SCRIPT-${id}\t\t\t\t\t| ${getRuleAction()} ${entity.name}\n"
        textScript += "Script objective\t\t\t\t| ${script.objective}\n"
        textScript += "Location\t\t\t\t\t| ${script.location}\n"
        textScript += "Steps\t\t\t\t\t\t| ${script.steps}\n"
        textScript += "Level\t\t\t\t\t\t| ${script.level.value}\n"
        textScript += "Technique\t\t\t\t\t| ${script.technique.value}\n"
        textScript += "Selection Criteria\t\t\t\t| ${script.inputTechniques.map { it.value }}\n\n"
        return textScript
    }

    open fun filterRules(listCreateACRule: List<Rule>): List<Rule> {
        return listCreateACRule.filter { it.ruleType == "endpoint-create" }
    }

    override fun compute(input: Any): Any? {
        listACRules = input as List<Rule>

        listACRules = filterRules(listACRules)

        listACRules.forEach { println(it.ruleType) }
        val scripts = ArrayList<Script>()

        var textScripts = ""
        var count = 1

        listACRules.forEach { ACRule ->

            val entity = ACRule.attributes["entity"] as Entity
            currentACRule = ACRule
            var urlinput = currentACRule.attributes["urlinput"] as List<RuleVariable>?
            var url = currentACRule.attributes["url"] as String?


            val inputs = getInputs(entity)
            val inputAllAttributes = getCompleteInputs(entity)
            val testArchitecture = TestArchitecture.cleanArchitecture(ruleAction = getRuleAction(), entity = entity, inputs = inputs, urlinput = urlinput, url=url)

            var location = "${testArchitecture.controllerLayerInstance}.${testArchitecture.controllerLayerMethod}"
            var endpoint = getEndpoint(testArchitecture)
            if(ACRule.hasValidations()) {
                location = "${testArchitecture.controllerLayerInstance}.${testArchitecture.controllerValidationLayerMethod}"
                endpoint = getEndpointWithValidations(testArchitecture)
            }
            testArchitecture.endpoints["endpoint"] = endpoint as String


            val objective = "Detect faults when validating the behavior of $location with mocked dependencies, ensuring it responds correctly to interactions and conditions"
            val preconditions = ArrayList<String>()
            val steps = createSteps(inputs, testArchitecture, endpoint)
            val inputTechniques = getInputTechniques()
            inputs.map { "Inform ${it.name}" }

            //Added to filter
            var reference: Any = entity
            if(currentACRule.hasValidations())
                reference = currentACRule

            val script = Script(
                Level.UNIT,
                objective,
                location,
                preconditions,
                steps,
                Technique.WHITEBOX,
                inputTechniques,
                inputs,
                reference
            )

            script.testArchitecture = testArchitecture
            script.inputAllAttributes = inputAllAttributes
            scripts.add(script)

            textScripts += printScript(count++, script, entity)


        }
        TestPlanGenerator.createFile(textScripts, "outputArtifacts/", "Endpoint${getRuleAction()}Scripts.txt")

        return scripts
    }
}
