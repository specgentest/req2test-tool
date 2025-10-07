package req2test.tool.approach.filter.testcase.unit.controller

import req2test.tool.approach.core.Filter
import req2test.tool.approach.core.Log
import req2test.tool.approach.core.TestArchitecture
import req2test.tool.approach.processor.generator.file.TestPlanGenerator
import req2test.tool.approach.entity.*

abstract class BaseListMockTCEndpointFilter (data: MutableMap<String, Any?>): Filter(data, null){

    lateinit var testArchitecture: TestArchitecture
    lateinit var endpoints:Map<String, String>
    lateinit var entity: Entity
    lateinit var ruleLayerMethod: String
    lateinit var controllerLayerMethod: String

    fun filterInputByParams(
        input: Map<String, Any?>?,
        inputParameters: List<String>
    ): Map<String, Any?>? {
        if (input == null) return null

        return input.mapNotNull { (key, value) ->
            when (value) {
                is Map<*, *> -> {
                    val nested = filterInputByParams(
                        value as Map<String, Any>,
                        inputParameters
                    )
                    if (!nested.isNullOrEmpty()) key to nested else null
                }
                else -> {
                    if (key in inputParameters) key to value else null
                }
            }
        }.toMap()
    }

    fun getInput(dataTest: DataTest?): DataTest {
        if(testArchitecture.urlinput == null)
            return DataTest(dataTest?.input?.filter { (key, value) -> key.contains("id")  }, true, "", "")
        else {
            val inputParameters = testArchitecture.urlinput!!.map { it.name.split(".").last() }
            val input = dataTest?.input
            return DataTest(filterInputByParams(input, inputParameters), true, "", "")
        }
    }

    fun getEndpointReplaced(dataTest: DataTest?, endpointKey: String): String? {
        if(testArchitecture.urlinput == null){
            val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String
            val endpointGet = testArchitecture.endpoints[endpointKey]?.replace("{id}", idAttribute)
            val id = dataTest?.input?.get(idAttribute)
            val endpointGetWithId = testArchitecture.endpoints[endpointKey]?.replace("{id}", id.toString())
            return endpointGetWithId
        }
        else {
            val parameters = testArchitecture.urlinput!!.map { it.name }
            var endpointGetWithParameters = testArchitecture.url.toString()
            parameters.forEach{
                    param ->
                val value = getValueFromMap(dataTest?.input!!, param)
                endpointGetWithParameters = endpointGetWithParameters.replace("{$param}", value.toString())
            }
            return endpointGetWithParameters
        }
    }

    fun getEndpoint(): String? {
        if(testArchitecture.url == null)   {
            val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String
            return testArchitecture.endpoints["retrieve"]?.replace("{id}", idAttribute)
        }
        return testArchitecture.url
    }

    fun getValueFromMap(data: Map<String, Any?>, path: String): Any? {
        val keys = path.split(".")
        var current: Any? = data

        for (key in keys) {
            if (current !is Map<*, *>) return null
            current = current[key]
        }

        return current
    }


    fun getUniqueAttributes(entity: Entity): List<String> {
        return entity.attributes.filter { it.dbValidations.contains("unique") }.map { it.name }
    }

    fun formatParameters(parameters: List<String>): String {
        return parameters.joinToString(prefix = "(", separator = ", ", postfix = ")")
    }

    override fun isMandatoryFilter(): Boolean {
        return false
    }

    open fun setEndpoints() {
        endpoints = testArchitecture.endpoints
    }

    open fun setRuleLayerMethod() {
        ruleLayerMethod = testArchitecture.ruleLayerMethod
    }

    open fun setControllerLayerMethod() {
        controllerLayerMethod = testArchitecture.controllerLayerMethod
    }

    open fun isValidationEndpoint(): Boolean {
        return false
    }

    open fun filterScripts(scripts: List<Script>): List<Script> {
        return scripts.filter { it.reference is Entity }
    }

    abstract fun createTestCases(entityDataTest: ListDataTest, ruleDataTest: ListDataTest, invalidIDDataTest: DataTest?, script: Script, entity: Entity): List<TestCase>

    override fun compute(input: Any): Any? {

        var scripts = input as List<Script>

        scripts = filterScripts(scripts)




        scripts.forEach { script ->

            testArchitecture = script.testArchitecture!!
            setEndpoints()
            setRuleLayerMethod()
            setControllerLayerMethod()

            val listEntitiesDataTest = data["ListEntitiesDataTest"] as List<ListDataTest>

            //Indica que é um script de ACRule sem Validation
            //Mantive o script.reference na Entity para as Rules puramente da entidade. Nas validations precisei alterar por questao de lógica
            if(script.reference is Entity) {
                val listRuleDataTest = data["ListRuleDataTest"] as List<ListDataTest>
                val ruleDataTest =
                    listRuleDataTest.find { (it.reference as ListDataTest).reference == script.reference }
                val entityDataTest = listEntitiesDataTest.find { (it.reference == script.reference) }

                if (ruleDataTest == null)
                    logs.add(
                        Log.createCriticalError(
                            filterName(),
                            "RuleDataTest not found for ${script.reference}"
                        )
                    )
                else if (entityDataTest == null)
                    logs.add(
                        Log.createCriticalError(
                            filterName(),
                            "EntityDataTest not found for ${script.reference}"
                        )
                    )
                else {
                    entity = script.reference
                    val invalidIDDataTest = ruleDataTest.dataTest.find { it.errorMessage == "invalid id" }
                    script.testCases.addAll(createTestCases(entityDataTest, ruleDataTest, invalidIDDataTest, script, entity))
                }
            }
            //Indica que é um script de ACRule com Validation
            if(script.reference is Rule) {
                val mapListRuleValidationDataTest =
                    data["MapListRuleValidationDataTest"] as Map<Rule, List<ListDataTest>>

                val ruleReference = script.reference as Rule

                val listEntities = data["ListEntities"] as List<Entity>
                entity = listEntities.find { it.name == ruleReference.reference } as Entity

                val entityDataTest = listEntitiesDataTest.find { (it.reference == entity) }


                //Data from RuleValidationData
                val listRuleValidationDataTest = mapListRuleValidationDataTest[ruleReference]

                if (listRuleValidationDataTest != null)
                {
                    listRuleValidationDataTest.forEach {
                            ruleValidationDataTest ->
                        if (entityDataTest != null) {
                            val ruleValidationDataTestFiltered = ruleValidationDataTest.dataTest.filter { it.errorMessage != "invalid id" }

                            val invalidIDDataTest = ruleValidationDataTest.dataTest.find { it.errorMessage == "invalid id" }

                            val listRuleValidationDataTestFiltered = ListDataTest(dataTest = ruleValidationDataTestFiltered, reference = ruleValidationDataTest.reference)
                            listRuleValidationDataTestFiltered.llmMessageHistory = ruleValidationDataTest.llmMessageHistory
                            listRuleValidationDataTestFiltered.varDictionaryAttributes = ruleValidationDataTest.varDictionaryAttributes
                            listRuleValidationDataTestFiltered.entitiesAllVariables = ruleValidationDataTest.entitiesAllVariables
                            script.testCases.addAll(createTestCases(listRuleValidationDataTestFiltered, listRuleValidationDataTestFiltered, invalidIDDataTest, script, entity))
                        }
                    }
                }
            }

        }
            //Exportando dados para .txt
            scripts.forEach { entityScript ->
                var textTestCases = ""
                var ctCount = 1
                entityScript.testCases.forEach {tc ->
                    var tcDescription = tc.expectedResult

                    textTestCases += "TC-${ctCount++}\t\t\t\t\t\t| ${tcDescription}\n"
                    textTestCases += "preconditions\t\t\t\t| ${tc.preconditions}\n"
                    textTestCases += "Input\t\t\t\t\t\t| ${tc.dataTest.input?.map { "\n\t\t\t\t\t\t\t${it.key}: ${it.value} " }}\n"
                    textTestCases += "Steps\t\t\t\t\t\t| ${tc.steps.map { "\n\t\t\t\t\t\t\t$it" }}\n"
                    textTestCases += "Expected Result\t\t\t\t| ${tc.expectedResult}\n\n"
                }

                if(isValidationEndpoint())
                    TestPlanGenerator.createFile(textTestCases , "outputArtifacts/", "${testArchitecture.ruleAction}${entity.name}EndpointValidationsTC.txt")
                else
                    TestPlanGenerator.createFile(textTestCases , "outputArtifacts/", "${testArchitecture.ruleAction}${entity.name}EndpointTC.txt")
            }
        return scripts
    }

}
