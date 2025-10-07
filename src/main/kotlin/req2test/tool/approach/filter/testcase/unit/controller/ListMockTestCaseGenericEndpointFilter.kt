package req2test.tool.approach.filter.testcase.unit.controller

import req2test.tool.approach.filter.ac.rule.type.RuleVariable
import req2test.tool.approach.processor.generator.file.TestPlanGenerator
import req2test.tool.approach.entity.*

open class ListMockTestCaseGenericEndpointFilter (data: MutableMap<String, Any?>): BaseListMockTCEndpointFilter(data){

    override fun filterKeyInput(): String { return "ListGenericEndpointMockScript" }
    override fun filterName(): String {

        return "ListMockTestCaseGenericEndpointFilter"
    }
    override fun filterKeyOutput(): String { return "ListMockTestCaseGenericEndpoint" }

    private fun createTestCaseValid200(genericRuleDataTest: ListDataTest?, script: Script): List<TestCase> {

        val validDataTest = genericRuleDataTest?.dataTest?.find { it.isValidInput }

        val testCases = mutableListOf<TestCase>()

        val rule = script.reference as Rule
        val description = rule.attributes["description"] as String
        val methodLocation = script.location
        val endpoint = testArchitecture.endpoints["endpoint"]

            if (validDataTest != null) {
                val attributeOutput = (rule.attributes["output"] as List<RuleVariable>).first()
                val attributeOutputName = attributeOutput.name
                val attributeOutputType = attributeOutput.type
                val output = validDataTest.input?.get(attributeOutputName)

                val expectedResult =
                    "$description and confirm that the method ${methodLocation} mapped by POST ${endpoint} returns $output"
                val positiveTestCase = TestCase(validDataTest, script, expectedResult)

                val steps = ArrayList<String>()
                steps.addAll(script.inputs.map { "Inform ${it.name}" })


                steps.add("Mock ${ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class, returning a mocked instance of $attributeOutputType with informed data: $output")
                steps.add("Call method ${controllerLayerMethod} mapped by POST ${endpoint} in ${testArchitecture.controllerLayerClass} class and return $attributeOutputType: $output")
                steps.add("Verify that ${ruleLayerMethod} was called")
                steps.add("Verify that the endpoint ${endpoint} returns 200")
                steps.add("Verify that the endpoint ${endpoint} contains $output")
                positiveTestCase.steps.addAll(steps)
                testCases.add(positiveTestCase)
            }

        return testCases
    }

    private fun createTestCaseBadRequest400(genericRuleDataTest: ListDataTest?, script: Script): List<TestCase> {
        val validDataTest = genericRuleDataTest?.dataTest?.find { !it.isValidInput }

        val testCases = mutableListOf<TestCase>()

        val rule = script.reference as Rule
        val description = rule.attributes["description"] as String
        val methodLocation = script.location
        val endpoint = testArchitecture.endpoints["endpoint"]

        if (validDataTest != null) {
            val attributeOutput = (rule.attributes["output"] as List<RuleVariable>).first()
            val attributeOutputName = attributeOutput.name
            val attributeOutputType = attributeOutput.type
            val output = validDataTest.input?.get(attributeOutputName)

            val expectedResult = "${methodLocation} mapped by POST ${endpoint} returns bad request 400 with the following error message: ${validDataTest.errorMessage}"

            val negativeTestCase = TestCase(validDataTest, script, expectedResult)

            val steps = ArrayList<String>()
            steps.addAll(script.inputs.map { "Inform ${it.name}" })


            steps.add("Mock ${ruleLayerMethod} in ${testArchitecture.ruleLayerClass} class  to return ${testArchitecture.ruleErrorAction} ${testArchitecture.ruleError} with the following error message: ${validDataTest.errorMessage}")
            steps.add("Call method ${controllerLayerMethod} mapped by POST ${endpoint} in ${testArchitecture.controllerLayerClass} class and return bad request code 400 with the following error message: ${validDataTest.errorMessage}")
            steps.add("Verify that ${ruleLayerMethod} was called")
            steps.add("Verify that the endpoint ${endpoint} returns 400")
            steps.add("Verify that the endpoint ${endpoint} contains the message: ${validDataTest.errorMessage}")
            negativeTestCase.steps.addAll(steps)
            testCases.add(negativeTestCase)
        }

        return testCases
    }

    override fun createTestCases(entityDataTest: ListDataTest, ruleDataTest: ListDataTest, invalidIDDataTest: DataTest?, script: Script, entity: Entity): List<TestCase> {
        val testCases = ArrayList<TestCase>()
        return testCases
    }

    fun createTestCases(genericRuleDataTest: ListDataTest?, script: Script): List<TestCase> {
        val testCases = ArrayList<TestCase>()
        createTestCaseValid200(genericRuleDataTest, script)?.let { script.testCases.addAll(it) }
        createTestCaseBadRequest400(genericRuleDataTest, script)?.let { script.testCases.addAll(it) }
        return testCases
    }

    override fun filterScripts(scripts: List<Script>): List<Script> {
        return scripts.filter { it.reference is Rule }
    }

    override fun compute(input: Any): Any? {

        var scripts = input as List<Script>

        scripts = filterScripts(scripts)

        scripts.forEach { script ->

            testArchitecture = script.testArchitecture!!
            setEndpoints()
            setRuleLayerMethod()
            setControllerLayerMethod()

            //Indica que é um script de ACRule com Validation
            if(script.reference is Rule) {
                val listGenericRulesDataTest = data["ListGenericRulesDataTest"] as List<ListDataTest>
                val ruleReference = script.reference
                val genericRuleDataTest  = listGenericRulesDataTest.find { it.reference == ruleReference }
                script.testCases.addAll(createTestCases(genericRuleDataTest, script))
            }

        }
        //Exportando dados para .txt
        scripts.forEach { script ->
            var textTestCases = ""
            var ctCount = 1
            script.testCases.forEach {tc ->
                var tcDescription = tc.expectedResult

                textTestCases += "TC-${ctCount++}\t\t\t\t\t\t| ${tcDescription}\n"
                textTestCases += "preconditions\t\t\t\t| ${tc.preconditions}\n"
                val rule = script.reference as Rule
                val inputs = (rule.attributes["input"] as List<RuleVariable>).map { it.name }
                println(inputs)
                val variablesInput = tc.dataTest.input
                    ?.filter { inputs.contains(it.key) }
                    ?.map { "\n\t\t\t\t\t\t\t${it.key}: ${it.value} " }

                textTestCases += "Input\t\t\t\t\t\t| ${variablesInput}\n"
                textTestCases += "Steps\t\t\t\t\t\t| ${tc.steps.map { "\n\t\t\t\t\t\t\t$it" }}\n"
                textTestCases += "Expected Result\t\t\t\t| ${tc.expectedResult}\n\n"
            }
                val ruleID = (script.reference as Rule).attributes["id"] as String
                TestPlanGenerator.createFile(textTestCases , "outputArtifacts/", "Generic${ruleID}EndpointTC.txt")
        }
        return scripts
    }
}
