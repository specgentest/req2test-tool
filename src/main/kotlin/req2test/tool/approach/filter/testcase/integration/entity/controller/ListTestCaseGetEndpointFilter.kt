package req2test.tool.approach.filter.testcase.integration.entity.controller

import req2test.tool.approach.entity.*

open class ListTestCaseGetEndpointFilter (data: MutableMap<String, Any?>): BaseListTCEndpointFilter(data){

    override fun filterKeyInput(): String { return "ListGetEndpointScript" }
    override fun filterName(): String {

        return "ListTestCaseGetEndpointFilter"
    }
    override fun filterKeyOutput(): String { return "ListTestCaseGetEndpoint" }

    fun getInput(dataTest: DataTest?): DataTest {
        if(testArchitecture.urlinput == null)
            return DataTest(dataTest?.input?.filter { (key, value) -> key.contains("id")  }, true, "", "")
        else {
            val inputParameters = testArchitecture.urlinput!!.map { it.name }
            return DataTest(dataTest?.input?.filter { (key, _) -> key in inputParameters }, true, "", "")
        }
    }

    fun getEndpointReplaced(dataTest: DataTest?): String? {
        if(testArchitecture.urlinput == null){
            val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String
            val endpointGet = testArchitecture.endpoints["retrieve"]?.replace("{id}", idAttribute)
            val id = dataTest?.input?.get(idAttribute)
            val endpointGetWithId = testArchitecture.endpoints["retrieve"]?.replace("{id}", id.toString())
            return endpointGetWithId
        }
        else {
            val parameters = testArchitecture.urlinput!!.map { it.name }
            var endpointGetWithParameters = testArchitecture.url.toString()
            parameters.forEach{
                    param ->
                val value = getValueFromMap(dataTest?.input!!, param)
                endpointGetWithParameters = endpointGetWithParameters.replace("{$param}", value.toString())
            }
            return endpointGetWithParameters
        }
    }

    fun getEndpoint(): String? {
        if(testArchitecture.url == null)   {
            val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String
            return testArchitecture.endpoints["retrieve"]?.replace("{id}", idAttribute)
        }
        return testArchitecture.url
    }

    fun getValueFromMap(data: Map<String, Any?>, path: String): Any? {
        val keys = path.split(".")
        var current: Any? = data

        for (key in keys) {
            if (current !is Map<*, *>) return null
            current = current[key]
        }

        return current
    }

    private fun createTestCaseValid200(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }

        val input = getInput(validDataTest)
        val endpointGetWithId = getEndpointReplaced(validDataTest)
        val endpointGet = getEndpoint()

        val output = validDataTest?.input

        if(validDataTest != null) {

            val dbInitMap = mutableMapOf<String, Any?>()
            dbInitMap[entity.name] = validDataTest.input

            val expectedResult =
                "Get an existing instance of ${entity.name} and confirm that the method ${script.location} mapped by GET $endpointGet gets the following attributes: $output"
            val positiveTestCase = TestCase(input, script, expectedResult)
            positiveTestCase.preconditions.add("The repository must have the instances that will be retrieved as following: $dbInitMap")

            val steps = ArrayList<String>()
            steps.add("Inform ${input.input}")
            steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by GET $endpointGetWithId in ${testArchitecture.controllerLayerClass} class and return an existing instance of ${entity.name}")
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

        val inputDataTest = DataTest(negativeDataTest?.input?.filter { (key, value) -> key.contains("id")  }, true, "", "")

        val input = DataTest(inputDataTest.input, false, negativeDataTest?.errorMessage, "")

        val endpointGet = testArchitecture.endpoints["retrieve"]?.replace("{id}", idAttribute)
        val id = inputParamMethod[idAttribute]
        val endpointGetWithId = testArchitecture.endpoints["retrieve"]?.replace("{id}", id.toString())

        val errorMessage = "invalid $idAttribute type"
        val expectedResult = "${script.location} mapped by GET $endpointGet returns bad request 400 with the following error message: $errorMessage"

        val negativeTestCase = TestCase(input, script, expectedResult)
        val steps = ArrayList<String>()

        //steps.addAll(script.inputs.map { "Inform ${it.name}" })
        steps.add("Inform ${input.input}")

        steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by GET $endpointGetWithId in ${testArchitecture.controllerLayerClass} class")
        steps.add("Verify that the endpoint $endpointGetWithId returns 400")
        steps.add("Verify that the endpoint $endpointGetWithId contains the message: $errorMessage")
        negativeTestCase.steps.addAll(steps)
        return negativeTestCase
    }


    private fun createTestCaseInstanceNotFound404(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = ruleDataTest.dataTest.find { it.isValidInput }

        val input = DataTest(validDataTest?.input?.filter { (key, value) -> key.contains("id")  }, true, "", "")

        val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String
        val endpointGet = testArchitecture.endpoints["retrieve"]?.replace("{id}", idAttribute)
        val endpointGetWithId = testArchitecture.endpoints["retrieve"]?.replace("{id}", validDataTest?.input?.get(idAttribute).toString())

        if(validDataTest != null) {
            val expectedResult = "${script.location} mapped by GET $endpointGet does not get an instance of ${entity.name} then returns not found 404"
            val instanceNotFoundTestCase = TestCase(input, script, expectedResult)
            instanceNotFoundTestCase.preconditions.add("The repository must be empty")
            val errorMessage = "${entity.name} not found"
            val steps = ArrayList<String>()
            steps.add("Inform ${input.input}")

            steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by GET $endpointGetWithId in ${testArchitecture.controllerLayerClass} class and It returns not found code 404 with the following message: $errorMessage")
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
        return testCases
}

}
