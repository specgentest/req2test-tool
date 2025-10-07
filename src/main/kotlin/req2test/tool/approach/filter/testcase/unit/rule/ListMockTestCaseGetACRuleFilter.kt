package req2test.tool.approach.filter.testcase.unit.rule

import req2test.tool.approach.entity.*

class ListMockTestCaseGetACRuleFilter (data: MutableMap<String, Any?>): BaseListMockTCACRuleFilter(data){

    override fun filterKeyInput(): String { return "ListGetACRuleMockScript" }
    override fun filterName(): String { return "ListMockTestCaseGetACRuleFilter" }
    override fun filterKeyOutput(): String { return "ListMockTestCaseGetACRule" }

    private fun createTestCaseValid(ruleDataTest: ListDataTest, script: Script): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }
        val entity = script.reference as Entity

        val input = DataTest(validDataTest?.input?.filter { (key, value) -> key.contains("id")  }, true, "", "")
        val output = validDataTest?.input

        if(validDataTest != null) {
            val expectedResult =
                "Get an existing instance of ${entity.name} with the following attributes: $output"
            val positiveTestCase = TestCase(input, script, expectedResult)

            val operation = "Get${entity}_"
            val testName = "${baseTestName}${operation}ValidIDSuccessfully"
            positiveTestCase.testName = testName


            positiveTestCase.preconditions.add("The repository must have the instance that will be retrieved with the following attributes: $output")
            positiveTestCase.preconditions.add("The following data: ${validDataTest.input}")

            val steps = ArrayList<String>()
            steps.add("Inform ${input.input}")

            var method = getReplacedMethod(testArchitecture.mapMethods["retrieve"].toString())

            steps.add("Mock $method method in ${testArchitecture.repositoryLayerClass}, returning a mocked instance of ${entity.name} with informed data, the same mocked in repository")
            steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class and return an existing instance of ${entity.name}")
            steps.add("Verify attributes of retrieved of ${entity.name}")
            steps.add("Verify mocks")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }

    //Replace Id for urlinput. Ex: findById -> findByNameAndAge
    fun getReplacedMethod(initMethod: String): String {
        var method = initMethod
        if (testArchitecture.urlinput != null) {
            val inputParameters = testArchitecture.urlinput!!.map { formatParamNameWithPrefix(it.name) }
            method = method.replace(
                "Id",
                inputParameters.joinToString("And")
            )
        }
        return method
    }

    // Converte "employee.fullName" -> "EmployeeFullName"
    fun formatParamNameWithPrefix(name: String): String {
        return name.split(".").joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
    }


    private fun createTestCaseInstanceNotFound(ruleDataTest: ListDataTest, script: Script): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }
        val entity = script.reference as Entity

        val input = DataTest(validDataTest?.input?.filter { (key, value) -> key.contains("id")  }, true, "", "")
        //val output = "${testArchitecture.ruleError} is ${testArchitecture.ruleErrorAction}"

        if(validDataTest != null) {

            val expectedResult = "${script.location} does not get an instance of ${entity.name}. ${testArchitecture.ruleError} is ${testArchitecture.ruleErrorAction}"

            //val expectedResult = "Get an existing instance of ${entity.name} with the following attributes: $output"
            val instanceNotFoundTestCase = TestCase(input, script, expectedResult)

            val operation = "Get${entity}_"
            val testName = "${baseTestName}${operation}InstanceNotFound"
            instanceNotFoundTestCase.testName = testName

            instanceNotFoundTestCase.preconditions.add("The repository must have no instance and It must return ${testArchitecture.findMethodReturnFalse}")

            val errorMessage = "${entity.name} not found"
            val steps = ArrayList<String>()
            steps.add("Inform ${input.input}")

            var method = getReplacedMethod(testArchitecture.mapMethods["retrieve"].toString())

            steps.add("Mock $method method in ${testArchitecture.repositoryLayerClass}, according to preconditions")
            steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass}")
            steps.add("Verify that ${testArchitecture.ruleError} is ${testArchitecture.ruleErrorAction} with the following error message: $errorMessage")

            steps.add("Verify mocks")
            instanceNotFoundTestCase.steps.addAll(steps)
            return instanceNotFoundTestCase
        }
        return null
    }

    private fun checkDBCommunicationTestCases(ruleDataTest: ListDataTest, script: Script): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }

        val input = DataTest(validDataTest?.input?.filter { (key, value) -> key.contains("id")  }, true, "", "")

        val entity = script.reference as Entity

        if(validDataTest != null) {
            val steps = ArrayList<String>()
            steps.add("Inform ${input.input}")

            var method = getReplacedMethod(testArchitecture.mapMethods["retrieve"].toString())

            steps.add("Mock $method method in ${testArchitecture.repositoryLayerClass} to return ${testArchitecture.dbErrorFramework} when try to retrieve ${entity.name}")
            steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass}")
            steps.add("Verify that ${testArchitecture.dbError} is ${testArchitecture.dbErrorAction}")
            steps.add("Verify mocks")

            val expectedResult = "${testArchitecture.dbError} is ${testArchitecture.dbErrorAction}"
            val testCase = TestCase(input, script, expectedResult)

            val operation = "Get${entity}_"
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
        createTestCaseValid(ruleDataTest, script)?.let { testCases.add(it) }
        createTestCaseInstanceNotFound(ruleDataTest, script)?.let { testCases.add(it) }
        checkDBCommunicationTestCases(ruleDataTest, script)?.let { testCases.add(it) }
        return testCases
    }

}
