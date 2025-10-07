package req2test.tool.approach.filter.testcase.unit.controller

import req2test.tool.approach.entity.*

open class ListMockTestCaseGetAllEndpointFilter (data: MutableMap<String, Any?>): BaseListMockTCEndpointFilter(data){

    override fun filterKeyInput(): String { return "ListGetAllEndpointMockScript" }
    override fun filterName(): String {

        return "ListMockTestCaseGetAllEndpointFilter"
    }
    override fun filterKeyOutput(): String { return "ListMockTestCaseGetAllEndpoint" }

    private fun createTestCaseValid200(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = ruleDataTest.dataTest.filter { it.isValidInput }

        val input = DataTest(HashMap<String, String>(), true, "", "")
        val output = validDataTest


        val endpointGetAll = testArchitecture.endpoints["retrieve all"]

        if(validDataTest != null) {
            val expectedResult =
                "Confirms that the method ${script.location} mapped by GET $endpointGetAll gets the three existing instances of ${entity.name}, as following: $output"
            val positiveTestCase = TestCase(input, script, expectedResult)
            positiveTestCase.preconditions.add("Use ${entity.name} constructor with all attributes as parameters to mock an instance")

            val steps = ArrayList<String>()
            steps.add("Mock ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class to return $output")
            steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by GET $endpointGetAll in ${testArchitecture.controllerLayerClass} class and return: $output")
            steps.add("Verify that ${testArchitecture.ruleLayerMethod} was called")
            steps.add("Verify that the endpoint $endpointGetAll returns 200")
            steps.add("Verify that the endpoint $endpointGetAll returns $output")

            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }


    private fun createTestCaseInternalError500(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }

        val input = DataTest(HashMap<String, String>(), true, "", "")
        val endpointGetAll = testArchitecture.endpoints["retrieve all"]

        if(validDataTest != null) {
            val expectedResult = "${script.location} mapped by GET $endpointGetAll returns internal error 500"
            val negativeTestCase = TestCase(input, script, expectedResult)
            negativeTestCase.preconditions.add("Use ${entity.name} constructor with all attributes as parameters to mock an instance")

            val steps = ArrayList<String>()
            steps.addAll(script.inputs.map { "Inform ${it.name}" })

            val errorMessage = "Failed to retrieve instances of ${entity.name} from database"
            steps.add("Mock ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class  to return ${testArchitecture.dbErrorAction} ${testArchitecture.dbError} with the following error message: $errorMessage")
            steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by GET $endpointGetAll in ${testArchitecture.controllerLayerClass} class and return internal error code 500 with the following error message: $errorMessage")

            steps.add("Verify that ${testArchitecture.ruleLayerMethod} was called")
            steps.add("Verify that the endpoint $endpointGetAll returns 500")
            steps.add("Verify that the endpoint $endpointGetAll contains the message: $errorMessage")

            negativeTestCase.steps.addAll(steps)
            return negativeTestCase
        }
        return null
    }

    override fun createTestCases(entityDataTest: ListDataTest, ruleDataTest: ListDataTest, invalidIDDataTest: DataTest?, script: Script, entity: Entity): List<TestCase> {
        val testCases = ArrayList<TestCase>()
        createTestCaseValid200(ruleDataTest, script, entity)?.let { script.testCases.add(it) }
        createTestCaseInternalError500(ruleDataTest, script, entity)?.let { script.testCases.add(it) }
        return testCases
}

}
