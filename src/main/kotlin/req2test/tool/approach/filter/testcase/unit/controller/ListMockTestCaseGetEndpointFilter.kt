package req2test.tool.approach.filter.testcase.unit.controller

import req2test.tool.approach.entity.*

open class ListMockTestCaseGetEndpointFilter (data: MutableMap<String, Any?>): BaseListMockTCEndpointFilter(data){

    override fun filterKeyInput(): String { return "ListGetEndpointMockScript" }
    override fun filterName(): String {

        return "ListMockTestCaseGetEndpointFilter"
    }
    override fun filterKeyOutput(): String { return "ListMockTestCaseGetEndpoint" }

    private fun createTestCaseValid200(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }

        val input = getInput(validDataTest)
        val endpointGetWithId = getEndpointReplaced(validDataTest, "retrieve")
        val endpointGet = getEndpoint()

        val output = validDataTest?.input

        if(validDataTest != null) {
            val expectedResult =
                "Get an existing instance of ${entity.name} and confirm that the method ${script.location} mapped by GET $endpointGet gets the following attributes: $output"
            val positiveTestCase = TestCase(input, script, expectedResult)
            positiveTestCase.preconditions.add("Use ${entity.name} constructor with all attributes as parameters to mock an instance")

            val steps = ArrayList<String>()
            steps.add("Inform ${input.input}")

            steps.add("Mock ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class to return $output")
            steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by GET $endpointGetWithId in ${testArchitecture.controllerLayerClass} class and return an existing instance of ${entity.name}")

            steps.add("Verify that ${testArchitecture.ruleLayerMethod} was called")
            steps.add("Verify that the endpoint $endpointGetWithId returns 200")
            steps.add("Verify that the endpoint $endpointGetWithId returns $output")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }


    private fun createTestCaseBadRequest400(invalidIDDataTest: DataTest?, script: Script, entity: Entity): TestCase {
        val negativeDataTest = invalidIDDataTest
        val notGeneratedAttributes = entity.attributes.filter { !it.generatedDB and !it.generatedConstructor }
        val inputParamMethod = HashMap<String, Any?>()

        notGeneratedAttributes.forEach {
            inputParamMethod[it.name] = negativeDataTest?.input?.get(it.name)
        }

        val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String

        inputParamMethod[idAttribute] = negativeDataTest?.input?.get(idAttribute)

        val input = getInput(negativeDataTest)
        val endpointGetWithId = getEndpointReplaced(negativeDataTest, "retrieve")
        val endpointGet = getEndpoint()


        val errorMessage = "invalid $idAttribute type"
        val expectedResult = "${script.location} mapped by GET $endpointGet returns bad request 400 with the following error message: $errorMessage"

        val negativeTestCase = TestCase(input, script, expectedResult)
        val steps = ArrayList<String>()

        steps.addAll(script.inputs.map { "Inform ${it.name}" })

        steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by GET $endpointGetWithId in ${testArchitecture.controllerLayerClass} class")
        steps.add("Verify that the endpoint $endpointGetWithId returns 400")
        steps.add("Verify that the endpoint $endpointGetWithId contains the message: $errorMessage")
        negativeTestCase.steps.addAll(steps)
        return negativeTestCase
    }



    private fun createTestCaseInternalError500(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }

        val endpointGetWithId = getEndpointReplaced(validDataTest, "retrieve")
        val endpointGet = getEndpoint()

        if(validDataTest != null) {
            val expectedResult = "${script.location} mapped by GET $endpointGet returns internal error 500"
            val negativeTestCase = TestCase(validDataTest, script, expectedResult)
            negativeTestCase.preconditions.add("Use ${entity.name} constructor with all attributes as parameters to mock an instance")

            val steps = ArrayList<String>()
            steps.addAll(script.inputs.map { "Inform ${it.name}" })

            val errorMessage = "Failed to retrieve ${entity.name} from database"
            steps.add("Mock ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class  to return ${testArchitecture.dbErrorAction} ${testArchitecture.dbError} with the following error message: $errorMessage")
            steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by GET $endpointGetWithId in ${testArchitecture.controllerLayerClass} class and return internal error code 500 with the following error message: $errorMessage")

            steps.add("Verify that ${testArchitecture.ruleLayerMethod} was called")
            steps.add("Verify that the endpoint $endpointGetWithId returns 500")
            steps.add("Verify that the endpoint $endpointGetWithId contains the message: $errorMessage")

            negativeTestCase.steps.addAll(steps)
            return negativeTestCase
        }
        return null
    }

    private fun createTestCaseInstanceNotFound404(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }

        val input = getInput(validDataTest)
        val endpointGetWithId = getEndpointReplaced(validDataTest, "retrieve")
        val endpointGet = getEndpoint()


        if(validDataTest != null) {
            val expectedResult = "${script.location} mapped by GET $endpointGet does not get an instance of ${entity.name} then returns not found 404"
            val instanceNotFoundTestCase = TestCase(input, script, expectedResult)
            val errorMessage = "${entity.name} not found"
            val steps = ArrayList<String>()
            steps.add("Inform ${input.input}")

            steps.add("Mock ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class to return ${testArchitecture.ruleError} with the following error message: $errorMessage")
            steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by GET $endpointGetWithId in ${testArchitecture.controllerLayerClass} class and It returns not found code 404 with the following message: $errorMessage")
            steps.add("Verify that ${testArchitecture.ruleLayerMethod} was called")
            steps.add("Verify that the endpoint $endpointGetWithId returns 404")
            steps.add("Verify that the endpoint $endpointGetWithId contains the message: $errorMessage")
            instanceNotFoundTestCase.steps.addAll(steps)
            return instanceNotFoundTestCase
        }
        return null
    }

    override fun createTestCases(entityDataTest: ListDataTest, ruleDataTest: ListDataTest, invalidIDDataTest: DataTest?, script: Script, entity: Entity): List<TestCase> {
        val testCases = ArrayList<TestCase>()
        createTestCaseValid200(ruleDataTest, script, entity)?.let { script.testCases.add(it) }
        createTestCaseInstanceNotFound404(ruleDataTest, script, entity)?.let { script.testCases.add(it) }
        createTestCaseBadRequest400(invalidIDDataTest, script, entity).let { script.testCases.add(it) }
        createTestCaseInternalError500(ruleDataTest, script, entity)?.let { script.testCases.add(it) }
        return testCases
    }
}
