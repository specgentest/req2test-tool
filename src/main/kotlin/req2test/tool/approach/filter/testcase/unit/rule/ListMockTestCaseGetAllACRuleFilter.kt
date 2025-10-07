package req2test.tool.approach.filter.testcase.unit.rule

import req2test.tool.approach.entity.*

class ListMockTestCaseGetAllACRuleFilter (data: MutableMap<String, Any?>): BaseListMockTCACRuleFilter(data){

    override fun filterKeyInput(): String { return "ListGetAllACRuleMockScript" }
    override fun filterName(): String { return "ListMockTestCaseGetAllACRuleFilter" }
    override fun filterKeyOutput(): String { return "ListMockTestCaseGetAllACRule" }

    private fun createTestCaseGetAllThreeInstances(ruleDataTest: ListDataTest, script: Script): TestCase? {
        val validDataTest = ruleDataTest.dataTest.filter { it.isValidInput }
        val entity = script.reference as Entity

        val input = DataTest(HashMap<String, String>(), true, "", "")
        val output = validDataTest

        if(validDataTest != null) {
            val expectedResult =
                "Get the three existing instances of ${entity.name}, as following: $output"
            val positiveTestCase = TestCase(input, script, expectedResult)
            val operation = "GetAll${entity}_"
            val testName = "${baseTestName}${operation}GetAllReturnsThreeInstancesSuccessfully"
            positiveTestCase.testName = testName

            positiveTestCase.preconditions.add("The repository must have the instances that will be retrieved as following: $output")

            val steps = ArrayList<String>()
            steps.add("Mock ${testArchitecture.mapMethods["retrieve all"]} method in ${testArchitecture.repositoryLayerClass}, returning a mocked instance of ${entity.name} with informed data, the same mocked in repository")
            steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class and return the existing instances of ${entity.name}")
            steps.add("Verify attributes of retrieved instances of ${entity.name}")
            steps.add("Verify mocks")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }

    private fun createTestCaseGetAllOneInstance(ruleDataTest: ListDataTest, script: Script): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }
        val entity = script.reference as Entity

        val input = DataTest(HashMap<String, String>(), true, "", "")
        val output = validDataTest

        if(validDataTest != null) {
            val expectedResult =
                "Get the one existing instance of ${entity.name}, as following: $output"
            val positiveTestCase = TestCase(input, script, expectedResult)
            val operation = "GetAll${entity}_"
            val testName = "${baseTestName}${operation}GetAllReturnsOneInstanceSuccessfully"
            positiveTestCase.testName = testName

            positiveTestCase.preconditions.add("The repository must have the instances that will be retrieved as following: $output")

            val steps = ArrayList<String>()
            steps.add("Mock ${testArchitecture.mapMethods["retrieve all"]} method in ${testArchitecture.repositoryLayerClass}, returning a mocked instance of ${entity.name} with informed data, the same mocked in repository")
            steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class and return the existing instances of ${entity.name}")
            steps.add("Verify attributes of retrieved instances of ${entity.name}")
            steps.add("Verify mocks")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }

    private fun createTestCaseGetAllReturnEmpty(script: Script): TestCase {
        val entity = script.reference as Entity
        val input = DataTest(HashMap<String, String>(), true, "", "")

            val expectedResult =
                "Get an empty list of ${entity.name}"
            val positiveTestCase = TestCase(input, script, expectedResult)
            val operation = "GetAll${entity}_"
            val testName = "${baseTestName}${operation}GetAllReturnsEmptyListSuccessfully"
            positiveTestCase.testName = testName

        positiveTestCase.preconditions.add("The repository must have no instance, and It must return ${testArchitecture.findMethodReturnFalse}")

        val steps = ArrayList<String>()
            steps.add("Mock ${testArchitecture.mapMethods["retrieve all"]} method in ${testArchitecture.repositoryLayerClass}, returning a empty list of ${entity.name}")
            steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class and return an empty list of ${entity.name}")
            steps.add("Verify the returned list of ${entity.name} is empty")
            steps.add("Verify mocks")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
    }


    private fun checkDBCommunicationTestCases(ruleDataTest: ListDataTest, script: Script): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }

        //empty input
        val input = DataTest(HashMap<String, String>(), true, "", "")

        val entity = script.reference as Entity

        if(validDataTest != null) {
            val steps = ArrayList<String>()
            steps.add("Mock ${testArchitecture.mapMethods["retrieve all"]} method in ${testArchitecture.repositoryLayerClass} to return ${testArchitecture.dbErrorFramework} when try to retrieve ${entity.name}")
            steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass}")
            steps.add("Verify that ${testArchitecture.dbError} is ${testArchitecture.dbErrorAction}")
            steps.add("Verify mocks")

            val expectedResult = "${testArchitecture.dbError} is ${testArchitecture.dbErrorAction}"
            val testCase = TestCase(input, script, expectedResult)

            val operation = "GetAll${entity}_"
            val testName = "${baseTestName}${operation}DBCommunicationError"
            testCase.testName = testName

            testCase.steps.addAll(steps)
            return testCase
        }
        return null
    }

    override fun createTestCases(
        entityDataTest: ListDataTest,
        ruleDataTest: ListDataTest,
        script: Script
    ): List<TestCase> {
        val testCases = ArrayList<TestCase>()
        createTestCaseGetAllThreeInstances(ruleDataTest, script)?.let { testCases.add(it) }
        createTestCaseGetAllOneInstance(ruleDataTest, script)?.let { testCases.add(it) }
        createTestCaseGetAllReturnEmpty(script)?.let { testCases.add(it) }
        checkDBCommunicationTestCases(ruleDataTest, script)?.let { testCases.add(it) }
        return testCases
    }

}
