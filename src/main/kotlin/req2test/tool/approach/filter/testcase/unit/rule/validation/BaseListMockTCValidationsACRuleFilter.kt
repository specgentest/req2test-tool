package req2test.tool.approach.filter.testcase.unit.rule.validation

import req2test.tool.approach.core.Filter
import req2test.tool.approach.core.Log
import req2test.tool.approach.core.TestArchitecture
import req2test.tool.approach.processor.generator.file.TestPlanGenerator
import req2test.tool.approach.entity.*

abstract class BaseListMockTCValidationsACRuleFilter (data: MutableMap<String, Any?>): Filter(data, null){

    lateinit var testArchitecture: TestArchitecture
    override fun filterKeyInput(): String { return "ListCreateACRuleMockScript" }
    override fun filterName(): String {

        return "ListMockTestCaseCreateACRuleFilter"
    }
    override fun filterKeyOutput(): String { return "ListMockTestCaseCreateACRule" }

    override fun isMandatoryFilter(): Boolean {
        return false
    }

    fun getUniqueAttributes(entity: Entity): List<String> {
        return entity.attributes.filter { it.dbValidations.contains("unique") }.map { it.name }
    }

    abstract fun createTCs(ruleValidationDataTest: ListDataTest, script: Script, entity: Entity): List<TestCase>

    override fun compute(input: Any): Any? {

        var scripts = input as List<Script>
        scripts = scripts.filter { it.reference is Rule }

        scripts.forEach { script ->

            testArchitecture = script.testArchitecture!!

            val mapListRuleValidationDataTest = data["MapListRuleValidationDataTest"] as Map<Rule, List<ListDataTest>>

            val ruleReference = script.reference as Rule

            val listEntities = data["ListEntities"] as List<Entity>
            val entity = listEntities.find { it.name == ruleReference.reference } as Entity


            //Data from RuleValidationData
            val listRuleValidationDataTest =  mapListRuleValidationDataTest[ruleReference]

            if (listRuleValidationDataTest == null)
                logs.add(
                    Log.createCriticalError(
                        filterName(),
                        "ListRuleValidationDataTest not found for ${ruleReference.raw}"
                    )
                )

            else {
                listRuleValidationDataTest.forEach {
                    ruleValidationDataTest ->
                        //Filtrando e removendo dado de "invalid id"
                        val ruleValidationDataTestFiltered = ruleValidationDataTest.dataTest.filter { it.errorMessage != "invalid id" }
                        val listRuleValidationDataTestFiltered = ListDataTest(dataTest = ruleValidationDataTestFiltered, reference = ruleValidationDataTest.reference)
                        listRuleValidationDataTestFiltered.llmMessageHistory = ruleValidationDataTest.llmMessageHistory
                        listRuleValidationDataTestFiltered.varDictionaryAttributes = ruleValidationDataTest.varDictionaryAttributes
                        listRuleValidationDataTestFiltered.entitiesAllVariables = ruleValidationDataTest.entitiesAllVariables

                    //script.testCases.addAll(createTCs(ruleValidationDataTest, script, entity))
                    script.testCases.addAll(createTCs(listRuleValidationDataTestFiltered, script, entity))
                }
            }
        }

            //Exportando dados para .txt
            scripts.forEach { ruleValidationScript ->
                var textTestCases = ""
                var ctCount = 1
                ruleValidationScript.testCases.forEach {tc ->
                    var tcDescription = tc.expectedResult

                    textTestCases += "TC-${ctCount++}\t\t\t\t\t\t| ${tcDescription}\n"
                    textTestCases += "preconditions\t\t\t\t| ${tc.preconditions}\n"
                    textTestCases += "Input\t\t\t\t\t\t| ${tc.dataTest.input?.map { "\n\t\t\t\t\t\t\t${it.key}: ${it.value} " }}\n"
                    textTestCases += "Steps\t\t\t\t\t\t| ${tc.steps.map { "\n\t\t\t\t\t\t\t$it" }}\n"
                    textTestCases += "Expected Result\t\t\t\t| ${tc.expectedResult}\n\n"
                }

                val rule = ruleValidationScript.reference as Rule
                if(ruleValidationScript.testCases.isNotEmpty())
                    TestPlanGenerator.createFile(textTestCases , "outputArtifacts/", "${ruleValidationScript.testArchitecture?.ruleAction}${rule.reference}ValidationsTestCases.txt")
            }
        return scripts
    }
}
