package req2test.tool.approach.filter.testcase.integration.entity.controller

import req2test.tool.approach.entity.*

open class ListTestCasePartialUpdateEndpointFilter (data: MutableMap<String, Any?>): BaseListTCEndpointFilter(data){

    override fun filterKeyInput(): String { return "ListPartialUpdateEndpointScript" }
    override fun filterName(): String {

        return "ListTestCasePartialUpdateEndpointFilter"
    }
    override fun filterKeyOutput(): String { return "ListTestCasePartialUpdateEndpoint" }

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

    private fun createTestCasePartialUpdateInstanceForOneAttribute(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase {
        val validDataTest = ruleDataTest.dataTest.filter { it.isValidInput }
        //val entity = script.reference as Entity

        val notGeneratedAttributes = entity.attributes.filter { !it.generatedDB and !it.generatedConstructor }
        //val generatedAttributes = entity.attributes.filter { it.generatedDB and it.generatedConstructor }

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

        var isFirstIteration = true
        notGeneratedAttributes.forEach {
            if(isFirstIteration)
                inputParamMethod[it.name] = positiveDataTwo.input?.get(it.name)
            else
                inputParamMethod[it.name] = null
            isFirstIteration = false
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

        val dbInitMap = mutableMapOf<String, Any?>()
        dbInitMap[entity.name] = listOf(inputOne)
        val dbInitDependenciesInputTwo = getInitialDB(positiveDataTwo)
        dbInitMap.putAll(dbInitDependenciesInputTwo)

        val endpointPartialUpdate = testArchitecture.endpoints["partial update"]?.replace("{id}", idAttribute)
        val id = inputParamMethod[idAttribute]
        val endpointPartialUpdateWithId = testArchitecture.endpoints["partial update"]?.replace("{id}", id.toString())

        val expectedResult =
            "Update an instance of ${entity.name} and confirm that the method ${script.location} mapped by PATCH $endpointPartialUpdate updates the instance when all data is valid, resulting in $output"
        val positiveTestCase = TestCase(input, script, expectedResult)
        positiveTestCase.preconditions.add("The ${entity.name} input must be body parameter. Only the $idAttribute must be in the url")
        positiveTestCase.preconditions.add("The repository must have the instances that will be retrieved as following: $dbInitMap")

        val steps = ArrayList<String>()
        steps.addAll(script.inputs.map { "Inform ${it.name}" })

        val requestParameter = extractBetweenParentheses(controllerLayerMethod)

        steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by PATCH $endpointPartialUpdateWithId in ${testArchitecture.controllerLayerClass} class")
        steps.add("The input request for the PATCH endpoint must be strictly: $requestParameter")
        steps.add("Verify attributes of updated instance of ${entity.name}")
        steps.add("Verify that the endpoint PATCH $endpointPartialUpdateWithId returns 200")
        steps.add("Verify that the endpoint PATCH $endpointPartialUpdateWithId contains $output")
        positiveTestCase.steps.addAll(steps)
        return positiveTestCase
    }

    private fun createTestCasePartialUpdateInstanceForManyAttribute(ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase {
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

        notGeneratedAttributes.forEach {
            inputParamMethod[it.name] = positiveDataTwo.input?.get(it.name)
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

        val dbInitMap = mutableMapOf<String, Any?>()
        dbInitMap[entity.name] = listOf(inputOne)
        val dbInitDependenciesInputTwo = getInitialDB(positiveDataTwo)
        dbInitMap.putAll(dbInitDependenciesInputTwo)

        val endpointPartialUpdate = testArchitecture.endpoints["partial update"]?.replace("{id}", idAttribute)
        val id = inputParamMethod[idAttribute]
        val endpointPartialUpdateWithId = testArchitecture.endpoints["partial update"]?.replace("{id}", id.toString())

        val expectedResult =
            "Update an instance of ${entity.name} and confirm that the method ${script.location} mapped by PATCH $endpointPartialUpdate updates the instance when all data is valid, resulting in $output"
        val positiveTestCase = TestCase(input, script, expectedResult)
        positiveTestCase.preconditions.add("The ${entity.name} input must be body parameter. Only the $idAttribute must be in the url")
        positiveTestCase.preconditions.add("The repository must have the instances that will be retrieved as following: $dbInitMap")

        val steps = ArrayList<String>()
        steps.addAll(script.inputs.map { "Inform ${it.name}" })

        val requestParameter = extractBetweenParentheses(controllerLayerMethod)

        steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by PATCH $endpointPartialUpdateWithId in ${testArchitecture.controllerLayerClass} class")
        steps.add("The input request for the PATCH endpoint must be strictly: $requestParameter")

        steps.add("Verify attributes of updated instance of ${entity.name}")
        steps.add("Verify that the endpoint PATCH $endpointPartialUpdateWithId returns 200")
        steps.add("Verify that the endpoint PATCH $endpointPartialUpdateWithId contains $output")
        positiveTestCase.steps.addAll(steps)
        return positiveTestCase
    }


    private fun createTestCaseBadRequest400(entityDataTest: ListDataTest, ruleDataTest: ListDataTest, script: Script, entity: Entity): TestCase {
        val negativeDataTest = entityDataTest.dataTest.find { !it.isValidInput }

        val validDataTest = ruleDataTest.dataTest.filter { it.isValidInput }

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
        inputTwo[idAttribute] = positiveDataOne.input?.get(idAttribute)

        val input = DataTest(inputParamMethod, true, "", "")

        val endpointPartialUpdate = testArchitecture.endpoints["partial update"]?.replace("{id}", idAttribute)
        val id = inputParamMethod[idAttribute]
        val endpointPartialUpdateWithId = testArchitecture.endpoints["partial update"]?.replace("{id}", id.toString())

        val expectedResult = "${script.location} mapped by PATCH $endpointPartialUpdate returns bad request 400 with the following error message: ${negativeDataTest?.errorMessage}"

        val negativeTestCase = TestCase(input, script, expectedResult)
        negativeTestCase.preconditions.add("The ${entity.name} input must be body parameter. Only the $idAttribute must be in the url")
        val steps = ArrayList<String>()

        steps.addAll(script.inputs.map { "Inform ${it.name}" })

        val requestParameter = extractBetweenParentheses(controllerLayerMethod)

        steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by PATCH $endpointPartialUpdateWithId in ${testArchitecture.controllerLayerClass} class and return bad request code 400 with the following error message: ${negativeDataTest?.errorMessage}")
        steps.add("The input request for the PATCH endpoint must be strictly: $requestParameter")

        steps.add("Verify that the endpoint $endpointPartialUpdateWithId returns 400")
        steps.add("Verify that the endpoint $endpointPartialUpdateWithId contains the message: ${negativeDataTest?.errorMessage}")
        negativeTestCase.steps.addAll(steps)
        return negativeTestCase
    }

    private fun createTestCaseBadRequest400ID(invalidIDDataTest: DataTest?, script: Script, entity: Entity): TestCase {
        val negativeDataTest = invalidIDDataTest
        val notGeneratedAttributes = entity.attributes.filter { !it.generatedDB and !it.generatedConstructor }
        val inputParamMethod = HashMap<String, Any?>()

        notGeneratedAttributes.forEach {
            inputParamMethod[it.name] = negativeDataTest?.input?.get(it.name)
        }

        val idAttribute = entity.attributes.find { it.name.startsWith("id") }?.name as String

        inputParamMethod[idAttribute] = negativeDataTest?.input?.get(idAttribute)
        val input = DataTest(inputParamMethod, false, negativeDataTest?.errorMessage, "")

        val endpointPartialUpdate = testArchitecture.endpoints["partial update"]?.replace("{id}", idAttribute)
        val id = inputParamMethod[idAttribute]
        val endpointPartialUpdateWithId = testArchitecture.endpoints["partial update"]?.replace("{id}", id.toString())

        val errorMessage = "invalid $idAttribute type"
        val expectedResult = "${script.location} mapped by PATCH $endpointPartialUpdate returns bad request 400 with the following error message: $errorMessage"

        val negativeTestCase = TestCase(input, script, expectedResult)
        val steps = ArrayList<String>()

        steps.addAll(script.inputs.map { "Inform ${it.name}" })

        val requestParameter = extractBetweenParentheses(controllerLayerMethod)
        steps.add("Call method ${testArchitecture.controllerLayerMethod} mapped by PATCH $endpointPartialUpdateWithId in ${testArchitecture.controllerLayerClass} class")
        steps.add("The input request for the PATCH endpoint must be strictly: $requestParameter")

        steps.add("Verify that the endpoint PATCH $endpointPartialUpdateWithId returns 400")
        steps.add("Verify that the endpoint PATCH $endpointPartialUpdateWithId contains the message: $errorMessage")
        negativeTestCase.steps.addAll(steps)
        return negativeTestCase
    }


    override fun createTestCases(entityDataTest: ListDataTest, ruleDataTest: ListDataTest, invalidIDDataTest: DataTest?, script: Script, entity: Entity): List<TestCase> {
        val testCases = ArrayList<TestCase>()
        createTestCasePartialUpdateInstanceForOneAttribute(ruleDataTest, script, entity).let { script.testCases.add(it) }
        createTestCasePartialUpdateInstanceForManyAttribute(ruleDataTest, script, entity).let { script.testCases.add(it) }
        createTestCaseBadRequest400ID(invalidIDDataTest, script, entity).let { script.testCases.add(it) }

        return testCases
    }

}
