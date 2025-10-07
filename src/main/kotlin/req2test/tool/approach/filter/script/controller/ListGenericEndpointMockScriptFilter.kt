package req2test.tool.approach.filter.script.controller

import req2test.tool.approach.core.TestArchitecture
import req2test.tool.approach.filter.ac.rule.type.RuleVariable
import req2test.tool.approach.processor.generator.file.TestPlanGenerator
import req2test.tool.approach.entity.*

class ListGenericEndpointMockScriptFilter (data: MutableMap<String, Any?>) : ListCreateEndpointMockScriptFilter(data) {
    override fun filterKeyInput(): String {
        return "ListACRules"
    }

    override fun filterName(): String {
        return "ListGenericEndpointMockScriptFilter"
    }

    override fun filterKeyOutput(): String {
        return "ListGenericEndpointMockScript"
    }

    override fun getRuleAction(): String {
        //removido pois é concatenado no nome do method do controller
        return ""
    }



    override fun filterRules(listCreateACRule: List<Rule>): List<Rule> {
        return listCreateACRule.filter { it.ruleType == "endpoint" }
    }

    fun printScript(id: Int, script: Script): String{
        var textScript = ""
        textScript += "SCRIPT-${id}\t\t\t\t\t| ${getRuleAction()}\n"
        textScript += "Script objective\t\t\t\t| ${script.objective}\n"
        textScript += "Location\t\t\t\t\t| ${script.location}\n"
        textScript += "Steps\t\t\t\t\t\t| ${script.steps}\n"
        textScript += "Level\t\t\t\t\t\t| ${script.level.value}\n"
        textScript += "Technique\t\t\t\t\t| ${script.technique.value}\n"
        textScript += "Selection Criteria\t\t\t\t| ${script.inputTechniques.map { it.value }}\n\n"
        return textScript
    }

    private fun generateClassName(description: String): String {
        return capitalizeWords(description
            .lowercase() // Converte para minúsculas
            .replace("calculate", "") // Remove "calculate" para manter a URL mais limpa
            .replace("of a", "") // Remove conectores desnecessários
            .replace("of", "")
            .replace("the", "")
            .replace("if", "")
            //.replace("determine", "validate")
            //.replace("find", "get")
            .trim())
            .replace(" ", "") // Substitui espaços por underscores
    }

    fun capitalizeWords(text: String): String {
        return text
            .lowercase() // Converte tudo para minúsculas para evitar problemas
            .split(" ") // Divide em palavras
            .filter { it.isNotEmpty() } // Remove espaços extras
            .joinToString(" ") { it.replaceFirstChar { ch -> ch.uppercaseChar() } } // Capitaliza a primeira letra
    }


    private fun generateEndpointName(description: String): String {
        return description
            .lowercase() // Converte para minúsculas
            .replace("calculate", "") // Remove "calculate" para manter a URL mais limpa
            .replace("of a", "") // Remove conectores desnecessários
            .replace("of", "")
            .replace("the", "")
            .replace("if", "")
            //.replace("determine", "validate")
            //.replace("find", "get")
            .replace(Regex("\\s+"), " ")
            .trim()
            .replace(" ", "-") // Substitui espaços por underscores
    }

    override fun getEndpoint(testArchitecture: TestArchitecture): String? {
        val rule = currentACRule
        val description = rule.attributes["description"] as String
        val endpointName = generateEndpointName(description)
        var endpoint = testArchitecture.endpoints["generic"] as String
        endpoint = endpoint.replace("{rule}", endpointName)
        testArchitecture.endpoints["endpoint"] = endpoint
        return endpoint
    }

    override fun compute(input: Any): Any? {
        listACRules = input as List<Rule>

        listACRules = filterRules(listACRules)

        listACRules.forEach { println(it.ruleType) }
        val scripts = ArrayList<Script>()

        var textScripts = ""
        var count = 1

        listACRules.forEach { ACRule ->

            //val entity = ACRule.attributes["entity"] as Entity
            currentACRule = ACRule
            val inputsRule = currentACRule.attributes["input"] as List<RuleVariable>
            val inputs = ArrayList<Attribute>()

            inputsRule.forEach { input ->
                val attribute = Attribute(name = input.name, type = input.type as String, validations = ArrayList<String>(), dbValidations = ArrayList<String>(),
                    generatedFormula = input.generatedFormula, generatedConstructor = false, generatedDB = false, hasDependency = false)
                inputs.add(attribute)
            }

            val className = generateClassName(currentACRule.attributes["description"] as String).replaceFirstChar { it.uppercase() }
            val ruleName = Entity(name = className, attributes = ArrayList<Attribute>(), entityValidations = null)
            val testArchitecture = TestArchitecture.cleanArchitecture(ruleAction = getRuleAction(), entity = ruleName, inputs = inputs)

            var location = "${testArchitecture.controllerLayerInstance}.${testArchitecture.controllerLayerMethod}"
            var endpoint = getEndpoint(testArchitecture)

            val objective = "Detect faults when validating the behavior of $location with mocked dependencies, ensuring it responds correctly to interactions and conditions"
            val preconditions = ArrayList<String>()
            val steps = createSteps(inputs, testArchitecture, endpoint)
            val inputTechniques = getInputTechniques()
            inputs.map { "Inform ${it.name}" }

            //Added to filter
            var reference: Any = currentACRule

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
            scripts.add(script)

            textScripts += printScript(count++, script)


        }
        TestPlanGenerator.createFile(textScripts, "outputArtifacts/", "GenericEndpointScripts.txt")

        return scripts
    }
}
