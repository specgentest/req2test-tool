package req2test.tool.approach.filter.testcase.unit.controller

import req2test.tool.approach.entity.*

open class ListMockTestCaseCreateEndpointFilter (data: MutableMap<String, Any?>): BaseListMockTCEndpointFilter(data){

    override fun filterKeyInput(): String { return "ListCreateEndpointMockScript" }
    override fun filterName(): String {

        return "ListMockTCCreateEndpointFilterFilter"
    }
    override fun filterKeyOutput(): String { return "ListMockTestCaseCreateEndpoint" }

    private fun createTestCaseValid201(entityDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = entityDataTest.dataTest.find { it.isValidInput }
        //val entity = script.reference as Entity

        if(validDataTest != null) {
            val expectedResult =
                "Create an instance of ${entity.name} and confirm that the method ${script.location} mapped by POST ${endpoints["create"]} saves the instance when all data is valid and unique"
            val positiveTestCase = TestCase(validDataTest, script, expectedResult)
            positiveTestCase.preconditions.add("The repository must be empty")
            positiveTestCase.preconditions.add("Use ${entity.name} constructor with all attributes as parameters to mock an instance")

            val steps = ArrayList<String>()
            steps.addAll(script.inputs.map { "Inform ${it.name}" })

            val output = validDataTest.input

            val requestParameter = extractBetweenParentheses(controllerLayerMethod)
            steps.add("Mock ${ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class, returning a mocked instance of ${entity.name} with informed data: $output")
            steps.add("Call method ${controllerLayerMethod} mapped by POST ${endpoints["create"]} in ${testArchitecture.controllerLayerClass} class and return a saved instance of ${entity.name}: $output")
            steps.add("The input request for the POST endpoint must be strictly: $requestParameter")
            steps.add("Verify attributes of created and saved instance of ${entity.name}")
            steps.add("Verify that ${ruleLayerMethod} was called")
            steps.add("Verify that the endpoint ${endpoints["create"]} returns 201")
            steps.add("Verify that the endpoint ${endpoints["create"]} contains $output")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }

    private fun createTestCaseBadRequest400(entityDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = entityDataTest.dataTest.find { !it.isValidInput }
        //val entity = script.reference as Entity

        if(validDataTest != null) {
            val expectedResult = "${script.location} mapped by POST ${endpoints["create"]} returns bad request 400 with the following error message: ${validDataTest.errorMessage}"
            val negativeTestCase = TestCase(validDataTest, script, expectedResult)
            negativeTestCase.preconditions.add("Use ${entity.name} constructor with all attributes as parameters to mock an instance")

            val steps = ArrayList<String>()
            steps.addAll(script.inputs.map { "Inform ${it.name}" })

            val requestParameter = extractBetweenParentheses(controllerLayerMethod)
            steps.add("Mock ${ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class  to return ${testArchitecture.ruleErrorAction} ${testArchitecture.ruleError} with the following error message: ${validDataTest.errorMessage}")
            steps.add("Call method ${controllerLayerMethod} mapped by POST ${endpoints["create"]} in ${testArchitecture.controllerLayerClass} class and return bad request code 400 with the following error message: ${validDataTest.errorMessage}")
            steps.add("The input request for the POST endpoint must be strictly: $requestParameter")
            steps.add("Verify that ${ruleLayerMethod} was called")
            steps.add("Verify that the endpoint ${endpoints["create"]} returns 400")
            steps.add("Verify that the endpoint ${endpoints["create"]} contains the message: ${validDataTest.errorMessage}")

            negativeTestCase.steps.addAll(steps)
            return negativeTestCase
        }
        return null
    }

    private fun createTestCaseInternalError500(entityDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = entityDataTest.dataTest.find { it.isValidInput }
        //val entity = script.reference as Entity

        if(validDataTest != null) {
            val expectedResult = "${script.location} mapped by POST ${endpoints["create"]} returns internal error 500"
            val negativeTestCase = TestCase(validDataTest, script, expectedResult)
            negativeTestCase.preconditions.add("Use ${entity.name} constructor with all attributes as parameters to mock an instance")

            val steps = ArrayList<String>()
            steps.addAll(script.inputs.map { "Inform ${it.name}" })

            val requestParameter = extractBetweenParentheses(controllerLayerMethod)
            val errorMessage = "Failed to save ${entity.name} in the database"
            steps.add("Mock ${ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class  to return ${testArchitecture.dbErrorAction} ${testArchitecture.dbError} with the following error message: $errorMessage")
            steps.add("Call method ${controllerLayerMethod} mapped by POST ${endpoints["create"]} in ${testArchitecture.controllerLayerClass} class and return internal error code 500 with the following error message: $errorMessage")
            steps.add("The input request for the POST endpoint must be strictly: $requestParameter")
            steps.add("Verify that ${ruleLayerMethod} was called")
            steps.add("Verify that the endpoint ${endpoints["create"]} returns 500")
            steps.add("Verify that the endpoint ${endpoints["create"]} contains the message: $errorMessage")

            negativeTestCase.steps.addAll(steps)
            return negativeTestCase
        }
        return null
    }

    override fun createTestCases(entityDataTest: ListDataTest, ruleDataTest: ListDataTest, invalidIDDataTest: DataTest?, script: Script, entity: Entity): List<TestCase> {
        val testCases = ArrayList<TestCase>()
        createTestCaseValid201(ruleDataTest, script, entity)?.let { script.testCases.add(it) }
        createTestCaseBadRequest400(entityDataTest, script, entity)?.let { script.testCases.add(it) }
        createTestCaseInternalError500(ruleDataTest, script, entity)?.let { script.testCases.add(it) }
        return testCases
    }

}
