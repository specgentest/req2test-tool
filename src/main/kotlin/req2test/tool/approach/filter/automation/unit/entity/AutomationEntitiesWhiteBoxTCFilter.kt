package req2test.tool.approach.filter.automation.unit.entity

import req2test.tool.approach.core.Filter
import req2test.tool.approach.core.Log
import req2test.tool.approach.core.OutputFolder
import req2test.tool.approach.filter.testplan.RequestController
import req2test.tool.approach.processor.adapter.AIAdapter
import req2test.tool.approach.processor.generator.file.TestPlanGenerator
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.Script
import req2test.tool.approach.entity.Technique
import req2test.tool.approach.entity.TestCase

class AutomationEntitiesWhiteBoxTCFilter (data: MutableMap<String, Any?>, processor: Any) : Filter(data, processor) {
    private val AIAdapter = processor as AIAdapter
    private var entities: List<Entity> = ArrayList()
    override fun filterKeyInput(): String {
        return "ListScriptTestCasesEntity"
    }

    override fun filterName(): String {
        return "AutomationEntitiesAttributesGeneratedTCFilter"
    }

    override fun filterKeyOutput(): String {
        return "AutomationEntitiesAttributesGeneratedTC"
    }

    private fun createEntitiesDeclarations(entities: List<Entity>): String {
        var declaration = ""
        entities.forEach { entity ->
            declaration += "\n${entity.name}\n"
            entity.attributes.forEach {
                if(!it.generatedDB) {
                    declaration += "- ${it.name}: ${it.type.replace("%", "")}\n"
                }
            }
        }
        return declaration
    }

    val textUnityTC: (List<TestCase>) -> String = { testCases ->
        var textTestCases = ""
        var ctCount = 1
        testCases.forEach {tc ->
            var tcDescription = tc.expectedResult

            var testName = tcDescription
            if(tc.testName != null)
                testName = tc.testName!!

            textTestCases += "TC-${ctCount++}\t\t\t\t\t\t| ${testName}\n"
            textTestCases += "preconditions\t\t\t\t| ${tc.preconditions}\n"
            textTestCases += "Input of the Endpoint\t\t\t\t\t\t| ${tc.dataTest.input?.map { "\n\t\t\t\t\t\t\t${it.key}: ${it.value} " }}\n"
            textTestCases += "Steps\t\t\t\t\t\t| ${tc.steps.map { "\n\t\t\t\t\t\t\t$it" }}\n"
            textTestCases += "Expected Result\t\t\t\t| ${tc.expectedResult}\n\n"
        }
        textTestCases
    }

    private fun generateAutomatedWhiteBoxEntityTestClass(testCases: List<TestCase>, script: Script, entities: List<Entity>): String {
        val entity = script.reference as Entity

        val testClassName = "${entity.name}MockTest"
        var prompt = "Create the class $testClassName with JUnit5 and Mockito to spy the validate method.\n"
        prompt += "Do not include generated attributes (e.g., id) in constructors.\n"
//        "For all attributes of ${entity.name} that are instances of other classes (i.e., non-primitive, non-String types), use mocks to create them.\n"
        prompt += "The name of the thrown exception must be ${entity.name}Exception.\n"

        val textTestCases = textUnityTC(testCases)

        prompt += "Consider the existing classes (Do not implement them):\n"
        val entitiesDeclarations = createEntitiesDeclarations(entities)

        val classes = entities.map { it.name }
        prompt += entitiesDeclarations
        prompt += "\nThe constructor of each class must accept all the attributes listed above as parameters.\n"
        prompt += "\nConsider type Date as LocalDate. Consider type Time as LocalTime.\n"
        prompt += "\nEnsure all model classes are imported from ${OutputFolder.packageName}.model.\n"
        prompt += "\nEnsure all exception classes are imported from ${OutputFolder.packageName}.exception.\n"

        prompt += """Structure each test case using the Arrange-Act-Assert pattern: 
                        first set up the necessary objects and data (Arrange), 
                        then execute the operation being tested (Act), 
                        and finally verify the result (Assert)."""

        prompt += "\nCreate only the $testClassName class as output from this prompt. \n\nThe classes $classes already exist. Do not create them.\n\n"

        """Below are the test cases for the class $testClassName:
                
            """.trimIndent()
        prompt+= textTestCases

        prompt = prompt.replace("\n", "\\n")

        val aiRequest: () -> String = {
            AIAdapter.getAnswerFromQuestion(prompt)
        }

        var result = RequestController.controlledRequest(aiRequest)
        return extractJava(result)
    }

    fun extractJava(text: String): String {
        // Regex para capturar código entre ```java e ```
        val regex = Regex("```java(.*?)```", RegexOption.DOT_MATCHES_ALL)

        // Procura o match e captura o grupo 1 (o código Java)
        val matchResult = regex.find(text)

        // Se encontrado, retorna o código capturado, senão retorna o texto original
        return matchResult?.groups?.get(1)?.value?.trim() ?: text.trim()
    }


    fun getClassName(javaClassText: String): String? {
        val regex = Regex("""\bclass\s+(\w+)""")
        return regex.find(javaClassText)?.groupValues?.get(1)
    }

    override fun compute(input: Any): Any? {
        val prompts = ArrayList<String>()
        val scripts = input as List<Script>
        try {
            entities = data["ListEntities"] as List<Entity>
        } catch (ex: Exception) {
            logs.add(Log.createCriticalError("AutomationEntityTestCasesFilter", "List Entities not found"))

        }
        scripts.forEach {script ->

            val whiteboxTCs = script.testCases.filter { it.technique == Technique.WHITEBOX }
                val classFile = generateAutomatedWhiteBoxEntityTestClass(whiteboxTCs, script, entities)
                val className = getClassName(classFile)
                script.automatedTestCases.add(classFile)
                TestPlanGenerator.createFile(classFile, OutputFolder.basePath + "output/unit/entity/", "${className}.java")

        }

        throwsCriticalErrors()

        return scripts
    }
}