package req2test.tool.approach.filter.testcase.integration.entity.controller

import req2test.tool.approach.entity.*

open class ListTestCaseUpdateEndpointFilter (data: MutableMap<String, Any?>): BaseListTCEndpointFilter(data){

    override fun filterKeyInput(): String { return "ListUpdateEndpointScript" }
    override fun filterName(): String {

        return "ListTestCaseUpdateEndpointFilter"
    }
    override fun filterKeyOutput(): String { return "ListTestCaseUpdateEndpoint" }

    private fun getInitialDB(validDataTest: DataTest?): MutableMap<String, MutableList<Map<*, *>>> {
        //Init database
        val dbInitMap = mutableMapOf<String, MutableList<Map<*, *>>>()

        validDataTest?.input?.forEach {
            if (it.value is Map<*, *>) {
                val entityName = entity.attributes.find {
                        attr -> attr.name.equals(it.key, true)  }
                    ?.type?.replace("%", "") as String
                if(!dbInitMap.containsKey(entityName))
                    dbInitMap[entityName] = mutableListOf()
                dbInitMap[entityName]?.add((it.value as Map<*, *>))
            }
        }
        return dbInitMap
    }

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

        val dbInitMap = mutableMapOf<String, Any?>()
        dbInitMap[entity.name] = listOf(inputOne)
        val dbInitDependenciesInputTwo = getInitialDB(positiveDataTwo)
        dbInitMap.putAll(dbInitDependenciesInputTwo)

        val endpointUpdate = testArchitecture.endpoints["update"]?.replace("{id}", idAttribute)
        val id = inputParamMethod[idAttribute]
        val endpointUpdateWithId = testArchitecture.endpoints["update"]?.replace("{id}", id.toString())

        val expectedResult =
            "Update an instance of ${entity.name} and confirm that the method ${script.location} mapped by PUT $endpointUpdate updates the instance when all data is valid, resulting in $output"
        val positiveTestCase = TestCase(input, script, expectedResult)
        positiveTestCase.preconditions.add("The ${entity.name} input ${input.input} must be body parameter. Only the $idAttribute must be in the url")
        positiveTestCase.preconditions.add("The repository must have the instances that will be retrieved as following: $dbInitMap")

        val steps = ArrayList<String>()
        steps.addAll(script.inputs.map { "Inform ${it.name}" })
        val requestParameter = extractWithoutFirstParam(controllerLayerMethod)

        steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by PUT $endpointUpdateWithId in ${testArchitecture.controllerLayerClass} class")
        steps.add("The input request for the PUT endpoint must be strictly: $requestParameter")

        steps.add("Verify attributes of updated instance of ${entity.name}")
        steps.add("Verify that the endpoint $endpointUpdateWithId returns 200")
        steps.add("Verify that the endpoint $endpointUpdateWithId contains $output")
        positiveTestCase.steps.addAll(steps)
        return positiveTestCase
    }

    private fun createTestCaseInvalidMissingObject(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        //Recupera o primeiro objeto relacionado
        val attr = entity.attributes.find { it.type.contains("%") }
        //Se nao tiver objeto relacionado nao tem este TC
        if(attr == null)
            return null

        val validDataTest = ruleDataTest.dataTest.filter { it.isValidInput }
        //val entity = script.reference as Entity

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

        val dbInitMap = mutableMapOf<String, Any?>()
        dbInitMap[entity.name] = listOf(inputOne)

        val endpointUpdate = testArchitecture.endpoints["update"]?.replace("{id}", idAttribute)
        val id = inputParamMethod[idAttribute]
        val endpointUpdateWithId = testArchitecture.endpoints["update"]?.replace("{id}", id.toString())

        val expectedResult =
            "Update an instance of ${entity.name} and confirm that the method ${script.location} mapped by PUT $endpointUpdate fail to update the instance when missing related instances"
        val positiveTestCase = TestCase(input, script, expectedResult)
        positiveTestCase.preconditions.add("The ${entity.name} input ${input.input} must be body parameter. Only the $idAttribute must be in the url")
        positiveTestCase.preconditions.add("The repository must have the instances that will be retrieved as following: $dbInitMap")

        val steps = ArrayList<String>()
        steps.addAll(script.inputs.map { "Inform ${it.name}" })

        val attrs = entity.attributes.filter { !it.type.contains("%") }

        val inputAux = HashMap<String, Any?>()
        input.input?.forEach {
            inputAux[it.key] = it.value
        }

        attrs.forEach {
            inputAux.remove(it.name)
        }

        val attrsNull = inputAux.filter { it.value == null }

        attrsNull.forEach {
            inputAux.remove(it.key)
        }

        val idPairs = inputAux
            .filterValues { it is Map<*, *> }
            .flatMap { (_, value) ->
                (value as Map<*, *>)
                    .filterKeys { it is String && it.contains("id", ignoreCase = true) }
                    .map { it.key.toString() to it.value }
            }.toMap()
        val missingObject = idPairs.toString().replace("{", "").replace("}", "")
        val errorMessage = "Missing object $missingObject"
        val requestParameter = extractWithoutFirstParam(controllerLayerMethod)

        steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by PUT $endpointUpdateWithId in ${testArchitecture.controllerLayerClass} class")
        steps.add("The input request for the PUT endpoint must be strictly: $requestParameter")

        steps.add("Verify attributes of updated instance of ${entity.name}")
        steps.add("Verify that the endpoint $endpointUpdateWithId returns 400")
        steps.add("Verify that the endpoint $endpointUpdateWithId contains $errorMessage")
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
        negativeTestCase.preconditions.add("The repository must be empty")
        val steps = ArrayList<String>()

        steps.addAll(script.inputs.map { "Inform ${it.name}" })
        val requestParameter = extractWithoutFirstParam(controllerLayerMethod)

        steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by PUT $endpointUpdateWithId in ${testArchitecture.controllerLayerClass} class and return bad request code 400 with the following error message: ${negativeDataTest?.errorMessage}")
        steps.add("The input request for the PUT endpoint must be strictly: $requestParameter")

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
        negativeTestCase.preconditions.add("The repository must be empty")
        val steps = ArrayList<String>()
        val requestParameter = extractWithoutFirstParam(controllerLayerMethod)

        steps.addAll(script.inputs.map { "Inform ${it.name}" })

        steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by PUT $endpointUpdateWithId in ${testArchitecture.controllerLayerClass} class")
        steps.add("The input request for the PUT endpoint must be strictly: $requestParameter")
        steps.add("Verify that the endpoint $endpointUpdateWithId returns 400")
        steps.add("Verify that the endpoint $endpointUpdateWithId contains the message: $errorMessage")
        negativeTestCase.steps.addAll(steps)
        return negativeTestCase
    }


    override fun createTestCases(entityDataTest: ListDataTest, ruleDataTest: ListDataTest, invalidIDDataTest: DataTest?, script: Script, entity: Entity): List<TestCase> {
        val testCases = ArrayList<TestCase>()
        createTestCaseValid200(ruleDataTest, script, entity).let { script.testCases.add(it) }
        createTestCaseInvalidMissingObject(ruleDataTest, script, entity).let { it?.let { it1 -> script.testCases.add(it1) } }
        createTestCaseBadRequest400ID(invalidIDDataTest, script, entity).let { script.testCases.add(it) }
        return testCases
}

}
