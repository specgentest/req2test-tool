package req2test.tool.approach.filter.testcase.unit.rule

import req2test.tool.approach.core.Log
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.ListDataTest
import req2test.tool.approach.entity.Script
import req2test.tool.approach.entity.TestCase
import req2test.tool.approach.processor.generator.file.TestPlanGenerator

class ListMockTestCaseCreateACRuleFilter (data: MutableMap<String, Any?>): BaseListMockTCACRuleFilter(data){

    override fun filterKeyInput(): String { return "ListCreateACRuleMockScript" }
    override fun filterName(): String {

        return "ListMockTestCaseCreateACRuleFilter"
    }
    override fun filterKeyOutput(): String { return "ListMockTestCaseCreateACRule" }

    private fun createTestCaseValidAndNonDuplicateData(entityDataTest: ListDataTest, script: Script): TestCase? {
        val validDataTest = entityDataTest.dataTest.find { it.isValidInput }
        val entity = script.reference as Entity

        if(validDataTest != null) {
            val expectedResult =
                "Create an instance of ${entity.name} and confirm that the ${script.location} saves the instance when all data is valid and unique"

            val output = validDataTest.input
            val positiveTestCase = TestCase(validDataTest, script, expectedResult)
            positiveTestCase.preconditions.add("The repository must be empty")

            val operation = "Create${entity}_"
            val testName = "${baseTestName}${operation}ValidAndNonDuplicateData"
            positiveTestCase.testName = testName

            val steps = ArrayList<String>()
            steps.addAll(script.inputs.map { "Inform ${it.name}" })

            val attrs = getUniqueAttributes(entity).toMutableList()
            attrs.forEach { attr ->
                steps.add("Mock ${testArchitecture.findMethod}${attr.replaceFirstChar { it.uppercase() }} in ${testArchitecture.repositoryLayerClass} to return ${testArchitecture.findMethodReturnFalse}")
            }

            var attributeDependecies = entity.attributeDependencies
            attributeDependecies.forEach { attrEntity ->
                val attr = attrEntity.lowercase()
                val data = validDataTest.input?.get(attr)
                val type = entity.attributes.find { it.name == attr }?.type?.replace("%", "")
                steps.add("Mock findById in ${type}Repository to return Optional $attr, where ${attr}: $data")
            }

            steps.add("Mock ${testArchitecture.mapMethods["create"]} method in ${testArchitecture.repositoryLayerClass}, returning a mocked instance of ${entity.name} with informed data: $output")
            steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class and return a saved instance of ${entity.name}")
            steps.add("Verify attributes of created and saved instance of ${entity.name}: $output")
            steps.add("Verify mocks")
            positiveTestCase.steps.addAll(steps)
            return positiveTestCase
        }
        return null
    }

    private fun createTestCaseInvalid(entityDataTest: ListDataTest, script: Script): TestCase? {
        val validDataTest = entityDataTest.dataTest.find { !it.isValidInput }
        val entity = script.reference as Entity

        if(validDataTest != null) {
            val expectedResult = "${script.location} return ${testArchitecture.ruleError} with the following error message: ${validDataTest.errorMessage}"
            val negativeTestCase = TestCase(validDataTest, script, expectedResult)

            val operation = "Create${entity}_"
            val testName = "${baseTestName}${operation}InvalidData"
            negativeTestCase.testName = testName

            val steps = ArrayList<String>()
            steps.addAll(script.inputs.map { "Inform ${it.name}" })


            var attributeDependecies = entity.attributeDependencies
            attributeDependecies.forEach { attrEntity ->
                val attr = attrEntity.lowercase()
                val data = validDataTest.input?.get(attr)
                val type = entity.attributes.find { it.name == attr }?.type?.replace("%", "")
                steps.add("Mock findById in ${type}Repository to return Optional $attr, where ${attr}: $data")
            }

            steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass}")
            steps.add("Verify that ${testArchitecture.ruleError} is ${testArchitecture.ruleErrorAction} with the following error message: ${validDataTest.errorMessage}")
            steps.add("Verify that method ${testArchitecture.mapMethods["create"]}(${entity.name}) from ${testArchitecture.repositoryLayerClass} was not called")
            steps.add("Verify that the constructor ${testArchitecture.entityConstructor} was called")
            negativeTestCase.steps.addAll(steps)
            return negativeTestCase
        }
        return null
    }

    private fun createTestCasesCheckDuplicity(entityDataTest: ListDataTest, script: Script): List<TestCase> {
        val testCases = mutableListOf<TestCase>()
        val validDataTest = entityDataTest.dataTest.find { it.isValidInput }

        val entity = script.reference as Entity

        val uniqueAttributes = script.inputs.filter { it.dbValidations.contains("unique") }

        if(validDataTest != null) {

            uniqueAttributes.forEach { uniqueAttribute ->

                val steps = ArrayList<String>()
                steps.addAll(script.inputs.map { "Inform ${it.name}" })
                steps.add("Mock ${testArchitecture.findMethod}${uniqueAttribute.name.replaceFirstChar { it.uppercase() }} in ${testArchitecture.repositoryLayerClass} to return ${testArchitecture.findMethodReturnTrue}")
                val attrs = getUniqueAttributes(entity).toMutableList()
                attrs.remove(uniqueAttribute.name)
                attrs.forEach { attr ->
                    steps.add("Mock ${testArchitecture.findMethod}${attr.replaceFirstChar { it.uppercase() }} in ${testArchitecture.repositoryLayerClass} to return ${testArchitecture.findMethodReturnFalse}")
                }

                var attributeDependecies = entity.attributeDependencies
                attributeDependecies.forEach { attrEntity ->
                    val attr = attrEntity.lowercase()
                    val data = validDataTest.input?.get(attr)
                    val type = entity.attributes.find { it.name == attr }?.type?.replace("%", "")
                    steps.add("Mock findById in ${type}Repository to return Optional $attr, where ${attr}: $data")
                }

                steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass}")
                steps.add("Verify that ${testArchitecture.ruleError} is ${testArchitecture.ruleErrorAction} with the following error message: ${uniqueAttribute.name} already exists")
                steps.add("Verify that method ${testArchitecture.mapMethods["create"]}(${entity.name}) from ${testArchitecture.repositoryLayerClass} was not called")

                val expectedResult = "${script.location} does not save an instance of ${entity.name}. ${testArchitecture.ruleError} is ${testArchitecture.ruleErrorAction}"
                val uniqueAttributeTestCase = TestCase(validDataTest, script, expectedResult)

                val operation = "Create${entity}_"
                val testName = "${baseTestName}${operation}WithDuplicated" + uniqueAttribute.name.replaceFirstChar { it.uppercase() }
                uniqueAttributeTestCase.testName = testName

                uniqueAttributeTestCase.steps.addAll(steps)
                testCases.add(uniqueAttributeTestCase)

            }

        }
        return testCases
    }


    private fun createTestCasesCheckDBCommunication(entityDataTest: ListDataTest, script: Script): List<TestCase> {
        val testCases = mutableListOf<TestCase>()
        val validDataTest = entityDataTest.dataTest.find { it.isValidInput }

        val entity = script.reference as Entity

        val uniqueAttributes = script.inputs.filter { it.dbValidations.contains("unique") }

        if(validDataTest != null) {
            val steps = ArrayList<String>()
            uniqueAttributes.forEach { uniqueAttribute ->
                steps.addAll(script.inputs.map { "Inform ${it.name}" })
                getUniqueAttributes(entity).forEach { attr ->
                    steps.add("Mock ${testArchitecture.findMethod}${attr.replaceFirstChar { it.uppercase() }} to return ${testArchitecture.findMethodReturnFalse}")
                }
            }

            var attributeDependecies = entity.attributeDependencies
            attributeDependecies.forEach { attrEntity ->
                val attr = attrEntity.lowercase()
                val data = validDataTest.input?.get(attr)
                val type = entity.attributes.find { it.name == attr }?.type?.replace("%", "")
                steps.add("Mock findById in ${type}Repository to return Optional $attr, where ${attr}: $data")
            }

            steps.add("**Mock ${testArchitecture.mapMethods["create"]} method in ${testArchitecture.repositoryLayerClass} to return ${testArchitecture.dbErrorFramework} when try to save ${entity.name}**")
                steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass}")
                steps.add("Verify that ${testArchitecture.dbError} is ${testArchitecture.dbErrorAction}")

                val expectedResult = "${script.location} does not save an instance of ${entity.name}. ${testArchitecture.dbError} is ${testArchitecture.dbErrorAction}"
                val uniqueAttributeTestCase = TestCase(validDataTest, script, expectedResult)

                val operation = "Create${entity}_"
                val testName = "${baseTestName}${operation}DBCommunicationError"
                uniqueAttributeTestCase.testName = testName

                uniqueAttributeTestCase.steps.addAll(steps)
                testCases.add(uniqueAttributeTestCase)

        }
        return testCases
    }


    private fun createTestCasesAvoidCascade(entityDataTest: ListDataTest, script: Script): List<TestCase> {
        val testCases = mutableListOf<TestCase>()
        val validDataTest = entityDataTest.dataTest.find { it.isValidInput }
        val entity = script.reference as Entity

        val attrs = entity.attributeDependencies

        attrs.forEach { attr ->
                if (validDataTest != null) {
                    val missingObject = validDataTest.input?.get(attr.lowercase())

                    val regex = Regex("idCustomer=([^,}]+)")
                    val matchResult = regex.find(missingObject.toString())

                    var message = "Missing object id$attr=null"
                    if (matchResult != null) {
                        val id = matchResult.groupValues[1]
                        message = "Missing object id$attr=$id"
                    }

                    val messageError = message
                    val expectedResult = "${script.location} does not save an instance of ${entity.name}. ${testArchitecture.ruleError} is ${testArchitecture.ruleErrorAction}, with the following error message: $messageError"

                    val testCase = TestCase(validDataTest, script, expectedResult)
                    val operation = "Create${entity}_"
                    val testName = "${baseTestName}${operation}MissingObject" + attr.replaceFirstChar { it.uppercase() }
                    testCase.testName = testName

                    val steps = ArrayList<String>()
                    steps.addAll(script.inputs.map { "Inform ${it.name}" })
                    steps.add("Mock findById in ${attr}Repository to return Optional empty")

                    var attributeDependecies = entity.attributeDependencies.filter { it != attr }
                    attributeDependecies.forEach { attrEntity ->
                        val attr = attrEntity.lowercase()
                        val data = validDataTest.input?.get(attr)
                        val type = entity.attributes.find { it.name == attr }?.type?.replace("%", "")
                        steps.add("Mock findById in ${type}Repository to return Optional $attr, where ${attr}: $data")
                    }

                    steps.add("Call method ${testArchitecture.ruleLayerMethod} in ${testArchitecture.ruleLayerClass}")
                    steps.add("Verify that ${testArchitecture.ruleError} is ${testArchitecture.ruleErrorAction} with the following error message: $messageError")
                    steps.add("Verify that method ${testArchitecture.mapMethods["create"]}(${entity.name}) from ${testArchitecture.repositoryLayerClass} was not called")
                    steps.add("Verify that method ${testArchitecture.findMethod}Id from ${attr}Repository was called")

                    testCase.steps.addAll(steps)
                    testCases.add(testCase)
                }
            }
        return testCases
    }

    override fun createTestCases(entityDataTest: ListDataTest, ruleDataTest: ListDataTest, script: Script): List<TestCase> {
        val testCases = ArrayList<TestCase>()
        val positiveTestCase = createTestCaseValidAndNonDuplicateData(ruleDataTest, script)
        if (positiveTestCase != null)
            testCases.add(positiveTestCase)
        testCases.addAll(createTestCasesCheckDuplicity(ruleDataTest, script))
        testCases.addAll(createTestCasesCheckDBCommunication(ruleDataTest, script))
        testCases.addAll(createTestCasesAvoidCascade(ruleDataTest, script))
        createTestCaseInvalid(entityDataTest, script)?.let { testCases.add(it) }
        return testCases
}

    override fun compute(input: Any): Any? {

        var scripts = input as List<Script>

        scripts = scripts.filter { it.reference is Entity }

        scripts.forEach { script ->

            testArchitecture = script.testArchitecture!!

            val listEntitiesDataTest = data["ListEntitiesDataTest"] as List<ListDataTest>

            val listRuleDataTest = data["ListRuleDataTest"] as List<ListDataTest>
            val ruleDataTest = listRuleDataTest.find { (it.reference as ListDataTest).reference == script.reference }
            val entityDataTest = listEntitiesDataTest.find { (it.reference  == script.reference) }

            if (ruleDataTest == null)
                logs.add(
                    Log.createCriticalError(
                        "ListMockTestCaseCreateACRuleFilter",
                        "RuleDataTest not found for ${script.reference}"
                    )
                )
            else if(entityDataTest == null)
                logs.add(
                    Log.createCriticalError(
                        "ListMockTestCaseCreateACRuleFilter",
                        "EntityDataTest not found for ${script.reference}"
                    )
                )
            else {

                val positiveTestCase = createTestCaseValidAndNonDuplicateData(ruleDataTest, script)
                if (positiveTestCase != null)
                    script.testCases.add(positiveTestCase)

                script.testCases.addAll(createTestCasesCheckDuplicity(ruleDataTest, script))
                script.testCases.addAll(createTestCasesCheckDBCommunication(ruleDataTest, script))
                script.testCases.addAll(createTestCasesAvoidCascade(ruleDataTest, script))

                createTestCaseInvalid(entityDataTest, script)?.let { script.testCases.add(it) }
            }
        }
            //Exportando dados para .txt
            scripts.forEach { entityScript ->

                var textTestCases = ""
                var ctCount = 1
                entityScript.testCases.forEach {tc ->
                    var tcDescription = tc.expectedResult

                    textTestCases += "TC-${ctCount++}\t\t\t\t\t\t| ${tcDescription}\n"
                    textTestCases += "preconditions\t\t\t\t| ${tc.preconditions}\n"
                    textTestCases += "Input\t\t\t\t\t\t| ${tc.dataTest.input?.map { "\n\t\t\t\t\t\t\t${it.key}: ${it.value} " }}\n"
                    textTestCases += "Steps\t\t\t\t\t\t| ${tc.steps.map { "\n\t\t\t\t\t\t\t$it" }}\n"
                    textTestCases += "Expected Result\t\t\t\t| ${tc.expectedResult}\n\n"
                }

                val entity = entityScript.reference as Entity
                TestPlanGenerator.createFile(textTestCases , "outputArtifacts/", "Create${entity.name}TestCases.txt")
            }

        return scripts
    }

}
