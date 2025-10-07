package req2test.tool.approach.filter.automation.unit.entity

import req2test.tool.approach.core.Filter
import req2test.tool.approach.core.Log
import req2test.tool.approach.core.OutputFolder
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.Script
import req2test.tool.approach.filter.testplan.RequestController
import req2test.tool.approach.processor.adapter.AIAdapter
import req2test.tool.approach.processor.generator.file.TestPlanGenerator

class AutomationEntitiesTestCasesFilter (data: MutableMap<String, Any?>, processor: Any) : Filter(data, processor) {
    private val AIAdapter = processor as AIAdapter
    private var entities: List<Entity> = ArrayList()
    override fun filterKeyInput(): String {
        return "ListScriptTestCasesEntity"
    }

    override fun filterName(): String {
        return "AutomationEntityTestCasesFilter"
    }

    override fun filterKeyOutput(): String {
        return "AutomatedEntitiesTestCases"
    }

    private fun createEntitiesDeclarations(entities: List<Entity>): String {
        var declaration = ""
        entities.forEach { entity ->
            declaration += "\n${entity.name}\n"
            entity.attributes.forEach {
                if(!it.generatedDB and !it.generatedConstructor) {
                    declaration += "- ${it.name}: ${it.type.replace("%", "")}\n"
                }
            }
        }
        return declaration
    }
    fun generateAutomatedEntityTestClass(script: Script, entities: List<Entity>): String {
        val entity = script.reference as Entity
        var prompt = "Given the testing data below for ${entity.name}, create 1 unit test for each input. Use Junit5. " +
                "Tests must consider inputs to the constructor. If the input is not valid, an exception must be thrown with the error message. " +
                "The name of the thrown exception must be ${entity.name}Exception. \n\nConsider the types:"
        val entitiesDeclarations = createEntitiesDeclarations(entities)

        prompt += entitiesDeclarations
        prompt += "\nConsider type Date as LocalDate. Consider type Time as LocalTime.\n"

        prompt += "\nEnsure all model classes are imported from ${OutputFolder.packageName}.model.\n"
        prompt += "\nEnsure all exception classes are imported from ${OutputFolder.packageName}.exception.\n"
        prompt += "\nAdd all necessary imports (e.g. Asserts and verify from JUnit5, LocalDate).\n"


        prompt +="""Structure each test case using the Arrange-Act-Assert pattern: 
                        first set up the necessary objects and data (Arrange), 
                        then execute the operation being tested (Act), 
                        and finally verify the result (Assert).
                        """
        prompt += "\nCreate only the ${entity.name}Test class as output from this prompt. Do not generate ${entity.name} class and ${entity.name}Exception class.\n\n"

        prompt += script.allDataTest

//        return prompt

        prompt = prompt.replace("\n", "\\n")

        //var result = AIAdapter.getAnswerFromQuestion(prompt)
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
            val classFile = generateAutomatedEntityTestClass(script, entities)
            val className = getClassName(classFile)
            script.automatedTestCases.add(classFile)
            TestPlanGenerator.createFile(classFile, OutputFolder.basePath + "output/unit/entity/", "${className}.java")
        }

        throwsCriticalErrors()

        return scripts
    }
}