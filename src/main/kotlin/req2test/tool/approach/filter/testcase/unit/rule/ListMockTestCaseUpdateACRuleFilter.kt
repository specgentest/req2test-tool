package req2test.tool.approach.filter.testcase.unit.rule

import req2test.tool.approach.entity.*

class ListMockTestCaseUpdateACRuleFilter (data: MutableMap<String, Any?>): BaseListMockTCACRuleFilter(data){

    override fun filterKeyInput(): String { return "ListUpdateACRuleMockScript" }
    override fun filterName(): String { return "ListMockTestCaseUpdateACRuleFilter" }
    override fun filterKeyOutput(): String { return "ListMockTestCaseUpdateACRule" }

    private fun createTestCaseUpdateInstance(ruleDataTest: ListDataTest, script: Script): TestCase {
        val validDataTest = ruleDataTest.dataTest.filter { it.isValidInput }
        val entity = script.reference as Entity

        val notGeneratedAttributes = entity.attributes.filter { !it.generatedDB and !it.generatedConstructor }

        val inputOne = HashMap<String, Any?>()
        val inputTwo = HashMap<String, Any?>()
        val inputParamMethod = HashMap<String, Any?>()

        val positiveDataOne =  validDataTest[0]
        val positiveDataTwo =  validDataTest[1]
        entity.attributes.forEach {
            inputOne[it.name] = positiveDataOne.input?.get(it.name)
            inputTwo[it.name] = positiveDataTwo.input?.get(it.name)
        }

        notGeneratedAttributes.forEach {
            inputParamMethod[it.name] = positiveDataTwo.input?.get(it.name)
        }

        val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String

        inputParamMethod[idAttribute] = positiveDataOne.input?.get(idAttribute)
        //Instance two with the instance one id
        inputTwo[idAttribute] = positiveDataOne.input?.get(idAttribute)

        val input = DataTest(inputParamMethod, true, "", "")
        val output = inputTwo

        val expectedResult = "${testArchitecture.ruleLayerMethod} updates the ${entity.name} instance correctly, resulting in $output"

        val positiveTestCase = TestCase(input, script, expectedResult)
        val operation = "Update${entity}_"
        val testName = "${baseTestName}${operation}Successfully"
        positiveTestCase.testName = testName

        val steps = ArrayList<String>()
        steps.addAll(script.inputs.map { "Inform ${it.name}" })
        steps.add("Mock findById in ${testArchitecture.repositoryLayerClass} to return ${entity.name} with the following data: $inputOne")

        var attributeDependecies = entity.attributeDependencies
        attributeDependecies.forEach { attrEntity ->
            val attr = attrEntity.lowercase()
            val data = positiveDataTwo.input?.get(attr)
            val type = entity.attributes.find { it.name == attr }?.type?.replace("%", "")
            steps.add("Mock findById in ${type}Repository to return Optional $attr, where ${attr}: $data")
        }

        steps.add("Mock ${testArchitecture.mapMethods["update"]} method in ${testArchitecture.repositoryLayerClass}, returning a mocked instance of ${entity.name} with following data: $output")

        steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class and return the existing instance: $output")
        steps.add("Verify attributes of retrieved instances of ${entity.name}")
        steps.add("Verify mocks")
        steps.add("Verify mock findById in ${testArchitecture.repositoryLayerClass}")
        steps.add("Verify mock ${testArchitecture.mapMethods["update"]} method in ${testArchitecture.repositoryLayerClass} was called")
        positiveTestCase.steps.addAll(steps)
        return positiveTestCase
    }

    private fun createTestCaseUpdateInvalidData(ruleDataTest: ListDataTest, negativeDataTest: DataTest?, script: Script): TestCase {
        val validDataTest = ruleDataTest.dataTest.filter { it.isValidInput }
        val entity = script.reference as Entity

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
            inputParamMethod[it.name] = negativeDataTest?.input?.get(it.name)
        }

        val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String

        inputParamMethod[idAttribute] = positiveDataOne.input?.get(idAttribute)
        //Instance two with the instance one id
        inputTwo[idAttribute] = positiveDataOne.input?.get(idAttribute)

        val input = DataTest(inputParamMethod, true, "", "")
        val output = inputTwo

        val expectedResult = "${script.location} does not update an instance of ${entity.name}. ${testArchitecture.ruleError} is ${testArchitecture.ruleErrorAction}"

        val positiveTestCase = TestCase(input, script, expectedResult)
        val operation = "Update${entity}_"
        val testName = "${baseTestName}${operation}InvalidData"
        positiveTestCase.testName = testName

        val steps = ArrayList<String>()
        steps.addAll(script.inputs.map { "Inform ${it.name}" })
        steps.add("Mock findById in ${testArchitecture.repositoryLayerClass} to return ${entity.name} with the following data: $inputOne")

        var attributeDependecies = entity.attributeDependencies
        attributeDependecies.forEach { attrEntity ->
            val attr = attrEntity.lowercase()
            val data = negativeDataTest?.input?.get(attr)
            val type = entity.attributes.find { it.name == attr }?.type?.replace("%", "")
            steps.add("Mock findById in ${type}Repository to return Optional $attr, where ${attr}: $data")
        }

        steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass}")
        steps.add("Verify that ${testArchitecture.ruleError} is ${testArchitecture.ruleErrorAction} with the following error message: ${negativeDataTest?.errorMessage}")
        steps.add("Verify mock findById in ${testArchitecture.repositoryLayerClass}")
        steps.add("Verify that method ${testArchitecture.mapMethods["update"]}(${entity.name}) from ${testArchitecture.repositoryLayerClass} was not called")
        positiveTestCase.steps.addAll(steps)
        return positiveTestCase
    }

    private fun createTestCaseUpdateInvalidId(ruleDataTest: ListDataTest, script: Script): TestCase {
        val validDataTest = ruleDataTest.dataTest.filter { it.isValidInput }
        val entity = script.reference as Entity

        val notGeneratedAttributes = entity.attributes.filter { !it.generatedDB and !it.generatedConstructor }

        val inputTwo = HashMap<String, Any?>()
        val inputParamMethod = HashMap<String, Any?>()

        val positiveDataOne =  validDataTest[0]
        val positiveDataTwo =  validDataTest[1]
        entity.attributes.forEach {
            inputTwo[it.name] = positiveDataTwo.input?.get(it.name)
        }

        notGeneratedAttributes.forEach {
            inputParamMethod[it.name] = positiveDataTwo.input?.get(it.name)
        }

        val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String

        inputParamMethod[idAttribute] = positiveDataOne.input?.get(idAttribute)
        //Instance two with the instance one id
        inputTwo[idAttribute] = positiveDataOne.input?.get(idAttribute)

        val input = DataTest(inputParamMethod, true, "", "")

        val expectedResult = "${script.location} does not update an instance of ${entity.name}. ${testArchitecture.ruleError} is ${testArchitecture.ruleErrorAction}"

        val positiveTestCase = TestCase(input, script, expectedResult)
        val operation = "Update${entity}_"
        val testName = "${baseTestName}${operation}InstanceNotFound"
        positiveTestCase.testName = testName

        val errorMessage = "${entity.name} not found"
        val steps = ArrayList<String>()
        steps.addAll(script.inputs.map { "Inform ${it.name}" })
        steps.add("Mock findById in ${testArchitecture.repositoryLayerClass} to return Optional empty")

        steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass}")
        steps.add("Verify mock findById in ${testArchitecture.repositoryLayerClass}")
        steps.add("Verify that method ${testArchitecture.mapMethods["update"]}(${entity.name}) from ${testArchitecture.repositoryLayerClass} was not called")
        positiveTestCase.steps.addAll(steps)
        return positiveTestCase
    }

    private fun checkDBCommunicationTestCases(ruleDataTest: ListDataTest, script: Script): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }

        //empty input
        val input = DataTest(HashMap<String, String>(), true, "", "")

        val entity = script.reference as Entity

        if(validDataTest != null) {

            val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String

            val steps = ArrayList<String>()
            steps.addAll(script.inputs.map { "Inform ${it.name}" })
            steps.add("Mock findById method in ${testArchitecture.repositoryLayerClass} to return ${testArchitecture.dbErrorFramework}")
            steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass}")
            steps.add("Verify that ${testArchitecture.dbError} is ${testArchitecture.dbErrorAction}")
            steps.add("Verify that method ${testArchitecture.mapMethods["update"]}(${entity.name}) from ${testArchitecture.repositoryLayerClass} was not called")
            steps.add("Verify mocks")

            val expectedResult = "${testArchitecture.dbError} is ${testArchitecture.dbErrorAction}"
            val testCase = TestCase(input, script, expectedResult)
            val operation = "Update${entity}_"
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
        createTestCaseUpdateInstance(ruleDataTest, script).let { testCases.add(it) }
        createTestCaseUpdateInvalidData(ruleDataTest, entityDataTest?.dataTest?.find { !it.isValidInput }, script).let { testCases.add(it) }
        createTestCaseUpdateInvalidId(ruleDataTest, script).let { testCases.add(it) }
        checkDBCommunicationTestCases(ruleDataTest, script)?.let { testCases.add(it) }
        return testCases
    }

}
