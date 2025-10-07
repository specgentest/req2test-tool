package req2test.tool.approach.filter.testcase.unit.controller

import req2test.tool.approach.entity.*

open class ListMockTestCaseFilterEndpointFilter (data: MutableMap<String, Any?>): BaseListMockTCEndpointFilter(data){

    override fun filterKeyInput(): String { return "ListFilterEndpointMockScript" }
    override fun filterName(): String {

        return "ListMockTestCaseFilterEndpointFilter"
    }
    override fun filterKeyOutput(): String { return "ListMockTestCaseFilterEndpoint" }


    private fun createTestCaseValid200(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }
        //val entity = script.reference as Entity

        val inputParamMethod = HashMap<String, Any?>()

        var params = ""
        script.inputs.forEach { input ->
            val attrName = input.name
            inputParamMethod[attrName] = validDataTest?.input?.get(attrName)
            if(params == "")
                params += "?$attrName=${inputParamMethod[attrName]}"
            else
                params += "&$attrName=${inputParamMethod[attrName]}"
        }

        val input = DataTest(inputParamMethod, true, "", "")
        val output = validDataTest?.input

        val endpointFilter = "${testArchitecture.endpoints["filter"]}"
        val endpointFilterWithParameters = "$endpointFilter$params"

        if(validDataTest != null) {
            val expectedResult =
                "Get filtered list of instances of ${entity.name} and confirm that the method ${script.location} mapped by GET $endpointFilter gets the following attributes: $output"
            val positiveTestCase = TestCase(input, script, expectedResult)
            positiveTestCase.preconditions.add("Use ${entity.name} constructor with all attributes as parameters to mock an instance")

            val steps = ArrayList<String>()
            steps.add("Inform ${input.input}")

            steps.add("Mock ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class to return $output")
            steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by GET $endpointFilterWithParameters in ${testArchitecture.controllerLayerClass} class and return a list of filtered instances of ${entity.name}")
            steps.add("Verify that ${testArchitecture.ruleLayerMethod} was called")
            steps.add("Verify that the endpoint $endpointFilterWithParameters returns 200")
            steps.add("Verify that the endpoint $endpointFilterWithParameters returns $output")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }


    private fun createTestCaseInternalError500(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }
        //val entity = script.reference as Entity

        val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String
        val endpointFilter = testArchitecture.endpoints["filter"]?.replace("{id}", idAttribute)
        val endpointFilterWithId = testArchitecture.endpoints["filter"]?.replace("{id}", validDataTest?.input?.get(idAttribute).toString())


        if(validDataTest != null) {
            val expectedResult = "${script.location} mapped by GET $endpointFilter returns internal error 500"
            val negativeTestCase = TestCase(validDataTest, script, expectedResult)
            //negativeTestCase.preconditions.add("Use ${entity.name} constructor with all attributes as parameters to mock an instance")

            val steps = ArrayList<String>()
            steps.addAll(script.inputs.map { "Inform ${it.name}" })

            val errorMessage = "Failed to retrieve and filter list of ${entity.name} from database"
            steps.add("Mock ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class  to return ${testArchitecture.dbErrorAction} ${testArchitecture.dbError} with the following error message: $errorMessage")
            steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by GET $endpointFilterWithId in ${testArchitecture.controllerLayerClass} class and return internal error code 500 with the following error message: $errorMessage")

            steps.add("Verify that ${testArchitecture.ruleLayerMethod} was called")
            steps.add("Verify that the endpoint $endpointFilterWithId returns 500")
            steps.add("Verify that the endpoint $endpointFilterWithId contains the message: $errorMessage")

            negativeTestCase.steps.addAll(steps)
            return negativeTestCase
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
        createTestCaseInternalError500(ruleDataTest, script, entity)?.let { testCases.add(it) }
        return testCases
    }

}
