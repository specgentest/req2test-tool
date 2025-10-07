package req2test.tool.approach.filter.testcase.integration.entity

import req2test.tool.approach.core.Filter
import req2test.tool.approach.core.Log
import req2test.tool.approach.processor.generator.file.TestPlanGenerator
import req2test.tool.approach.entity.*

class ListIntegrationTestCaseEntityFilter (data: MutableMap<String, Any?>): Filter(data, null){
    override fun filterKeyInput(): String { return "ListEntitiesDataTest" }
    override fun filterName(): String {

        return "ListIntegrationTestCaseEntityFilter"
    }
    override fun filterKeyOutput(): String { return "ListEntityScriptIntegration" }

    private fun getInitialDB(entity: Entity, validDataTest: DataTest?): MutableMap<String, MutableList<Map<*, *>>> {
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

    override fun compute(input: Any): Any? {
        val listEntitiesDataTest = input as List<ListDataTest>
        var entitiesScript = data["ListIntegrationEntityScript"]
        if(entitiesScript == null)
            logs.add(Log.createCriticalError("ListIntegrationTestCaseEntityFilter", "ListIntegrationEntityScript not found"))

        entitiesScript = entitiesScript as List<Script>

        entitiesScript.forEach {entityScript ->
            val entityDataTest = listEntitiesDataTest.find { it.reference == entityScript.reference }
            if(entityDataTest == null)
                logs.add(Log.createCriticalError("ListUnitTestCaseEntityFilter", "EntityDataTest not found for ${entityScript.reference}"))
            else {
                entityScript.allDataTest = entityDataTest.convertToJson()
                val entity = entityScript.reference as Entity

                val validDataTest = entityDataTest.dataTest.find { it.isValidInput }

                val dbInitMap = getInitialDB(entity, validDataTest)


                //Generated attribute
                val attributes = entity.attributes.filter { it.generatedDB }
                attributes.forEach { attribute ->

                    if(validDataTest != null) {
                        val expectedResult = attribute.generatedFormula as String
                        val testCase = TestCase(validDataTest, entityScript, expectedResult, attribute)
                        testCase.preconditions.add("Initialize the database with the following data: $dbInitMap")
                        testCase.preconditions.add("Use constructor with all attributes as parameters to mock ${entity.name}")

                        entityScript.testCasesAttributeGenerated.add(testCase)
                    }
                }

            }
        }

        //Exportando dados para .txt
        entitiesScript.forEach { entityScript ->

            val ent = entityScript.reference as Entity

            var textTestCases = ""
            var ctCount = 0

            var textTestCasesAttributeGenerated = ""
            entityScript.testCasesAttributeGenerated.forEach {tc ->
                var tcDescription = ""
                if(tc.dataTest.isValidInput)
                    tcDescription = "Must create an instance of ${ent.name}"
                else
                    tcDescription = tc.dataTest.className

                textTestCasesAttributeGenerated += "TC-${ctCount++}\t\t\t\t\t\t| ${tcDescription}\n"
                textTestCasesAttributeGenerated += "preconditions\t\t\t\t| ${tc.preconditions}\n"
                textTestCasesAttributeGenerated += "Input\t\t\t\t\t\t| ${tc.dataTest.input?.map { "\n\t\t\t\t\t\t\t${it.key}: ${it.value} " }}\n"
                textTestCasesAttributeGenerated += "Expected Result\t\t\t\t| ${tc.expectedResult}\n\n"
            }

            val entity = entityScript.reference as Entity
            TestPlanGenerator.createFile(textTestCasesAttributeGenerated , "outputArtifacts/", "${entity.name}IntegrationAttributeGeneratedTestCases.txt")
        }

        throwsCriticalErrors()
        return entitiesScript
    }

}