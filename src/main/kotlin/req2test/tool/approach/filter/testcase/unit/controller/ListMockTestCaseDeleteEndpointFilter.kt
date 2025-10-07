package req2test.tool.approach.filter.testcase.unit.controller

import req2test.tool.approach.entity.*

open class ListMockTestCaseDeleteEndpointFilter (data: MutableMap<String, Any?>): BaseListMockTCEndpointFilter(data){

    override fun filterKeyInput(): String { return "ListDeleteEndpointMockScript" }
    override fun filterName(): String {

        return "ListMockTestCaseDeleteEndpointFilter"
    }
    override fun filterKeyOutput(): String { return "ListMockTestCaseDeleteEndpoint" }

    private fun createTestCaseValid204(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }

        val input = getInput(validDataTest)
        val endpointDeleteWithId = getEndpointReplaced(validDataTest, "delete")
        val endpointDelete = getEndpoint()

        if(validDataTest != null) {
            val expectedResult =
                "Delete an existing instance of ${entity.name} and confirm that the method ${script.location} mapped by DELETE $endpointDelete returns 204"
            val positiveTestCase = TestCase(input, script, expectedResult)
            positiveTestCase.preconditions.add("Use ${entity.name} constructor with all attributes as parameters to mock an instance")

            val steps = ArrayList<String>()
            steps.add("Inform ${input.input}")

            steps.add("Mock ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class to return void")
            steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by DELETE $endpointDeleteWithId in ${testArchitecture.controllerLayerClass}")

            steps.add("Verify that ${testArchitecture.ruleLayerMethod} was called")
            steps.add("Verify that the endpoint $endpointDeleteWithId returns 204")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }

    private fun createTestCaseInternalError500(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }

        val input = getInput(validDataTest)
        val endpointDeleteWithId = getEndpointReplaced(validDataTest, "delete")
        val endpointDelete = getEndpoint()

        if(validDataTest != null) {
            val expectedResult = "${script.location} mapped by DELETE $endpointDelete returns internal error 500"
            val negativeTestCase = TestCase(validDataTest, script, expectedResult)
            negativeTestCase.preconditions.add("Use ${entity.name} constructor with all attributes as parameters to mock an instance")

            val steps = ArrayList<String>()
            steps.addAll(script.inputs.map { "Inform ${it.name}" })

            val errorMessage = "Failed to delete ${entity.name} from database"
            steps.add("Mock ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class  to return ${testArchitecture.dbErrorAction} ${testArchitecture.dbError} with the following error message: $errorMessage")
            steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by DELETE $endpointDeleteWithId in ${testArchitecture.controllerLayerClass} class and return internal error code 500 with the following error message: $errorMessage")

            steps.add("Verify that ${testArchitecture.ruleLayerMethod} was called")
            steps.add("Verify that the endpoint $endpointDeleteWithId returns 500")
            steps.add("Verify that the endpoint $endpointDeleteWithId contains the message: $errorMessage")

            negativeTestCase.steps.addAll(steps)
            return negativeTestCase
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

        val input = getInput(negativeDataTest)
        val endpointDeleteWithId = getEndpointReplaced(negativeDataTest, "delete")
        val endpointDelete = getEndpoint()

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

        val input = getInput(validDataTest)
        val endpointDeleteWithId = getEndpointReplaced(validDataTest, "delete")
        val endpointDelete = getEndpoint()

        if(validDataTest != null) {
            val expectedResult = "${script.location} mapped by DELETE $endpointDelete does not delete an instance of ${entity.name} then returns not found 404"
            val instanceNotFoundTestCase = TestCase(input, script, expectedResult)
            val errorMessage = "${entity.name} not found"
            val steps = ArrayList<String>()
            steps.add("Inform ${input.input}")

            steps.add("Mock ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class to return ${testArchitecture.ruleError} with the following error message: $errorMessage")
            steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by DELETE $endpointDeleteWithId in ${testArchitecture.controllerLayerClass} class and It returns not found code 404 with the following message: $errorMessage")
            steps.add("Verify that ${testArchitecture.ruleLayerMethod} was called")
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
        createTestCaseInternalError500(ruleDataTest, script, entity)?.let { script.testCases.add(it) }
        return testCases
    }

}
