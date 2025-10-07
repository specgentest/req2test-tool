package req2test.tool.approach.filter.testcase.unit.rule.validation

import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.ListDataTest
import req2test.tool.approach.entity.Script
import req2test.tool.approach.entity.TestCase

class ListMockTestCaseValidationsCreateACRuleFilter (data: MutableMap<String, Any?>): BaseListMockTCValidationsACRuleFilter(data){

    override fun filterKeyInput(): String { return "ListCreateACRuleMockScript" }
    override fun filterName(): String {
        return "ListMockTestCaseValidationsCreateACRuleFilter"
    }
    override fun filterKeyOutput(): String { return "ListMockTestCaseValidationsCreateACRule" }

    override fun createTCs(ruleValidationDataTest: ListDataTest, script: Script, entity: Entity): List<TestCase> {
        val testCases = ArrayList<TestCase>()
        createTestCaseValidAndNonDuplicateData(ruleValidationDataTest, script, entity)?.let { testCases.add(it) }
        testCases.addAll(createTestCaseInvalid(ruleValidationDataTest, script, entity))
        return testCases
    }

    private fun createTestCaseValidAndNonDuplicateData(entityDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = entityDataTest.dataTest.find { it.isValidInput }

        if(validDataTest != null) {
            val expectedResult =
                "Create an instance of ${entity.name} and confirm that the ${script.location} saves the instance when all data is valid and unique"
            val positiveTestCase = TestCase(validDataTest, script, expectedResult)
            val operation = "Create${entity}_"
            val testName = "${baseTestName}${operation}ValidAndNonDuplicateData"
            positiveTestCase.testName = testName

            val output = validDataTest.input
            positiveTestCase.preconditions.add("The repository must be empty")

            val steps = ArrayList<String>()
            steps.addAll(script.inputs.map { "Inform ${it.name}" })

            val attrs = getUniqueAttributes(entity).toMutableList()
            attrs.forEach { attr ->
                steps.add("Mock ${testArchitecture.findMethod}${attr.replaceFirstChar { it.uppercase() }} in ${testArchitecture.repositoryLayerClass} to return ${testArchitecture.findMethodReturnFalse}")
            }

            var attributeDependecies = entity.attributeDependencies
            attributeDependecies.forEach { attrEntity ->
                val attr = attrEntity.lowercase()
                val data = validDataTest.input?.get(attr)
                val type = entity.attributes.find { it.name == attr }?.type?.replace("%", "")
                steps.add("Mock findById in ${type}Repository to return Optional $attr, where ${attr}: $data")
            }

            steps.add("Mock ${testArchitecture.mapMethods["create"]} method in ${testArchitecture.repositoryLayerClass}, returning a mocked instance of ${entity.name} with informed data: $output")
            steps.add("Call method ${testArchitecture.ruleValidationLayerMethod} in ${testArchitecture.ruleLayerClass} class and return a saved instance of ${entity.name}")
            steps.add("Verify attributes of created and saved instance of ${entity.name}: $output")
            steps.add("Verify mocks")

            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }

    private fun createTestCaseInvalid(entityDataTest: ListDataTest, script: Script, entity: Entity): List<TestCase> {
        val listInvalidDataTest = entityDataTest.dataTest.filter { !it.isValidInput }
        val testCases = ArrayList<TestCase>()
        listInvalidDataTest.forEach { invalidDataTest ->
            //val invalidDataTest = entityDataTest.dataTest.filter { !it.isValidInput }

            if (invalidDataTest != null) {
                val expectedResult =
                    "${script.location} return ${testArchitecture.ruleError} with the following error message: ${invalidDataTest.errorMessage}"
                val negativeTestCase = TestCase(invalidDataTest, script, expectedResult)

                val operation = "Create${entity}_"
                val testName = "${baseTestName}${operation}Invalid"
                negativeTestCase.testName = testName


                val steps = ArrayList<String>()
                steps.addAll(script.inputs.map { "Inform ${it.name}" })

                var attributeDependecies = entity.attributeDependencies
                attributeDependecies.forEach { attrEntity ->
                    val attr = attrEntity.lowercase()
                    val data = invalidDataTest.input?.get(attr)
                    val type = entity.attributes.find { it.name == attr }?.type?.replace("%", "")
                    steps.add("Mock findById in ${type}Repository to return Optional $attr, where ${attr}: $data")
                }
                
                steps.add("Call method ${testArchitecture.ruleValidationLayerMethod} in ${testArchitecture.ruleLayerClass}")
                steps.add("Verify that ${testArchitecture.ruleError} is ${testArchitecture.ruleErrorAction} with the following error message: ${invalidDataTest.errorMessage}")
                steps.add("Verify that method ${testArchitecture.mapMethods["create"]}(${entity.name}) from ${testArchitecture.repositoryLayerClass} was not called")
                steps.add("Verify that the constructor ${testArchitecture.entityConstructor} was called")

                negativeTestCase.steps.addAll(steps)
                testCases.add(negativeTestCase)
            }
        }
        return testCases
    }
}
