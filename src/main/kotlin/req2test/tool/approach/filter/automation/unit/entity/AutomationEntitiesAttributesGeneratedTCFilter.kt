package req2test.tool.approach.filter.automation.unit.entity

import req2test.tool.approach.core.Filter
import req2test.tool.approach.core.Log
import req2test.tool.approach.core.OutputFolder
import req2test.tool.approach.filter.testplan.RequestController
import req2test.tool.approach.processor.adapter.AIAdapter
import req2test.tool.approach.processor.generator.file.TestPlanGenerator
import req2test.tool.approach.entity.Attribute
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.Script

class AutomationEntitiesAttributesGeneratedTCFilter (data: MutableMap<String, Any?>, processor: Any) : Filter(data, processor) {
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

    private fun generateAutomatedEntityTestClass(script: Script, entities: List<Entity>): String {
        val entity = script.reference as Entity

        var prompt = "Create the class ${entity.name}GeneratedAttributeTest with JUnit5 to validate:\n"
        prompt += "Do not include generated attributes (e.g., id) in constructors.\n"
//        "For all attributes of ${entity.name} that are instances of other classes (i.e., non-primitive, non-String types), use mocks to create them.\n"

        script.testCasesAttributeGenerated.forEach {tc ->
            val attribute = tc.expectedResultReference as Attribute
            prompt += "- ${attribute.name} as ${attribute.type} and with the following validation: ${attribute.generatedFormula}\n\n"
//            prompt += "\nGiven a valid ${entity.name} below:\n"
            prompt += "Use the following data as constructor parameters.\n"
            prompt += "${entity.name} = ${tc.dataTest.input}\n"
//            prompt += "Create a unit test with JUnit5 to validate:\n"
            prompt += "The test must create a ${entity.name} and validate if ${attribute.name} was generated correctly.\n"
        }

        prompt += "Consider the existing classes (Do not implement them):\n"
        val entitiesDeclarations = createEntitiesDeclarations(entities)

        val classes = entities.map { it.name }
        prompt += entitiesDeclarations
        prompt += "\nThe constructor of each class must accept all the attributes listed above as parameters.\n"
        prompt += "\nConsider type Date as LocalDate. Consider type Time as LocalTime.\n"
        prompt += "\nEnsure all model classes are imported from ${OutputFolder.packageName}.model.\n"
        prompt += "\nEnsure all exception classes are imported from ${OutputFolder.packageName}.exception.\n"
        prompt += "\nAdd all necessary imports (e.g. Asserts and verify from JUnit5, LocalDate).\n"

        prompt += """Structure each test case using the Arrange-Act-Assert pattern: 
                        first set up the necessary objects and data (Arrange), 
                        then execute the operation being tested (Act), 
                        and finally verify the result (Assert).
                        """

        prompt += "\nCreate only the ${entity.name}GeneratedAttributeTest class as output from this prompt. \n\nThe classes $classes already exist. Do not create them.\n\n"

//        return prompt

        prompt = prompt.replace("\n", "\\n")

        val aiRequest: () -> String = {
            AIAdapter.getAnswerFromQuestion(prompt)
        }

        var result = RequestController.controlledRequest(aiRequest)

//        var result = AIAdapter.getAnswerFromQuestion(prompt)

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

            println("SCRIPT: " + script.testCasesAttributeGenerated)
            if(script.testCasesAttributeGenerated.size > 0) {
                val classFile = generateAutomatedEntityTestClass(script, entities)
                val className = getClassName(classFile)
                script.automatedTestCases.add(classFile)
                TestPlanGenerator.createFile(classFile, OutputFolder.basePath + "output/unit/entity/", "${className}.java")
            }
        }

        throwsCriticalErrors()

        return scripts
    }
}