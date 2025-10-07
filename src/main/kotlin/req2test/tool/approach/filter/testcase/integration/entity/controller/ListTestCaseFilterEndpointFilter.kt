package req2test.tool.approach.filter.testcase.integration.entity.controller

import req2test.tool.approach.entity.*

open class ListTestCaseFilterEndpointFilter (data: MutableMap<String, Any?>): BaseListTCEndpointFilter(data){

    override fun filterKeyInput(): String { return "ListFilterEndpointScript" }
    override fun filterName(): String {

        return "ListTestCaseFilterEndpointFilter"
    }
    override fun filterKeyOutput(): String { return "ListTestCaseFilterEndpoint" }


    fun createInputParameterMap(validDataTest: DataTest?, script: Script): HashMap<String, Any?> {
        val inputParamMap = HashMap<String, Any?>()
        script.inputs.forEach { input ->
            val attrName = input.name
            inputParamMap[attrName] = validDataTest?.input?.get(attrName)
        }
        return inputParamMap
    }

    fun convertParameterMapToString(inputParamMap: HashMap<String, Any?>): String {
        var params = ""
        inputParamMap.forEach { (key, value) ->
            if (params.isEmpty()) {
                params += "?$key=$value"
            } else {
                params += "&$key=$value"
            }
        }
        return params
    }


    fun getEndpointFilter(): String {
        val endpointFilter = "${testArchitecture.endpoints["filter"]}"
        return endpointFilter
    }

    fun getEndpointFilterWithParameters(inputParameter: HashMap<String, Any?>): String {
        val params = convertParameterMapToString(inputParameter)
        val endpointFilter = getEndpointFilter()
        val endpointFilterWithParameters = "$endpointFilter$params"
        return endpointFilterWithParameters
    }

    private fun createTestCaseValid200(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }

        val inputParamMethod = createInputParameterMap(validDataTest, script)
        val input = DataTest(inputParamMethod, true, "", "")
        val output = validDataTest?.input
        val endpointFilter = getEndpointFilter()
        val endpointFilterWithParameters = getEndpointFilterWithParameters(inputParamMethod)

        if(validDataTest != null) {

            val dbInitMap = mutableMapOf<String, Any?>()
            dbInitMap[entity.name] = validDataTest.input

            val expectedResult =
                "Get filtered list of instance of ${entity.name} and confirm that the method ${script.location} mapped by GET $endpointFilter gets the following output: $output"
            val positiveTestCase = TestCase(input, script, expectedResult)
            positiveTestCase.preconditions.add("The repository must have the instances that will be retrieved as following: $dbInitMap")
            val steps = ArrayList<String>()

            steps.add("Inform ${input.input}")
            steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by GET $endpointFilterWithParameters in ${testArchitecture.controllerLayerClass} class")
            steps.add("When a parameter has a null value, omit it from the request url")
            steps.add("Verify that the endpoint $endpointFilterWithParameters returns 200")
            steps.add("Verify that the endpoint $endpointFilterWithParameters returns $output")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }


        return null
    }

    private fun createTestCaseOneFilterFilledInstance(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }

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
        val endpointFilter = getEndpointFilter()
        val endpointFilterWithParameters = getEndpointFilterWithParameters(inputParamMethod)

        if(validDataTest != null) {

            val dbInitMap = mutableMapOf<String, Any?>()
            dbInitMap[entity.name] = validDataTest.input

            val expectedResult =
                "Get filtered list of instance of ${entity.name} and confirm that the method ${script.location} mapped by GET $endpointFilter gets the following output: $output"
            val positiveTestCase = TestCase(input, script, expectedResult)
            positiveTestCase.preconditions.add("The repository must have the instances that will be retrieved as following: $dbInitMap")
            val steps = ArrayList<String>()

            steps.add("Inform ${input.input}")
            steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by GET $endpointFilterWithParameters in ${testArchitecture.controllerLayerClass} class")
            steps.add("When a parameter has a null value, omit it from the request url")
            steps.add("Verify that the endpoint $endpointFilterWithParameters returns 200")
            steps.add("Verify that the endpoint $endpointFilterWithParameters returns $output")
            steps.add("The response is always a list")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }

    private fun createTestCaseNullInputsFilterReturnAllInstances(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = ruleDataTest.dataTest.filter { it.isValidInput }

        val inputParamMethod = HashMap<String, Any?>()

        script.inputs.forEach { input ->
            val attrName = input.name
            inputParamMethod[attrName] = null
        }

        val input = DataTest(inputParamMethod, true, "", "")
        val endpointFilter = getEndpointFilter()
        val endpointFilterWithParameters = getEndpointFilterWithParameters(inputParamMethod)


        if(validDataTest != null) {
            val dbInitMap = mutableMapOf<String, Any?>()
            dbInitMap[entity.name] = validDataTest.map { it.input }
            val output = dbInitMap

            val expectedResult =
                "Get filtered list of instance of ${entity.name} and confirm that the method ${script.location} mapped by GET $endpointFilter gets the following output: $output"
            val positiveTestCase = TestCase(input, script, expectedResult)
            positiveTestCase.preconditions.add("The repository must have the instances that will be retrieved as following: $dbInitMap")
            val steps = ArrayList<String>()

            steps.add("Inform ${input.input}")
            steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by GET $endpointFilterWithParameters in ${testArchitecture.controllerLayerClass} class")
            steps.add("When a parameter has a null value, omit it from the request url")
            steps.add("Verify that the endpoint $endpointFilterWithParameters returns 200")
            steps.add("Verify that the endpoint $endpointFilterWithParameters returns $output")
            steps.add("The response is always a list")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }

    private fun createTestCaseOneFilterFilledAndReturnEmptyList(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val filteredRuleDataTest = ruleDataTest.dataTest.filter { it.isValidInput }
        val validDataTest1 = filteredRuleDataTest[0]
        val validDataTest2 = filteredRuleDataTest[1]
        val validDataTest3 = filteredRuleDataTest[2]

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


        val endpointFilter = getEndpointFilter()
        val endpointFilterWithParameters = getEndpointFilterWithParameters(inputParamMethod)


        if(validDataTest1 != null) {
            val dbInitMap = mutableMapOf<String, Any?>()
            val db = ArrayList<Map<String, Any?>?>()
            db.add(validDataTest2.input)
            db.add(validDataTest3.input)
            dbInitMap[entity.name] = db

            val expectedResult =
                "Method ${script.location} mapped by GET $endpointFilter gets an empty list of ${entity.name}"
            val positiveTestCase = TestCase(input, script, expectedResult)
            positiveTestCase.preconditions.add("The repository must have the instances that will be retrieved as following: $dbInitMap")
            val steps = ArrayList<String>()

            steps.add("Inform ${input.input}")
            steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by GET $endpointFilterWithParameters in ${testArchitecture.controllerLayerClass} class")
            steps.add("When a parameter has a null value, omit it from the request url")
            steps.add("Verify that the endpoint $endpointFilterWithParameters returns 200")
            steps.add("Verify that the endpoint $endpointFilterWithParameters returns an empty list")
            steps.add("The response is always a list")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }



    override fun createTestCases(
        entityDataTest: ListDataTest,
        ruleDataTest: ListDataTest,
        invalidIDDataTest: DataTest?,
        script: Script,
        entity: Entity,
    ): List<TestCase> {
        val testCases =  ArrayList<TestCase>()
        createTestCaseValid200(ruleDataTest, script, entity)?.let { testCases.add(it) }
        createTestCaseOneFilterFilledInstance(ruleDataTest, script, entity)?.let { testCases.add(it) }
        createTestCaseNullInputsFilterReturnAllInstances(ruleDataTest, script, entity)?.let { testCases.add(it) }
        createTestCaseOneFilterFilledAndReturnEmptyList(ruleDataTest, script, entity)?.let { testCases.add(it) }
        return testCases
    }

}
