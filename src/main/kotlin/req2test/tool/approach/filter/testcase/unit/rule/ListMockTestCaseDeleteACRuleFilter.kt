package req2test.tool.approach.filter.testcase.unit.rule

import req2test.tool.approach.entity.*

class ListMockTestCaseDeleteACRuleFilter (data: MutableMap<String, Any?>): BaseListMockTCACRuleFilter(data){

    override fun filterKeyInput(): String { return "ListDeleteACRuleMockScript" }
    override fun filterName(): String { return "ListMockTestCaseDeleteACRuleFilter" }
    override fun filterKeyOutput(): String { return "ListMockTestCaseDeleteACRule" }


    private fun createDeleteTestCaseValid(ruleDataTest: ListDataTest, script: Script): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }
        val entity = script.reference as Entity

        val input = DataTest(validDataTest?.input?.filter { (key, value) -> key.contains("id")  }, true, "", "")
        val output = validDataTest?.input

        if(validDataTest != null) {
            val expectedResult =
                "Delete an existing instance of ${entity.name}"
            val positiveTestCase = TestCase(input, script, expectedResult)
            val operation = "Delete${entity}_"
            val testName = "${baseTestName}${operation}ValidData"
            positiveTestCase.testName = testName

            val steps = ArrayList<String>()
            steps.add("Inform ${input.input}")

            val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String
            val methodExist = "${testArchitecture.findMethod}${idAttribute.replaceFirstChar { it.uppercase() }}"
            steps.add("Mock $methodExist in ${testArchitecture.repositoryLayerClass} to return true")

            //steps.add("Mock ${testArchitecture.mapMethods["delete"]} method in ${testArchitecture.repositoryLayerClass}, returning a mocked instance of ${entity.name} with informed data, the same mocked in repository")
            steps.add("Mock ${testArchitecture.mapMethods["delete"]} method in ${testArchitecture.repositoryLayerClass}, returning void")
            steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class. This is a void method")
            steps.add("Verify mock ${testArchitecture.mapMethods["delete"]} method in ${testArchitecture.repositoryLayerClass} was called")
            steps.add("Verify that $methodExist in ${testArchitecture.repositoryLayerClass} was called")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }

    private fun createTestCaseInstanceNotFound(ruleDataTest: ListDataTest, script: Script): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }
        val entity = script.reference as Entity

        val input = DataTest(validDataTest?.input?.filter { (key, value) -> key.contains("id")  }, true, "", "")

        if(validDataTest != null) {

            val expectedResult = "${script.location} does not find the instance of ${entity.name}. ${testArchitecture.ruleError} is ${testArchitecture.ruleErrorAction}"

            val instanceNotFoundTestCase = TestCase(input, script, expectedResult)
            instanceNotFoundTestCase.preconditions.add("The repository must have no instance and It must return ${testArchitecture.findMethodReturnFalse}")

            val operation = "Delete${entity}_"
            val testName = "${baseTestName}${operation}InstanceNotFound"
            instanceNotFoundTestCase.testName = testName

            val errorMessage = "${entity.name} not found"

            val steps = ArrayList<String>()
            steps.add("Inform ${input.input}")

            val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String
            val methodExist = "${testArchitecture.findMethod}${idAttribute.replaceFirstChar { it.uppercase() }}"
            steps.add("Mock $methodExist in ${testArchitecture.repositoryLayerClass} to return false")

            steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass}")
            steps.add("Verify that ${testArchitecture.ruleError} is ${testArchitecture.ruleErrorAction} with the following error message: $errorMessage")
            steps.add("Verify mock ${testArchitecture.mapMethods["delete"]} method in ${testArchitecture.repositoryLayerClass} was not called")
            steps.add("Verify that $methodExist in ${testArchitecture.repositoryLayerClass} was called")
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

            val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String
            val methodExist = "${testArchitecture.findMethod}${idAttribute.replaceFirstChar { it.uppercase() }}"

            val steps = ArrayList<String>()
            steps.add("Inform ${input.input}")
            steps.add("Mock ${methodExist} method in ${testArchitecture.repositoryLayerClass} to return ${testArchitecture.dbErrorFramework}")
            steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass}")
            steps.add("Verify that ${testArchitecture.dbError} is ${testArchitecture.dbErrorAction}")
            steps.add("Verify mocks")

            val expectedResult = "${testArchitecture.dbError} is ${testArchitecture.dbErrorAction}"
            val testCase = TestCase(input, script, expectedResult)

            val operation = "Delete${entity}_"
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
        createDeleteTestCaseValid(ruleDataTest, script)?.let { testCases.add(it) }
        createTestCaseInstanceNotFound(ruleDataTest, script)?.let { testCases.add(it) }
        checkDBCommunicationTestCases(ruleDataTest, script)?.let { testCases.add(it) }
        return testCases
    }

}
