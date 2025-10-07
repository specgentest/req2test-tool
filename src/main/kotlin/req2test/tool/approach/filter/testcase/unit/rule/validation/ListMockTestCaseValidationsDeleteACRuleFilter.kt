package req2test.tool.approach.filter.testcase.unit.rule.validation

import req2test.tool.approach.entity.*

class ListMockTestCaseValidationsDeleteACRuleFilter (data: MutableMap<String, Any?>): BaseListMockTCValidationsACRuleFilter(data){

    override fun filterKeyInput(): String { return "ListDeleteACRuleMockScript" }
    override fun filterName(): String {
        return "ListMockTestCaseValidationsDeleteACRuleFilter"
    }
    override fun filterKeyOutput(): String { return "ListMockTestCaseValidationsDeleteACRule" }

    override fun createTCs(ruleValidationDataTest: ListDataTest, script: Script, entity: Entity): List<TestCase> {
        val testCases = ArrayList<TestCase>()
        createDeleteTestCaseValid(ruleValidationDataTest, script, entity)?.let { testCases.add(it) }
        testCases.addAll(createTestCaseInstanceNotFound(ruleValidationDataTest, script, entity))
        return testCases
    }

    private fun createDeleteTestCaseValid(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = ruleDataTest.dataTest.first()

        val input = DataTest(validDataTest?.input?.filter { (key, value) -> key.contains("id")  }, true, "", "")
        val output = validDataTest.input


        if(validDataTest != null) {
            val expectedResult =
                "Delete an existing instance of ${entity.name}"
            val positiveTestCase = TestCase(input, script, expectedResult)

            val steps = ArrayList<String>()
            steps.add("Inform ${input.input}")

            val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String
            val methodExist = "${testArchitecture.findMethod}${idAttribute.replaceFirstChar { it.uppercase() }}"
            steps.add("Mock $methodExist in ${testArchitecture.repositoryLayerClass} to return true")

            steps.add("Mock ${testArchitecture.mapMethods["delete"]} method in ${testArchitecture.repositoryLayerClass}, returning a mocked instance of ${entity.name} with informed data, the same mocked in repository")
            steps.add("Call method ${testArchitecture.ruleValidationLayerMethod} in ${testArchitecture.ruleLayerClass} class. This is a void method")
            steps.add("Verify mock ${testArchitecture.mapMethods["delete"]} method in ${testArchitecture.repositoryLayerClass} was called")
            steps.add("Verify that $methodExist in ${testArchitecture.repositoryLayerClass} was called")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }


    private fun createTestCaseInstanceNotFound(ruleDataTest: ListDataTest, script: Script, entity: Entity): List<TestCase> {
        val negativeDataTest = ruleDataTest.dataTest.filter { !it.isValidInput }
        val testCases = ArrayList<TestCase>()

        negativeDataTest.forEach { invalidDataTest ->

            val input = DataTest(invalidDataTest?.input?.filter { (key, value) -> key.contains("id") }, true, "", "")

            if(invalidDataTest != null) {

                val expectedResult = "${script.location} does not delete an instance of ${entity.name}. ${testArchitecture.ruleError} is ${testArchitecture.ruleErrorAction}"

                val instanceNotFoundTestCase = TestCase(input, script, expectedResult)
                instanceNotFoundTestCase.preconditions.add("The repository must have only the instance ${invalidDataTest.input} and It must return ${testArchitecture.findMethodReturnFalse}")

                val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String
                val methodExist = "${testArchitecture.findMethod}${idAttribute.replaceFirstChar { it.uppercase() }}"

                val steps = ArrayList<String>()
                steps.add("Inform ${input.input}")
                steps.add("Mock $methodExist in ${testArchitecture.repositoryLayerClass} to return true")
                steps.add("Call method ${testArchitecture.ruleValidationLayerMethod} in ${testArchitecture.ruleLayerClass}")
                steps.add("Verify that ${testArchitecture.ruleError} is ${testArchitecture.ruleErrorAction} with the following error message: instance exists but ${invalidDataTest.errorMessage}")
                steps.add("Verify that $methodExist was called")
                steps.add("Verify that ${testArchitecture.mapMethods["delete"]} was not called")

                steps.add("Verify mocks")
                instanceNotFoundTestCase.steps.addAll(steps)
                testCases.add(instanceNotFoundTestCase)
            }
        }
        return testCases
    }
}
