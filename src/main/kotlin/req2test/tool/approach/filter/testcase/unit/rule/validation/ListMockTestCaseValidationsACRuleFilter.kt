package req2test.tool.approach.filter.testcase.unit.rule.validation

import req2test.tool.approach.core.Filter
import req2test.tool.approach.core.Log
import req2test.tool.approach.core.TestArchitecture
import req2test.tool.approach.processor.generator.file.TestPlanGenerator
import req2test.tool.approach.entity.*

class ListMockTestCaseValidationsACRuleFilter (data: MutableMap<String, Any?>): Filter(data, null){

    lateinit var testArchitecture: TestArchitecture

    override fun filterKeyInput(): String { return "ListCreateACRuleMockScript" }
    override fun filterName(): String {

        return "ListMockTestCaseCreateACRuleFilter"
    }
    override fun filterKeyOutput(): String { return "ListMockTestCaseCreateACRule" }

    private fun getUniqueAttributes(entity: Entity): List<String> {
        return entity.attributes.filter { it.dbValidations.contains("unique") }.map { it.name }
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

            positiveTestCase.preconditions.add("The repository must be empty")

            val steps = ArrayList<String>()
            steps.addAll(script.inputs.map { "Inform ${it.name}" })

            val attrs = getUniqueAttributes(entity).toMutableList()
            attrs.forEach { attr ->
                steps.add("Mock ${testArchitecture.findMethod}${attr.replaceFirstChar { it.uppercase() }} in ${testArchitecture.repositoryLayerClass} to return ${testArchitecture.findMethodReturnFalse}")
            }

            steps.add("Mock ${testArchitecture.mapMethods["create"]} method in ${testArchitecture.repositoryLayerClass}, returning a mocked instance of ${entity.name} with informed data")
            steps.add("Call method ${testArchitecture.ruleValidationLayerMethod} in ${testArchitecture.ruleLayerClass} class and return a saved instance of ${entity.name}")
            steps.add("Verify attributes of created and saved instance of ${entity.name}")
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
                val testName = "${baseTestName}${operation}InvalidID"
                negativeTestCase.testName = testName

                val steps = ArrayList<String>()
                steps.addAll(script.inputs.map { "Inform ${it.name}" })

                steps.add("Mock the constructor ${testArchitecture.entityConstructor} to return ${testArchitecture.ruleErrorAction} ${testArchitecture.ruleError} with the following error message: ${invalidDataTest.errorMessage}")
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


    override fun compute(input: Any): Any? {

        var scripts = input as List<Script>
        scripts = scripts.filter { it.reference is Rule }

        scripts.forEach { script ->

            testArchitecture = script.testArchitecture!!

            val mapListRuleValidationDataTest = data["MapListRuleValidationDataTest"] as Map<Rule, List<ListDataTest>>

            val ruleReference = script.reference as Rule

            val listEntities = data["ListEntities"] as List<Entity>
            val entity = listEntities.find { it.name == ruleReference.reference } as Entity


            //Data from RuleValidationData
            val listRuleValidationDataTest =  mapListRuleValidationDataTest[ruleReference]

            if (listRuleValidationDataTest == null)
                logs.add(
                    Log.createCriticalError(
                        "ListMockTestCaseCreateACRuleFilter",
                        "ListRuleValidationDataTest not found for ${ruleReference.raw}"
                    )
                )

            else {
                listRuleValidationDataTest.forEach {
                    ruleValidationDataTest ->
                    val positiveTestCase = createTestCaseValidAndNonDuplicateData(ruleValidationDataTest, script, entity)
                    if (positiveTestCase != null)
                        script.testCases.add(positiveTestCase)
                    createTestCaseInvalid(ruleValidationDataTest, script, entity).let { script.testCases.addAll(it) }
                }
            }
        }

            //Exportando dados para .txt
            scripts.forEach { entityScript ->
                var textTestCases = ""
                var ctCount = 1
                entityScript.testCases.forEach {tc ->
                    var tcDescription = tc.expectedResult

                    textTestCases += "TC-${ctCount++}\t\t\t\t\t\t| ${tcDescription}\n"
                    textTestCases += "preconditions\t\t\t\t| ${tc.preconditions}\n"
                    textTestCases += "Input\t\t\t\t\t\t| ${tc.dataTest.input?.map { "\n\t\t\t\t\t\t\t${it.key}: ${it.value} " }}\n"
                    textTestCases += "Steps\t\t\t\t\t\t| ${tc.steps.map { "\n\t\t\t\t\t\t\t$it" }}\n"
                    textTestCases += "Expected Result\t\t\t\t| ${tc.expectedResult}\n\n"
                }

                val rule = entityScript.reference as Rule
                TestPlanGenerator.createFile(textTestCases , "outputArtifacts/", "Create${rule.reference}ValidationTestCases.txt")
            }
        return scripts
    }
}
