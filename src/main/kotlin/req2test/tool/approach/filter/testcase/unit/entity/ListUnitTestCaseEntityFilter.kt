package req2test.tool.approach.filter.testcase.unit.entity

import req2test.tool.approach.core.Filter
import req2test.tool.approach.core.Log
import req2test.tool.approach.core.TestArchitecture
import req2test.tool.approach.processor.generator.file.TestPlanGenerator
import req2test.tool.approach.entity.*

class ListUnitTestCaseEntityFilter (data: MutableMap<String, Any?>): Filter(data, null){
    lateinit var testArchitecture: TestArchitecture

    override fun filterKeyInput(): String { return "ListEntitiesDataTest" }
    override fun filterName(): String {

        return "ListTestCaseEntityUnitFilter"
    }
    override fun filterKeyOutput(): String { return "ListScriptTestCasesEntity" }

    private fun createTestCaseVerifyValidateMethodConstructor(positiveDataOne: DataTest?, script: Script): TestCase {
        val entity = script.reference as Entity
        val notGeneratedAttributes = entity.attributes.filter { !it.generatedDB and !it.generatedConstructor }

        val inputOne = HashMap<String, Any?>()

        notGeneratedAttributes.forEach {
            inputOne[it.name] = positiveDataOne?.input?.get(it.name)
        }

        val input = DataTest(inputOne, true, "", "")

        val expectedResult = "Instance of ${entity.name} created and validate() method called"

        val positiveTestCase = TestCase(input, script, expectedResult)
        positiveTestCase.technique = Technique.WHITEBOX

        val steps = ArrayList<String>()
        steps.add("Inform ${input.input}")

        steps.add("Call the constructor ${testArchitecture.entityConstructor} with the following: $inputOne")
        steps.add("Verify that ${testArchitecture.ruleError} was not called")
        steps.add("Verify all attributes")
        steps.add("Verify that method validate() was called")
        positiveTestCase.steps.addAll(steps)
        return positiveTestCase
    }

    private fun createTestCaseVerifyValidateFailMethodConstructor(negativeData: DataTest?, script: Script): TestCase {
        val entity = script.reference as Entity
        val notGeneratedAttributes = entity.attributes.filter { !it.generatedDB and !it.generatedConstructor }

        val inputOne = HashMap<String, Any?>()

        notGeneratedAttributes.forEach {
            inputOne[it.name] = negativeData?.input?.get(it.name)
        }

        val input = DataTest(inputOne, true, "", "")

        val expectedResult = "Instance of ${entity.name} was not created and validate() method was called"

        val negativeTestCase = TestCase(input, script, expectedResult)
        negativeTestCase.technique = Technique.WHITEBOX

        val steps = ArrayList<String>()
        steps.add("Inform ${input.input}")

        steps.add("Call the constructor ${testArchitecture.entityConstructor} with the following: $inputOne")
        steps.add("Verify that ${testArchitecture.ruleError} was called whit the following message: ${input.errorMessage}")
        steps.add("Verify that method validate() was called")
        negativeTestCase.steps.addAll(steps)
        return negativeTestCase
    }

    override fun compute(input: Any): Any? {
        val listEntitiesDataTest = input as List<ListDataTest>
        var entitiesScript = data["ListUnitEntityScript"]
        if(entitiesScript == null)
            logs.add(Log.createCriticalError("ListUnitTestCaseEntityFilter", "ListUnitEntityScript not found"))

        entitiesScript = entitiesScript as List<Script>

        entitiesScript.forEach {entityScript ->
            testArchitecture = entityScript.testArchitecture!!
            val entityDataTest = listEntitiesDataTest.find { it.reference == entityScript.reference }
            if(entityDataTest == null)
                logs.add(Log.createCriticalError("ListUnitTestCaseEntityFilter", "EntityDataTest not found for ${entityScript.reference}"))
            else {

                //Creating white box testing
                //We use another class only to automate this white box test case
                val positiveDataTest = entityDataTest.dataTest.find { it.isValidInput }
                val negativeDataTest = entityDataTest.dataTest.find { !it.isValidInput }
                createTestCaseVerifyValidateMethodConstructor(positiveDataTest, entityScript).let { entityScript.testCases.add(it) }
                createTestCaseVerifyValidateFailMethodConstructor(negativeDataTest, entityScript).let { entityScript.testCases.add(it) }

                //Creating black box testing - We use allDataTest to create black box automated testing
                entityScript.allDataTest = entityDataTest.convertToJson()
                val entity = entityScript.reference as Entity
                entityDataTest.dataTest.forEach {
                    var expectedResult = ""
                    if(it.isValidInput)
                        expectedResult = "${entity.name} created"
                    else
                        expectedResult = it.errorMessage as String

                    val testCase = TestCase(it, entityScript, expectedResult)
                    testCase.steps.addAll(entityScript.steps)
                    entityScript.testCases.add(testCase)
                }
                //Generated attribute - caixa preta
                val attributes = entity.attributes.filter { it.generatedConstructor }
                attributes.forEach { attribute ->
                    val validDataTest = entityDataTest.dataTest.find { it.isValidInput }
                    println(entityDataTest.convertToJson())
                    if(validDataTest != null) {
                        val expectedResult = attribute.generatedFormula as String
                        val testCase = TestCase(validDataTest, entityScript, expectedResult, attribute)
                        testCase.steps.addAll(entityScript.steps)
                        entityScript.testCasesAttributeGenerated.add(testCase)
                    }
                }
            }
        }

        throwsCriticalErrors()

        //Exportando dados para .txt
        entitiesScript.forEach { entityScript ->

            val ent = entityScript.reference as Entity

            var textTestCases = ""
            var ctCount = 0
            entityScript.testCases.forEach {tc ->
                var tcDescription = tc.dataTest.className

                textTestCases += "TC-${ctCount++}\t\t\t\t\t\t| ${tcDescription}\n"
                textTestCases += "Technique\t\t\t\t\t| ${tc.technique.value}\n"
                textTestCases += "Input\t\t\t\t\t\t| ${tc.dataTest.input?.map { "\n\t\t\t\t\t\t\t${it.key}: ${it.value} " }}\n"
                if(tc.steps.isNotEmpty())
                    textTestCases += "Steps\t\t\t\t\t\t| ${tc.steps.map { "\n\t\t\t\t\t\t\t$it" }}\n"
                textTestCases += "Expected Result\t\t\t\t| ${tc.expectedResult}\n\n"
            }

            var textTestCasesAttributeGenerated = ""
            entityScript.testCasesAttributeGenerated.forEach {tc ->
                var tcDescription = ""
                if(tc.dataTest.isValidInput)
                    tcDescription = "Must create an instance of ${ent.name}"
                else
                    tcDescription = tc.dataTest.className

                textTestCasesAttributeGenerated += "TC-${ctCount++}\t\t\t\t\t\t| ${tcDescription}\n"
                textTestCasesAttributeGenerated += "Input\t\t\t\t\t\t| ${tc.dataTest.input?.map { "\n\t\t\t\t\t\t\t${it.key}: ${it.value} " }}\n"
                textTestCasesAttributeGenerated += "Expected Result\t\t\t\t| ${tc.expectedResult}\n\n"
            }

            val entity = entityScript.reference as Entity
            TestPlanGenerator.createFile(textTestCases , "outputArtifacts/", "${entity.name}TestCases.txt")
            TestPlanGenerator.createFile(textTestCasesAttributeGenerated , "outputArtifacts/", "${entity.name}AttributeGeneratedTestCases.txt")
        }

        return entitiesScript
    }

}
