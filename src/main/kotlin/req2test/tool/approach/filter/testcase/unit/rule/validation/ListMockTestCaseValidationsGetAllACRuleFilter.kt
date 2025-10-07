package req2test.tool.approach.filter.testcase.unit.rule.validation

import req2test.tool.approach.entity.*

class ListMockTestCaseValidationsGetAllACRuleFilter (data: MutableMap<String, Any?>): BaseListMockTCValidationsACRuleFilter(data){

    override fun filterKeyInput(): String { return "ListGetAllACRuleMockScript" }
    override fun filterName(): String {
        return "ListMockTestCaseValidationsGetAllACRuleFilter"
    }
    override fun filterKeyOutput(): String { return "ListMockTestCaseValidationsGetAllACRule" }

    override fun createTCs(ruleValidationDataTest: ListDataTest, script: Script, entity: Entity): List<TestCase> {
        val testCases = ArrayList<TestCase>()
        createTestCaseGetAllThreeValidInstances(ruleValidationDataTest, script, entity)?.let { testCases.add(it) }
        return testCases
    }

    //TC - valida os dois cenários, pois se resolver chamar o findAll dentro de findAllWithExtraValidation e filtrar no UC as condicoes abaixo vao validar
    private fun createTestCaseGetAllThreeValidInstances(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val dataTest = ruleDataTest

        val input = DataTest(HashMap<String, String>(), true, "", "")
        val output = ruleDataTest.dataTest.filter { it.isValidInput }

        if(dataTest != null) {
            val expectedResult =
                "Get the three existing instances of ${entity.name}, as following: $output"
            val positiveTestCase = TestCase(input, script, expectedResult)
            positiveTestCase.preconditions.add("The repository must have the instances as following: $data")

            val steps = ArrayList<String>()
            steps.add("Mock ${testArchitecture.mapMethods["retrieve all validation"]} method in ${testArchitecture.repositoryLayerClass}, returning a mocked instances of ${entity.name} with informed data, the same mocked in repository")
            steps.add("Call method ${testArchitecture.ruleValidationLayerMethod} in ${testArchitecture.ruleLayerClass} class and return the existing instances of ${entity.name}")
            steps.add("Verify attributes of retrieved instances of ${entity.name}")
            steps.add("Verify that list returned should have exactly $output")
            steps.add("Verify that there are 3 elements in the list returned")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }
}
