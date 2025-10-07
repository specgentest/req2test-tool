package req2test.tool.approach.filter.testcase.unit.rule.validation

import req2test.tool.approach.entity.*

class ListMockTestCaseValidationsPartialUpdateACRuleFilter (data: MutableMap<String, Any?>): BaseListMockTCValidationsACRuleFilter(data){

    override fun filterKeyInput(): String { return "ListPartialUpdateACRuleMockScript" }
    override fun filterName(): String {
        return "ListMockTestCaseValidationsPartialUpdateACRuleFilter"
    }
    override fun filterKeyOutput(): String { return "ListMockTestCaseValidationsPartialUpdateACRule" }

    override fun createTCs(ruleValidationDataTest: ListDataTest, script: Script, entity: Entity): List<TestCase> {
        val testCases = ArrayList<TestCase>()
        createTestCasePartialUpdateInstanceForManyAttributes(ruleValidationDataTest, script, entity)?.let { testCases.add(it) }
        testCases.addAll(createTestCasePartialUpdateInvalidData(ruleValidationDataTest, script, entity))
        return testCases
    }

    private fun createTestCasePartialUpdateInstanceForManyAttributes(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase {
        val validDataTest = ruleDataTest.dataTest.filter { it.isValidInput }

        val notGeneratedAttributes = entity.attributes.filter { !it.generatedDB and !it.generatedConstructor }

        val inputOne = HashMap<String, Any?>()
        val inputTwo = HashMap<String, Any?>()
        val inputParamMethod = LinkedHashMap<String, Any?>()

        val positiveDataOne =  validDataTest[0]
        val positiveDataTwo =  validDataTest[1]
        entity.attributes.forEach {
            inputOne[it.name] = positiveDataOne.input?.get(it.name)
            inputTwo[it.name] = positiveDataTwo.input?.get(it.name)
        }

        val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String

        inputParamMethod[idAttribute] = inputOne[idAttribute]

        var isFirstIteration = false
        notGeneratedAttributes.forEach {
            if(isFirstIteration)
                inputParamMethod[it.name] = positiveDataTwo.input?.get(it.name)
            else
                inputParamMethod[it.name] = null
            isFirstIteration = true
        }

        inputOne.forEach {
            if(inputParamMethod[it.key] != null) {
                inputTwo[it.key] = inputParamMethod[it.key]
            }
            else
                inputTwo[it.key] = inputOne[it.key]
        }
        inputTwo[idAttribute] = positiveDataOne.input?.get(idAttribute)

        val input = DataTest(inputParamMethod, true, "", "")
        val output = inputTwo

        val expectedResult = "${testArchitecture.ruleValidationLayerMethod} partial updates the ${entity.name} instance correctly, resulting in $output"

        val positiveTestCase = TestCase(input, script, expectedResult)

        val steps = ArrayList<String>()
        steps.addAll(script.inputs.map { "Inform ${it.name}" })
        steps.add("Mock ${testArchitecture.findMethod}${idAttribute.replaceFirstChar { it.uppercase() }} in ${testArchitecture.repositoryLayerClass} to return ${testArchitecture.findMethodReturnTrue} with the following data: $inputOne")
        steps.add("Mock ${testArchitecture.mapMethods["update"]} method in ${testArchitecture.repositoryLayerClass}, returning a mocked instance of ${entity.name} with following data: $output")

        steps.add("Call method ${testArchitecture.ruleValidationLayerMethod} in ${testArchitecture.ruleLayerClass} class with the following parameters: $inputParamMethod and return the existing instance: $output")
        steps.add("Verify attributes of retrieved instances of ${entity.name}")
        steps.add("Verify mocks")
        steps.add("Verify mock ${testArchitecture.findMethod}${idAttribute.replaceFirstChar { it.uppercase() }} in ${testArchitecture.repositoryLayerClass}")
        steps.add("Verify mock ${testArchitecture.mapMethods["update"]} method in ${testArchitecture.repositoryLayerClass} was called")
        steps.add("Verify that validate() method in ${entity.name} class was called")
        positiveTestCase.steps.addAll(steps)
        return positiveTestCase
    }


    private fun createTestCasePartialUpdateInvalidData(ruleDataTest: ListDataTest, script: Script, entity: Entity): List<TestCase> {
        val negativeDataTest = ruleDataTest.dataTest.filter { !it.isValidInput }
        val validDataTest = ruleDataTest.dataTest.filter { it.isValidInput }
        val testCases = ArrayList<TestCase>()

        negativeDataTest.forEach { invalidDataTest ->

            if (invalidDataTest != null) {

                val notGeneratedAttributes = entity.attributes.filter { !it.generatedDB and !it.generatedConstructor }

                val inputParamMethod = HashMap<String, Any?>()
                val inputOne = HashMap<String, Any?>()
                val inputTwo = HashMap<String, Any?>()

                val positiveDataOne =  validDataTest[0]
                val positiveDataTwo =  validDataTest[1]
                entity.attributes.forEach {
                    inputOne[it.name] = positiveDataOne.input?.get(it.name)
                    inputTwo[it.name] = positiveDataTwo.input?.get(it.name)
                }

                notGeneratedAttributes.forEach {
                    inputParamMethod[it.name] = invalidDataTest?.input?.get(it.name)
                }

                val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String

                //Instance two with the instance one id
                inputParamMethod[idAttribute] = positiveDataOne.input?.get(idAttribute)

                inputTwo[idAttribute] = positiveDataOne.input?.get(idAttribute)

                val input = DataTest(inputParamMethod, true, "", "")
                val output = inputTwo

                val expectedResult = "${script.location} does not partial update an instance of ${entity.name}. ${testArchitecture.ruleError} is ${testArchitecture.ruleErrorAction}"

                val negativeTestCase = TestCase(input, script, expectedResult)

                val steps = ArrayList<String>()
                steps.addAll(script.inputs.map { "Inform ${it.name}" })
                steps.add("Mock ${testArchitecture.findMethod}${idAttribute.replaceFirstChar { it.uppercase() }} in ${testArchitecture.repositoryLayerClass} to return ${testArchitecture.findMethodReturnFalse}")

                steps.add("Call method ${testArchitecture.ruleValidationLayerMethod} in ${testArchitecture.ruleLayerClass}")
                steps.add("Verify that ${testArchitecture.ruleError} is ${testArchitecture.ruleErrorAction} with the following error message: ${invalidDataTest?.errorMessage}")
                steps.add("Verify that  ${testArchitecture.findMethod}${idAttribute.replaceFirstChar { it.uppercase() }} method was called")
                steps.add("Verify that validate() method in ${entity.name} class was called")
                steps.add("Verify that method ${testArchitecture.mapMethods["partial update"]}(${entity.name}) from ${testArchitecture.repositoryLayerClass} was not called")
                negativeTestCase.steps.addAll(steps)
                testCases.add(negativeTestCase)
            }
        }
        return testCases
    }
}
