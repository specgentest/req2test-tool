package req2test.tool.approach.filter.testcase.unit.controller

import req2test.tool.approach.entity.*

open class ListMockTestCaseUpdateEndpointFilter (data: MutableMap<String, Any?>): BaseListMockTCEndpointFilter(data){

    override fun filterKeyInput(): String { return "ListUpdateEndpointMockScript" }
    override fun filterName(): String {

        return "ListMockTestCaseUpdateEndpointFilter"
    }
    override fun filterKeyOutput(): String { return "ListMockTestCaseUpdateEndpoint" }

    private fun createTestCaseValid200(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase {
        val validDataTest = ruleDataTest.dataTest.filter { it.isValidInput }

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
        inputTwo[idAttribute] = positiveDataOne.input?.get(idAttribute)

        val input = DataTest(inputParamMethod, true, "", "")
        val output = inputTwo

        val id = inputParamMethod[idAttribute]

        val inputUrl = getInput(positiveDataOne)
        val endpointUpdateWithId = getEndpointReplaced(positiveDataOne, "update")
        val endpointUpdate = getEndpoint()


        val expectedResult =
            "Update an instance of ${entity.name} and confirm that the method ${script.location} mapped by PUT $endpointUpdate updates the instance when all data is valid, resulting in $output"
        val positiveTestCase = TestCase(input, script, expectedResult)
        positiveTestCase.preconditions.add("Use ${entity.name} constructor with all attributes as parameters to mock an instance")
        positiveTestCase.preconditions.add("The ${entity.name} input $input must be body parameter. Only the $idAttribute must be in the url")

        val steps = ArrayList<String>()
        steps.addAll(script.inputs.map { "Inform ${it.name}" })

        val requestParameter = extractBetweenParentheses(controllerLayerMethod)
        steps.add("Mock ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class, returning a mocked instance of ${entity.name} with informed data: $output")
        //steps.add("Use the following input: $inputOne")
        steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by PUT $endpointUpdateWithId in ${testArchitecture.controllerLayerClass} class and return an updated instance of ${entity.name}: $output")
        steps.add("The input request for the PUT endpoint must be strictly: $requestParameter")
        steps.add("If the parameter is already in url parameter it does not add in input request: ${inputUrl.input}")
        steps.add("Verify attributes of updated instance of ${entity.name}")
        steps.add("Verify that ${testArchitecture.ruleLayerMethod} was called")
        steps.add("Verify that the endpoint $endpointUpdateWithId returns 200")
        steps.add("Verify that the endpoint $endpointUpdateWithId contains $output")
        positiveTestCase.steps.addAll(steps)
        return positiveTestCase
    }


    private fun createTestCaseBadRequest400(entityDataTest: ListDataTest, ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase {
        val negativeDataTest = entityDataTest.dataTest.find { !it.isValidInput }
        //val entity = script.reference as Entity
        val notGeneratedAttributes = entity.attributes.filter { !it.generatedDB and !it.generatedConstructor }
        val inputParamMethod = HashMap<String, Any?>()

        notGeneratedAttributes.forEach {
            inputParamMethod[it.name] = negativeDataTest?.input?.get(it.name)
        }

        val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String

        //ruleDataTest is needed to fill id. Because entityDataTest does not have it
        inputParamMethod[idAttribute] = ruleDataTest.dataTest.first()?.input?.get(idAttribute)
        val input = DataTest(inputParamMethod, false, negativeDataTest?.errorMessage, "")

        val endpointUpdate = testArchitecture.endpoints["update"]?.replace("{id}", idAttribute)
        val id = inputParamMethod[idAttribute]
        val endpointUpdateWithId = testArchitecture.endpoints["update"]?.replace("{id}", id.toString())

        val expectedResult = "${script.location} mapped by PUT $endpointUpdate returns bad request 400 with the following error message: ${negativeDataTest?.errorMessage}"

        val negativeTestCase = TestCase(input, script, expectedResult)
        negativeTestCase.preconditions.add("The ${entity.name} input ${negativeDataTest?.input} must be body parameter. Only the $idAttribute must be in the url")
        val steps = ArrayList<String>()

        steps.addAll(script.inputs.map { "Inform ${it.name}" })

        val requestParameter = extractBetweenParentheses(controllerLayerMethod)
        steps.add("Mock ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class  to return ${testArchitecture.ruleErrorAction} ${testArchitecture.ruleError} with the following error message: ${negativeDataTest?.errorMessage}")
        steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by PUT $endpointUpdateWithId in ${testArchitecture.controllerLayerClass} class and return bad request code 400 with the following error message: ${negativeDataTest?.errorMessage}")
        steps.add("The input request for the PUT endpoint must be strictly: $requestParameter")
        steps.add("Verify that ${testArchitecture.ruleLayerMethod} was called")
        steps.add("Verify that the endpoint $endpointUpdateWithId returns 400")
        steps.add("Verify that the endpoint $endpointUpdateWithId contains the message: ${negativeDataTest?.errorMessage}")
        negativeTestCase.steps.addAll(steps)
        return negativeTestCase
    }

    private fun createTestCaseBadRequest400ID(invalidIDDataTest: DataTest?, script: Script, entity: Entity): TestCase {
        val negativeDataTest = invalidIDDataTest
        //val entity = script.reference as Entity
        val notGeneratedAttributes = entity.attributes.filter { !it.generatedDB and !it.generatedConstructor }
        val inputParamMethod = HashMap<String, Any?>()

        notGeneratedAttributes.forEach {
            inputParamMethod[it.name] = negativeDataTest?.input?.get(it.name)
        }

        val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String

        //ruleDataTest is needed to fill id. Because entityDataTest does not have it
        inputParamMethod[idAttribute] = negativeDataTest?.input?.get(idAttribute)
        val input = DataTest(inputParamMethod, false, negativeDataTest?.errorMessage, "")

        val endpointUpdate = testArchitecture.endpoints["update"]?.replace("{id}", idAttribute)
        val id = inputParamMethod[idAttribute]
        val endpointUpdateWithId = testArchitecture.endpoints["update"]?.replace("{id}", id.toString())

        val errorMessage = "invalid $idAttribute type"
        val expectedResult = "${script.location} mapped by PUT $endpointUpdate returns bad request 400 with the following error message: $errorMessage"

        val negativeTestCase = TestCase(input, script, expectedResult)
        val steps = ArrayList<String>()

        steps.addAll(script.inputs.map { "Inform ${it.name}" })
        val requestParameter = extractBetweenParentheses(controllerLayerMethod)

        steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by PUT $endpointUpdateWithId in ${testArchitecture.controllerLayerClass} class")
        steps.add("The input request for the PUT endpoint must be strictly: $requestParameter")

        steps.add("Verify that the endpoint $endpointUpdateWithId returns 400")
        steps.add("Verify that the endpoint $endpointUpdateWithId contains the message: $errorMessage")
        negativeTestCase.steps.addAll(steps)
        return negativeTestCase
    }

    private fun createTestCaseInternalError500(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }

        val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String
        val endpointUpdate = testArchitecture.endpoints["update"]?.replace("{id}", idAttribute)
        val endpointUpdateWithId = testArchitecture.endpoints["update"]?.replace("{id}", validDataTest?.input?.get(idAttribute).toString())


        if(validDataTest != null) {
            val expectedResult = "${script.location} mapped by PUT $endpointUpdate returns internal error 500"
            val negativeTestCase = TestCase(validDataTest, script, expectedResult)
            negativeTestCase.preconditions.add("Use ${entity.name} constructor with all attributes as parameters to mock an instance")
            negativeTestCase.preconditions.add("The ${entity.name} input $validDataTest must be body parameter. Only the $idAttribute must be in the url")

            val steps = ArrayList<String>()
            steps.addAll(script.inputs.map { "Inform ${it.name}" })
            val requestParameter = extractBetweenParentheses(controllerLayerMethod)

            val errorMessage = "Failed to update ${entity.name} int the database"
            steps.add("Mock ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class  to return ${testArchitecture.dbErrorAction} ${testArchitecture.dbError} with the following error message: $errorMessage")
            steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by PUT $endpointUpdateWithId in ${testArchitecture.controllerLayerClass} class and return internal error code 500 with the following error message: $errorMessage")
            steps.add("The input request for the PUT endpoint must be strictly: $requestParameter")

            steps.add("Verify that ${testArchitecture.ruleLayerMethod} was called")
            steps.add("Verify that the endpoint $endpointUpdateWithId returns 500")
            steps.add("Verify that the endpoint $endpointUpdateWithId contains the message: $errorMessage")

            negativeTestCase.steps.addAll(steps)
            return negativeTestCase
        }
        return null
    }

    private fun createTestCaseInstanceNotFound404(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }

        val input = DataTest(validDataTest?.input?.filter { (key, value) -> key.contains("id")  }, true, "", "")

        val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String
        val endpointUpdate = testArchitecture.endpoints["update"]?.replace("{id}", idAttribute)
        val endpointUpdateWithId = testArchitecture.endpoints["update"]?.replace("{id}", validDataTest?.input?.get(idAttribute).toString())

        if(validDataTest != null) {
            val expectedResult = "${script.location} mapped by UPDATE $endpointUpdate does not update an instance of ${entity.name} then returns not found 404"
            val instanceNotFoundTestCase = TestCase(input, script, expectedResult)
            val errorMessage = "${entity.name} not found"
            val steps = ArrayList<String>()
            steps.add("Inform ${input.input}")
            val requestParameter = extractBetweenParentheses(controllerLayerMethod)

            steps.add("Mock ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class to return ${testArchitecture.ruleError} with the following error message: $errorMessage")
            steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by UPDATE $endpointUpdateWithId in ${testArchitecture.controllerLayerClass} class and It returns not found code 404 with the following message: $errorMessage")
            steps.add("The input request for the PUT endpoint must be strictly: $requestParameter")

            steps.add("Verify that ${testArchitecture.ruleLayerMethod} was called")
            steps.add("Verify that the endpoint $endpointUpdateWithId returns 404")
            steps.add("Verify that the endpoint $endpointUpdateWithId contains the message: $errorMessage")
            instanceNotFoundTestCase.steps.addAll(steps)
            return instanceNotFoundTestCase
        }
        return null
    }

    override fun createTestCases(entityDataTest: ListDataTest, ruleDataTest: ListDataTest, invalidIDDataTest: DataTest?, script: Script, entity: Entity): List<TestCase> {
        val testCases = ArrayList<TestCase>()
        createTestCaseValid200(ruleDataTest, script, entity).let { script.testCases.add(it) }
        createTestCaseBadRequest400(entityDataTest, ruleDataTest, script, entity).let { script.testCases.add(it) }
        createTestCaseBadRequest400ID(invalidIDDataTest, script, entity).let { script.testCases.add(it) }
        createTestCaseInstanceNotFound404(ruleDataTest, script, entity)?.let { script.testCases.add(it) }
        createTestCaseInternalError500(ruleDataTest, script, entity)?.let { script.testCases.add(it) }
        return testCases
}

}
