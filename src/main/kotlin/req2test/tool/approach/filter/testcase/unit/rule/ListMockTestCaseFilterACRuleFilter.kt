package req2test.tool.approach.filter.testcase.unit.rule

import req2test.tool.approach.entity.*

class ListMockTestCaseFilterACRuleFilter (data: MutableMap<String, Any?>): BaseListMockTCACRuleFilter(data){

    override fun filterKeyInput(): String { return "ListFilterACRuleMockScript" }
    override fun filterName(): String { return "ListMockTestCaseFilterACRuleFilter" }
    override fun filterKeyOutput(): String { return "ListMockTestCaseFilterACRule" }


    private fun createTestCaseFilteredInstance(ruleDataTest: ListDataTest, script: Script): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }
        val entity = script.reference as Entity

        val inputParamMethod = HashMap<String, Any?>()

        script.inputs.forEach { input ->
            val attrName = input.name
            inputParamMethod[attrName] = validDataTest?.input?.get(attrName)
        }

        val input = DataTest(inputParamMethod, true, "", "")
        val output = validDataTest?.input

        if(validDataTest != null) {
            val expectedResult =
                "Get filtered instances of ${entity.name}, as following: $output"
            val positiveTestCase = TestCase(input, script, expectedResult)

            val operation = "Filter${entity}_"
            val testName = "${baseTestName}${operation}AllFiltersFilled"
            positiveTestCase.testName = testName

            positiveTestCase.preconditions.add("The repository must have the instances that will be retrieved as following: $output")
            positiveTestCase.preconditions.add("Use constructor with all attributes as parameters to mock ${entity.name}")
            val executeVerb = testArchitecture.ruleLayerMethod.split("(")
            val filterMethod =
                testArchitecture.mapMethods["filter"]?.let { testArchitecture.ruleLayerMethod.replace(executeVerb[0], it) }
            val steps = ArrayList<String>()
            steps.add("Inform ${input.input}")
            steps.add("Mock $filterMethod method in ${testArchitecture.repositoryLayerClass}, returning a mocked instance of ${entity.name} with informed data, the same mocked in repository")
            steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class and return the existing instance of ${entity.name}")
            steps.add("Verify attributes of retrieved instances of ${entity.name}")
            steps.add("Verify that $filterMethod was called")
            //steps.add("Verify mocks")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }

    private fun createTestCaseOneFilterFilledInstance(ruleDataTest: ListDataTest, script: Script): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }
        val entity = script.reference as Entity

        val inputParamMethod = HashMap<String, Any?>()

        var isFirstAttribute = true
        script.inputs.forEach { input ->
            val attrName = input.name
            if(isFirstAttribute) {
                inputParamMethod[attrName] = validDataTest?.input?.get(attrName)
                isFirstAttribute = false
            }
            else
                inputParamMethod[attrName] = null
        }

        val input = DataTest(inputParamMethod, true, "", "")
        val output = validDataTest?.input

        if(validDataTest != null) {
            val expectedResult =
                "Get filtered instances of ${entity.name}, as following: $output"
            val positiveTestCase = TestCase(input, script, expectedResult)

            val operation = "Filter${entity}_"
            val testName = "${baseTestName}${operation}OneFilterFilled"
            positiveTestCase.testName = testName

            positiveTestCase.preconditions.add("The repository must have the instances that will be retrieved as following: $output")
            positiveTestCase.preconditions.add("Use constructor with all attributes as parameters to mock ${entity.name}")
            val executeVerb = testArchitecture.ruleLayerMethod.split("(")
            val filterMethod =
                testArchitecture.mapMethods["filter"]?.let { testArchitecture.ruleLayerMethod.replace(executeVerb[0], it) }
            val steps = ArrayList<String>()
            steps.add("Inform ${input.input}")
            steps.add("Mock $filterMethod method in ${testArchitecture.repositoryLayerClass}, returning a mocked instance of ${entity.name} with informed data, the same mocked in repository")
            steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class and return the existing instance of ${entity.name}")
            steps.add("Verify attributes of retrieved instances of ${entity.name}")
            steps.add("Verify that $filterMethod was called")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }

    private fun createTestCaseNullInputsFilterReturnAllInstances(ruleDataTest: ListDataTest, script: Script): TestCase? {
        val validDataTest = ruleDataTest.dataTest.filter { it.isValidInput }
        val entity = script.reference as Entity

        val inputParamMethod = HashMap<String, Any?>()

        script.inputs.forEach { input ->
            val attrName = input.name
            inputParamMethod[attrName] = null
        }

        val input = DataTest(inputParamMethod, true, "", "")
        val output = validDataTest

        if(validDataTest != null) {
            val expectedResult =
                "Get the three instances of ${entity.name}, as following: $output"
            val positiveTestCase = TestCase(input, script, expectedResult)

            val operation = "Filter${entity}_"
            val testName = "${baseTestName}${operation}NoFilterFilledReturnsAllInstances"
            positiveTestCase.testName = testName

            positiveTestCase.preconditions.add("The repository must have the instances that will be retrieved as following: $output")
            positiveTestCase.preconditions.add("Use constructor with all attributes as parameters to mock ${entity.name}")
            val executeVerb = testArchitecture.ruleLayerMethod.split("(")
            val filterMethod =
                testArchitecture.mapMethods["filter"]?.let { testArchitecture.ruleLayerMethod.replace(executeVerb[0], it) }
            val steps = ArrayList<String>()
            steps.add("Inform ${input.input}")
            steps.add("Mock $filterMethod method in ${testArchitecture.repositoryLayerClass}, returning a mocked instance of ${entity.name} with informed data, the same mocked in repository")
            steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class and return the existing instances of ${entity.name}")
            steps.add("Verify that returned list of ${entity.name} has 3 elements")
            steps.add("Verify attributes of retrieved instances of ${entity.name}")
            steps.add("Verify that $filterMethod was called")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }

    private fun createTestCaseOneFilterFilledAndReturnEmptyList(ruleDataTest: ListDataTest, script: Script): TestCase? {
        val filteredRuleDataTest = ruleDataTest.dataTest.filter { it.isValidInput }
        val validDataTest1 = filteredRuleDataTest[0]
        val validDataTest2 = filteredRuleDataTest[1]
        val validDataTest3 = filteredRuleDataTest[2]

        val entity = script.reference as Entity

        val inputParamMethod = HashMap<String, Any?>()

        var isAttributeDefined = true
        script.inputs.forEach { input ->
            val attrName = input.name
            val value1 = validDataTest1.input?.get(attrName)
            val value2 = validDataTest2.input?.get(attrName)
            val value3 = validDataTest3.input?.get(attrName)
            if(value1 != value2 != value3 && isAttributeDefined) {
                inputParamMethod[attrName] = validDataTest1.input?.get(attrName)
                isAttributeDefined = false
            }
            else
                inputParamMethod[attrName] = null
        }

        val input = DataTest(inputParamMethod, true, "", "")
        val output = ArrayList<DataTest>()
        output.add(validDataTest2)
        output.add(validDataTest3)

        if(validDataTest1 != null) {
            val expectedResult =
                "Get an empty list of ${entity.name} when filter"
            val positiveTestCase = TestCase(input, script, expectedResult)

            val operation = "Filter${entity}_"
            val testName = "${baseTestName}${operation}OneFilterFilledReturnsEmptyList"
            positiveTestCase.testName = testName

            positiveTestCase.preconditions.add("The repository must have the instances as following: $output")
            positiveTestCase.preconditions.add("Use constructor with all attributes as parameters to mock ${entity.name}")
            val executeVerb = testArchitecture.ruleLayerMethod.split("(")
            val filterMethod =
                testArchitecture.mapMethods["filter"]?.let { testArchitecture.ruleLayerMethod.replace(executeVerb[0], it) }
            val steps = ArrayList<String>()
            steps.add("Inform ${input.input}")
            steps.add("Mock $filterMethod method in ${testArchitecture.repositoryLayerClass}, returning an empty list of ${entity.name} with informed data, the same mocked in repository")
            steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class and return an empty list of ${entity.name}")
            steps.add("Verify the list of ${entity.name} is empty")
            steps.add("Verify that $filterMethod was called")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }


    private fun checkDBCommunicationTestCases(ruleDataTest: ListDataTest, script: Script): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }

        //empty input
        val input = DataTest(HashMap<String, String>(), true, "", "")

        val entity = script.reference as Entity

        if(validDataTest != null) {
            val steps = ArrayList<String>()
            steps.add("Mock ${testArchitecture.mapMethods["filter"]} method in ${testArchitecture.repositoryLayerClass} to return ${testArchitecture.dbErrorFramework} when try to filter ${entity.name}")
            steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass}")
            steps.add("Verify that ${testArchitecture.dbError} is ${testArchitecture.dbErrorAction}")
            steps.add("Verify mocks")

            val expectedResult = "${testArchitecture.dbError} is ${testArchitecture.dbErrorAction}"
            val testCase = TestCase(input, script, expectedResult)

            val operation = "Filter${entity}_"
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
        val testCases =  ArrayList<TestCase>()
        createTestCaseFilteredInstance(ruleDataTest, script)?.let { testCases.add(it) }
        createTestCaseOneFilterFilledInstance(ruleDataTest, script)?.let { testCases.add(it) }
        createTestCaseNullInputsFilterReturnAllInstances(ruleDataTest, script)?.let { testCases.add(it) }
        createTestCaseOneFilterFilledAndReturnEmptyList(ruleDataTest, script)?.let { testCases.add(it) }
        checkDBCommunicationTestCases(ruleDataTest, script)?.let { testCases.add(it) }
        return testCases
    }

}
