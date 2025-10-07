package req2test.tool.approach.filter.testcase.integration.entity.controller

import req2test.tool.approach.entity.*

open class ListTestCaseCreateEndpointFilter (data: MutableMap<String, Any?>): BaseListTCEndpointFilter(data){

    override fun filterKeyInput(): String { return "ListCreateEndpointScript" }
    override fun filterName(): String {

        return "ListTestCaseCreateEndpointFilter"
    }
    override fun filterKeyOutput(): String { return "ListTestCaseCreateEndpoint" }

    private fun getInitialDB(validDataTest: DataTest?): MutableMap<String, MutableList<Map<*, *>>> {
        //Init database
        val dbInitMap = mutableMapOf<String, MutableList<Map<*, *>>>()

        validDataTest?.input?.forEach {
            if (it.value is Map<*, *>) {
                val entityName: String? = entity.attributes.find {
                        attr -> attr.name.equals(it.key, true)  }
                    ?.type?.replace("%", "")
                if(entityName != null) {
                    if (!dbInitMap.containsKey(entityName))
                        dbInitMap[entityName] = mutableListOf()
                    dbInitMap[entityName]?.add((it.value as Map<*, *>))
                }
            }
        }
        return dbInitMap
    }

    private fun getOutputDB(validDataTest: DataTest?): Map<String, Any?>? {
        val output = validDataTest?.input?.map { (key, value) ->
            var newValue = value
            val input = entity.attributes.find { it.name == key }
            if(input?.generatedFormula != null) {
                if (input.generated) {
                    newValue = input.generatedFormula
                }
            }
            key to newValue
        }?.toMap()
        return output
    }

    private fun createTestCaseValid201(entityDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = entityDataTest.dataTest.find { it.isValidInput }

        if(validDataTest != null) {
            val expectedResult =
                "Create an instance of ${entity.name} and confirm that the method ${script.location} mapped by POST ${endpoints["create"]} saves the instance when all data is valid and unique"
            val positiveTestCase = TestCase(validDataTest, script, expectedResult)

            val dbInitMap = getInitialDB(validDataTest)
            val output = getOutputDB(validDataTest)
            positiveTestCase.preconditions.add("Initialize the database with the following data: $dbInitMap")


            val steps = ArrayList<String>()
            steps.addAll(script.inputs.map { "Inform ${it.name}" })

            val requestParameter = extractBetweenParentheses(controllerLayerMethod)
            steps.add("Call method ${controllerLayerMethod} mapped by POST ${endpoints["create"]} in ${testArchitecture.controllerLayerClass} class")
            steps.add("The input request for the POST endpoint must be strictly: $requestParameter")
            steps.add("Verify that the endpoint ${endpoints["create"]} returns 201")
            steps.add("Verify that the endpoint ${endpoints["create"]} contains $output")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }


    private fun createTestCasesCheckDuplicity(entityDataTest: ListDataTest, script: Script, entity: Entity): List<TestCase> {
        val testCases = mutableListOf<TestCase>()
        val validDataTest = entityDataTest.dataTest.find { it.isValidInput }

        val uniqueAttributes = script.inputs.filter { it.dbValidations.contains("unique") }

        if(validDataTest != null) {

            uniqueAttributes.forEach { uniqueAttribute ->

                val dbInitMap = getOutputDB(validDataTest)

                val expectedResult = "Fail to create duplicated instance of ${entity.name} using the method ${script.location} mapped by POST ${endpoints["create"]}"
                val uniqueAttributeTestCase = TestCase(validDataTest, script, expectedResult)

                uniqueAttributeTestCase.preconditions.add("Initialize the database with the following data: $dbInitMap")

                val steps = ArrayList<String>()
                steps.addAll(script.inputs.map { "Inform ${it.name}" })
                val requestParameter = extractBetweenParentheses(controllerLayerMethod)

                steps.add("Call method ${controllerLayerMethod} mapped by POST ${endpoints["create"]} in ${testArchitecture.controllerLayerClass} class")
                steps.add("The input request for the POST endpoint must be strictly: $requestParameter")
                steps.add("Verify that the endpoint ${endpoints["create"]} returns 400")

                val errorMessage = "${uniqueAttribute.name} already exists"
                steps.add("Verify that the endpoint ${endpoints["create"]} returns contains the following error message: $errorMessage")

                uniqueAttributeTestCase.steps.addAll(steps)
                testCases.add(uniqueAttributeTestCase)
            }

        }
        return testCases
    }



    private fun createTestCaseBadRequest400MissingObjectInDB(entityDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = entityDataTest.dataTest.find { it.isValidInput }

        if(validDataTest != null) {
            val expectedResult =
                "Create an instance of ${entity.name} and confirm that the method ${script.location} mapped by POST ${endpoints["create"]} saves the instance when all data is valid and unique"
            val positiveTestCase = TestCase(validDataTest, script, expectedResult)

            //Init database
            val dbInitMap = getInitialDB(validDataTest)

            //O caso de teste só existe se tem relacionamento com outras entidades
            if(dbInitMap.isEmpty())
                return null

            val removed = removeFirstFromFirstGroup(dbInitMap)
            val removedObject = removed?.second
            val removedObjectId = removedObject
                ?.entries
                ?.firstOrNull { (it.key as String).contains("id") }
                ?.let { "${it.key}=${it.value}" }

            positiveTestCase.preconditions.add("Initialize the database with the following data: $dbInitMap")

            val steps = ArrayList<String>()
            steps.addAll(script.inputs.map { "Inform ${it.name}" })

            val requestParameter = extractBetweenParentheses(controllerLayerMethod)

            steps.add("Call method ${controllerLayerMethod} mapped by POST ${endpoints["create"]} in ${testArchitecture.controllerLayerClass} class")
            steps.add("The input request for the POST endpoint must be strictly: $requestParameter")
            steps.add("Verify that the endpoint ${endpoints["create"]} returns 400")
            steps.add("Verify that the endpoint ${endpoints["create"]} contains:  Missing object $removedObjectId")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }

    private fun removeFirstFromFirstGroup(
        data: MutableMap<String, MutableList<Map<*, *>>>
    ): Pair<String, Map<*, *>>? {
        if (data.isEmpty()) return null

        val (key, list) = data.entries.first()
        val removed = list.removeAt(0)
        return key to removed
    }

    private fun createTestCaseBadRequest400(entityDataTest: ListDataTest, script: Script, entity: Entity): TestCase? {
        val validDataTest = entityDataTest.dataTest.find { !it.isValidInput }

        if(validDataTest != null) {
            val expectedResult = "${script.location} mapped by POST ${endpoints["create"]} returns bad request 400 with the following error message: ${validDataTest.errorMessage}"
            val negativeTestCase = TestCase(validDataTest, script, expectedResult)
            val dbInitMap = getInitialDB(validDataTest)
            negativeTestCase.preconditions.add("Initialize the database with the following data: $dbInitMap")

            val steps = ArrayList<String>()
            steps.addAll(script.inputs.map { "Inform ${it.name}" })
            val requestParameter = extractBetweenParentheses(controllerLayerMethod)

            steps.add("Call method ${controllerLayerMethod} mapped by POST ${endpoints["create"]} in ${testArchitecture.controllerLayerClass} class")
            steps.add("The input request for the POST endpoint must be strictly: $requestParameter")
            steps.add("Verify that the endpoint ${endpoints["create"]} returns 400")
            steps.add("Verify that the endpoint ${endpoints["create"]} contains the message: ${validDataTest.errorMessage}")

            negativeTestCase.steps.addAll(steps)
            return negativeTestCase
        }
        return null
    }

    override fun createTestCases(entityDataTest: ListDataTest, ruleDataTest: ListDataTest, invalidIDDataTest: DataTest?, script: Script, entity: Entity): List<TestCase> {
        val testCases = ArrayList<TestCase>()
        createTestCaseValid201(ruleDataTest, script, entity)?.let { script.testCases.add(it) }
        createTestCasesCheckDuplicity(ruleDataTest, script, entity).let { script.testCases.addAll(it) }
        createTestCaseBadRequest400MissingObjectInDB(ruleDataTest, script, entity)?.let { script.testCases.add(it) }
        createTestCaseBadRequest400(entityDataTest, script, entity)?.let { script.testCases.add(it) }
        return testCases
    }
}
