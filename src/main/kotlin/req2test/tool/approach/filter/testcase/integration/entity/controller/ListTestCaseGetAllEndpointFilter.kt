package req2test.tool.approach.filter.testcase.integration.entity.controller

import req2test.tool.approach.entity.*

open class ListTestCaseGetAllEndpointFilter (data: MutableMap<String, Any?>): BaseListTCEndpointFilter(data){

    override fun filterKeyInput(): String { return "ListGetAllEndpointScript" }
    override fun filterName(): String {

        return "ListTestCaseGetAllEndpointFilter"
    }
    override fun filterKeyOutput(): String { return "ListTestCaseGetAllEndpoint" }

    private fun createTestCaseGetAllThreeInstances(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = ruleDataTest.dataTest.filter { it.isValidInput }

        val input = DataTest(HashMap<String, String>(), true, "", "")

        val endpointGetAll = testArchitecture.endpoints["retrieve all"]

        if(validDataTest != null) {
            val dbInitMap = mutableMapOf<String, Any?>()
            dbInitMap[entity.name] = validDataTest.map { it.input }
            val output = dbInitMap

            val expectedResult =
                "Confirms that the method ${script.location} mapped by GET $endpointGetAll gets the three existing instances of ${entity.name}, as following: $output"
            val positiveTestCase = TestCase(input, script, expectedResult)
            positiveTestCase.preconditions.add("The repository must have the instances that will be retrieved as following: $dbInitMap")

            val steps = ArrayList<String>()
            steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by GET $endpointGetAll in ${testArchitecture.controllerLayerClass} class and return: $output")
            steps.add("Verify that the endpoint $endpointGetAll returns 200")
            steps.add("Verify that the endpoint $endpointGetAll returns $output")

            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }

    private fun createTestCaseGetAllOneInstance(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }

        val input = DataTest(HashMap<String, String>(), true, "", "")

        val endpointGetAll = testArchitecture.endpoints["retrieve all"]

        if(validDataTest != null) {
            val dbInitMap = mutableMapOf<String, Any?>()
            dbInitMap[entity.name] = validDataTest.input
            val output = dbInitMap

            val expectedResult =
                "Confirms that the method ${script.location} mapped by GET $endpointGetAll gets the one existing instance of ${entity.name}, as following: $output"
            val positiveTestCase = TestCase(input, script, expectedResult)
            positiveTestCase.preconditions.add("The repository must have the instances that will be retrieved as following: $dbInitMap")

            val steps = ArrayList<String>()
            steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by GET $endpointGetAll in ${testArchitecture.controllerLayerClass} class and return: $output")
            steps.add("Verify that the endpoint $endpointGetAll returns 200")
            steps.add("Verify that the endpoint $endpointGetAll returns $output")

            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }

    private fun createTestCaseGetAllReturnEmpty(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {

        val input = DataTest(HashMap<String, String>(), true, "", "")

        val endpointGetAll = testArchitecture.endpoints["retrieve all"]

            val dbInitMap = mutableMapOf<String, Any?>()
            val output = dbInitMap

            val expectedResult =
                "Confirms that the method ${script.location} mapped by GET $endpointGetAll gets the one existing instance of ${entity.name}, as following: $output"
            val positiveTestCase = TestCase(input, script, expectedResult)
            positiveTestCase.preconditions.add("The repository must have be empty")

            val steps = ArrayList<String>()
            steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by GET $endpointGetAll in ${testArchitecture.controllerLayerClass} class and return: $output")
            steps.add("Verify that the endpoint $endpointGetAll returns 200")
            steps.add("Verify that the endpoint $endpointGetAll returns empty")

            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
    }



    override fun createTestCases(entityDataTest: ListDataTest, ruleDataTest: ListDataTest, invalidIDDataTest: DataTest?, script: Script, entity: Entity): List<TestCase> {
        val testCases = ArrayList<TestCase>()
        createTestCaseGetAllThreeInstances(ruleDataTest, script, entity)?.let { script.testCases.add(it) }
        createTestCaseGetAllOneInstance(ruleDataTest, script, entity)?.let { script.testCases.add(it) }
        createTestCaseGetAllReturnEmpty(ruleDataTest, script, entity)?.let { script.testCases.add(it) }
        return testCases
}

}
