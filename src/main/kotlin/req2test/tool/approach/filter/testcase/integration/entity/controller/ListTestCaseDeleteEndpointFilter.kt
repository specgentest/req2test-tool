package req2test.tool.approach.filter.testcase.integration.entity.controller

import req2test.tool.approach.entity.*

open class ListTestCaseDeleteEndpointFilter (data: MutableMap<String, Any?>): BaseListTCEndpointFilter(data){

    override fun filterKeyInput(): String { return "ListDeleteEndpointScript" }
    override fun filterName(): String {

        return "ListTestCaseDeleteEndpointFilter"
    }
    override fun filterKeyOutput(): String { return "ListTestCaseDeleteEndpoint" }


    private fun createTestCaseValid204(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }

        val input = DataTest(validDataTest?.input?.filter { (key, value) -> key.contains("id")  }, true, "", "")

        val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String
        val endpointDelete = testArchitecture.endpoints["delete"]?.replace("{id}", idAttribute)
        val id = validDataTest?.input?.get(idAttribute)
        val endpointDeleteWithId = testArchitecture.endpoints["delete"]?.replace("{id}", id.toString())

        if(validDataTest != null) {
            val expectedResult =
                "Delete an existing instance of ${entity.name} and confirm that the method ${script.location} mapped by DELETE $endpointDelete returns 204"
            val positiveTestCase = TestCase(input, script, expectedResult)
            val postFieldInput = getCreateEntityInputs(entity)
            val variablesInput = validDataTest.input
                ?.filter { postFieldInput.contains(it.key) }
                ?.map { "\n\t\t\t\t\t\t\t${it.key}: ${it.value} " }

            val dbInitMap = mutableMapOf<String, Any?>()
            dbInitMap[entity.name] = validDataTest.input

            positiveTestCase.preconditions.add("Initialize the database with the following data: $dbInitMap")

            val steps = ArrayList<String>()
            steps.add("Inform ${input.input}")

            steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by DELETE $endpointDeleteWithId in ${testArchitecture.controllerLayerClass}")
            steps.add("Verify that the endpoint $endpointDeleteWithId returns 204")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }


    private fun createTestCaseBadRequest400(invalidIDDataTest: DataTest?, script: Script, entity: Entity): TestCase {
        val negativeDataTest = invalidIDDataTest
        val notGeneratedAttributes = entity.attributes.filter { !it.generatedDB and !it.generatedConstructor }
        val inputParamMethod = HashMap<String, Any?>()

        notGeneratedAttributes.forEach {
            inputParamMethod[it.name] = negativeDataTest?.input?.get(it.name)
        }

        val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String

        inputParamMethod[idAttribute] = negativeDataTest?.input?.get(idAttribute)
        val input = DataTest(inputParamMethod, false, negativeDataTest?.errorMessage, "")

        val endpointDelete = testArchitecture.endpoints["delete"]?.replace("{id}", idAttribute)
        val id = inputParamMethod[idAttribute]
        val endpointDeleteWithId = testArchitecture.endpoints["delete"]?.replace("{id}", id.toString())

        val errorMessage = "invalid $idAttribute type"
        val expectedResult = "${script.location} mapped by DELETE $endpointDelete returns bad request 400 with the following error message: $errorMessage"

        val negativeTestCase = TestCase(input, script, expectedResult)
        val steps = ArrayList<String>()

        steps.addAll(script.inputs.map { "Inform ${it.name}" })

        steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by DELETE $endpointDeleteWithId in ${testArchitecture.controllerLayerClass} class")
        steps.add("Verify that the endpoint $endpointDeleteWithId returns 400")
        steps.add("Verify that the endpoint $endpointDeleteWithId contains the message: $errorMessage")
        negativeTestCase.steps.addAll(steps)
        return negativeTestCase
    }

    private fun createTestCaseInstanceNotFound404(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }

        val input = DataTest(validDataTest?.input?.filter { (key, value) -> key.contains("id")  }, true, "", "")

        val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String
        val endpointDelete = testArchitecture.endpoints["delete"]?.replace("{id}", idAttribute)
        val endpointDeleteWithId = testArchitecture.endpoints["delete"]?.replace("{id}", validDataTest?.input?.get(idAttribute).toString())

        if(validDataTest != null) {
            val expectedResult = "${script.location} mapped by DELETE $endpointDelete does not delete an instance of ${entity.name} then returns not found 404"
            val instanceNotFoundTestCase = TestCase(input, script, expectedResult)
            val errorMessage = "${entity.name} not found"
            val steps = ArrayList<String>()
            steps.add("Inform ${input.input}")

            steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by DELETE $endpointDeleteWithId in ${testArchitecture.controllerLayerClass} class")
            steps.add("Verify that the endpoint $endpointDeleteWithId returns 404")
            steps.add("Verify that the endpoint $endpointDeleteWithId contains the message: $errorMessage")
            instanceNotFoundTestCase.steps.addAll(steps)
            return instanceNotFoundTestCase
        }
        return null
    }

    override fun createTestCases(entityDataTest: ListDataTest, ruleDataTest: ListDataTest, invalidIDDataTest: DataTest?, script: Script, entity: Entity): List<TestCase> {
        val testCases = ArrayList<TestCase>()
        createTestCaseValid204(ruleDataTest, script, entity)?.let { script.testCases.add(it) }
        createTestCaseInstanceNotFound404(ruleDataTest, script, entity)?.let { script.testCases.add(it) }
        createTestCaseBadRequest400(invalidIDDataTest, script, entity).let { script.testCases.add(it) }
        return testCases
    }

}
