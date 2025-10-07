package req2test.tool.approach.filter.testcase.unit.rule

import req2test.tool.approach.core.Filter
import req2test.tool.approach.core.Log
import req2test.tool.approach.core.TestArchitecture
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.ListDataTest
import req2test.tool.approach.entity.Script
import req2test.tool.approach.entity.TestCase
import req2test.tool.approach.processor.generator.file.TestPlanGenerator

abstract class BaseListMockTCACRuleFilter (data: MutableMap<String, Any?>): Filter(data, null){

    lateinit var testArchitecture: TestArchitecture

    fun getUniqueAttributes(entity: Entity): List<String> {
        return entity.attributes.filter { it.dbValidations.contains("unique") }.map { it.name }
    }

    fun formatParameters(parameters: List<String>): String {
        return parameters.joinToString(prefix = "(", separator = ", ", postfix = ")")
    }

    override fun isMandatoryFilter(): Boolean {
        return false
    }

    abstract fun createTestCases(entityDataTest: ListDataTest, ruleDataTest: ListDataTest, script: Script): List<TestCase>

    override fun compute(input: Any): Any? {

        var scripts = input as List<Script>

        scripts = scripts.filter { it.reference is Entity }

        scripts.forEach { script ->

            testArchitecture = script.testArchitecture!!
            val entity = script.reference as Entity
            baseTestName = "test"

            val listEntitiesDataTest = data["ListEntitiesDataTest"] as List<ListDataTest>

            val listRuleDataTest = data["ListRuleDataTest"] as List<ListDataTest>
            val ruleDataTest = listRuleDataTest.find { (it.reference as ListDataTest).reference == script.reference }
            val entityDataTest = listEntitiesDataTest.find { (it.reference  == script.reference) }

            if (ruleDataTest == null)
                logs.add(
                    Log.createCriticalError(
                        filterName(),
                        "RuleDataTest not found for ${script.reference}"
                    )
                )
            else if(entityDataTest == null)
                logs.add(
                    Log.createCriticalError(
                        filterName(),
                        "EntityDataTest not found for ${script.reference}"
                    )
                )
            else {
                script.testCases.addAll(createTestCases(entityDataTest, ruleDataTest, script))
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
                TestPlanGenerator.createFile(textTestCases , "outputArtifacts/", "${testArchitecture.ruleAction}${entity.name}TestCases.txt")
            }
        return scripts
    }

}
