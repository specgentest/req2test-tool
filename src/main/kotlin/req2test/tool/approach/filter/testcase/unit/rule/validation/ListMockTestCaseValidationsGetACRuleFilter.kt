package req2test.tool.approach.filter.testcase.unit.rule.validation

import req2test.tool.approach.entity.*

class ListMockTestCaseValidationsGetACRuleFilter (data: MutableMap<String, Any?>): BaseListMockTCValidationsACRuleFilter(data){

    override fun filterKeyInput(): String { return "ListGetACRuleMockScript" }
    override fun filterName(): String {
        return "ListMockTestCaseValidationsGetACRuleFilter"
    }
    override fun filterKeyOutput(): String { return "ListMockTestCaseValidationsGetACRule" }

    override fun createTCs(ruleValidationDataTest: ListDataTest, script: Script, entity: Entity): List<TestCase> {
        val testCases = ArrayList<TestCase>()
        createTestCaseValid(ruleValidationDataTest, script, entity)?.let { testCases.add(it) }
        testCases.addAll(createTestCaseInstanceNotFound(ruleValidationDataTest, script, entity))
        return testCases
    }

    private fun createTestCaseValid(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = ruleDataTest.dataTest.first()

        val input = DataTest(validDataTest?.input?.filter { (key, value) -> key.contains("id")  }, true, "", "")
        val output = validDataTest.input

        if(validDataTest != null) {
            val expectedResult =
                "Get an existing instance of ${entity.name} with the following attributes: $output"
            val positiveTestCase = TestCase(input, script, expectedResult)
            positiveTestCase.preconditions.add("The repository must have the instance that will be retrieved with the following attributes: $output")
            positiveTestCase.preconditions.add("The following data: ${validDataTest.input}")

            val steps = ArrayList<String>()
            steps.add("Inform ${input.input}")

            steps.add("Mock ${testArchitecture.mapMethods["retrieve"]} method in ${testArchitecture.repositoryLayerClass}, returning a mocked instance of ${entity.name} with informed data, the same mocked in repository")
            steps.add("Call method ${testArchitecture.ruleValidationLayerMethod} in ${testArchitecture.ruleLayerClass} class and return an existing instance of ${entity.name}")
            steps.add("Verify attributes of retrieved of ${entity.name}")
            steps.add("Verify mocks")
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

                val expectedResult = "${script.location} does not get an instance of ${entity.name}. ${testArchitecture.ruleError} is ${testArchitecture.ruleErrorAction}"
                val instanceNotFoundTestCase = TestCase(input, script, expectedResult)
                instanceNotFoundTestCase.preconditions.add("The repository must have only the instance ${invalidDataTest.input} and It must return ${testArchitecture.findMethodReturnFalse}")

                val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String
                val methodExist = "${testArchitecture.findMethod}${idAttribute.replaceFirstChar { it.uppercase() }}"

                val steps = ArrayList<String>()
                steps.add("Inform ${input.input}")
                steps.add("Mock $methodExist in ${testArchitecture.repositoryLayerClass} to return ${testArchitecture.findMethodReturnTrue} with the following data: ${invalidDataTest.input}")
                steps.add("Mock ${testArchitecture.mapMethods["retrieve"]} method in ${testArchitecture.repositoryLayerClass}, according to preconditions")
                steps.add("Call method ${testArchitecture.ruleValidationLayerMethod} in ${testArchitecture.ruleLayerClass}")
                steps.add("Verify that ${testArchitecture.ruleError} is ${testArchitecture.ruleErrorAction} with the following error message: instance exists but ${invalidDataTest.errorMessage}")
                steps.add("Verify that $methodExist was called")

                steps.add("Verify mocks")
                instanceNotFoundTestCase.steps.addAll(steps)
                testCases.add(instanceNotFoundTestCase)
            }
        }
        return testCases
    }
}
