package req2test.tool.approach.filter.testcase.unit.rule.validation

import req2test.tool.approach.entity.*

class ListMockTestCaseValidationsFilterACRuleFilter (data: MutableMap<String, Any?>): BaseListMockTCValidationsACRuleFilter(data){

    override fun filterKeyInput(): String { return "ListFilterACRuleMockScript" }
    override fun filterName(): String {
        return "ListMockTestCaseValidationsFilterACRuleFilter"
    }
    override fun filterKeyOutput(): String { return "ListMockTestCaseValidationsFilterACRule" }

    override fun createTCs(ruleValidationDataTest: ListDataTest, script: Script, entity: Entity): List<TestCase> {
        val testCases = ArrayList<TestCase>()
        createTestCaseFilteredInstance(ruleValidationDataTest, script, entity)?.let { testCases.add(it) }
        return testCases
    }

    private fun createTestCaseFilteredInstance(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        var validDataTest = ruleDataTest.dataTest.filter { it.isValidInput }.first()

        val inputParamMethod = HashMap<String, Any?>()

        script.inputs.forEach { input ->
            val attrName = input.name
            inputParamMethod[attrName] = validDataTest.input?.get(attrName)
        }

        val input = DataTest(inputParamMethod, true, "", "")
        val output = validDataTest.input

        if(validDataTest != null) {
            val expectedResult =
                "Get filtered instance of ${entity.name}, as following: $output"
            val positiveTestCase = TestCase(input, script, expectedResult)
            positiveTestCase.preconditions.add("The repository must have the instances that will be retrieved as following: $output")
            positiveTestCase.preconditions.add("Use constructor with all attributes as parameters to mock ${entity.name}")
            //Use the execute verb with removing parameter to replace for the filter
            val executeVerb = testArchitecture.ruleValidationLayerMethod.split("(")
            val filterMethod =
                testArchitecture.mapMethods["filter validation"]?.let { testArchitecture.ruleValidationLayerMethod.replace(executeVerb[0], it) }
            val steps = ArrayList<String>()
            steps.add("Inform ${input.input}")
            steps.add("Mock $filterMethod method in ${testArchitecture.repositoryLayerClass}, returning a mocked instance of ${entity.name} with informed data, the same mocked in repository")
            steps.add("Call method ${testArchitecture.ruleValidationLayerMethod} in ${testArchitecture.ruleLayerClass} class and return the existing instance of ${entity.name}")
            steps.add("Verify attributes of retrieved instances of ${entity.name}")
            steps.add("Verify that $filterMethod was called")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }

}
