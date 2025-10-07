package req2test.tool.approach.filter.testcase.unit.entity

import req2test.tool.approach.core.Filter
import req2test.tool.approach.core.Log
import req2test.tool.approach.core.TestArchitecture
import req2test.tool.approach.processor.generator.file.TestPlanGenerator
import req2test.tool.approach.entity.*

class ListUnitTestCaseEntityUpdateFilter (data: MutableMap<String, Any?>): Filter(data, null){
    override fun filterKeyInput(): String { return "ListRuleDataTest" }

    lateinit var testArchitecture: TestArchitecture

    override fun filterName(): String {

        return "ListUnitTestCaseEntityUpdateFilter"
    }
    override fun filterKeyOutput(): String { return "ListScriptTestCasesEntity" }


    private fun createTestCaseValidateUpdate(positiveDataOne: DataTest?, positiveDataTwo: DataTest?, script: Script): TestCase {
        val entity = script.reference as Entity

        val notGeneratedAttributes = entity.attributes.filter { !it.generatedDB and !it.generatedConstructor }

        val inputOne = HashMap<String, Any?>()
        val inputTwo = HashMap<String, Any?>()

        notGeneratedAttributes.forEach {
            inputOne[it.name] = positiveDataOne?.input?.get(it.name)
            inputTwo[it.name] = positiveDataTwo?.input?.get(it.name)
        }

        val input = DataTest(inputTwo, true, "", "")
        val output = inputTwo

        val expectedResult = "validate() success when all fields of ${entity.name} are updated correctly, resulting in $output"

        val positiveTestCase = TestCase(input, script, expectedResult)
        positiveTestCase.preconditions.add("Initially It must have to create an instance of ${entity.name} as following: $inputOne")
        positiveTestCase.preconditions.add("Use the constructor ${testArchitecture.entityConstructor}")

        val steps = ArrayList<String>()
        steps.add("Inform ${input.input}")

        steps.add("Use the setter method of each attribute to update the existing instance of ${entity.name}")
        steps.add("Call method validate() in ${entity.name} class")
        steps.add("Verify that ${testArchitecture.ruleError} was not called")
        steps.add("Verify all attributes updated")
        positiveTestCase.steps.addAll(steps)
        return positiveTestCase
    }

    private fun createTestCaseInvalidUpdate(positiveDataOne: DataTest?, negativeData: DataTest, script: Script): TestCase {
        val entity = script.reference as Entity

        val notGeneratedAttributes = entity.attributes.filter { !it.generatedDB and !it.generatedConstructor }

        val inputOne = HashMap<String, Any?>()

        notGeneratedAttributes.forEach {
            inputOne[it.name] = positiveDataOne?.input?.get(it.name)
        }

        val expectedResult = "validate() fails and ${testArchitecture.ruleError} is ${testArchitecture.ruleErrorAction}"

        val negativeTestCase = TestCase(negativeData, script, expectedResult)
        negativeTestCase.preconditions.add("Initially It must have to create an instance of ${entity.name} as following: $inputOne")
        negativeTestCase.preconditions.add("Use the constructor ${testArchitecture.entityConstructor}")

        val steps = ArrayList<String>()
        steps.add("Inform ${negativeData.input}")

        steps.add("Use the setter method of each attribute to update the existing instance of ${entity.name}")
        steps.add("Call method validate() in ${entity.name} class")
        steps.add("Verify that ${testArchitecture.ruleError} is ${testArchitecture.ruleErrorAction} with the following message: ${negativeData.errorMessage}")
        negativeTestCase.steps.addAll(steps)
        return negativeTestCase
    }

    override fun compute(input: Any): Any? {
        val dataTest = input as List<ListDataTest>

        val listEntitiesDataTest = data["ListEntitiesDataTest"] as List<ListDataTest>

        var entitiesScript = data["ListUnitEntityUpdateScript"]
        if(entitiesScript == null)
            logs.add(Log.createCriticalError("ListUnitTestCaseEntityUpdateFilter", "ListUnitEntityUpdateScript not found"))

        entitiesScript = entitiesScript as List<Script>

        entitiesScript.forEach {entityScript ->

            testArchitecture = entityScript.testArchitecture!!

            val positiveDataTest = dataTest.find { (it.reference as ListDataTest).reference == entityScript.reference }


            val positiveDataOne = positiveDataTest?.dataTest?.get(0)
            val positiveDataTwow = positiveDataTest?.dataTest?.get(1)

            val entityDataTest = listEntitiesDataTest.find { it.reference == entityScript.reference }

            val negativeDataTest = entityDataTest?.dataTest?.find { !it.isValidInput }

            createTestCaseValidateUpdate(positiveDataOne, positiveDataTwow, entityScript).let { entityScript.testCases.add(it) }
            if (negativeDataTest != null) {
                createTestCaseInvalidUpdate(positiveDataOne, negativeDataTest, entityScript).let { entityScript.testCases.add(it) }
            }

        }

        throwsCriticalErrors()

        //Exportando dados para .txt
        entitiesScript.forEach { entityScript ->

            val ent = entityScript.reference as Entity

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
            TestPlanGenerator.createFile(textTestCases , "outputArtifacts/", "${entity.name}UpdateTestCases.txt")
        }
        return entitiesScript
    }

}
